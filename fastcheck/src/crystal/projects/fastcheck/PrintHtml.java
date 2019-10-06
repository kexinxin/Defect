
package crystal.projects.fastcheck;

import java.io.*;
import java.util.*;

import crystal.analysis.callgraph.CallGraph;
import crystal.ast.*;
import crystal.cfg.*;
import crystal.general.*;
import crystal.projects.fastcheck.GraphAnalyzer.*;


/** Generates an html file highlighting the error (leak or double free). */
public class PrintHtml {

	public static final String LINE_COLOR = "#666666";
    public static final String STAT_COLOR = "blue";
	public static final String TEST_COLOR = "red";
	public static final String SRC_COLOR = "#FF3030";
	public static final String SINK_COLOR = "#30FF30";
	
    private static boolean printIOError = true;
	
    static void printHtml(Slice s, 
    		List<BoolExpr> leakpath,
            List<BoolExpr> originalLeakpath, 
    		List<List<BoolExpr>> dfrees,
            Set<Atom> alltests,
            boolean genPng) 
    {
    	PrintStream ps = null;
    	String filename = 
            Main.OUTDIR + "/" + s.kind + "/site" + s.name() + ".html";
    	
        try {
            ps = new PrintStream(new FileOutputStream(filename));
        }
        catch(IOException iex) {
            System.out.println("Could not write to file: " + filename);
            System.exit(1);
        }

        /* design is a table as follows
         * +---------+---------+
         * |      header       |
         * +---------+---------+
         * | code    | graph   |
         * |         | legend  |
         * +---------+---------+
        */
        
		/* print the header */
		ps.print("<html>\n<body>\n");
        ps.print("<table border=0>\n<tr>\n<td valign=top colspan=3>\n");
        ps.print("<h2> Site #"+s.name() + ": " + message(s.kind) + 
            " </h2>\n<hr><pre>\n");
        CFGStatNode src = s.sourceSite;
        Statement astat = src.getStatement();
        ps.println(
        		  String.format(" Site #%-3s : ", s.name()) 
            	+ PrintDot.trim(src)
                + "\n Function  : \"" + s.fun + "\""
                + "\n Location  : file \"" + astat.getFile() + "\", line "
                	+ astat.getLeftLine()
                + "\n Details   : " + s.kind.description + "\n" 
                + explainLeak(leakpath) 
                + explainDFree(dfrees));
		
        
        ps.print("</pre>\n</td>\n</tr>\n<tr><td colspan=3><hr></td></tr>");
        /* second row */
		
		/* highlight the error-related code */
		ps.print("<tr><td valign=top><pre>\n");
		code.clear();
        
		try {
            Map<Integer,Integer> hlLines = new TreeMap<Integer,Integer>();
            addCodeFor(hlLines, s.fun, src, true, false);
		    for (VFGNode n : s.nodes) {
		        CFGNode cn = n.getCFGNode();
                if (cn != null) 
                    addCodeFor(hlLines, n.fun, cn, cn == src, n.isSink());
		    }

		    if (leakpath != null)
		        for (BoolExpr ci : leakpath) {
                    CFGSwitchNode sw = ci.getTest();
                    if (sw != null) {
                        int bn = ci.getBranchNum();
                        highlightTest(sw, bn);
                        hlLines.put(sw.getTestSrc().getLeftLine(), bn);
                    }
                }

		    /* print line numbers */
		    for (Function f : code.keySet()) {
		        ps.printf("%-6s /* File %s, line %d */\n", 
		            "", f.getFile(), f.getLeftLine());

                ps.print("</pre><code>");
                /* allow function header to wrap around (too long sometimes) */
                ps.print("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                f.printDecl(ps);
		        ps.println("</code><pre>");

		        int i = f.getLeftLine();
		        for (String str : code.get(f)) 
                    if (hlLines.containsKey(i)) {
                        int b = hlLines.get(i);
                        char c = (b == -1) ?' ' : ((b == 0) ? '+' : '-');
                        ps.printf("<div style='background-color:%s;'>"+ 
                            "<b> %3d:%c</b> %s</div>", 
                            "#f0f0f0", i++, c, str);
                    } else
                        ps.printf("<font color=\"%s\"> %3d: </font>  %s\n", 
                            LINE_COLOR, i++, str);
		        ps.println("\n");
		    }
        } catch (Exception e) {
            if (printIOError) {
                System.err.println("Error in printHtml: " + e.getMessage());
                e.printStackTrace();
                System.err.println("=> continuing ...");
                printIOError = false;
            }
        }

		/* include the dot image */
        ps.print("</pre>\n</td><td></td><td valign=top>");
        if (genPng) 
            ps.print("<img src=\"site" + s.name() +".png\"/>\n");
        if (genPng && !alltests.isEmpty()) {
            ps.println("<pre>\n Legend of conditions: \n  ");
            PrintDot.printTxtLegend(alltests, ps);
            ps.println("</pre>\n");
        }
        ps.print("</td>\n</tr>\n");
        
        ps.print("<tr><td colspan=3><hr></td></tr>\n<tr>");
        
        
        if (s.kind == Category.FREED_COND_LEAKED || !VFGraph.isSource(src)) 
            ps.print("<td colspan=3><h3>Additional Information</h3>\n");
        
        if (!VFGraph.isSource(src)) 
            printHistoryLinks(ps, src);
            
        if (s.kind == Category.FREED_COND_LEAKED) {
            ps.print("<pre>\n Before simplification \n" +
                explainLeak(originalLeakpath)); 
            ps.print("</pre>");
        }
                
        ps.print("</td></tr>\n");
        
        /* print the epilogue */
        ps.print("</table></body>\n</html>\n");
		ps.close();
	}


    private static void addCodeFor(Map<Integer, Integer> hlLines, 
            Function f, CFGNode cn, boolean isSrc, boolean isSink) {
        if (cn instanceof CFGStatNode) {
            CFGStatNode csn = (CFGStatNode)cn;
            String color = null;
            if (isSrc)
                color = SRC_COLOR;
            else if (isSink)
                color = SINK_COLOR;
            else 
                color = STAT_COLOR;
            highlightAST(f, csn.getStatement(), color);
            hlLines.put(csn.getStatement().getLeftLine(), -1);
        }
    }


    private static void printHistoryLinks(PrintStream ps, CFGStatNode src) {
        ps.print("<p> This site is generated from a call to an allocator " +
                "function. ");
        assert src.isCall();
        List<String> ls = new ArrayList<String>();
        for (Function f : CallGraph.getCallees(src)) {
            Set<Slice> lsl = Main.producers.get(f);
            if (lsl != null)
                for (Slice slice : lsl) {
                    ls.add(String.format(
                        " <li> <a href=\"../%s/site%d.html\"> Site #%d </a> \n",
                        slice.kind.dirname, slice.id, slice.id));
                }
        }
        ps.printf(" The function is marked as an allocator" +
                " because of the following %d %s\n ",
            ls.size(), (ls.size() == 1 ? "site:" : "sites:"));
            
        
        ps.println("<ul>");
        for (String str : ls)
            ps.println(str);
        ps.println("</ul>");
    }
    

	private static String message(Category k) {
    	String[] colors = {"green", "red", "slategray", "blueviolet"};
    	return "<b><font color=" + colors[k.level] + ">" + 
    		k.message() + "</font></b>";
    }

	private static String explainLeak(List<BoolExpr> leakpath) {
    	if (leakpath == null || leakpath.isEmpty()) 
    		return "";
    	
        String s = "Leak path \n";
        for (BoolExpr be : leakpath)
        	s += be.longMessage(false);
		return s;
	}

    private static String explainDFree(List<List<BoolExpr>> dfrees) {
    	if (dfrees == null || dfrees.isEmpty()) 
    		return "";
    	
        String s = " Double frees (" + dfrees.size() + ")\n";
        for (List<BoolExpr> free : dfrees) {
        	s += "   Free on path: ";
            if (free != null)
            for (BoolExpr be : free)
                s += be.longMessage(false);
            else
                s += "<?null ---?>\n";
        }
		return s;
	}

	private static void highlightAST(Function f, ASTNode n, String color) 
    {
		List<String> buf = getCode(f);
		String begin = ((color == STAT_COLOR) ? "" : "<b>") + 
        "<font color=\"" + color + "\">";
		String end = "</font>" + ((color == STAT_COLOR) ? "" : "</b>");
		int base = n.getLeftLine() - f.getLeftLine();
    	for (int i = 0; i <= (n.getRightLine() - n.getLeftLine()); i++) {
    		String str = buf.get(base + i);
    		buf.set(base + i, begin + str + end);
    	}
	}

    private static void highlightTest(CFGSwitchNode sw, int branch) {
        /* find the switch by scanning the entire code ... */
        for (Function f : code.keySet())
            for (CFGNode n : f.getCFGNodes())
                if (n == sw)  {
                    highlightAST(f, sw.getTestSrc(), TEST_COLOR);
                    if (!sw.isIfThenElse() && branch > 0)
                        highlightAST(f, sw.getLabelSrc(branch), TEST_COLOR);
                }
    }

	static Map<Function, ArrayList<String>> code = 
    	new HashMap<Function, ArrayList<String>>();
    
	private static ArrayList<String> getCode(Function fun) {
		ArrayList<String> buf = code.get(fun);
		if (buf != null) return buf;
		
		buf = new ArrayList<String>();
		code.put(fun, buf);
		
		Symtab tab = fun.getSymTab().getParent().getParent();
		String dir = extractDir(((FileSymtab)tab).getFileName());
		String file = dir + extractFile(fun.getFile());
		
		int ll = fun.getLeftLine();
		int lc = fun.getLeftCol();
		int rl = fun.getRightLine();
		int rc = fun.getRightCol();
		
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader in = new BufferedReader(isr);
			
			String s;
			for(int i = 1; (s = in.readLine()) != null; i++) {
				if (i < ll || i > rl) continue;
				if (i == ll) s = s.substring(lc, s.length());
				if (i == rc) s = s.substring(0, rc);
				s = s.replace("<", "&lt;");
				s = s.replace("\t", "        ");
				buf.add(s);
			}
		}
		catch (IOException e) {
            if (printIOError) 
                System.err.println("IO error: " + e.getMessage());
            throw new RuntimeException("aborting printHtml");
		}
		
		return buf;
	}

	private static String extractDir(String name) {
		name = name.substring(0, name.lastIndexOf('/') + 1);
		return name.substring(0, name.lastIndexOf('/') + 1);
	}

	private static String extractFile(String name) {
		name = name.substring(name.lastIndexOf('/') + 1, name.length());
		return name.substring(name.lastIndexOf('/') + 1, name.length());
	}
}
