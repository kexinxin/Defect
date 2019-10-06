
package crystal.projects.fastcheck;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import crystal.projects.fastcheck.GraphAnalyzer.Category;
import crystal.util.Pair;

/** options for FastCheck */
public class Options {

    /* Options available in the command line */
    
    /** model assignments involving pointer arithmethic */
    static boolean modelPointerArithmetic = true;
    
    /** for calls to alloc wrappers, show where malloc in wrapper originates from */
    static boolean printHistory = false;

    /** print summary of stats in latex format (for building table) */
    static boolean printLaTeXOutput = false;
    
    /** print txt warnings in stdout */
    static boolean printTxt = true;
    
    /** generage html files */
    static boolean genHtml = false;
    
    /** generage dot files */
    static boolean genDot = false;
    
    /** generage png files */
    static boolean genPng = false;
    
    /** show statistics */
    static boolean showStats = false;
    
    /** cases to print based on category index */
    static int directSelect = (1 << Category.FREED_COND_LEAKED.index) | 
        (1 << Category.NOT_FREED_LOCAL_NOT_MAIN.index);
    
    public static final List<String> sourceNames;
    public static final List<Pair<String,Integer>> sinkNames;

    static {
        sourceNames = new ArrayList<String>();
        sourceNames.add("malloc");
        sourceNames.add("calloc");
        sourceNames.add("xmalloc");
        sourceNames.add("xcalloc");
        sourceNames.add("strdup");
        sourceNames.add("strndup");

        sinkNames = new ArrayList<Pair<String,Integer>>();
        sinkNames.add(new Pair<String,Integer>("free",0));
        sinkNames.add(new Pair<String,Integer>("xfree",0));
    }
    
    
    /* Internal options, not available in the command line */
    
    /** use cleanup algorithm to remove unnecessary conditions */
    static boolean cleanupLeakPath = true;
    
    /** exclude from stats/output all sites starting inside an allocator */
    static boolean excludeAllocatorSites = false;
    
    /** filter null checks */
    static boolean filterNullChecks = true;
    
    /** use chaching during pdom */
    static boolean pdomCaching = true;
    
    /** filter tests based on vars in and not in slice */
    static boolean enhancedFilter = true;
    
    /** debugging flag: used to print forward slice, not just the chop of VFG */
    static final boolean printFW = false;
    
    /** debugging flag: used to print all VFG */
    static final boolean printallVFG = false;
    
    /** simplify formulas when possible */
    static boolean simplifyBoolExpr = true;
    
    /** bound on the size of the formulas */
    static int maxTrackedHeight = 30;
    
    /** bound used for printing */ 
    public static int outputHeightLimit = 10;
    
    static boolean writeSATFormulae = true;
    
