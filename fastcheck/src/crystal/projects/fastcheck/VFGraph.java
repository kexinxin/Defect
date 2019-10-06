
package crystal.projects.fastcheck;

import java.io.*;
import java.util.*;

import crystal.analysis.callgraph.CallGraph;
import crystal.analysis.pointer.*;
import crystal.ast.ArithmOps;
import crystal.cfg.*;
import crystal.core.*;
import crystal.general.*;
import crystal.util.*;


/** The value-flow graph (VFG) */
public class VFGraph {
    
    static boolean mergeEachRegion = true;
    
    /** a warning counter */
    static int readExprIgnored = 0;
    
    /** nodes in the graph (expr at program points) */
    ThreeLevelMap<CFGNode, CoreExpr, Boolean, VFGNode> nodes =
        new ThreeLevelMap<CFGNode, CoreExpr, Boolean, VFGNode>();

    /** nodes for globals and pointer expressions */
    TwoLevelMap<CoreExpr, CFGNode, VFGNode> regions = 
        new TwoLevelMap<CoreExpr, CFGNode, VFGNode>();
    
    /** creates an empty dependence graph */
    public VFGraph() { }
    
    /** gets or creates node for e@n, left true if expression is in the lhs,
     * function f is where n is located, argNum is the position of e in n when
     * n is a call (this is used to recognize if e@n is a sink or not.
     * 
     * TODO: make nodes cache sensitive to argNum, if you have a call
     * s(x,x), where only arg0 is a sink, then there should be 2 nodes
     * x@s:sink x@s:nosink. */
     VFGNode get(CFGNode n, CoreExpr e, boolean left, Function f, int argNum) {
        assert e != null;
        if (e instanceof CoreUnaryExpr) {
            CoreUnaryExpr u = (CoreUnaryExpr) e;
            if (u.isCast())
                e = u.getExpr();
        }
        VFGNode d = nodes.get(n, e, left);
        if (d == null) {
            if (isSink(n, argNum))
                d = new SinkNode(n, f, 0);
            else if (e == n.getWrittenMem() && 
                    (isSource(n) || n.isAssign() || left)) {
                d = new AssignNode(n, f);
            } else if (n.isEntry())
                d = new ParamAtEntry(e, f);
            else if (n.isReturn())
                d = new ReturnNode(n, f);
            else
                d = new ParamAtCall(n, e, f);
            nodes.put(n, e, left, d);
        }
        return d;
    }

     static boolean isSource(CFGNode n) {
        if (n instanceof CFGCallNode) {
            Function f = ((CFGCallNode) n).getCall().getFunSymbol();
            return isSource(f);
        }
        return false;
    }

    static boolean isSource(Function f) {
    	/* NULL TEST */
    	if (f == null) 
    		return false;
    	
        String fn = f.getName();
        for (String s : Options.sourceNames)
            if (fn.equals(s))
                return true;
        return false;
    }
     
     static boolean isSink(CFGNode n, int argNum) {
         if (n instanceof CFGCallNode) {
        	 /* NULL TEST */
        	 Function f = ((CFGCallNode) n).getCall().getFunSymbol();
        	 if (f != null) {
        		 String fn = f.getName();
        		 for (Pair<String,Integer> p : Options.sinkNames)
        			 if (fn.equals(p.fst) && argNum == p.snd)
        				 return true;
        	 }
         }
         return false;
     }
    
    /** gets (or creates) node for aliases of e */
    VFGNode getRegion(CoreDerefExpr e, CFGNode where, Function f) {
        Region r = Region.regionOf(e);
        CoreExpr reg = r.getInternalExpression().getRep();
        // CoreExpr reg = e.getExpr().getRep();
        assert where != null;
        return internalGetRegion(f, where, reg);
    }

    private VFGNode internalGetRegion(Function f, CFGNode where, CoreExpr reg) {
        CFGNode originalWhere = where;
        if (mergeEachRegion)
            where = null;
        
        VFGNode d = regions.get(reg, where);
        if (d == null) {
            d = new RegionNode(reg);
            regions.put(reg, where, d);
            if (where != null) {
                /* one region with no CFGNode that is connected to all others */
                VFGNode regionMain = internalGetRegion(null, null, reg);
                regionMain.addSucc(d, BoolExpr.UNK);
                d.addSucc(regionMain, BoolExpr.UNK);
            } 

        }
        if (f != null)
            ((RegionNode)d).addStmt(f,originalWhere);
        return d;
    }
    
    static Map<Function,CoreExpr> constants = new HashMap<Function, CoreExpr>();
    
