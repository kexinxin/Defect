package crystal.projects.fastcheck;

import java.io.*;
import java.util.*;

import crystal.cfg.*;
import crystal.core.*;
import crystal.general.*;
import crystal.util.Pair;

/** methods to analyze the value flow graph */
public class GraphAnalyzer {
    
    /** whole-program value flow graph */
    static VFGraph dg = null;
    
    
    /** enumeration for the different categories of leaks */
    public enum Category {
        
        NOT_FREED_LOCAL_NOT_MAIN(1, 1, "not_freed.local.not_main",
        "Never freed cell, cell only assigned to local variables in " +
        "functions different than main"),

        FREED_COND_LEAKED (2, 1, "freed.cond.leaked",
                  "Allocated cell is freed, but also leaks on some paths"),

        FREED_OK(3, 0, "freed.ok",
        "Allocated cell is always freed or returned from allocator function," +
        " except when null"),

        NOT_FREED_NOT_LOCAL(4, 2, "not_freed.not_local",     
				"Never freed cell, cell may be stored " +
                "into global scope, aggregates or heap pointers. "),

        NOT_FREED_LOCAL_MAIN(5, 2, "not_freed.local.main",
        		"Never freed cell, placed in local variable in main"), 
                
        FREED_COND_UNKNOWN(6, 3, "freed.local.unknown",
                "Cell may be freed, but guard condition has unk "),
                
        FREED_IN_NON_LOCAL(7, 3, "freed.not_local.unknown",
        		"Cell may be freed, but is stored in non local variable " +
                "(i.e. global, heap cell, struct or array)");
        
        		
        public final String dirname;
        public final String description;
        public final int level;
        public final int index;
        
        Category(int i, int l, String dn, String desc) {
            dirname = dn;
            level = l;
            description = desc;
            index = i;
        }
        
        public String message() {
        	switch (level) {
        	case 0: return "no errors"; 
        	case 1: return "memory leak error"; 
        	case 2: return "benign memory leak"; 
        	case 3: return "don't know"; 
        	default: return "";
        	}
        }
        
        public String toString() {
            return dirname;
        }
        
        public boolean isSelected() {
            return (Options.directSelect & (1 << index)) != 0;
        }
    }
    
    /** All allocation sites in the program. Additional call sites 
     *  of allocator wrappers are added as the analysis proceeds.
     *  We use a sorted structure to be able to generate the same 
     *  id's on different runs of the tool. */
    static TreeSet<Pair<Function,CFGNode>> sites = 
        new TreeSet<Pair<Function,CFGNode>>(
                new Comparator<Pair<Function,CFGNode>>() {
                    public int compare(Pair<Function,CFGNode> a ,
                            Pair<Function,CFGNode> b ) {
                        return PrintDot.location(a.snd,false).compareTo(
                            PrintDot.location(b.snd,false));
                    }
                }
        );

    /** The allocation sites that have been analyzed so far. */
    static Set<CFGNode> analyzed = new HashSet<CFGNode>();

