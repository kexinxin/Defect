package crystal.projects.fastcheck;

import crystal.analysis.callgraph.CallGraph;
import crystal.cfg.*;
import crystal.core.*;
import crystal.general.*;
import crystal.util.Pair;
import crystal.util.TwoLevelMap;

import java.util.*;

/**
 * A class representing the VFG slice of a source node. The slice is the set of
 * all nodes on paths between the source and the sinks CFL-reachable (matching
 * call/return edges) from that source.
 */
public class Slice {

    /** The numeric identifier of the slice */
    int id;

    /**
     * is this slice contained inside an allocator-like function (produces
     * sources)
     */
    boolean producer;

    /** sourceSite (explicit in case source is region node) */
    CFGStatNode sourceSite;

    /** The source node */
    VFGNode source;

    /** The function of the source */
    Function fun;

    /** The set of sink nodes */
    Set<SinkLikeNode> sinks = new HashSet<SinkLikeNode>();

    /** The set of all nodes in the slice. */
    Set<VFGNode> nodes = new HashSet<VFGNode>();

    /**
     * The set of all nodes reachable from the source. A superset of the nodes
     * in the slice.
     */
    Set<VFGNode> reachable = new HashSet<VFGNode>();

    /** ??? Region nodes, where this slice may also end */
    Set<RegionNode> regions = new HashSet<RegionNode>();

    /** Slice classification. */
    GraphAnalyzer.Category kind;

    /** The constructor that builds the slice */
    public Slice(int i, Function f, VFGNode s, CFGStatNode site) {
        id = i;
        source = s;
        fun = f;
        nodes.add(source);
        sourceSite = site;

        forwardTraversal();
        if (!reachesRegion()) backwardTraversal();
    }

    /**
     * Forward traversal is divided in 2 phases: 1. CFL reachability up to the
     * source's function. 2a. If no region is encountered and we return the
     * source cell, this function becomes an "producer", hence - Slice doesn't
     * include outside calls and returns of this fucntion are marked as sinks.
     * 2b. If a region is encountered, the slice also includes the part reached
     * after returning. A simple reachability is used at this point (without
     * matching call edges) just to determine if any sink is reached
     */
    public void forwardTraversal() {

        Stack<Pair<VFGNode, VFGNode>> worklist = new Stack<Pair<VFGNode, VFGNode>>();

        assert source != null;
        worklist.push(new Pair<VFGNode, VFGNode>(source, null));

        Map<VFGNode, Set<ReturnNode>> summary = new HashMap<VFGNode, Set<ReturnNode>>();

        TwoLevelMap<VFGNode, CFGNode, VFGNode> unmap = new TwoLevelMap<VFGNode, CFGNode, VFGNode>();

        Set<ReturnNode> tentativeSinks = forwardCFL(worklist,
            unmap, summary, true);

        if (reachesRegion()) {
            // continue traversing forward
            for (VFGNode n : tentativeSinks)
                worklist.add(new Pair<VFGNode, VFGNode>(n, null));
            tentativeSinks = forwardCFL(worklist, unmap, summary,
                false);
            assert (tentativeSinks == null);
        } else {
            // stop traversal, add new sources/sinks
            if (!tentativeSinks.isEmpty()) {
                /**
                 * within this function we didn't reach a region, so this
                 * function behaves like an allocator/producer. So we add calls
                 * to the producer as source nodes
                 */
                for (ReturnNode n : tentativeSinks) {
                    n.setSink();
                    sinks.add(n);
                    for (VFGNode call : n.succs) {
                        CFGNode callSite = call.getCFGNode();
                        if (callSite == null) {
                            // location goes into region in caller
                            assert (call instanceof RegionNode);
                            for (Function f : ((RegionNode) call).where
                                    .keySet())
                                for (CFGNode c : ((RegionNode) call).where
                                        .get(f))
                                    if (c.isCall()) for (Function fe : CallGraph
                                            .getCallees((CFGStatNode) c))
                                        if (fe == n.fun) registerProducerCall(
                                            f, c);
                        } else
                            registerProducerCall(call.fun, callSite);
                    }
                }

                /* register this function as an producer */
                if (!producer) {
                    producer = true;
                    Set<Slice> ls = Main.producers.get(fun);
                    if (ls == null) {
                        ls = new HashSet<Slice>();
                        Main.producers.put(fun, ls);
                    }
                    ls.add(this);
                }

            }
        }
    }

