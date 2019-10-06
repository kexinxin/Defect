package crystal.projects.fastcheck;

import org.sat4j.core.*;
import org.sat4j.minisat.*;
import org.sat4j.specs.*;

import java.util.*;
import java.lang.Math;

class SATProblem {

	// ISolver instance, created by isSatisfiable()
	private ISolver solver = null;
	// List of variables, ordered by id, created by isSatisfiable()
	// (note: variable ids start at 1, so indexes into variables must subtract 1)
	private List<BoolExpr> variables = null;
	
	// null indicates that isSatisfiable has not been called
	private Boolean satisfiable = null;
	
	private HashSet<HashSet<BoolExpr>> clauses = new HashSet<HashSet<BoolExpr>>();
	
	public static List<BoolExpr> solve(BoolExpr e) {
		// Do the CNF conversion
		List<List<BoolExpr>> flatcnf = new LinkedList<List<BoolExpr>>();
		BoolExpr cnf = e.toCNF();
		cnf.flatCNF(flatcnf); // convert to CNF
		SATProblem p = new SATProblem();
		List<BoolExpr> solution = solve(p, flatcnf);
		Stats.recordSATFormula(p.variables.size(), e, cnf);
		return solution;
	}
	
    /** returns a list of BoolExpr that satisfies the cnf formula
    or null if it is not satisfiable */
	public static List<BoolExpr> solve(SATProblem p, List<List<BoolExpr>> cnf)  {
		for(List<BoolExpr> clause : cnf) {
			p.addClause(clause);
		}
		try {
			return p.model();			
		} catch (TimeoutException e) {
			System.err.println("model() threw a TimeoutException, SAT solver returning null: " + e);
			return null;
		} catch (ContradictionException e) {
			System.err.println("model() threw a Contradiction Exception, SAT solver returning null: " + e);
			return null;
		}
	}
	
	/** returns a list of BoolExpr that satisfies the SAT problem
	    or null if it is not satisfiable */
	public List<BoolExpr> model() throws TimeoutException, ContradictionException {
		if (satisfiable ==  null) 
			this.isSatisfiable();
		
        if (!satisfiable.booleanValue())
			return null;
		
		int[] m = solver.model();
		ArrayList<BoolExpr> l = new ArrayList<BoolExpr>();
		for(int i = 0; i < m.length; i++) {    
			BoolExpr e = variables.get(Math.abs(m[i])-1);
			if(m[i] < 0) 
				e = e.not();
			l.add(e);
		}
		return l; 
	}
    
	// Add a clause to the SAT problem
	public void addClause(Iterable<BoolExpr> guards) {
		HashSet<BoolExpr> clause = new HashSet<BoolExpr>();
		for(BoolExpr g : guards) {
			clause.add(g);
		}
		clauses.add(clause);
	}
	
	// Is the SAT problem satisfiable?
	public boolean isSatisfiable() throws ContradictionException, TimeoutException {
		// assign an integer to each guard
		Map<BoolExpr,Integer> id = new HashMap<BoolExpr,Integer>();
		variables = new ArrayList<BoolExpr>();
		for(Set<BoolExpr> clause : clauses) {
			for(BoolExpr g : clause) {
				BoolExpr base = baseGuard(g);
				if(!id.containsKey(base)) {
					id.put(base, new Integer(id.size()+1));
					variables.add(base);
				}
			}
		}
		
		solver = SolverFactory.newDefault();
		
		// tell the solver what to expect
		solver.newVar(id.size());
		solver.setExpectedNumberOfClauses(clauses.size());
		
		// convert each clause into an instance of sat4j's VecInt structure
		for(Set<BoolExpr> clause : clauses) {
			VecInt vclause = new VecInt();
			for(BoolExpr g : clause) {
				int gid = id.get(baseGuard(g));
				vclause.push((negate(g) ? -gid : gid));
			}
			solver.addClause(vclause);
		}
		
		boolean s = solver.isSatisfiable();
		satisfiable = new Boolean(s);
		return s;
	}
	
	// ---- For handling "Not" guards
	private BoolExpr baseGuard(BoolExpr g) {
		if(g instanceof Not) {
			return baseGuard(((Not)g).base);
		} else 
			return g;
	}
	
	private boolean negate(BoolExpr g) {
		return (g instanceof Not);
	}

}