    public static void findAndPrintLeaks(VFGraph graph) {
        dg = graph;
        
        for (Function fun : Symtab.getFunctions()) 
            if (!fun.isInit() && !VFGraph.isSource(fun)) 
                for (CFGNode n : fun.getCFGNodes())
                    if (VFGraph.isSource(n)) 
                        sites.add(new Pair<Function,CFGNode>(fun,n));
        
        for (int i = 1; !sites.isEmpty(); i++) {
            Pair<Function,CFGNode> p = sites.pollFirst();
            analyzeSource(i, p.fst, p.snd);
        }
   }

    
    /** evaluates a source  may trigger the computation of guards */
    private static void analyzeSource(int id, Function fun, CFGNode site) 
    {
        Stats.slicing.begin();
        if (VFGraph.isSource(site)) Stats.numAllocs ++;
        else Stats.numCallsToAW ++;
        Stats.numSlices++;
        
        /* identify the source node in the VFG */
        CoreDerefExpr cde = site.getWrittenMem();
        VFGNode source = null;
        
        /* start node is different if cde is global/region */
        if (cde != null && cde.isPureLocalVar())
            source = dg.get(site, cde, true, fun, -1);
        else
            source = dg.getRegion(cde, site, fun);

        /* perform simple reachability */
        Slice s = new Slice(id, fun, source, (CFGStatNode) site);
        Stats.fwsliceCounter.update(s.fwsize());
        Stats.sliceCounter.update(s.size());
        Stats.slicing.finish();
        
        if (s.producer && Options.excludeAllocatorSites)
            return;
        
        Map<SinkLikeNode, BoolExpr> freecond = 
            new HashMap<SinkLikeNode, BoolExpr>();
        
        BoolExpr leakcond = BoolExpr.TMP;
        
        /* perform guarded reachability if needed */
        if (s.reachesSink() && !s.reachesRegion()) {
            Stats.guards.begin();
            Stats.guardCounter.update(s.size());
            (new GuardsVFG(s)).genGuards(freecond);
            leakcond = simplify(computeLeakCond(freecond, 
                site.getWrittenMem(), s));
            Stats.guards.finish();
        } 
        
        s.kind = getKind(fun, s, leakcond, cde);

        List<BoolExpr> leakSol = null;
        List<List<BoolExpr>> dfrees = null;
        
        if (s.kind == Category.FREED_COND_LEAKED ||
                s.kind == Category.FREED_OK) {
            Stats.satsolver.begin();
            /* generate clauses to correlate branches */
            BoolExpr branchCorr = genBranchCorrelation(leakcond);
            if (s.kind == Category.FREED_COND_LEAKED) {
                leakSol = SATProblem.solve(leakcond.and(branchCorr));
                Stats.SATSolvers ++;
            }
            dfrees = findDoubleFrees(source, freecond, s);
            Stats.satsolver.finish();
            
            if (leakSol == null || leakSol.isEmpty())
                s.kind = Category.FREED_OK;
            else
                for (BoolExpr e : leakSol)
                    if (e == BoolExpr.UNK)
                        s.kind = Category.FREED_COND_UNKNOWN;
        }
        
        Stats.incCount(s.kind);
        if (s.kind == Category.FREED_COND_LEAKED || 
                s.kind == Category.NOT_FREED_LOCAL_NOT_MAIN)
            Stats.reportedCounter.update(s.size());

        List<BoolExpr> originalLeakpath = leakSol;
        /** clean up solution */
        if (leakSol != null && s.kind != Category.FREED_COND_UNKNOWN) {
            List<BoolExpr> tmp = null;
            // when size == 1, we might have leakSol = unk
            if (Options.cleanupLeakPath && leakSol.size() > 1)
                tmp = cleanLeakCond(site, leakSol, s);
            if (tmp == null) 
                sortLeakpath(leakSol);
            else 
                leakSol = tmp;
        }
        
        Stats.printing.begin();
        if (Options.printTxt && s.kind.isSelected()) {
            if (s.kind == Category.FREED_COND_LEAKED) 
                PrintTxt.printConditional(s, leakSol);
            else if (s.kind == Category.NOT_FREED_LOCAL_NOT_MAIN)
                PrintTxt.printNoFree(s);
            else
                PrintTxt.printOther(s);
        }
        
        if (Options.genDot || Options.genHtml) { 
            Set<Atom> alltests = new HashSet<Atom>();
            boolean genPng = false;
            if (shouldGenDot(s)) {
                Stats.printingDot.begin();
                PrintDot.printDot(s, leakSol, dfrees, alltests);
                Stats.printingDot.finish();
                if (Options.genPng) {
                    genPng = true;
                    Stats.printingPNG.begin();
                    generatePNG(s);
                    Stats.printingPNG.finish();
                }
            }
            if (Options.genHtml) {
                Stats.printingHtml.begin();
                PrintHtml.printHtml(s, leakSol, 
                    originalLeakpath, dfrees, alltests, genPng);
                Stats.printingHtml.finish();
            }
        }
        
        Stats.printing.finish();
        
        return;
    }


    private static boolean shouldGenDot(Slice s) {
        return s.kind != Category.FREED_IN_NON_LOCAL &&
                s.kind != Category.NOT_FREED_NOT_LOCAL &&
                Options.genDot;
    }

