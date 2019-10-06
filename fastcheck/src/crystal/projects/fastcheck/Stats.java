
package crystal.projects.fastcheck;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.lang.StringBuffer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import crystal.projects.fastcheck.GraphAnalyzer.Category;


public class Stats {
    
    static Counter fwsliceCounter = new Counter();
    static Counter sliceCounter = new Counter();
    static Counter guardCounter = new Counter();
    static Counter reportedCounter = new Counter();
    
    static int totalFun = 0;
    static int numAllocs = 0;
    static int numSlices = 0;
    static int numCallsToAW = 0;
    
    static Timer reachdefs = new Timer("Reaching Defs");
    static Timer graph = new Timer("Building VFG");
    static Timer slicing = new Timer("Slicing");
    static Timer guards = new Timer("Computing Guards");
    static Timer satsolver = new Timer("Calls to SAT solver");
    static Timer printing = new Timer("Printing");
    static Timer printingDot = new Timer(" Printing DOT files");
    static Timer printingPNG = new Timer(" Generating PNG files");
    static Timer printingHtml = new Timer(" Printing HTML files");
    static Timer totaltime = new Timer("Total");

    /** calls tot he sat solver */
    static int SATSolvers = 0;

    static Map<Category, Integer> count = new HashMap<Category,Integer>();
    
    static void incCount(Category w) {
        Integer i = count.get(w);
        count.put(w, i == null ? 1 : i+1);
    }
        
    static void printCounts() {
        System.out.println();
        
        System.out.println();
        System.out.println("Allocation site summary: ");
        System.out.printf("\n   %-24s  %s\n", "Allocation kind", "Count");
        System.out.println("   --------------------------------");
        int tot  = 0;
        for (Category w : Category.values()) {
            Integer i = count.get(w);
            int ii = (i == null) ? 0 : i;
            System.out.printf("   %-26s: %4d\n", w, ii);
            tot += ii;
        }        

        System.out.println("   --------------------------------");
        System.out.printf("   %-26s: %4d\n\n", "total" , tot);
        
        Integer icl = count.get(Category.FREED_COND_LEAKED);
        if (SATSolvers != (icl == null ? 0 : icl))
            System.out.printf("   %-24s: %4d\n\n", "Calls to SAT Solver" , 
                SATSolvers);
        
        
        System.out.println("Size statistics:");
        System.out.println("   VFG graphs total = " + VFGNode.sid + " nodes ");
        System.out.println("   Forward Slice    = " + fwsliceCounter);
        System.out.println("   FW + BW Slice    = " + sliceCounter); 
        System.out.println("   Guards           = " + guardCounter);
        System.out.println("   Reported         = " + reportedCounter);
        System.out.println();
        
        System.out.println("Other statistics:");
        System.out.println("   Total slices            = " + numSlices);
        System.out.println("   Alloc sites             = " + numAllocs);
        System.out.println("   Calls to alloc wrappers = " + numCallsToAW);
        System.out.println("   Alloc wrappers          = " + Main.producers.keySet().size());
        System.out.println("   Free  wrappers          = " + Main.consumers.size());
        System.out.println("   Total functions         = " + totalFun);
      

        System.out.println();
        
        System.out.println("Time breakdown: ");
        reachdefs.printPretty();
        graph.printPretty();
        slicing.printPretty();
        guards.printPretty();
        satsolver.printPretty();
        printing.printPretty();
        if (Options.genDot) 
            printingDot.printPretty();
        
        if (Options.genPng)
            printingPNG.printPretty();
        
        if (Options.genHtml)
            printingHtml.printPretty();
        
        System.out.println("-------------------");
        totaltime.printPretty();
        System.out.println();
            
    }
    
