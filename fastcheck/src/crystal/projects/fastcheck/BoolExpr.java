package crystal.projects.fastcheck;

import crystal.ast.*;
import crystal.cfg.*;
import crystal.core.*;
import crystal.util.*;

import java.util.*;

/** Guard formulas */
public abstract class BoolExpr {
    
    int height = 0;
    
    /** true constant */
    public static ConstantAtom TRUE = new ConstantAtom(" true ");
    
    /** false constant */
    public static ConstantAtom FALSE = new ConstantAtom(" false ");
    
    /** special constant (used to denote loops and uninit data) */
    public static ConstantAtom TMP = new ConstantAtom(" tmp ");
    
    /** special constant of unknown */
    public static ConstantAtom UNK = new ConstantAtom(" ? ");
    
    private static int uid = 1;
    public static ConstantAtom freshUnknown() {
        return new ConstantAtom("unk" + uid++);
    }
    
    static {
        UNK.height = Options.maxTrackedHeight + 1;
    }

    public static boolean isTrue(CFGNode cond) {
        CoreExpr e = cond.getCoreKid(0);
        if (e.isNumConstant()) 
            return e != CoreExpr.zero;
        return false;
    }
    
    public static boolean isFalse(CFGNode cond) {
        CoreExpr e = cond.getCoreKid(0);
        if (e.isNumConstant()) 
            return e == CoreExpr.zero;
        return false;
    }
    
    /** is the expression 'e' a NULL constant */
    private static boolean isNull(CoreExpr e) {
        if (e == CoreExpr.zero || e.isNumConstant() || e.isAddrConstant())
            return true;
        if (e instanceof CoreUnaryExpr) {
            ((CoreUnaryExpr)e).isCast();
            return isNull(e.getKid(0));
        }
        return false;
    }
    
    /** return true when !neq + (var == null),
     *               or   neq + (var != null)  */
    boolean isTestNull(CoreExpr var, boolean neq) {
        boolean res1 = false;
        boolean res2 = false;
        Atom a = null;
        boolean negated = false;
        if (this instanceof Atom) 
            a = (Atom) this;
        if (this instanceof Not) {
            Not no = (Not) this;
            if (no.base instanceof Atom)
                a = (Atom) no.base;
            negated = true;
        }
        if (a != null) {
            CoreExpr e = a.cond.getTest();
            if (isVar(e, var)) {
                // switch (e)  => res false
                // switch (!e) => res true
                res1 = negated;
                res2 = !negated;
            } else if (e instanceof CoreUnaryExpr) {
                if (((CoreUnaryExpr)e).getOp() == LogicalOps.NOT)
                    if (isVar(e.getKid(0),var)) {
                        res1 = !negated;
                        res2 = negated;
                    }
            } else if (e instanceof CoreBinaryExpr) {
                CoreExpr e1 = e.getKid(0);
                CoreExpr e2 = e.getKid(1);
                int op = ((CoreBinaryExpr)e).getOp();
                if (op == CompareOps.EQ
                        && ((isVar(e1, var) && isNull(e2)) ||
                        (isVar(e2, var) && isNull(e1)))) {
                    // switch (var == null) => true
                    // switch (!(var == null)) => false
                    res1 = !negated;
                    res2 = negated;
                }
                else if (op == CompareOps.NEQ
                        && ((isVar(e1, var) && isNull(e2)) ||
                                (isVar(e2, var) && isNull(e1)))) {
                    // switch (var != null) => false
                    // switch (!(var != null)) => true
                    res1 = negated;
                    res2 = !negated;
                }
            }
        }
        return (neq ? res2 : res1);
    }
    
    private static boolean isVar(CoreExpr e, CoreExpr var) {
        if (e == var)
            return true;
        
        if (e instanceof CoreUnaryExpr) 
            return (((CoreUnaryExpr)e).isCast() && e.getKid(0) == var);
        return false;
    }
    
