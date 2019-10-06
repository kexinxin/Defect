package crystal.analysis.pointer;

import java.util.*;

import crystal.ast.*;
import crystal.cfg.*;
import crystal.core.*;
import crystal.general.*;
import crystal.analysis.callgraph.*;

/**
 *   Flow-Insensitive Unification-Based Pointer Analysis
 *  - For each pointer assignment:
 *  -             Steensgaard:      *e1 = e2      => unify (*e1, e2)
 *  -             One-level flow:   *e1 = e2      => unify (**e1, *e2) 
 *  - For pointer arithmetic exprs:  e1 = e2 + i  => unify (e1, e2) 
 *  - Recursive unifications:       unify(e1, e2) => unify(*e1, *e2)
 */
public class PointerAnalysis {

	/** If true, perform a Steensgaard-style analysis. Otherwise
	 *  perform a one-level flow (OLF) analysis. For OLF, only the 
	 *  unification part is performed; points-to sets are not computed.
	 *  Therefore, function pointer disambiguation via points-to analysis 
	 *  is only done when running Steensgaard. 
	 *  Region partitioning is available for both Steensgaard and OLF. */
	public static boolean STEENSGAARD = false;
	
	/** Control the treatment of structure fields:
	 *  I = field-insensitive; B = field-based; S = field-sensitive. */
	public static char FIELD_TREATMENT = 'S';

	/** If true, collapse fields when structs are used in
	 *  a type-inconsistent manner. */
	public static boolean CHECK_TYPE_CONSISTENCY = false;
	
	 /** If true, enable pending lists. */ 
	public static boolean PENDING_LISTS = true;

	 /** If true, include assignments of shadow parameters. */ 
	public static boolean INCLUDE_SHADOWS = false;


	/** The worklist containing pairs of expressions to unify. */
	public static UnifyList worklist = new UnifyList();
	
	/** True if the pointer analysis has been run. */
	public static boolean available = false;
	
	
	/** Run the pointer analysis algorithm. */
	public static void run() {
		if (available)
			return;
		
		if (Options.VERBOSITY >= 3)
			System.out.println();

		if (Options.VERBOSITY >= 3) 
			System.out.println("   Switches on: " + options());
		
		if (FIELD_TREATMENT == 'B' && !CoreExpr.KEEP_FIRST_FIELD)
			throw new RuntimeException("Field-based analysis requires " +
					" keeping the first field.");

		if (isFieldBased()) 
			fmap = new HashMap<Symbol,CoreExpr>();

		for (Function fsym : Symtab.getFunctions()) 
			ConstraintBuilder.builder.collectConstraints(fsym);
		
		/* Run the analysis algorithm */
		fillEquivalenceClasses();
		performUnifications();

		if (STEENSGAARD)
			collectFunctionPtSets();
		
		if (Options.VERBOSITY >= 20) 
			for(List<CoreExpr> set : buildSets(EQ_CLASSES).values())
				if (set.size() > 1)
					System.out.println(" " + set);
		
		available = true;
	}	

	private static void fillEquivalenceClasses() {		
		/* Fill core expression representing function symbols */
		List<CoreAddrExpr> v = new ArrayList<CoreAddrExpr>();
		for (CoreAddrExpr e : CoreExpr.varMap.values()) {
			Symbol sym = e.getSymbol();
			if (sym.isFunction() && sym.isDefined())
				v.add(e);
		}

		for (CoreAddrExpr e : v) init(e);

		/* Fill all of the other expression nodes. */
		for (CoreExpr e : CoreExpr.allExprs.values()) init(e);
		for (CoreExpr e : CoreExpr.varMap.values())   init(e);
		for (CoreExpr e : CoreExpr.allocMap.values()) init(e);
	}
	

	private static void init(CoreExpr e) {
		PTInfo.fill(e);
		if (FIELD_TREATMENT == 'S' && CHECK_TYPE_CONSISTENCY)
			PTInfo.enforceTypeConsistency(e);
	}

	public static long performUnifications() {
		UnifyList tmplist = new UnifyList();
		for (long i = 0; ; i++) {
			boolean found = false;
			while (!worklist.isEmpty() && !found) 
				found = process(worklist.removeHead(), tmplist);
			
			/* note: worklist will be empty upon return */
			if (!found) return i;
			
			worklist.append(tmplist); 
		}
	}

	static boolean process(UnifyList.Elem crt, UnifyList list) {
		assert crt.expr1 != null && crt.expr2 != null;
		if (useless(crt.expr1, crt.expr2)) 
			return false;

		if (PointerAnalysis.PENDING_LISTS &&
				crt.pending && !crt.expr2.getInfo().hasPointers()) {
			list.addTail(crt);
			return false;
		}
		
		if (STEENSGAARD || !crt.onelevel) 
			PTInfo.unify(crt.expr1, crt.expr2);
		else 
			PTInfo.assign(crt.expr1, crt.expr2);

		return true;
	}

	private static boolean useless(CoreExpr e1, CoreExpr e2) {
		return (e2.isNumConstant() ||
				(e1.ptinfo != null && e2.ptinfo != null &&
				 e1.getRep() == e2.getRep()));
	}
	
	public static void addAssign(CoreExpr e1, CoreExpr e2) {
		assert e1 != null && e2 != null;
		if (useless(e1, e2)) return;
		worklist.addHead(e1, e2, true, true);
	}

	public static void addOpAssign(CoreExpr e1, CoreExpr e2) {
		assert e1 != null && e2 != null;
		if (useless(e1, e2)) return;
		worklist.addHead(e1, e2, true, false);
	}

	public static void addUnify(CoreExpr e1, CoreExpr e2) {
		assert e1 != null && e2 != null;
		if (useless(e1, e2)) return;
		worklist.addHead(e1, e2, false, false);
	}

