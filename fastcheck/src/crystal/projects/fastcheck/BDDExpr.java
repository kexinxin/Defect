package crystal.projects.fastcheck;

import crystal.util.*;

/** Bdd representation of boolean formulas to simplify our guards before
 * calling the SAT solver */
public class BDDExpr {
    Terminal test;
    BDDExpr t;
    BDDExpr f;
    static BDDExpr T = new BDDExpr();
    static BDDExpr F = new BDDExpr();
    static BDDExpr U = new BDDExpr();
    
    static ThreeLevelMap<BDDExpr, BDDExpr, Terminal, BDDExpr> map =
        new ThreeLevelMap<BDDExpr, BDDExpr, Terminal, BDDExpr> ();
    
    static {
        T.test = BoolExpr.TRUE;
        F.test = BoolExpr.FALSE;
        U.test = BoolExpr.UNK;
    }
    
    static BDDExpr getExpr(Terminal a, BDDExpr t, BDDExpr f) {
        if (a == BoolExpr.TRUE )
            return T;
        if (a == BoolExpr.FALSE)
            return F;
        if (a == BoolExpr.UNK)
            return U;
        
        BDDExpr b = map.get(t,f,a);
        if (b == null) {
            b = new BDDExpr();
            b.test = a;
            b.t = t;
            b.f = f;
            map.put(t, f, a, b);
        }
        return b;
    }
    
    static boolean larger(BDDExpr a, BDDExpr b) {
        return a == U || 
        (a.test.id > b.test.id && b != U);
    }
    
    static BDDExpr combineOr(BDDExpr a, BDDExpr b) {
        if (a == U || b == U)
            return U;
        
        if (a.test == BoolExpr.TRUE || b.test == BoolExpr.TRUE)
            return T;
        if (a.test == BoolExpr.FALSE)
            return b;
        if (b.test == BoolExpr.FALSE)
            return a;
        if (a == b)
            return a;
        
        if (a.test == b.test) 
            return getExpr(a.test, combineOr(a.t, b.t), combineOr(a.f, b.f));
        
        if (larger(a,b)) {
            BDDExpr s = a;
            a = b;
            b = s;
        }

        return getExpr(a.test, combineOr(a.t, b), combineOr(a.f, b));
        
    }
    
    static BDDExpr combineAnd(BDDExpr a, BDDExpr b) {
        if (a == U || b == U)
            return U;
        
        if (a.test == BoolExpr.FALSE || b.test == BoolExpr.FALSE)
            return F;
        if (a.test == BoolExpr.TRUE)
            return b;
        if (b.test == BoolExpr.TRUE)
            return a;
        if (a == b)
            return a;

        if (a.test == b.test) 
            return getExpr(a.test, combineAnd(a.t, b.t), combineAnd(a.f, b.f));
        
        if (larger(a,b)) {
            BDDExpr s = a;
            a = b;
            b = s;
        }
        
        return getExpr(a.test, combineAnd(a.t, b), combineAnd(a.f, b));
        
    }
    
    static BDDExpr swapTF(BDDExpr n) {
        if (n == T)
            return F;
        if (n == F)
            return T;
        if (n == U)
            return U;
        return getExpr(n.test, swapTF(n.t), swapTF(n.f));
    }
    
    void simplify() {
        if (t != null) {
            t.simplify();
            while (t.t == t.f && t.t != null)
                t = t.t;
        }
        if (f != null) {
            f.simplify();
            while (f.t == f.f && f.t != null)
                f = f.t;
        }
    }
    
    public String toString() {
        String s = " [ ";
        if (t != null && t != F)
            s += test + ((t != T) ? " && " + t : "");
        
        if (f != null && f != F) {
            if (t != null && t != F)
                s += " || ";
            s += "!" + test + ((f != T) ? " && " + f : "");
        }
        if (t == null && f == null)
            s += test;
        s += "]";
        return s;
    }
    
    public BoolExpr toGuard() {
        if (t == null && f == null)
            return test;
            
        BoolExpr s = null;
        if (t != null)
            s = test.and(t.toGuard());
        else
            s = BoolExpr.FALSE;
        
        if (f != null) 
            s = s.or(test.not().and(f.toGuard()));
            
        return s;
    }
}