    boolean isTestLikeNull(Slice s, CoreExpr def, boolean neq) {
        if (!Options.enhancedFilter)
            return isTestNull(def, neq);
        
        boolean res1 = false;
        boolean res2 = false;
        Atom a = null;
        boolean negated = false;
        if (this instanceof Atom) 
            a = (Atom) this;
        if (this instanceof Not) {
            Not no = (Not) this;
            if (no.base instanceof Atom)
                a = (Atom) no.base;
            negated = true;
        }
        if (a != null) {
            CoreExpr e = a.cond.getTest();
            if (s.inSlice(e)) {
                // switch (e)  => res false
                // switch (!e) => res true
                res1 = negated;
                res2 = !negated;
            } else if (e instanceof CoreUnaryExpr) {
                if (((CoreUnaryExpr)e).getOp() == LogicalOps.NOT)
                    if (s.inSlice(e.getKid(0))) {
                        res1 = !negated;
                        res2 = negated;
                    }
            } else if (e instanceof CoreBinaryExpr) {
                CoreExpr e1 = e.getKid(0);
                CoreExpr e2 = e.getKid(1);
                int op = ((CoreBinaryExpr)e).getOp();
                boolean beq = op == CompareOps.EQ;
                boolean bneq = op == CompareOps.NEQ;
                if ((beq || bneq) &&
                        ((s.inSlice(e1) && !s.inSlice(e2)) ||
                        (s.inSlice(e2) && !s.inSlice(e1)))) {
                    res1 = (beq) ? !negated : negated;
                    res2 = (beq) ? negated  : !negated;
                }
            }
        }
        return (neq ? res2 : res1);
    }
    
    public String longMessage(boolean not) {
        return null;
    }
    
    String message(boolean not, int branch, CFGSwitchNode cond) {
        String str;
        
        char pre = not ? '!' : ' ';
        if (cond != null) {
            String test = cond.getTestSrc().toString();
            if (!cond.isIfThenElse())
                test = " [case " + test + " = " + (branch > 0 ? 
                    cond.getLabelSrc(branch) : "default") + " ]";
            str = String.format("  %c %-30s %s\n", pre, test ,
                    PrintDot.location(cond, true)); 
        } 
        else {
            if (this == FALSE || this == TRUE)
                str = String.format("  %c %-30s %s\n", pre, this,
                    " trivial "); 
            else 
                str = String.format("  %c %-30s %s\n", pre, this ,
                    " unk [too complex]"); 
        }
        return str;
    }

    /** convert to disjuntive normal form */
    public abstract BoolExpr toDNF();
    
    /** convert to conjunctive normal form */
    public abstract BoolExpr toCNF();

    public abstract BDDExpr toBDD();
    
    public abstract Collection<Atom> getAtoms();

    /** used to flat a dnf formula */
    public void flatDNF(List<List<BoolExpr>> l) {
        List<BoolExpr> li = new LinkedList<BoolExpr>();
        flatDNFhelper(li);
        if (li.size() == 1 && li.contains(FALSE))
            return;
        
        if (!l.contains(li))
            l.add(li);
        
    }

    /** used to flat an internal conjunction subformula in a dnf formula */
    public void flatDNFhelper(List<BoolExpr> l) {
        if (l.contains(not())) {
            l.clear();
            l.add(FALSE);
        } else
            if (!l.contains(this))
                l.add(this);
    }

    /** used to flat a dnf formula */
    public void flatCNF(List<List<BoolExpr>> l) {
        List<BoolExpr> li = new LinkedList<BoolExpr>();
        flatCNFhelper(li);
        if (li.size() == 1 && li.contains(TRUE))
            return;
        
        if (!l.contains(li))
            l.add(li);
    }
    

    /** used to flat an internal disjunction subformula in a cnf formula */
    public void flatCNFhelper(List<BoolExpr> l) {
        if (l.contains(not())) {
            l.clear();
            l.add(TRUE);
        } else
            if (!l.contains(this))
                l.add(this);
    }

    /** get atom for a condition c (taking branch b) */
    public static BoolExpr getAtom(CFGSwitchNode c, int b) {
        return Atom.get(c, b);
    }
    
    /** negate this guard */
    public BoolExpr not() {
        if (height >= Options.maxTrackedHeight)
            return UNK;
        return Not.get(this);
    }
    
    /** return (this && g) */
    public BoolExpr and(BoolExpr g) {
        if (height >= Options.maxTrackedHeight || 
                g.height >= Options.maxTrackedHeight)
            return UNK;
        return And.get(this, g);
    }
    
    /** return (this || g) */
    public BoolExpr or(BoolExpr g) {
        if (height >= Options.maxTrackedHeight || 
                g.height >= Options.maxTrackedHeight)
            return UNK;
        return Or.get(this, g);
    }
        