	/* Needed for field-based analyses. */
	static Map<Symbol, CoreExpr> fmap;
	
	private static Map<CoreExpr,List<Function>> fptsets = 
		new HashMap<CoreExpr,List<Function>>();
	
	private static void collectFunctionPtSets() {
		for (CoreAddrExpr e : PTInfo.funSymbols) {
			CoreExpr rep = e.getRep();
			List<Function> v = fptsets.get(rep);
			if (v == null) {
				v = new ArrayList<Function>();
				fptsets.put(rep, v);
			}
			v.add((Function)e.getSymbol());
		}
	}
	
	public static List<Function> getCallees(CFGCallNode node) {
		assert STEENSGAARD : 
			"Call to 'getCallees' is allowed only for Steensgaard analysis.";
			
		List<Function> v = new ArrayList<Function>();
		
		/* Direct function call. */
		CallExpr call = node.getCall();
		Function fsym = call.getFunSymbol();
		if (fsym != null) {
			v.add(fsym);
			return v;
		}
		
		/* Call to an allocation function. */
		CoreExpr e = CoreExpr.allocMap.get(call);
		if (e != null) 
			return v;
				
		/* Indirect call through a function pointer. */
		e = node.getFun().getRep();
		List<Function> set = fptsets.get(e);
		if (set != null)
			for (Function fun : set) 
				if (CallGraph.matchArgs(fun, node))
					v.add(fun);

		return v;
	}

	static boolean matchArgs(Function f, CFGCallNode callsite) {
		int p = f.numParams();
		int a = callsite.numArgs();
		if (a < p)
			return false;
		else if (a == p)
			return true;
		else
			return (p > 1) && f.getParam(p-1) == Symbol.varargs;
	}

	private static String options() {
		List<String> s = new ArrayList<String>();
		s.add(STEENSGAARD ? "Steensgaard" : "one-level-flow");
		
		switch(FIELD_TREATMENT) {
		case 'I' : s.add("field-insensitive"); break;
 		case 'B' : s.add("field-based"); break;
 		case 'S' : s.add("field-sensitive"); break;
 		default: assert false : "invalid field option";
		}
		
		if (FIELD_TREATMENT == 'S' && CHECK_TYPE_CONSISTENCY) 
			s.add("with type consistency");
		return s.toString();
	}

	public static final boolean EQ_CLASSES = true;
	public static final boolean PT_SETS = false;
	
	/* b = true  => full equivalence classes */
	/* b = false => points-to sets only */
	public static void printStats(boolean b) {
		String name = b ? "Equivalence class" : "Points-to set";
		Map<CoreExpr,Integer> map = buildCounts(b);
		Map<Integer, Integer> buckets = new HashMap<Integer,Integer>();
		for (Integer i : map.values()) {
			Integer buck = buckets.get(i);
			int v = buck == null ? 0 : buck.intValue();
			buckets.put(i, new Integer(v+1));
		}
		
		List<Integer> list = new ArrayList<Integer>();
		for (Integer i : buckets.keySet()) list.add(i);
		Collections.sort(list);
		System.out.println("   " + name + " sizes:  " + list);
		List<Integer> counts = new ArrayList<Integer>();
		for (Integer i : list) counts.add(buckets.get(i));
		System.out.println("   " + name + " counts: " + counts);
	}
	
	private static Map<CoreExpr,Integer> buildCounts(boolean b) {
		Map<CoreExpr,Integer> map = new HashMap<CoreExpr,Integer>();
		for (CoreExpr e : CoreExpr.allExprs.values()) addCount(e, map, b);
		for (CoreExpr e : CoreExpr.varMap.values())   addCount(e, map, b);
		for (CoreExpr e : CoreExpr.allocMap.values()) addCount(e, map, b);
		return map;
	}

	/* b = true  => full equivalence classes */
	/* b = false => points-to sets only */
	private static void addCount(CoreExpr e, 
			Map<CoreExpr,Integer> map, boolean b) 
	{
		if (e.toString().contains("@dummy")) return;
		if (b || e.isAddrConstant() || e.isStringConstant() || e.isAlloc()) {
			CoreExpr r = e.getRep();
			Integer c = map.get(r);
			int v = c == null ? 0 : c.intValue(); 
			map.put(r, new Integer(v+1));
		}
	}

	/* b = true  => full equivalence classes */
	/* b = false => points-to sets only */
	public static Map<CoreExpr,List<CoreExpr>> buildSets(boolean b) {
		Map<CoreExpr,List<CoreExpr>> map = 
			new HashMap<CoreExpr,List<CoreExpr>>();
		for (CoreExpr e : CoreExpr.allExprs.values()) addSet(e, map, b);
		for (CoreExpr e : CoreExpr.varMap.values())   addSet(e, map, b);
		for (CoreExpr e : CoreExpr.allocMap.values()) addSet(e, map, b);
		return map;
	}
	
	private static void addSet(CoreExpr e, 
			Map<CoreExpr,List<CoreExpr>> map, boolean b) 
	{
		if (e.toString().contains("@dummy")) return;
		if (b || e.isAddrConstant() || e.isStringConstant() || e.isAlloc()) {
			CoreExpr r = e.getRep();
			List<CoreExpr> pt = map.get(r);
			if (pt == null) {
				pt = new ArrayList<CoreExpr>();
				map.put(r, pt);
			}
			pt.add(e);
		}
	}

	public static boolean isFieldBased() {
		return FIELD_TREATMENT == 'B';
	}

	public static boolean isFieldInsensitive() {
		return FIELD_TREATMENT == 'I';
	}

	public static boolean checkConsistency() {
		return FIELD_TREATMENT == 'S' && CHECK_TYPE_CONSISTENCY;
	}
}