package crystal.cfg;

import crystal.ast.*;

public class CFGNopNode extends CFGStatNode {

	static enum NopType {Entry, Exit, Empty};
	NopType nt;
	
	CFGNopNode(NopType t) {
		stat = new ExprStat(NumberExpr.one);
		nt = t;
	}
	
	public String toString() {
		return nt.toString(); 
	}
	
	void setSucc(int i, CFGNode n) {
		assert false;
	}
	
	public boolean isEntry() {
		return nt.equals(NopType.Entry);
	}

	public boolean isExit() {
		return nt.equals(NopType.Exit);
	}
}