    public abstract String internalString(BoolExpr last);
    
   public String toString() {
        if (height > Options.outputHeightLimit)
            return " ... (" + height + ((this == UNK) ? "?" : "" ) + ") ...";
        return internalString(null);    	
    }
    
   static boolean printed = false;
    public int getBranchNum() {
        if (!printed) {
            printed = true;
            System.out.println("getBranchNum not supported for: " + this);
        }
        return -1;
    }
    
    public CFGSwitchNode getTest() {
        return null;
    }
    
    public boolean containsUnk() {
        return this == UNK;
    }
}

class Or extends BoolExpr {
    BoolExpr a;
    BoolExpr b;
    
    private Or(BoolExpr a, BoolExpr b) {
        this.a = a;
        this.b = b;
        height = ((a.height > b.height) ? a.height : b.height) + 1;
    }
    
    /** single object per guard expression: or */
    static TwoLevelMap<BoolExpr, BoolExpr, BoolExpr> ors =
        new TwoLevelMap<BoolExpr, BoolExpr, BoolExpr>();
    
    public static BoolExpr get(BoolExpr t, BoolExpr g) {
        BoolExpr r = ors.get(t, g);
        if (r != null)
            return r;

        if (Options.simplifyBoolExpr) {
            if (t == FALSE) 
                return g;

            if (g == FALSE)
                return t;

            if (t == TRUE || g == TRUE)
                return TRUE;

            if (t == g)
                return t;

            if ((t instanceof Not) && (((Not)t).base == g)) 
                r = TRUE;

            if ((g instanceof Not) && (((Not)g).base == t))
                r = TRUE;

            if ((t instanceof And) && (g instanceof And)) {
                And ta = (And) t;
                And gg = (And) g;
                if (ta.a == gg.a)
                    r = ta.a.and(ta.b.or(gg.b));
                else if (ta.a == gg.b)
                    r = ta.a.and(ta.b.or(gg.a));
                else if (ta.b == gg.a)
                    r = ta.b.and(ta.a.or(gg.b));
                else if (ta.b == gg.b)
                    r = ta.b.and(ta.a.or(gg.a));
            }
        }
        if (r == null) 
            r = new Or(t, g);

        ors.put(t, g, r);
        return r;
    }
    
    public String internalString(BoolExpr last) {
        String s = a.internalString(this) + " or " + b.internalString(this);
        if (last != null && !(last instanceof Or))
            s = "(" + s + ")";
        return s;
    }

    public BoolExpr toDNF() {
        return a.toDNF().or(b.toDNF());
    }

    public BoolExpr toCNF() {
        BoolExpr ga = a.toCNF();
        BoolExpr gb = b.toCNF();
        if (ga instanceof And) {
            And o = (And)ga;
            BoolExpr oa = o.a.or(gb).toCNF();
            BoolExpr ob = o.b.or(gb).toCNF();
            return oa.and(ob);
        }
        if (gb instanceof And) {
            And o = (And)gb;
            BoolExpr oa = o.a.or(ga).toCNF();
            BoolExpr ob = o.b.or(ga).toCNF();
            return oa.and(ob);
        }
        return ga.or(gb);
    }
    
    
    public BDDExpr toBDD() {
        BDDExpr l = a.toBDD();
        BDDExpr r = b.toBDD();
        BDDExpr res = BDDExpr.combineOr(l, r);
        return res;
    }

    public void flatDNF(List<List<BoolExpr>> l) {
        a.flatDNF(l);
        b.flatDNF(l);
        List<BoolExpr> tt = new LinkedList<BoolExpr>();
        tt.add(TRUE);
        if (l.contains(tt)) {
            l.clear();
            l.add(tt);
        }
    }

    public void flatCNFhelper(List<BoolExpr> l) {
        a.flatCNFhelper(l);
        b.flatCNFhelper(l);
        boolean clear = l.contains(TRUE);
        if (!clear)
            for (BoolExpr g : l)
                if (l.contains(g.not())) {
                    clear = true;
                    break;
                }
        if (clear) {
            l.clear();
            l.add(TRUE);
        }
    }
    
    public Collection<Atom> getAtoms() {
        Collection<Atom> s = new HashSet<Atom>();
        s.addAll(a.getAtoms());
        s.addAll(b.getAtoms());
        return s;
    }
    