    /** filters additional arguments used only by this analysis, returns
     * arguments to give to crystal. Stops on first argument that is not 
     * a FastCheck argument. */
    static String[] filterArgs(String[] args) {
        if (args.length == 0) 
            usage();
        
        List<String> argList = new LinkedList<String>();
        int i = 0;
        boolean stubs = true;
        
        while (i < args.length) {
            if (args[i].equals("-help")) {
                usage();
                i++;
                continue;
            }
            
            String[] ai = args[i].split("=", 2);
            if (ai.length == 2) {
                if (ai[0].equals("-parsefrom")) {
                    String fname = ai[1];
                    try {
                        FileInputStream fs = new FileInputStream(fname);
                        InputStreamReader isr = new InputStreamReader(fs);
                        BufferedReader bf = new BufferedReader(isr);
                        String line = null;
                        while ((line = bf.readLine()) != null) 
                            argList.add(line);
                    }
                    catch(IOException ex) {}
                } else if (ai[0].equals("-stubs")) 
                    stubs = getOption(ai);
                else if (ai[0].equals("-latex"))  {
                    Options.printLaTeXOutput = getOption(ai);
                    Options.showStats |= Options.printLaTeXOutput;
                }
                else if (ai[0].equals("-warns")) {
                    setWarnLevel(ai[1]);
                    if (directSelect > 0) 
                        printTxt = true;
                    else
                        printTxt = false;
                }
                else if (ai[0].equals("-sources")) {
                    setSources(ai[1]);
                }
                else if (ai[0].equals("-sinks")) {
                    setSinks(ai[1]);
                }

                else if (ai[0].equals("-srcLikeSites")) 
                    Options.excludeAllocatorSites = getOption(ai);
                else if (ai[0].equals("-srcLikeHistory")) 
                    Options.printHistory = getOption(ai);
                else if (ai[0].equals("-genDot")) 
                    Options.genDot = getOption(ai);
                else if (ai[0].equals("-genPng")) {
                    Options.genPng = getOption(ai);
                    Options.genDot |= Options.genPng;
                } 
                else if (ai[0].equals("-printStats")) 
                    Options.showStats = getOption(ai);
                else if (ai[0].equals("-genHtml")) 
                    Options.genHtml = getOption(ai);
                else if (ai[0].equals("-pointerArithm")) 
                    Options.modelPointerArithmetic = getOption(ai);
                
                else break;
            } 
            else break;
            i++;
        }
        
        if (stubs)
            argList.add("../stubs.c");
        
        for (; i < args.length ; i++)
            argList.add(args[i]);
         
        
        return argList.toArray(new String[argList.size()]);
    }
    
