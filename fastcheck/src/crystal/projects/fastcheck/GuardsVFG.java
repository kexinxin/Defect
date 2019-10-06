
package crystal.projects.fastcheck;

import java.util.*;

import crystal.core.*;
import crystal.util.*;

/** A class that computes guards over the value-flow graph.
 *  The alogorithm is similar to the one that computes guards over the CFG,
 *  except for variable redefinitions, which are not present in the VFG.
 *  CFL matching of call/returns was already done during slicing.
 */
public class GuardsVFG {
    
    /** current set of visited VFG edges */
    EdgeSet edges;
    
    /** end point of the guard we are computing */
    VFGNode currentTarget = null;
    
    /** current slice */
    Slice slice = null;
    
    public GuardsVFG(Slice s) {
        slice = s;
    }
    
    /** generates guards (only if slice leads to frees), 
     * add to fc the conditions to reach each free f in the slice. */
    public void genGuards(Map<SinkLikeNode, BoolExpr> fc) {
        assert slice.reachesSink();
        for (SinkLikeNode f : slice.sinks) {
            currentTarget = f;
            edges = new EdgeSet();
            BoolExpr g = genGuard(slice.source);
            fc.put(f, g);
        }
    }
    
    /** computes guards between start and target in context, stores
     * cache to avoid recomputing it */
    private BoolExpr genGuard(VFGNode start) {
        BoolExpr g = null;
        if (slice.inSlice(start)) {
            g = BoolExpr.FALSE;

            boolean cguard = !((start instanceof ParamAtCall) 
                    || (start instanceof ReturnNode));

            for (VFGNode s : start.succs) 
                if (slice.inSlice(s)) { 
                    if (!edges.containsEdge(start, s)) {
                        BoolExpr gedge;
                        if (cguard) {
                            gedge = start.guards.get(s); 
                            if (gedge == BoolExpr.TMP) {
                                assert start.hasDef();
                                CoreExpr e = start.getDef();
                                gedge = GuardsCFG.computeGuard(e,
                                    start.getCFGNode(),
                                    s.getCFGNode(), start.getFun(), slice);
                                start.guards.put(s, gedge);
                            }
                            if (edges.hasEdge(start))
                                // drop second instance
                                gedge = BoolExpr.TRUE;
                        } else
                            gedge = BoolExpr.TRUE;

                        edges.put(start, s, true);
                        BoolExpr gf = genGuard(s);
                        edges.put(start, s, false);
                        assert (gf != null);
                        g = g.or(gf.and(gedge));
                    }
                }

            if (start == currentTarget) {
                g = BoolExpr.TRUE;
                if (!start.isSink())
                    System.out.println(" >>>> VFGGuards: WARNING" );
                Main.consumers.add(start.getFun());
            }

        }
        return g;
    }

    /** set of edges */
    static class EdgeSet extends TwoLevelMap<VFGNode,VFGNode,Boolean> {
        private static final long serialVersionUID = 1L;

        public EdgeSet() {
        }
        
        /** return if conttains edge from n to n.succ */
        public boolean containsEdge(VFGNode n, VFGNode s){
            if (get(n,s) == null)
                return false;
            return get(n,s);
        }
        
        /** return if conttains edge from n to n.succ */
        public boolean hasEdge(VFGNode n){
            if (!containsKey(n))
                return false;
            for (Boolean b : valuesOfFst(n)) 
                if (b)
                    return true;
            return false;
        }
        
        EdgeSet copy() {
            return (EdgeSet) super.clone();
        }

    }
}