    public boolean containsUnk() {
        return a.containsUnk() || b.containsUnk();
    }

}

class And extends BoolExpr {
    BoolExpr a;
    BoolExpr b;
   
    private And(BoolExpr a, BoolExpr b) {
        this.a = a;
        this.b = b;
        height = ((a.height > b.height) ? a.height : b.height) + 1;
    }
    
    /** single object per guard expression: and */
    static TwoLevelMap<BoolExpr, BoolExpr, BoolExpr> ands =
        new TwoLevelMap<BoolExpr, BoolExpr, BoolExpr>();
   
    public static BoolExpr get(BoolExpr t, BoolExpr g) {
        BoolExpr r = ands.get(t, g);
        if (r != null)
            return r;
        
        if (Options.simplifyBoolExpr) {
            if (t == TRUE) 
                return g;
            
            if (g == TRUE)
                return t;
            
            if (t == FALSE || g == FALSE)
                return FALSE;
            
            if (t == g)
                return t;
            
            if ((t instanceof Not) && (((Not)t).base == g)) 
                r = FALSE;
            
            if ((g instanceof Not) && (((Not)g).base == t))
                r = FALSE;
            
        }
        if (r == null) 
            r = new And(t, g);
        
        ands.put(t, g, r);
        return r;
    }
    
    public String internalString(BoolExpr last){
        String s = a.internalString(this) + " and " + b.internalString(this);
        if (last != null && !(last instanceof And))
            s = "(" + s + ")";
        return s;
    }
    
    public BoolExpr toDNF() {
        BoolExpr ga = a.toDNF();
        BoolExpr gb = b.toDNF();
        if (ga instanceof Or) {
            Or o = (Or)ga;
            BoolExpr oa = o.a.and(gb).toDNF();
            BoolExpr ob = o.b.and(gb).toDNF();
            return oa.or(ob);
        }
        if (gb instanceof Or) {
            Or o = (Or)gb;
            BoolExpr oa = o.a.and(ga).toDNF();
            BoolExpr ob = o.b.and(ga).toDNF();
            return oa.or(ob);
        }
        return ga.and(gb);
    }
    

    public BoolExpr toCNF() {
        return a.toCNF().and(b.toCNF());
    }
    
    public void flatCNF(List<List<BoolExpr>> l) {
        a.flatCNF(l);
        b.flatCNF(l);
        List<BoolExpr> tt = new LinkedList<BoolExpr>();
        tt.add(FALSE);
        if (l.contains(tt)) {
            l.clear();
            l.add(tt);
        }
    }
    
    
    public void flatDNFhelper(List<BoolExpr> l) {
        a.flatDNFhelper(l);
        b.flatDNFhelper(l);
        boolean clear = l.contains(FALSE);
        if (!clear)
            for (BoolExpr g : l)
                if (l.contains(g.not())) {
                    clear = true;
                    break;
                }
        if (clear) {
            l.clear();
            l.add(FALSE);
        }
    }
    
    public Collection<Atom> getAtoms() {
        Collection<Atom> s = new HashSet<Atom>();
        s.addAll(a.getAtoms());
        s.addAll(b.getAtoms());
        return s;
    }
    
    public BDDExpr toBDD() {
        BDDExpr l = a.toBDD();
        BDDExpr r = b.toBDD();
        BDDExpr res = BDDExpr.combineAnd(l, r);
        return res;
    }

    public boolean containsUnk() {
        return a.containsUnk() || b.containsUnk();
    }
}

abstract class Terminal extends BoolExpr {
    static int sid = 0;
    int id = sid++;
    
    public abstract String fullString();
}

class Atom extends Terminal {
    CFGSwitchNode cond;
    int val;
    
    
    private Atom(CFGSwitchNode c, int val) {
        cond = c;
        this.val = val;
    }
    
    /** single object per guard expression: atoms */
    static TwoLevelMap<CFGNode, Integer, BoolExpr> atoms =
        new TwoLevelMap<CFGNode, Integer, BoolExpr>();

    public static int findBranch(CFGSwitchNode c, BoolExpr e) {
        for (Integer i : atoms.get(c).keySet())
            if (atoms.get(c, i) == e)
                return i;
        return -1;
    }
    
    public CFGSwitchNode getTest() {
        return cond;
    }
    
    public int getBranchNum() {
        return val;
    }
    