    /** run dot to generate png file from dot file */
    private static void generatePNG(Slice s) {
        try {
            String m="dot -Tpng  " +
                    Main.OUTDIR + "/" + s.kind + "/site" + s.name() + ".dot -o " +
                    Main.OUTDIR + "/" + s.kind + "/site" + s.name() + ".png";
            Process p = Runtime.getRuntime().exec("dot -Tpng  " +
            Main.OUTDIR + "/" + s.kind + "/site" + s.name() + ".dot -o " +
            Main.OUTDIR + "/" + s.kind + "/site" + s.name() + ".png");
            try {
                int c = p.waitFor();
                if (c != 0) {
                    System.err.println("WARN: proc exit code: " + c);
                    String si;
                    BufferedReader br = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                    while ((si = br.readLine()) != null) {
                        System.out.println(si);
                    }
                }
                
            } catch (InterruptedException ex) {
                System.err.println("Exception while closing down proc: "
                                   + ex.getMessage());
                ex.printStackTrace(System.err);
            }
        } catch (Exception e) {
            System.err.println("Couldn't run dot to generate png file");
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    /** cleans output to generate path directly to leak
     *  (eliminate unnecessary tests, sort relevant tests) */
    static List<BoolExpr> cleanLeakCond(CFGNode src, List<BoolExpr> leakpath, 
        Slice s) {
        Set<CFGNode> visited = new HashSet<CFGNode>();
        List<BoolExpr> res = new ArrayList<BoolExpr>();
        CFGNode next = src;
        
        while (next != null) {
            if (next.isExit())
                return res;
            
            if (next != src && 
                    next.getWrittenMem() == src.getWrittenMem()) {
                if (next.isAssign()) {
                    CoreExpr e = ((CFGAssignNode) next).getRight();
                    if (VFGraph.extractCoreDerefExpr(e) != next.getWrittenMem())
                        return res;
                } else
                    return res;

            }

            int k = next.numSuccs();
            if (k == 1) 
                next = next.getSucc(0);
            else {
                int b = -1;
                BoolExpr btaken = null;
                for (BoolExpr bo : leakpath) 
                    if (bo.getTest() == next) {
                        b = bo.getBranchNum();
                        if (b != -1) {
                            btaken = bo;
                            break;
                        }
                    }
                
                if (btaken != null) {
                    if (visited.contains(next)) {
                        return res;
                    } else
                        visited.add(next);
                    assert b < k;
                    res.add(btaken);
                    next = next.getSucc(b);
                } else {
                    BoolExpr at = Atom.get((CFGSwitchNode)next,0); 
                    /** avoid taking branches we eliminated (x != null) */
                    boolean tr = at.isTestLikeNull(s, 
                        src.getWrittenMem(), false);
                    int bi = (tr ? 1 : 0);
                    
                    /** take different branch second time */
                    if (visited.contains(next)) 
                        bi = 1 - bi;
                    else
                        visited.add(next);
                    next = next.getSucc(bi);
                    
                }
            }
        }
        return null;
    }
    
    /** sort conditions by file/line number */
    public static void sortLeakpath(List<BoolExpr> leakpath) {
        Collections.sort(leakpath, new Comparator<BoolExpr> () {
            public int compare(BoolExpr o1, BoolExpr o2) {
                return o1.longMessage(false).compareTo(o2.longMessage(false));
            }
        });
    }
    
    /** determines the kind of leak expressed by leakCond */
    private static Category getKind(Function fun, Slice s, BoolExpr leakCond, 
            CoreDerefExpr cde) {
        /* clasify this case */
        VFGNode n = s.source;
        
        if (!s.reachesSink() || leakCond == BoolExpr.TRUE) {
            if (s.flowsIntoUnresolvedCall()) {
                System.out.println(" " + s + " flows into unresolved call"
                    + s.unresolved);
                return Category.FREED_IN_NON_LOCAL;
            }

            if (fun.getName().equals("main"))
                return Category.NOT_FREED_LOCAL_MAIN;

            if (cde == null) 
                System.out.println(" > " + n + " has null def");
            if (s.reachesRegion() || cde == null || !cde.isPureLocalVar() ) 
                return Category.NOT_FREED_NOT_LOCAL;

            return Category.NOT_FREED_LOCAL_NOT_MAIN;
        } 
        
        if (s.reachesRegion())  
            return Category.FREED_IN_NON_LOCAL;
        
        if (leakCond.height <= Options.maxTrackedHeight) {
            if (leakCond == BoolExpr.FALSE)
                return Category.FREED_OK;

            if (leakCond.containsUnk())
                return Category.FREED_COND_UNKNOWN;
            
            return Category.FREED_COND_LEAKED;
        }
        
        return Category.FREED_COND_UNKNOWN;
    }
    
    /** determines double frees by looking at pairs of free conditions.
     * dfreeCNF will contain a list of CNF formulas in dimacs format, 
     * one for each dfree condition.
     * @returns a string containing all formulas together. */
    private static List<List<BoolExpr>> findDoubleFrees(VFGNode startNode, 
            Map<SinkLikeNode, BoolExpr> freecond, Slice s) {
        List<List<BoolExpr>> dfrees = new ArrayList<List<BoolExpr>>();
        List<BoolExpr> doublefree = doubleFreeConds(freecond, s);
        for (BoolExpr gdf : doublefree) {
            BoolExpr branchCorr = genBranchCorrelation(gdf);
        	dfrees.add(SATProblem.solve(gdf.and(branchCorr)));
        }
        return dfrees;
    }
    
    /** generates branch correlations for all branches 
     * mentioned in e */
    private static BoolExpr genBranchCorrelation(BoolExpr e) {
        BoolExpr b = BoolExpr.TRUE;
        Set<CFGSwitchNode> done = new HashSet<CFGSwitchNode>();
        for (Atom a : e.getAtoms()) {
            CFGSwitchNode s = a.getTest();
            if (!done.contains(s)) {
                done.add(s);
                if (!s.isIfThenElse()) {
                    BoolExpr clause = BoolExpr.FALSE;
                    for (int i = 0; i < s.numSuccs(); i ++)
                        clause = clause.or(Atom.get(s, i));
                    b = b.and(clause);

                    for (int i = 0; i < s.numSuccs(); i ++)
                        for (int j = i+1; j < s.numSuccs(); j ++)
                                b = b.and(Atom.get(s,i).not().or(
                                    Atom.get(s,j).not()));
                }
            }
        }
        return b;
    }
    
    /** Computes the condition on which a leak might occur. 
     * @returns not ( freecond_1 or ... or freecond_n). */
    private static BoolExpr computeLeakCond(
            Map<SinkLikeNode, BoolExpr> freecond, CoreExpr srcExpr, Slice s) {
        BoolExpr leakcond = BoolExpr.TRUE;
        for (SinkLikeNode fr : freecond.keySet())  {
            CoreExpr e = (fr.getSinkExpr());
            leakcond = leakcond.and(
                filterNC(filterNC(freecond.get(fr).not(), e, s), srcExpr, s));
        }
        return leakcond;
    }

    /** computes conditions for double frees, based on the conditions
     * to reach each free */
    private static List<BoolExpr> doubleFreeConds(
        Map<SinkLikeNode, BoolExpr> freecond, Slice s) {
        List<BoolExpr> doublefree = new LinkedList<BoolExpr>();
        Set<SinkLikeNode> keys = freecond.keySet();
        SinkLikeNode[] arr = new SinkLikeNode[keys.size()];
        arr = keys.toArray(arr);
        for (int k = 0; k < arr.length; k++) {
            BoolExpr gk = freecond.get(arr[k]);
            gk = filterNC(gk, arr[k].getSinkExpr(), s);
            if (gk == BoolExpr.FALSE || gk == null)
                continue;
            for (int j = k+1; j < arr.length; j++) {
                BoolExpr gj = freecond.get(arr[j]);
                gj = filterNC(gj, arr[j].getSinkExpr(), s);
                if (gj == BoolExpr.FALSE || gj == null)
                    continue;
                BoolExpr gboth = simplify(gj.and(gk));
//              if (gboth != BoolExpr.FALSE) 
                doublefree.add(gboth);
            }
        }
        return doublefree;
    }
    
     /** replaces null checks (freed == null) by false
     *  and (freed != null) by true */
    private static BoolExpr filterNC(BoolExpr g, CoreExpr freed, Slice s) {
        if (!Options.filterNullChecks)
            return g;
        
        if (g.isTestLikeNull(s, freed, false))
            return BoolExpr.FALSE;
    
        if (g.isTestLikeNull(s, freed, true))
            return BoolExpr.TRUE;
        
        if (g instanceof And) {
            And ga = (And) g;
            return filterNC(ga.a, freed, s).and(filterNC(ga.b, freed, s));
        }

        if (g instanceof Or) {
            Or ga = (Or) g;
            return filterNC(ga.a, freed, s).or(filterNC(ga.b, freed, s));
        }
            
        if (g instanceof Not) {
            Not ga = (Not) g;
            return filterNC(ga.base, freed, s).not();
        }
            
        return g;
    }
    
    
    /** uses BDDs to simplify formulas */
    public static BoolExpr simplify(BoolExpr glc) {
        if (glc.height < 100) {
            BDDExpr b = glc.toBDD();
            b.simplify();
            glc = b.toGuard();
        }
        return glc;
    }

}