    /** Adds nodes and edges to this graph based on result of reaching definitions 
     * analysis. No guards are computed at this point. */
    void addFunDependences(Function fun, Map<CFGNode, IBitVector<CFGNode>> res) {
        for (CFGNode n : fun.getCFGNodes()) {
            /** target defined expression */
            CoreDerefExpr cde = n.getWrittenMem();
            VFGNode node = null;

            /* defines a global / heap value */
            if (cde != null && isRelevant(cde)) {
                if (cde.isPureLocalVar())
                    node = get(n, cde, true, fun, -1);
                else
                    node = getRegion(cde, n, fun);
            }

            /** Simple assignments */
            if (node != null && n.isAssign()) {
                CoreExpr ei = ((CFGAssignNode) n).getRight();
                connectWithDef(fun, res.get(n), node, ei, n);
            }
            
            if (node == null && n.isReturn()) {
                CoreExpr ei = n.getCoreKid(0);
                if (ei != null)
                    connectWithDef(fun, res.get(n), get(n, ei, false, fun, 0), ei, n);
            }

            /** method parameters */
            if (n.isCall()) {
                CFGCallNode cn = (CFGCallNode) n;

                /** actual definitions */
                for (int i = 0; i < cn.numArgs(); i++) {
                    CoreDerefExpr a = extractCoreDerefExpr(cn.getArg(i));
                    if (isRelevant(a)) {
                        VFGNode arg = get(n, a, false, fun, i);
                        connectWithDef(fun, res.get(n), arg, a, n);
                    }
                }

                /** actual to formals */
                for (Function f : CallGraph.getCallees(cn))  
                    if (f.getCFG() != null) {
                        for (int i = 0; i < cn.numArgs(); i++) {
                            if (isSink(n, i))
                                continue;
                            CoreDerefExpr a = extractCoreDerefExpr(cn.getArg(i));
                            if (isRelevant(a)) {
                                VFGNode arg = get(n, a, false, fun, i);
                                Symbol parvar = null;
                                if (i < f.numParams())
                                    parvar = f.getParam(i);
                                else
                                    parvar = Function.varargs;
                                VFGNode par = get(f.getCFG().entryNode, 
                                    CoreExpr.getVar(parvar), true, f, i); 
                                arg.addSucc(par);
                            }
                        }

                        /** return variable to lhs */
                        if (node != null) {
                            CFGNode ex = f.getCFG().exitNode;
                            for (int i = 0; i < ex.numPreds(); i++) {
                                CFGNode rets = ex.getPred(i); 
                                CoreExpr rete = rets.getCoreKid(0);
                                get(rets, rete, false, f, 0).addSucc(node);
                            }
                        }
                    }
            }

        }

    }
        
        

    /** adds node as a successor of the definitions of ei */
    private void connectWithDef(Function fun, IBitVector<CFGNode> defs, 
            VFGNode node, CoreExpr ei, CFGNode where) {
        CoreDerefExpr e = extractCoreDerefExpr(ei);
        if (e != null) {
            if (e.isPureLocalVar()) {
                /* reading a local expression */
                for (CFGNode rd : defs) {
                    CoreDerefExpr d = rd.getWrittenMem();
                    if (defines(rd, e, fun)) {
                        CoreDerefExpr def = (d == null) ? e : d;
                        VFGNode src = get(rd, def, true, fun, -1);
                        assert src != null;
                        BoolExpr b;
                        if (node instanceof RegionNode)
                            b = BoolExpr.UNK;
                        else 
                            b = BoolExpr.TMP;
                        src.addSucc(node, b);
                    }
                }
            } else {
                /* reading global or from region */
                getRegion(e, where, fun).addSucc(node, BoolExpr.UNK);
            }
        }
    }

    /** If ei is either (* e) or ((cast-type) *e),  or 
     * (* (e + i)) returns (*e).
     * returns null otherwise. */
    public static CoreDerefExpr extractCoreDerefExpr(CoreExpr ei) {
        CoreDerefExpr e = null;
        if (ei.isDeref())
            e = (CoreDerefExpr) ei;
        else if (ei instanceof CoreUnaryExpr) {
            /* casts can also be a dereference expression */
            CoreUnaryExpr eiu = (CoreUnaryExpr)ei;
            if (eiu.isCast())
                e = extractCoreDerefExpr(eiu.getExpr());
        } else if (ei instanceof CoreBinaryExpr) {
            int op = ((CoreBinaryExpr)ei).getOp();
            
            if (Options.modelPointerArithmetic && (
                    op == ArithmOps.PLUS || op == ArithmOps.MINUS )) {
                CoreDerefExpr e1 = extractCoreDerefExpr(ei.getKid(0));
                if (e1 != null)
                    e = e1;
                else
                    e = extractCoreDerefExpr(ei.getKid(1));
            }
        }
        return e;
    }
    
    /** Expression e is relevant if it can have pointer values */
    private static boolean isRelevant(CoreExpr e) {
        return (e != null && !e.isAddrConstant() && 
                !e.isNumConstant() &&
                !e.isStringConstant() && 
                !e.isAlloc() &&
                (e.getType().isPointer() ||
                e.getRep().getInfo().hasPointers())
        ); 
    }
    
    /** is e defined by n? (does e depends on the value defined by n?) */
    private boolean defines(CFGNode n, CoreExpr e, Function fun) {
        CoreDerefExpr def = n.getWrittenMem();
        if (def == null) {
            /* definition of a parameter */
            assert n.isEntry() || n.isReturn();
            if (n.isEntry())
                for (int i = 0 ; i < fun.numParams(); i++)
                    if (CoreExpr.getVar(fun.getParam(i)) == e)
                        return true;
            return false;
        }
        return e == def;
    }
    
    /** prints all graph (no slices w.r.t alloc sites) to a single dot file */
    public void printFullDot() {
        /* printing */
        try {
            PrintStream ps = 
                new PrintStream(new FileOutputStream("graphs/depgraph.dot"));
             ps.println("digraph alltogether { ");
             ps.println("  node[fontsize=12];");
             ps.println("  graph[center = true];");

             for (VFGNode n : nodes.threeFinalValues())  
                 PrintDot.printDot(ps, null, n, null);
             
             for (VFGNode n : regions.finalValues())
                 PrintDot.printDot(ps, null, n, null);
             
             ps.println("}; ");
             ps.close();
             
        } catch(IOException iex) {
            System.out.println(" problem writting dot file");
        }
    }
    
    void clearFunction(Function fun) {
        for(CFGNode c : fun.getCFGNodes())
            nodes.remove(c);
    }
    

}
