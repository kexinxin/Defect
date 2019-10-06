
package crystal.projects.fastcheck;

import java.io.*;
import java.util.*;

import crystal.ast.*;
import crystal.cfg.*;
import crystal.core.*;
import crystal.projects.fastcheck.VFGNode.VFGNodeVisitor;


/** Generates a dot file containing the guarded VFG for a slice and 
 *  the path conditions for memory leaks or double-frees (if any) */ 
public class PrintDot {

    /** alltests is initially empty and filled with all test conditions
     * seen in the code */
    static void printDot(Slice s, 
    		List<BoolExpr> leakpath, 
    		List<List<BoolExpr>> dfrees,
            Set<Atom> alltests) 
    {
    	PrintStream ps = null;
    	String fname = Main.OUTDIR + "/" + s.kind + "/site" + s.name() + ".dot";
    	
        try {
            ps = new PrintStream(new FileOutputStream(fname));
        }
        catch(IOException iex) {
            System.out.println("Could not write to file: " + fname);
            System.exit(1);
        }
        
        /* print dot header */
        ps.println("digraph case" + s.name() + " { ");
        /* NOTE: removed page size to generate normal size PNG pictures */ 
        ps.println("    rankdir=TD;");
        ps.println("    node[fontsize=12,font=courier,height=0.2];");
        ps.println("    edge[fontsize=10,font=courier];");
        ps.println("    graph[center = true,color=white,fontsize=10,"+
            "font=courier,height=0.2,fontcolor=gray];");
        
        
        /* print graph */
        for (VFGNode tp :
            (Options.printallVFG ? Main.graph.nodes.threeFinalValues() : 
            (Options.printFW ? s.reachable : s.nodes))) 
            printDot(ps, s, tp, alltests);
            	
        ps.println("}; ");
        ps.close();
    } 

    /** generate legend for those atoms mentioned in the leak condition */
    static void printLegend(VFGNode n, List<BoolExpr> leakcond, PrintStream ps) 
    {
    	if (leakcond.isEmpty())
    		return;
    	
        ps.printf(" nlegend [label=\"{ %-33s", "Legend:");

        for (BoolExpr c : leakcond) 
            for (Terminal a : c.getAtoms()) 
                ps.print(" | " + a + " = " + fixString(a.fullString()));

        ps.println("}\", shape=record, align=left];");
    }
    
    /** generate legend for those atoms mentioned in the leak condition */
    static void printTxtLegend(Set<Atom> allExpr, PrintStream ps) 
    {
        String[] arr = new String[allExpr.size()];
        int i = 0;
        for (Atom a : allExpr) 
            arr[i++] = String.format("   %-5s: %s", a, a.longMessage(false));
        Arrays.sort(arr);
        for (String a : arr) 
            ps.print(a);
    }

    /** fixes strings for dot files */
    static String fixString(String s) {
        if (s == null)
            return null;

        return s.
        	replace("\\\"","\\'").
        	replace("\"", "\\'").
            replace("&amp;","&").
        	replace("&","&amp;").
            replace(">","&gt;").
        	replace("<", "&lt;");
    }

    public static String location(CFGNode node, boolean pretty) {
        ASTNode ast = null;
        if (node.isSwitch())  
            ast = ((CFGSwitchNode)node).getTestSrc();
        else
            ast = ((CFGStatNode)node).getStatement();
    
        if (ast.getFile() == null) {
            CFGNode c = node.getPred(0);
            if (c.isSwitch())  
                ast = ((CFGSwitchNode)c).getTestSrc();
            else
                ast = ((CFGStatNode)c).getStatement();
    
        }
        int lline = ast.getLeftLine();
        int rline = ast.getRightLine(); 
        int lcol = ast.getLeftCol();
        int rcol = ast.getRightCol();
    
        if (pretty)
            return ast.getFile() + ", line " +lline;
        return ast.getFile() + ":" + 
        lline + ":" + lcol + "-" + 
        (lline == rline ? "" : rline + ":") + rcol;
    }

    public static String trim(CFGNode n) {
        return n.toString().trim();
    }
    
    
    static void printLabel(VFGNode vn, boolean issource, PrintStream ps, 
            CFGNode n, CoreExpr e, boolean inslice) {
        String color = "fillcolor=white";
        if (n != null && VFGraph.isSource(n))
            color = "color=red, fillcolor=white";
        else if (issource)
            color = "color=red, fillcolor=lightgrey";
        else if (vn.isSink() && (vn instanceof ReturnNode))
            color = "color=green, fillcolor=lightgrey";
        else if (vn.isSink())
            color = "color=green, fillcolor=white";
        else if (vn instanceof RegionNode)
            color = "color=magenta, fillcolor=white";
        
        if (vn instanceof AssignNode)
            assert e == vn.getCFGNode().getWrittenMem();
        if (vn instanceof ReturnNode)
            assert e == vn.getCFGNode().getCoreKid(0);
        
        String expr = (((vn instanceof AssignNode) || 
                        (vn instanceof SinkLikeNode)) ? "" : 
                e + ((vn instanceof RegionNode) ? "(region)" : "") + " @ ");
        
        
        ps.println("n" + vn.id 
            + " [label=<<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\""
            + (inslice ? "" : " color=\"grey\"")
            + "><tr><td>" + PrintDot.fixString(expr) 
            + ((n == null) ? " ? " : 
                PrintDot.fixString(PrintDot.trim(n))).replace("&","&amp;")
            + "</td></tr><tr><td><font point-size=\"10\" color=\"#666666\">" 
            + (vn.fun == null ? "" : vn.fun.getName()) 
            + " " + ((n == null) ? "" :PrintDot.location(n,false))
            + "</font></td></tr></table>>, shape=rectangle, style=filled," 
            + color + "];"
            );
        
    }
    
    public static void printEdges(VFGNode v, PrintStream ps, Slice s, 
            Set<Atom> allExpr) {
        if (!v.isSink()) 
            for (VFGNode t : v.succs) 
                if (s == null || Options.printFW || s.inSlice(t)) {
                    /* get tests for legend */
                    BoolExpr b = v.guards.get(t);
                    if (allExpr != null && b != null)
                        allExpr.addAll(b.getAtoms());

                    /* print edge  */
                    ps.println("n" + v.id + " -> " + "n" + t.id +
                            ((v instanceof ParamAtCall) ? "[color=blue];" :
                             ((v instanceof ReturnNode) ? "[color=red];" :
                              "[label=\"["+ 
                              PrintDot.fixString(b.toString())
                              +"]\"];")));
                }
    }
    
    
    /** print node and outgoing edges to ps */
    public static void printDot(PrintStream ps, Slice s, VFGNode v, 
            Set<Atom> allExpr) {
        printLabel(v, s != null && s.source == v, ps, v.getCFGNode(), 
            v.accept(new NodePrint(), null), 
            !Options.printFW || s.inSlice(v));
        printEdges(v, ps, s, allExpr);
    }
    
    /** printing of nodes */
    static class NodePrint implements VFGNodeVisitor<Object,CoreExpr> {

        public CoreExpr visit(AssignNode n, Object in) {
            return n.getDef();
        }

        public CoreExpr visit(SinkNode n, Object in) {
            return n.getSinkExpr();
        }

        public CoreExpr visit(ParamAtCall n, Object in) {
            return n.actual;
        }

        public CoreExpr visit(ParamAtEntry n, Object in) {
            return n.par;
        }

        public CoreExpr visit(RegionNode n, Object in) {
            return n.region;
        }

        public CoreExpr visit(ReturnNode n, Object in) {
            return n.ret.getCoreKid(0);
        }
    }
}
