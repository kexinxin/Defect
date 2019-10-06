package crystal.ast;

import java.util.*;

public class SwitchStat extends Statement {
	private static final long serialVersionUID = 3257284734064015667L;

	Expression e;
    Statement s;
    
    public SwitchStat(Expression e, Statement s) {
    	this.e = e;
    	this.s = s;
    }

    public Expression getExpr() {
    	return e;
    }
    
    public Statement getStat() {
    	return s;
    }
    
    /* Returns a map from case expressions to their code.
     * Returns null when cases fall through. */
    public Map<Expression,List<Statement>> getCases() {
    	Map<Expression,List<Statement>> list = 
    			new HashMap<Expression,List<Statement>>();
    	
    	if (!(s instanceof BlockStat))
    		return null;
    	
    	BlockStat bs = (BlockStat)s;
    	List<Statement> currentCase = null;
    	for (Statement crt : bs.body) {
    		if (crt instanceof CaseLabelStat) {
    			/* check fall-through cases */
    			if (currentCase != null)
    				return null;
    			
    			CaseLabelStat ls = (CaseLabelStat)crt;
    			Statement inside = ls.labeled;
    			
    			/* Check fall-through cases */
    			if (inside instanceof CaseLabelStat)
    				return null;
    			
    			currentCase = new ArrayList<Statement>();
    			currentCase.add(inside);
    			list.put(ls.guard, currentCase);
    		}
    		else if (crt instanceof BreakStat) {
    			/* End the current case */
    			currentCase = null;
    		}
    		else {
    			if (currentCase == null)
    				return null;
    			currentCase.add(crt);
    		}
    	}
    	
    	return list;
    }
    
	/* Child expressions interface */
	public int numExprKids() {
		return 1;
	}

	public Expression getExprKid(int i) {
		assert (i < numExprKids());
		return e;
	}

	public void setExprKid(int i, Expression e) {
		assert (i < numExprKids());
		this.e = e;
	}

	/* Child statements interface */
	public int numStatKids() {
		return 1;
	}
    
	public Statement getStatKid(int i) {
		assert (i < numStatKids());
		return s;
	}

	protected void setStatKid(int i, Statement s) {
		assert (i < numStatKids());
		this.s = s;
	}

	public <S,T> S accept(StatVisitor<S,T>  v, T t) {
		return v.visit(this, t);
	}
}
