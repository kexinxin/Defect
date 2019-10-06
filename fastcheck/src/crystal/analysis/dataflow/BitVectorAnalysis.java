package crystal.analysis.dataflow;

import crystal.cfg.*;
import crystal.util.*;

public abstract class BitVectorAnalysis<E> 
	extends DataflowAnalysis<IBitVector<E>>
{
	Indexer<E> index;
	
	private boolean may;
	
	static final boolean MAY = true;
	static final boolean MUST = false;
	
	public BitVectorAnalysis(String s, boolean f, boolean m) {
		super(s,f);
		may = m;
	}

    protected boolean merge(IBitVector<E> bv, CFGNode n)  {
		IBitVector<E> old = res.get(n);
		if (old == null) {
			res.put(n, bv.copy());
			return true;
		}
		return may ? old.or(bv) : old.and(bv);
	}
}