    /** forward CFL reachability with summaries */
    private Set<ReturnNode> forwardCFL(
            Stack<Pair<VFGNode, VFGNode>> worklist,
            TwoLevelMap<VFGNode, CFGNode, VFGNode> unmap,
            Map<VFGNode, Set<ReturnNode>> summary,
            boolean detectWrapper)
    {

        Set<ReturnNode> tentativeSinks = null;
        if (detectWrapper) tentativeSinks = new HashSet<ReturnNode>();

        while (!worklist.isEmpty()) {
            Pair<VFGNode, VFGNode> p = worklist.pop();
            VFGNode n = p.fst;
            VFGNode src = p.snd;

            if (reachable.contains(n)) continue;

            reachable.add(n);

            if (n.isSink()) sinks.add((SinkLikeNode) n);
            else {
                if (n instanceof RegionNode) {
                    regions.add((RegionNode) n);
                    if (detectWrapper) {
                        for (VFGNode s : n.succs) {
                            assert s != null;
                            worklist
                                    .add(new Pair<VFGNode, VFGNode>(
                                        s, null));
                        }
                        break;
                    }
                }

                if (n instanceof ParamAtCall) {
                    if (n.succs.isEmpty()
                            && ((CFGCallNode) n.getCFGNode())
                                    .getFun().isDeref())
                    {
                        calleeUnresolved = true;
                        unresolved = n;
                    }

                    for (VFGNode s : n.succs) {
                        assert (s instanceof ParamAtEntry);
                        if (!unmap.containsKeys(s, n.getCFGNode()))
                        {
                            unmap.put(s, n.getCFGNode(), src);
                        } else {
                            // reuse summary
                            if (summary.get(s) != null) for (ReturnNode ret : summary
                                    .get(s))
                            {
                                for (VFGNode ncall : ret.succs)
                                    if (ncall.getCFGNode() == n
                                            .getCFGNode())
                                    {
                                        assert ncall != null;
                                        worklist
                                                .add(new Pair<VFGNode, VFGNode>(
                                                    ncall, src));
                                    }
                            }
                        }
                        assert s != null;
                        worklist.push(new Pair<VFGNode, VFGNode>(s,
                            s));
                    }

                } else if (n instanceof ReturnNode) {
                    if (fun == n.fun && detectWrapper) tentativeSinks
                            .add((ReturnNode) n);
                    else {
                        // add return point to summary
                        if (src != null) {
                            Set<ReturnNode> rets = summary.get(src);
                            if (rets == null) {
                                rets = new HashSet<ReturnNode>();
                                summary.put(src, rets);
                            }
                            rets.add((ReturnNode) n);
                        }
                        // propagate to calling contexts
                        Set<CFGNode> context = (src == null || unmap
                                .get(src) == null) ? null : unmap
                                .get(src).keySet();
                        assert (!detectWrapper || context != null) : n.fun;
                        for (VFGNode s : n.succs)
                            if (shouldFollow(s, context,
                                detectWrapper))
                            {
                                assert s != null;
                                worklist
                                        .push(new Pair<VFGNode, VFGNode>(
                                            s, unmap.get(src, s
                                                    .getCFGNode())));
                            }

                    }
                } else
                    for (VFGNode s : n.succs) {
                        assert s != null;
                        worklist.push(new Pair<VFGNode, VFGNode>(s,
                            src));
                    }
            }
        }
        return tentativeSinks;
    }

    private boolean shouldFollow(VFGNode afterRet,
            Set<CFGNode> context, boolean dw)
    {
        if (!dw && context == null) return true;
        CFGNode n = afterRet.getCFGNode();
        if (n != null && context.contains(n)) return true;

        if (afterRet instanceof RegionNode) {
            for (Function f : ((RegionNode) afterRet).where
                    .keySet())
                for (CFGNode c : ((RegionNode) afterRet).where
                        .get(f))
                    if (context.contains(c)) return true;
        }
        return false;
    }

    private static void registerProducerCall(Function fun,
            CFGNode callSite)
    {
        if (!GraphAnalyzer.analyzed.contains(callSite)) {
            assert fun != null;
            /* set callers of this function as sources */
            GraphAnalyzer.analyzed.add(callSite);
            GraphAnalyzer.sites.add(new Pair<Function, CFGNode>(
                fun, callSite));
        }
    }

    /**
     * The backward traversal from the sinks. Only nodes encountered during the
     * forward traversal are considered.
     */
    public void backwardTraversal() {
        Stack<VFGNode> worklist = new Stack<VFGNode>();

        worklist.addAll(sinks);
        while (!worklist.isEmpty()) {
            VFGNode n = worklist.pop();
            if (!nodes.contains(n) && reachable.contains(n)) {
                nodes.add(n);
                for (VFGNode p : n.preds)
                    worklist.add(p);
            }
        }
    }

    boolean inSlice(VFGNode n) {
        return (nodes.contains(n));
    }

    boolean reachesSink() {
        return !sinks.isEmpty();
    }

    public String toString() {
        return "slice ["
                + source
                + "]"
                + (reachesSink() ? " --> { " + sinks + " }"
                    : " ~~>X ");
    }

    public int name() {
        return id;
    }

    boolean reachesRegion() {
        return !regions.isEmpty();
    }

    private HashSet<CoreExpr> seenvars = null;

    boolean inSlice(CoreExpr e) {
        if ((e instanceof CoreBinaryExpr) || e.isNumConstant()) return false;

        if (e instanceof CoreUnaryExpr) {
            /* casts can also be a dereference expression */
            CoreUnaryExpr eiu = (CoreUnaryExpr) e;
            if (eiu.isCast()) return inSlice(eiu.getExpr());
        }

        if (!e.isDeref()) return false;

        if (!e.isPureLocalVar()) return reachesRegion();

        if (seenvars == null) {
            seenvars = new HashSet<CoreExpr>();
            for (VFGNode n : nodes)
                if (n.getDef() != null) seenvars.add(n.getDef());
        }

        return seenvars.contains(e);
    }

    public int fwsize() {
        return reachable.size();
    }

    public int size() {
        return nodes.size();
    }

    boolean calleeUnresolved = false;

    VFGNode unresolved = null;

    public boolean flowsIntoUnresolvedCall() {
        return calleeUnresolved;
    }

}