    static void setSources(String a) {
        StringTokenizer st = new StringTokenizer(a, ",");
        sourceNames.clear();
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            sourceNames.add(s);
        }
    }
    
    static void setSinks(String a) {
        StringTokenizer st = new StringTokenizer(a, ",");
        sinkNames.clear();
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            StringTokenizer si = new StringTokenizer(s, ":");
            try {
            String sn = si.nextToken();
            assert si.hasMoreTokens();
            Integer in = Integer.parseInt(si.nextToken());
            sinkNames.add(new Pair<String,Integer>(sn, in));
            } catch(Exception ex) {
                System.out.println(" Incorrect sinks option " + s);
            }
        }
    }

    static void setWarnLevel(String a) {
        StringTokenizer st = new StringTokenizer(a, ",");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.equals("none")) {
                directSelect = 0;
                break;
            }
            if (s.equals("high")) {
                for (Category c : Category.values())
                    if (c.level == 1)
                        directSelect |= (1 << c.index);
            }
            if (s.equals("medium")) {
                for (Category c : Category.values())
                    if (c.level == 2)
                        directSelect |= (1 << c.index);
            }
            if (s.equals("low")) {
                for (Category c : Category.values())
                    if (c.level == 3)
                        directSelect |= (1 << c.index);
            }
            else if (s.equals("all")) {
                directSelect |= 0777;
            }
            else {
                try {
                    int si = Integer.parseInt(s);
                    directSelect |= (1 << si);
                } catch (Exception e) {}
            }
        }
    }
    
    /** extract yes/no from an option */
    private static boolean getOption(String[] ai) {
        if (ai[1].equals("yes"))
            return true;
        else
            if (ai[1].equals("no"))
                return false;
        System.err.println("fastcheck: invalid option " + ai[0] + "=" + ai[1]);
        System.exit(1);
        return false;
    }

    /** prints usage information */
    private static void usage() {
        System.out.printf("Usage: fastcheck [options] file1.i file2.i ... \n");
        System.out.printf("Available options: (default options marked with X) \n");
        System.out.printf(" %-20s %2s %-50s\n","-help","",
          "Show this help message");
        System.out.println("\nSource and sinks:");
        System.out.printf(" %-20s %2s %-50s\n","-sources=s1,...,sn", "", 
        "  List of source nodes. ");
        String srcs = "";
        boolean first = true;
        for (String s : sourceNames) {
            if (first)
                first = false;
            else 
                srcs += ",";
            srcs += s;
        }       
        System.out.printf("     %s\n", "(default sources=" + srcs + ")");
        System.out.printf(" %-20s %2s %-50s\n","-sinks=s1:i1,...,sk:ik", "", 
            "List of sinks s:i (call sink s consummes arg i)");
        String snks = "";
        first = true;
        for (Pair<String,Integer> s : sinkNames) {
            if (first)
                first = false;
            else 
                snks += ",";
            snks += s.fst + ":" + s.snd;
        }
        System.out.printf("     %s\n", "(default sinks=" + snks + ")");
        
        System.out.println("\nStubs and Input:");
        System.out.printf(" %-20s %2s %-50s\n","-stubs=yes", "X", 
          "Load ../stubs.c file");
        System.out.printf(" %-20s %2s %-50s\n","-parsefrom=<file.txt>",
            "",
          "Extract from file.txt the file names to analyze");
        
        System.out.println("\nWarnings and Output:");
        System.out.printf(" %-20s %2s %-50s\n","-warns=clist","", 
            "Print in stdout warnings that match clist.");
        System.out.printf("   where clist=c1,...,cn is a " +
                "list of indices and classifications: \n");
        System.out.printf("     Indices identify a single bug category: \n");
        for (Category c : GraphAnalyzer.Category.values()) 
            System.out.printf(" %-20s %2s %-50s\n","      " + c.index, 
                (c.index == 1 || c.index == 2) ? "X" : "", 
                "  for the category " + c.dirname);
        
        System.out.printf("     Classifications group several of them:\n");
                
        System.out.printf("    %-19sX %-50s\n","   high  ","  matches " +
            Category.NOT_FREED_LOCAL_NOT_MAIN.index +
                " and " +
                Category.FREED_COND_LEAKED.index);
        System.out.printf("    %-20s %-50s\n","   medium ", "  matches " +
            Category.NOT_FREED_NOT_LOCAL.index +
                " and " +
                Category.NOT_FREED_LOCAL_MAIN.index);
        System.out.printf("    %-20s %-50s\n","   low ",  "  matches " +
            Category.FREED_COND_UNKNOWN.index +
                " and " +
                Category.FREED_IN_NON_LOCAL.index);
        
        System.out.printf("    %-20s %-50s\n","   all ",  "  matches all");
        System.out.printf("    %-20s %-50s\n","   none ", "  print no messages");
        System.out.println();
        
        System.out.printf(" %-20s %2s %-50s\n","-printStats=yes",
            (Options.showStats ? "X": ""),
        "Print overall statistics (# warnings, time) ");
        System.out.printf(" %-20s %2s %-50s\n","-latex=yes",
            (Options.printLaTeXOutput ? "X": ""),
          "Generate summary in latex format");
        System.out.printf(" %-20s %2s %-50s\n","-genHtml=yes",
            (Options.genHtml ? "X": ""),
          "Generate html reports (source code highlighting)");
        System.out.printf(" %-20s %2s %-50s\n","-genDot=yes",
            (Options.genDot ? "X": ""),
          "Generate dot file with slice subgraph");
        System.out.printf(" %-20s %2s %-50s\n","-genPng=yes",
            (Options.genPng ? "X": ""),
          "Generage png images using dot (requires");
        System.out.printf(" %-20s %2s %-50s\n", "",
            "", 
            " to have dot installed and in path)");
        System.out.println("\nAllocators/producers: ");
        
        System.out.printf(" %-20s %2s %-50s\n","-srcLikeSites=yes", 
            (!Options.excludeAllocatorSites ? "X": ""),
            "Analyze allocator-like calls as source sites");
        System.out.printf(" %-20s %2s %-50s\n","-srcLikeHistory=yes", 
            (Options.printHistory ? "X": ""),
            "Print source of allocator-like sites");
        
        System.out.println("\nOther options: ");
        System.out.printf(" %-20s %2s %-50s\n","-pointerArithm=yes",
            (Options.modelPointerArithmetic ? "X": ""),
          "Approximate pointer arithmetic assignments");
        
        System.exit(0); 
    }
    

}