    public static void printLaTeXTableRow() {
        System.out.print(" [program] & [size] & ");
        // timers;
        Timer.printSeveral(reachdefs, graph);
        slicing.printSimple();
        Timer.printSeveral(guards, satsolver);
        
        // total cases (allocations + wrappers)
        int tot  = 0;
        for (Category w : Category.values()) {
            Integer i = count.get(w);
            tot += (i == null) ? 0 : i;
        }
        System.out.printf(" %4d &", tot);
        // total warnings + empty for 
        // bugs/false positive colums (manual inspection)
        Integer i = count.get(Category.FREED_COND_LEAKED);
        Integer j = count.get(Category.NOT_FREED_LOCAL_NOT_MAIN);
        System.out.printf(" %4d & [bug] & [fp] & ", 
            ((i != null) ? i : 0) +
            ((j != null) ? j : 0));
        
        // each bug kind
        i = count.get(Category.NOT_FREED_NOT_LOCAL);
        System.out.printf(" %4d & ", (i != null) ? i : 0);
        
        i = count.get(Category.NOT_FREED_LOCAL_MAIN);
        System.out.printf(" %4d & ", (i != null) ? i : 0);
        
        i = count.get(Category.NOT_FREED_LOCAL_NOT_MAIN);
        System.out.printf(" {\\bf %4d} & ", (i != null) ? i : 0);
        
        i = count.get(Category.FREED_OK);
        int res = (i != null) ? i : 0;
        System.out.printf(" %4d & ", res);
        
        i = count.get(Category.FREED_COND_LEAKED);
        System.out.printf(" {\\bf %4d} & ", (i != null) ? i : 0);
        
        i = count.get(Category.FREED_COND_UNKNOWN);
        System.out.printf(" %4d & ", (i != null) ? i : 0);

        i = count.get(Category.FREED_IN_NON_LOCAL);
        System.out.printf(" %4d \\\\ ", (i != null) ? i : 0);
    }
    
    static class Timer {
        long start = 0;
        long total = 0;
        boolean measuring = false;
        String name;
        
        Timer(String n) {
            name = n;
        }
        
        long begin() {
            assert !measuring;
            long t = System.nanoTime();
            start = t;
            measuring = true;
            return t;
        }
        
        long begin(long t) {
            assert !measuring;
            start = t;
            measuring = true;
            return t;
        }
        
        long finish() {
            assert measuring;
            long t = System.nanoTime();
            total += (t - start);
            measuring = false;
            return t;
        }
        
        void printPretty() {
            System.out.printf("  %-22s : %.2f s\n", name, total/1e9);
        }
        
        void printSimple() {
            if (total == 0)
                System.out.printf("  0 & ");
            else if (total < 1e7)
                System.out.printf("  < 0.01 & ");
            else
                System.out.printf("  %.2f & ", total/1e9);
        }
        
        static void printSeveral(Timer... timers) {
            long tt = 0;
            for (Timer t : timers) 
                tt+= t.total;
            if (tt == 0)
                System.out.printf("  0 & ");
            else if (tt < 1e7)
                System.out.printf("  < 0.01 & ");
            else
                System.out.printf("  %.2f & ", tt/1e9);
        }
    }        

    static class Counter {
        int max = 0;
        int min = -1;
        int tot = 0;
        int con = 0;
        
        public void update(int n) {
            if (n > max) 
                max = n;
            if (n < min || min == -1)
                min = n;
            tot+= n;
            con++;
        }
        public String toString() {
            if (con == 0)
                return " -- none --";
            return String.format(" max %d, min %d, total %d, avg %.2f",
                max, min, con, (float)tot/(float)con);
        }
    }
    
    static Map<Integer,Timer> debugTimers = new HashMap<Integer,Timer>();
    
    public static Timer getTimer(int i) {
        Timer t = debugTimers.get(i);
        if (t == null) {
            t = new Timer("Case " + i);
            debugTimers.put(i, t);
        }
        return t;
    }
    
    static StringBuffer satFormulae = new StringBuffer();
    
    public static void recordSATFormula(int nvariables, BoolExpr e, BoolExpr cnf) {
    	int old = Options.outputHeightLimit ;   	
        Options.outputHeightLimit = 1000;   	
    	
    	// begin hack
    	// The following is a quick hack to remove redundant terms from the CNF
    	List<List<BoolExpr>> flat = new LinkedList<List<BoolExpr>>();
    	cnf.flatCNF(flat); // the flat cnf function removes duplicate terms
    	// convert flat cnf back into a BoolExpr
    	BoolExpr simplified = null;
    	for(List<BoolExpr> clause : flat) {
    		BoolExpr clause_prime = null;
    		for(BoolExpr term : clause)
    		{
    			if(clause_prime == null) 
    				clause_prime = term;
    			else 
    				clause_prime = clause_prime.or(term);
    		}
    		if(simplified == null) 
    			simplified = clause_prime;
    		else
    			simplified = simplified.and(clause_prime);
    	}
    	if(simplified != null) cnf = simplified;
    	// end hack
    	
    	satFormulae.append(nvariables + " " + e + "\n");
    	satFormulae.append(nvariables + " " + cnf + "\n");
        Options.outputHeightLimit = old;   	
    }
        
    public static void writeSATFormulae(String filename) {
    	try {
    		BufferedWriter b = new BufferedWriter(new FileWriter(filename));
    		b.write(satFormulae.toString());
    		b.close();
    	} catch(IOException e) {
    		System.err.println("Error writing SAT formula file:" + e);
    	}
    }
}
