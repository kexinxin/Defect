package crystal.projects.fastcheck;

import java.io.*;
import java.util.*;

import crystal.analysis.dataflow.ReachingDefs;
import crystal.analysis.pointer.*;
import crystal.general.*;
import crystal.projects.fastcheck.GraphAnalyzer.Category;
/** Main class for FastCheck */
public class Main {
    
    /** reaching definition analysis */
    static ReachingDefs reachDef;
    
    /** dependence graph datastructure */
    static VFGraph graph;
    
    static public void main(String[] args) throws Exception {
        crystal.general.Options.PROGNAME = "fastcheck";
    	
        runCrystal(args);
        
        reachDef = new ReachingDefs();
        graph = new VFGraph(); 
        
        Stats.totaltime.begin();
        Stats.Timer t = new Stats.Timer("Building graph");

        t.begin();
        System.out.print("Building graph ...");
        constructUnguardedGraph();
        t.finish();
        System.out.printf("done. (%.2f s)\n", t.total/1e9);
        
        t = new Stats.Timer("Analyzing sites");
        t.begin();
        
        if (Options.genHtml || Options.genDot)
            prepareOutputFiles();
        
        System.out.print("Analyzing sites ...");
        GraphAnalyzer.findAndPrintLeaks(graph);
        t.finish();
        System.out.printf("done. (%.2f s)\n", t.total/1e9);
        
        Stats.totaltime.finish();
        System.out.printf("Total fastcheck time... (%.2f s)\n", 
            Stats.totaltime.total/1e9);
        printStats();
    }

    /** set options for crystal, and run it */
    private static void runCrystal(String[] args) {
        crystal.general.Options.CFG = true;
        crystal.general.Options.CALL_GRAPH = true;
        crystal.general.Options.POINTER_ANALYSIS = true;
        PointerAnalysis.STEENSGAARD = true;        
        crystal.Main.main(Options.filterArgs(args));
        System.out.println();
    }
   

    /** Runs reaching definition analysis. Using result of the analysis
     * generates the unguarded value flow graph (no guards are computed) */
	private static void constructUnguardedGraph() {
        for (Function func : Symtab.getFunctions()) {
            if (!func.isInit()) {
                Stats.totalFun ++;
                Stats.reachdefs.begin();
                reachDef.analyze(func);
                Stats.graph.begin(Stats.reachdefs.finish());
                graph.addFunDependences(func, reachDef.getResult());
                Stats.graph.finish();
            }
        }
        
	}
    
    /** source producers are functions that behave like alloc wrappers  */
    static Map<Function, Set<Slice>> producers = 
        new HashMap<Function, Set<Slice>>();
    
    /** consumers are functions that behave like free wrappers */
    static Set<Function> consumers = new HashSet<Function>();
    
    private static void printStats() {
        System.out.println();
        
        if (Options.genHtml || Options.genDot)
            System.out.println("Generated results in dir '"+OUTDIR+"'. ");
        
        if (crystal.general.Options.VERBOSITY >= 3)
            crystal.Main.printMemoryUsage(); 

        if (Options.showStats) {
            if (VFGraph.readExprIgnored > 0)
                System.out.println(" Expr ignored " + VFGraph.readExprIgnored);

            if (Options.printLaTeXOutput)
                Stats.printLaTeXTableRow();

            Stats.printCounts();
        }
        
        if (Options.writeSATFormulae)
            Stats.writeSATFormulae(OUTDIR + 
                System.getProperty("file.separator") + "sat_formulae");
        
    }
    static final String OUTDIR = "output";
    
    /** creates directory for all output */
    private static void prepareOutputFiles() {
        prepareDir(OUTDIR);
        
        for (Category w : Category.values()) 
            prepareDir(OUTDIR + System.getProperty("file.separator") 
                + w.dirname);
    }

    private static void prepareDir(String name) {
        File f = new File(name);
        if (!f.exists()) {
            if (!f.mkdir()) 
                System.out.println("Couldn't create '" + name 
                    + "' dir... quiting...");
        } else
            for (File fi : f.listFiles())
                fi.delete();

    }
    

}