    public static BoolExpr get(CFGSwitchNode c, int b) {
        BoolExpr g = atoms.get(c, b);
        if (g == null) {
            final int TRUE_BRANCH = 0;
            final int FALSE_BRANCH = 1;
            if (c.isIfThenElse() && b == FALSE_BRANCH)  
                g = getAtom(c, TRUE_BRANCH).not();
            else {
                if (isTrue(c))
                    g = TRUE;
                else if (isFalse(c))
                    g = FALSE;
                else 
                    g = new Atom(c, b);
            }
            atoms.put(c, b, g);
        }
        return g;
    }
    
    
    public String internalString(BoolExpr b) {
        return "c"+id; 
    }
    
    public String fullString() {
        return cond.toString() + (!cond.isIfThenElse() ? " == #" + val : "");
    }

    public BoolExpr toDNF() {
        return this;
    }

    public BoolExpr toCNF() {
        return this;
    }
    
    public Collection<Atom> getAtoms() {
        Collection<Atom> s = new HashSet<Atom>();
        s.add(this);
        return s;
    }
    
    public BDDExpr toBDD() {
        BDDExpr res = BDDExpr.getExpr(this, TRUE.toBDD(), FALSE.toBDD());
        return res;
    }
    

    public String longMessage(boolean not) {
        return message(not, val, cond);
    }
    
}

class Not extends BoolExpr {
    BoolExpr base;
    
    private Not (BoolExpr g) {
        base = g;
        height = g.height + 1;
    }

    /** single object per guard expression: not */
    static Map<BoolExpr, BoolExpr> nots =
        new HashMap<BoolExpr, BoolExpr>();
    
    static {
        nots.put(TRUE, FALSE);
        nots.put(FALSE, TRUE);
    }
    
    public static BoolExpr get(BoolExpr t) {
        BoolExpr g = nots.get(t);
        if (g == null) {
            g = new Not(t);
            nots.put(t, g);
            nots.put(g, t);
        }
        return g;
    }
    
    public String internalString(BoolExpr last){
        return "! " + base.internalString(this);
    }
    
    public BoolExpr toDNF() {
        /* pushes not down */
        if (base instanceof And) {
            And a = (And) base;
            return a.a.not().or(a.b.not()).toDNF();
        }
        if (base instanceof Or) {
            Or a = (Or) base;
            return a.a.not().and(a.b.not()).toDNF();
        }
        return this;
    }
    
    public BoolExpr toCNF() {
        /* pushes not down */
        if (base instanceof And) {
            And a = (And) base;
            return a.a.not().or(a.b.not()).toCNF();
        }
        if (base instanceof Or) {
            Or a = (Or) base;
            return a.a.not().and(a.b.not()).toCNF();
        }
        return this;
    }
    
    public Collection<Atom> getAtoms() {
        Collection<Atom> s = new HashSet<Atom>();
        s.addAll(base.getAtoms());
        return s;
    }
    
    public BDDExpr toBDD() {
        BDDExpr bbdd = base.toBDD();
        BDDExpr res = BDDExpr.swapTF(bbdd);
        return res;
    }

    public String longMessage(boolean not) {
        return base.longMessage(true);
    }
 

    /** only valid when BoolExpr is either Atom or Not(Atom) */
    public CFGSwitchNode getTest() {
        assert base instanceof Terminal;
        return base.getTest();
    }
    
    /** only valid when BoolExpr is either Atom or Not(Atom) */
    public int getBranchNum() {
        assert base instanceof Atom;
        Atom b = (Atom) base;
        if (!b.cond.isIfThenElse())
            return -1;
        return 1 - b.val;
    }
    
    public boolean containsUnk() {
        return base.containsUnk();
    }
}

class ConstantAtom extends Terminal {
    String name;
    
    ConstantAtom(String n) {
        name = n;
    }
    public String internalString(BoolExpr e) {
        return name;
    }

    public BoolExpr toDNF() {
        return this;
    }
    
    public BoolExpr toCNF() {
        return this;
    }
    
    public Collection<Atom> getAtoms() {
        return new HashSet<Atom>();
    }
    
    public BDDExpr toBDD() {
        BDDExpr res = BDDExpr.getExpr(this, null, null);
        return res;
    }

    public String fullString() {
        return name;
    }
    
    public String longMessage(boolean not) {
        return message(not, -1, null);
    }
 
}

