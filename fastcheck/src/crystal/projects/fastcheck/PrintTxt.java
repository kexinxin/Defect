package crystal.projects.fastcheck;

import java.io.*;
import java.util.*;

import crystal.analysis.callgraph.*;
import crystal.cfg.*;
import crystal.general.Function;

/** generates warning messages for standard output */
public class PrintTxt {
    
    static boolean first = true;
    
    /** prints conditional leak warning */
    public static void printConditional(Slice slice, List<BoolExpr> leakpath) {
        CFGNode src = slice.source.getCFGNode();
        PrintStream o = System.out;
        String ins = PrintDot.trim(src);
        String loc = PrintDot.location(src, true);
        if (first) {
            o.println();
            o.println();
            first = false;
        }
        o.printf("Error: %s: allocation leaks memory\n" , loc);
        o.printf( "     %s\n", ins);
        printSource(src);
        o.print("Leak path: ");
        for (BoolExpr c : leakpath) 
            o.printf("%s           ", c.longMessage(false));
        o.println();
    }    

    /** prints never freed warning */
    public static void printNoFree(Slice slice) {
        CFGNode src = slice.source.getCFGNode();
        String ins = PrintDot.trim(src);
        String loc = PrintDot.location(src, true);
        if (first) {
            System.out.println();
            System.out.println();
            first = false;
        }
        System.out.printf("Error: %s: allocation never freed:\n" , loc);
        System.out.printf( "     %s\n", ins);
        printSource(src);
        System.out.println();
    }
    
    /** prints other kinds of warnings */
    public static void printOther(Slice slice) {
        CFGNode src = slice.sourceSite;
        String ins = PrintDot.trim(src);
        String loc = PrintDot.location(src, true);
        if (first) {
            System.out.println();
            System.out.println();
            first = false;
        }
        System.out.printf("Error: %s: %s\n" , loc, slice.kind.description);
        System.out.printf( "     %s\n", ins);
        printSource(src);
        System.out.println();
    }
    
    
    /** prints source history of allocator site */
    private static void printSource(CFGNode src) {
        if (Options.printHistory && !VFGraph.isSource(src)) {
            assert src.isCall();
            System.out.println( "     (site from allocator function ");
            List<String> ls = new ArrayList<String>();
            for (Function f : CallGraph.getCallees((CFGCallNode) src)) {
                Set<Slice> lsl = Main.producers.get(f);
                if (lsl != null)
                    for (Slice s : lsl) {
                        Formatter fo = new Formatter();
                        fo.format("       %-30s %s\n",
                            PrintDot.trim(s.source.getCFGNode()),
                            PrintDot.location(s.source.getCFGNode(), true));
                        ls.add(fo.toString());
                    }
            }
            System.out.printf( "      derived from the following %d %s\n ",
                ls.size(), (ls.size() == 1 ? "site:" : "sites:"));
                
            for (String s : ls)
                System.out.print(s);
            System.out.println( "      )");

        }
    }
}
    
