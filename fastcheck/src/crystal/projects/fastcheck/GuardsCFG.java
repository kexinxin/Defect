package crystal.projects.fastcheck;

import java.util.*;

import crystal.cfg.*;
import crystal.core.*;
import crystal.general.*;
import crystal.util.*;

/** A class that computes guards over the control-flow graph.
 *  Given two CFG nodes m and n, such that m defines variable x and 
 *  n uses x, compute a path condition for which the value flows from
 *  m to n. The computation must account for paths that redefine x.
 *  
 *  Recursive version presented in the paper
 */
public class GuardsCFG {
    Indexer<CFGNode> indexer;
    
    /** current expression */
    private CoreExpr def = null;
    
    /** exit node of the function (used for post-dominance) */
    private CFGNode exitNode = null;
    
    /** end point of the current queried guard edge */
    private CFGNode end = null;
    
    /** current slice */    
    private Slice slice;
    
    public GuardsCFG(CoreExpr e, CFGNode cend, Function f, Slice s) {
        def = e;
        end = cend;
        fun = f;
        indexer = new Indexer<CFGNode>(fun.getCFG().getAllNodes());
        exitNode = f.getCFG().exitNode;
        pdomCacheA = new IBitVector<CFGNode>(indexer);
        pdomCacheB = pdomCacheA.copy();
        slice = s;
        
    }
    
    /** Computes the guard from 'start' to 'end' (both should be 
     * instructions in 'fun') parameterized on 'e' (see class comment). */
    public static BoolExpr computeGuard(CoreExpr e, 
            CFGNode start, CFGNode end, Function fun, Slice s){
        BoolExpr guard = BoolExpr.UNK;
        if (start == null || end == null)
            return guard;
        
        assert start.getWrittenMem() == null || start.getWrittenMem() == e;
        if (start == end) {
            assert start.getWrittenMem() != null;
            assert start.numSuccs() == 1;
            start = start.getSucc(0);
        }
        guard = (new GuardsCFG(e, end, fun, s)).getGuard(start, 
            new EdgeSet());
        return guard;
    }

    Map<CFGNode, BoolExpr> cache = new HashMap<CFGNode, BoolExpr>();
    
    private BoolExpr getGuard(CFGNode from, EdgeSet e) {

        if (pdom(from)) 
            return BoolExpr.TRUE;
        
        if (cache.get(from) != null)
            return cache.get(from);
        
        BoolExpr guard = BoolExpr.FALSE;
        for (int i = 0; guard != BoolExpr.TRUE && i < from.numSuccs(); i ++) 
            if (!e.containsEdge(from, i)) {
                CFGNode succ = from.getSucc(i);
                if (succ.getWrittenMem() != def || succ == end) {
                    BoolExpr ci; 
                    if (e.hasEdge(from))
                        // droping second instance of test
                        ci = BoolExpr.TRUE;
                    else
                        // first instance of the test
                        ci = getDirectGuard(from, i);
                    e.addEdge(from, i);
                    ci = ci.and(getGuard(succ, e));
                    e.removeEdge(from, i);
                    guard = guard.or(ci);
                }
            }
        cache.put(from, guard);
        return guard;
    }
    
    private Function fun = null;

    boolean pdom(CFGNode from) {
        int ans = pdom(from, new IBitVector<CFGNode>(indexer));
        return (ans & PDOM_MASK) != 0;
    }

    /** have we computed the cache for n? */
    IBitVector<CFGNode> pdomCacheA;
    /** what is the cached value for n? */
    IBitVector<CFGNode> pdomCacheB;
    
    /** mask bit for pdom: p & PDOM_MASK != 0 => post-dom = true */ 
    static final int PDOM_MASK      = 0002;
    /** mask bit for caching: p & CAN_CACHE_MASK != 0
     *  => we can store the current value in the cache */
    static final int CAN_CACHE_MASK = 0001;
    
    /** Return 2 bits PDOM + CANT_CACHE. the additional flag cant_cache
     * is tracked to only store a cache of final values (not temporary values
     * computed during the traversal). 
     * This trick is important for nested loops. */
    int pdom(CFGNode from, IBitVector<CFGNode> visited) {

        if (from == end)
            return PDOM_MASK | CAN_CACHE_MASK;
        
        if (from == exitNode)
            return CAN_CACHE_MASK;

        if (Options.pdomCaching && pdomCacheA.get(from))
            return (pdomCacheB.get(from) ? PDOM_MASK : 0) | CAN_CACHE_MASK;

        if (visited.get(from)) 
            return PDOM_MASK;
        visited.set(from);
        
        int p = PDOM_MASK | CAN_CACHE_MASK;
        for (int i = 0 ; ((p & PDOM_MASK) != 0) && i < from.numSuccs(); i++) {
            CFGNode n = from.getSucc(i);
            if (n.getWrittenMem() == def && n != end)
                p &= ~PDOM_MASK; 
            if ((p & PDOM_MASK) != 0) 
                p &= pdom(n, visited);
            
        }
        visited.clear(from);
        if (from.isSwitch()) {
                if ((p & CAN_CACHE_MASK) == CAN_CACHE_MASK) {
                pdomCacheA.set(from);
                if ((p & PDOM_MASK) != 0)
                    pdomCacheB.set(from);
            }
        }
        
        return p;

    }

    /** saves direct branches we already created */
    TwoLevelMap<CFGNode, Integer, BoolExpr> branches =
        new TwoLevelMap<CFGNode, Integer, BoolExpr>();
    
    /** computes the guard between n and succ, where succ is a succesor of n
     * if test is def == null (def != null) return false (true) */
    private BoolExpr getDirectGuard(CFGNode n, int branch) {
        BoolExpr g = branches.get(n, branch);
        if (g == null) {
            g = BoolExpr.TRUE;

            if (n.isSwitch()) 
                g = BoolExpr.getAtom((CFGSwitchNode)n, branch);
            
            if (g.isTestLikeNull(slice, def, false))
                g = BoolExpr.FALSE;
        
            if (g.isTestLikeNull(slice, def, true))
                g = BoolExpr.TRUE;
            
            branches.put(n, branch, g);
        }
        return g;
    }
    
    /** respresenting a set of edges. Assumes switch nodes in edge set have 
     * at most 32 successors. */
    static class EdgeSet extends HashMap<CFGNode,Integer> {
        private static final long serialVersionUID = 1L;

        public EdgeSet() {
        }
        
        /** add edge from n to n.succ */
        public void addEdge(CFGNode n, int succ) {
            Integer i = get(n);
            int val = (i == null) ? 0 : i;
            if (succ >= 32)
                throw new RuntimeException("switch with too many succesors,"
                    + " need to update implementation");
            val |= 1<<succ;
            put(n, val);
        }
        
        /** add edge from n to n.succ */
        public void removeEdge(CFGNode n, int succ) {
            Integer i = get(n);
            int val = (i == null) ? 0 : i;
            if (succ >= 32)
                throw new RuntimeException("switch with too many succesors,"
                    + " need to update implementation");
            val &= (~(1<<succ));
            put(n, val);
        }
        
        
        /** return if conttains edge from n to n.succ */
        public boolean containsEdge(CFGNode n, int succ){ 
            Integer i = get(n);
            return (i != null && ((i & 1<<succ) != 0));
        }

        /** return if conttains edge from n to n.succ */
        public boolean hasEdge(CFGNode n){ 
            Integer i = get(n);
            return (i != null && i > 0);
        }
        
    }
}
