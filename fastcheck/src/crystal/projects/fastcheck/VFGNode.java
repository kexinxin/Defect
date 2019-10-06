
package crystal.projects.fastcheck;

import java.util.*;

import crystal.cfg.*;
import crystal.core.*;
import crystal.general.*;

/** A node in the value-flow graph. */
public abstract class VFGNode {
    
    boolean visiting = false;
    
    static int sid = 0;
    int id = sid++;
    
    protected Function fun;
    
    public final Function getFun() {
        return fun;
    }
    
    public abstract CFGNode getCFGNode();
    
    List<VFGNode> succs = new ArrayList<VFGNode>();
    
    Map<VFGNode, BoolExpr> guards = new HashMap<VFGNode, BoolExpr>();
    
    List<VFGNode> preds = new ArrayList<VFGNode>();
  
    /** the value defined by this node is used by 'succ'. */
    void addSucc(VFGNode succ, BoolExpr c) {
        addSucc(succ);
        BoolExpr old = guards.get(succ);
        if (old == null) {
            assert c != null;
        } else
            c = old.or(c);
        guards.put(succ, c);
    }

    /** add a successor, do not set any guard condition. */
    void addSucc(VFGNode succ) {
        if (!succs.contains(succ)) {
            succs.add(succ);
            succ.preds.add(this);
        }
    }
    
    VFGNode(Function f) {
        fun = f;
    }
    
    boolean isSink() {
        return false;
    }
     
    public boolean hasDef() {
        return false;
    }
    
    public CoreExpr getDef() {
        return null;
    }
            
    /** visitor with input and outpout */
    static interface VFGNodeVisitor<I,O> {
        
        public O visit(AssignNode n, I in);
        
        public O visit(SinkNode n, I in);
        
        public O visit(ParamAtCall n, I in);
        
        public O visit(ParamAtEntry n, I in);
        
        public O visit(RegionNode n, I in);
        
        public O visit(ReturnNode n, I in);
    }
    
    public abstract <I,O> O accept(VFGNodeVisitor<I,O> v, I in);
   
}

class AssignNode extends VFGNode {
    CFGNode assign;
    
    public AssignNode(CFGNode a, Function f) {
        super(f);
        assign = a;
    }
    
    public String toString() {
        return PrintDot.trim(assign); 
    }
    
    public CFGNode getCFGNode() {
        return assign;
    }
    
    public boolean hasDef() {
        return true;
    }
    
    public CoreExpr getDef() {
        return assign.getWrittenMem();
    }
    
    public <I,O> O accept(VFGNodeVisitor<I,O> v, I in) {
        return v.visit(this, in);
    }
    
}

class ParamAtCall extends VFGNode {
    CFGNode call;
    CoreExpr actual;
    
    public ParamAtCall(CFGNode c, CoreExpr x, Function f) {
        super(f);
        call = c;
        actual = x;
    }
    
    public String toString() {
        return actual + "@" + PrintDot.fixString(PrintDot.trim(call)); 
    }
    
    public CFGNode getCFGNode() {
        return call;
    }
    
    public <I,O> O accept(VFGNodeVisitor<I,O> v, I in) {
        return v.visit(this, in);
    }
}

class ParamAtEntry extends VFGNode {
    CoreExpr par;
    
    public ParamAtEntry(CoreExpr p, Function f) {
        super(f);
        par = p;
    }

    public String toString() {
        return par + "@ entry"; 
    }
    
    public CFGNode getCFGNode() {
        return fun.getCFG().entryNode;
    }
    
    public boolean hasDef() {
        return true;
    }
    
    public CoreExpr getDef() {
        return par;
    }
    
    public <I,O> O accept(VFGNodeVisitor<I,O> v, I in) {
        return v.visit(this, in);
    }
}

class ReturnNode extends SinkLikeNode {
    CFGNode ret;
    
    public ReturnNode(CFGNode r, Function f) {
        super(f);
        ret = r;
    }

    public void setSink() {
        sink = true;
    }
    
    public String toString() {
        return PrintDot.trim(ret); 
    }
    
    public CFGNode getCFGNode() {
        return ret;
    }
    
    public CoreExpr getSinkExpr() {
        return ret.getCoreKid(0);
    }
  
    public <I,O> O accept(VFGNodeVisitor<I,O> v, I in) {
        return v.visit(this, in);
    }
}

class RegionNode extends VFGNode {
    CoreExpr region;
    Map<Function,Set<CFGNode>> where;
    
    public RegionNode(CoreExpr r) {
        super(null);
        region = r;
        where = new HashMap<Function,Set<CFGNode>>();
    }
    
    public void addStmt(Function f, CFGNode stmt) {
        Set<CFGNode> w = where.get(f);
        if (w == null) {
            w = new HashSet<CFGNode>();
            where.put(f, w);
        }
        if (!w.contains(stmt))
            w.add(stmt);
    }
    
    public CFGNode getCFGNode() {
//        if (where == null || where.size() != 1)
//            return null;
//        for (Pair<Function,CFGNode> n : where) {
//            fun = n.fst;
//            return n.snd;
//        }
        return null;
    }
    
    public <I,O> O accept(VFGNodeVisitor<I,O> v, I in) {
        return v.visit(this, in);
    }
}

/** Node for free statements in the value flow graph */
class SinkNode extends SinkLikeNode {
    CFGNode sinkIns;
    int sinkExprNum = 0;
    
    /** Sink node is an instruction fr in function f where
     * the argument # argNum is the experssion that ends
     * in this sink */
    public SinkNode(CFGNode fr, Function f, int argNum) { 
        super(f);
        sinkIns = fr;
        sink = true;
        sinkExprNum = argNum;
    }
    
    public String toString() {
        return PrintDot.trim(sinkIns); 
    }
    
    public <I,O> O accept(VFGNodeVisitor<I,O> v, I in) {
        return v.visit(this, in);
    }
    
    public CFGNode getCFGNode() {
        return sinkIns;
    }
    
    public final CoreExpr getSinkExpr() {
        return ((CFGCallNode)sinkIns).getArg(sinkExprNum);
    }
}

abstract class SinkLikeNode extends VFGNode {
    boolean sink = false;
    public SinkLikeNode(Function f) {
        super(f);
    }
    public abstract CoreExpr getSinkExpr();
    public abstract CFGNode getCFGNode();
    public boolean isSink() {
        return sink;
    }
}
    