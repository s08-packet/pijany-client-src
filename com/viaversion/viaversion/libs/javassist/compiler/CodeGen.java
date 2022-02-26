/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.compiler;

import com.viaversion.viaversion.libs.javassist.bytecode.Bytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.Opcode;
import com.viaversion.viaversion.libs.javassist.compiler.CompileError;
import com.viaversion.viaversion.libs.javassist.compiler.MemberResolver;
import com.viaversion.viaversion.libs.javassist.compiler.TokenId;
import com.viaversion.viaversion.libs.javassist.compiler.TypeChecker;
import com.viaversion.viaversion.libs.javassist.compiler.ast.ASTList;
import com.viaversion.viaversion.libs.javassist.compiler.ast.ASTree;
import com.viaversion.viaversion.libs.javassist.compiler.ast.ArrayInit;
import com.viaversion.viaversion.libs.javassist.compiler.ast.AssignExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.BinExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.CallExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.CastExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.CondExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Declarator;
import com.viaversion.viaversion.libs.javassist.compiler.ast.DoubleConst;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Expr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.FieldDecl;
import com.viaversion.viaversion.libs.javassist.compiler.ast.InstanceOfExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.IntConst;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Keyword;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Member;
import com.viaversion.viaversion.libs.javassist.compiler.ast.MethodDecl;
import com.viaversion.viaversion.libs.javassist.compiler.ast.NewExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Pair;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Stmnt;
import com.viaversion.viaversion.libs.javassist.compiler.ast.StringL;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Symbol;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Variable;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Visitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public abstract class CodeGen
extends Visitor
implements Opcode,
TokenId {
    static final String javaLangObject = "java.lang.Object";
    static final String jvmJavaLangObject = "java/lang/Object";
    static final String javaLangString = "java.lang.String";
    static final String jvmJavaLangString = "java/lang/String";
    protected Bytecode bytecode;
    private int tempVar;
    TypeChecker typeChecker;
    protected boolean hasReturned;
    public boolean inStaticMethod;
    protected List<Integer> breakList;
    protected List<Integer> continueList;
    protected ReturnHook returnHooks;
    protected int exprType;
    protected int arrayDim;
    protected String className;
    static final int[] binOp = new int[]{43, 99, 98, 97, 96, 45, 103, 102, 101, 100, 42, 107, 106, 105, 104, 47, 111, 110, 109, 108, 37, 115, 114, 113, 112, 124, 0, 0, 129, 128, 94, 0, 0, 131, 130, 38, 0, 0, 127, 126, 364, 0, 0, 121, 120, 366, 0, 0, 123, 122, 370, 0, 0, 125, 124};
    private static final int[] ifOp = new int[]{358, 159, 160, 350, 160, 159, 357, 164, 163, 359, 162, 161, 60, 161, 162, 62, 163, 164};
    private static final int[] ifOp2 = new int[]{358, 153, 154, 350, 154, 153, 357, 158, 157, 359, 156, 155, 60, 155, 156, 62, 157, 158};
    private static final int P_DOUBLE = 0;
    private static final int P_FLOAT = 1;
    private static final int P_LONG = 2;
    private static final int P_INT = 3;
    private static final int P_OTHER = -1;
    private static final int[] castOp = new int[]{0, 144, 143, 142, 141, 0, 140, 139, 138, 137, 0, 136, 135, 134, 133, 0};

    public CodeGen(Bytecode b) {
        this.bytecode = b;
        this.tempVar = -1;
        this.typeChecker = null;
        this.hasReturned = false;
        this.inStaticMethod = false;
        this.breakList = null;
        this.continueList = null;
        this.returnHooks = null;
    }

    public void setTypeChecker(TypeChecker checker) {
        this.typeChecker = checker;
    }

    protected static void fatal() throws CompileError {
        throw new CompileError("fatal");
    }

    public static boolean is2word(int type, int dim) {
        if (dim != 0) return false;
        if (type == 312) return true;
        if (type != 326) return false;
        return true;
    }

    public int getMaxLocals() {
        return this.bytecode.getMaxLocals();
    }

    public void setMaxLocals(int n) {
        this.bytecode.setMaxLocals(n);
    }

    protected void incMaxLocals(int size) {
        this.bytecode.incMaxLocals(size);
    }

    protected int getTempVar() {
        if (this.tempVar >= 0) return this.tempVar;
        this.tempVar = this.getMaxLocals();
        this.incMaxLocals(2);
        return this.tempVar;
    }

    protected int getLocalVar(Declarator d) {
        int v = d.getLocalVar();
        if (v >= 0) return v;
        v = this.getMaxLocals();
        d.setLocalVar(v);
        this.incMaxLocals(1);
        return v;
    }

    protected abstract String getThisName();

    protected abstract String getSuperName() throws CompileError;

    protected abstract String resolveClassName(ASTList var1) throws CompileError;

    protected abstract String resolveClassName(String var1) throws CompileError;

    protected static String toJvmArrayName(String name, int dim) {
        if (name == null) {
            return null;
        }
        if (dim == 0) {
            return name;
        }
        StringBuffer sbuf = new StringBuffer();
        int d = dim;
        while (true) {
            if (d-- <= 0) {
                sbuf.append('L');
                sbuf.append(name);
                sbuf.append(';');
                return sbuf.toString();
            }
            sbuf.append('[');
        }
    }

    protected static String toJvmTypeName(int type, int dim) {
        char c = 'I';
        switch (type) {
            case 301: {
                c = 'Z';
                break;
            }
            case 303: {
                c = 'B';
                break;
            }
            case 306: {
                c = 'C';
                break;
            }
            case 334: {
                c = 'S';
                break;
            }
            case 324: {
                c = 'I';
                break;
            }
            case 326: {
                c = 'J';
                break;
            }
            case 317: {
                c = 'F';
                break;
            }
            case 312: {
                c = 'D';
                break;
            }
            case 344: {
                c = 'V';
                break;
            }
        }
        StringBuffer sbuf = new StringBuffer();
        while (true) {
            if (dim-- <= 0) {
                sbuf.append(c);
                return sbuf.toString();
            }
            sbuf.append('[');
        }
    }

    public void compileExpr(ASTree expr) throws CompileError {
        this.doTypeCheck(expr);
        expr.accept(this);
    }

    public boolean compileBooleanExpr(boolean branchIf, ASTree expr) throws CompileError {
        this.doTypeCheck(expr);
        return this.booleanExpr(branchIf, expr);
    }

    public void doTypeCheck(ASTree expr) throws CompileError {
        if (this.typeChecker == null) return;
        expr.accept(this.typeChecker);
    }

    @Override
    public void atASTList(ASTList n) throws CompileError {
        CodeGen.fatal();
    }

    @Override
    public void atPair(Pair n) throws CompileError {
        CodeGen.fatal();
    }

    @Override
    public void atSymbol(Symbol n) throws CompileError {
        CodeGen.fatal();
    }

    @Override
    public void atFieldDecl(FieldDecl field) throws CompileError {
        field.getInit().accept(this);
    }

    @Override
    public void atMethodDecl(MethodDecl method) throws CompileError {
        this.setMaxLocals(1);
        for (ASTList mods = method.getModifiers(); mods != null; mods = mods.tail()) {
            Keyword k = (Keyword)mods.head();
            if (k.get() != 335) continue;
            this.setMaxLocals(0);
            this.inStaticMethod = true;
        }
        for (ASTList params = method.getParams(); params != null; params = params.tail()) {
            this.atDeclarator((Declarator)params.head());
        }
        Stmnt s = method.getBody();
        this.atMethodBody(s, method.isConstructor(), method.getReturn().getType() == 344);
    }

    public void atMethodBody(Stmnt s, boolean isCons, boolean isVoid) throws CompileError {
        if (s == null) {
            return;
        }
        if (isCons && this.needsSuperCall(s)) {
            this.insertDefaultSuperCall();
        }
        this.hasReturned = false;
        s.accept(this);
        if (this.hasReturned) return;
        if (!isVoid) throw new CompileError("no return statement");
        this.bytecode.addOpcode(177);
        this.hasReturned = true;
    }

    private boolean needsSuperCall(Stmnt body) throws CompileError {
        if (body.getOperator() == 66) {
            body = (Stmnt)body.head();
        }
        if (body == null) return true;
        if (body.getOperator() != 69) return true;
        ASTree expr = body.head();
        if (expr == null) return true;
        if (!(expr instanceof Expr)) return true;
        if (((Expr)expr).getOperator() != 67) return true;
        ASTree target = ((Expr)expr).head();
        if (!(target instanceof Keyword)) return true;
        int token = ((Keyword)target).get();
        if (token == 339) return false;
        if (token == 336) return false;
        return true;
    }

    protected abstract void insertDefaultSuperCall() throws CompileError;

    @Override
    public void atStmnt(Stmnt st) throws CompileError {
        if (st == null) {
            return;
        }
        int op = st.getOperator();
        if (op == 69) {
            ASTree expr = st.getLeft();
            this.doTypeCheck(expr);
            if (expr instanceof AssignExpr) {
                this.atAssignExpr((AssignExpr)expr, false);
                return;
            }
            if (CodeGen.isPlusPlusExpr(expr)) {
                Expr e = (Expr)expr;
                this.atPlusPlus(e.getOperator(), e.oprand1(), e, false);
                return;
            }
            expr.accept(this);
            if (CodeGen.is2word(this.exprType, this.arrayDim)) {
                this.bytecode.addOpcode(88);
                return;
            }
            if (this.exprType == 344) return;
            this.bytecode.addOpcode(87);
            return;
        }
        if (op == 68 || op == 66) {
            ASTList list = st;
            while (list != null) {
                ASTree h = list.head();
                list = list.tail();
                if (h == null) continue;
                h.accept(this);
            }
            return;
        }
        if (op == 320) {
            this.atIfStmnt(st);
            return;
        }
        if (op == 346 || op == 311) {
            this.atWhileStmnt(st, op == 346);
            return;
        }
        if (op == 318) {
            this.atForStmnt(st);
            return;
        }
        if (op == 302 || op == 309) {
            this.atBreakStmnt(st, op == 302);
            return;
        }
        if (op == 333) {
            this.atReturnStmnt(st);
            return;
        }
        if (op == 340) {
            this.atThrowStmnt(st);
            return;
        }
        if (op == 343) {
            this.atTryStmnt(st);
            return;
        }
        if (op == 337) {
            this.atSwitchStmnt(st);
            return;
        }
        if (op == 338) {
            this.atSyncStmnt(st);
            return;
        }
        this.hasReturned = false;
        throw new CompileError("sorry, not supported statement: TokenId " + op);
    }

    private void atIfStmnt(Stmnt st) throws CompileError {
        ASTree expr = st.head();
        Stmnt thenp = (Stmnt)st.tail().head();
        Stmnt elsep = (Stmnt)st.tail().tail().head();
        if (this.compileBooleanExpr(false, expr)) {
            this.hasReturned = false;
            if (elsep == null) return;
            elsep.accept(this);
            return;
        }
        int pc = this.bytecode.currentPc();
        int pc2 = 0;
        this.bytecode.addIndex(0);
        this.hasReturned = false;
        if (thenp != null) {
            thenp.accept(this);
        }
        boolean thenHasReturned = this.hasReturned;
        this.hasReturned = false;
        if (elsep != null && !thenHasReturned) {
            this.bytecode.addOpcode(167);
            pc2 = this.bytecode.currentPc();
            this.bytecode.addIndex(0);
        }
        this.bytecode.write16bit(pc, this.bytecode.currentPc() - pc + 1);
        if (elsep == null) return;
        elsep.accept(this);
        if (!thenHasReturned) {
            this.bytecode.write16bit(pc2, this.bytecode.currentPc() - pc2 + 1);
        }
        this.hasReturned = thenHasReturned && this.hasReturned;
    }

    private void atWhileStmnt(Stmnt st, boolean notDo) throws CompileError {
        boolean alwaysBranch;
        List<Integer> prevBreakList = this.breakList;
        List<Integer> prevContList = this.continueList;
        this.breakList = new ArrayList<Integer>();
        this.continueList = new ArrayList<Integer>();
        ASTree expr = st.head();
        Stmnt body = (Stmnt)st.tail();
        int pc = 0;
        if (notDo) {
            this.bytecode.addOpcode(167);
            pc = this.bytecode.currentPc();
            this.bytecode.addIndex(0);
        }
        int pc2 = this.bytecode.currentPc();
        if (body != null) {
            body.accept(this);
        }
        int pc3 = this.bytecode.currentPc();
        if (notDo) {
            this.bytecode.write16bit(pc, pc3 - pc + 1);
        }
        if (alwaysBranch = this.compileBooleanExpr(true, expr)) {
            this.bytecode.addOpcode(167);
            alwaysBranch = this.breakList.size() == 0;
        }
        this.bytecode.addIndex(pc2 - this.bytecode.currentPc() + 1);
        this.patchGoto(this.breakList, this.bytecode.currentPc());
        this.patchGoto(this.continueList, pc3);
        this.continueList = prevContList;
        this.breakList = prevBreakList;
        this.hasReturned = alwaysBranch;
    }

    protected void patchGoto(List<Integer> list, int targetPc) {
        Iterator<Integer> iterator = list.iterator();
        while (iterator.hasNext()) {
            int pc = iterator.next();
            this.bytecode.write16bit(pc, targetPc - pc + 1);
        }
    }

    private void atForStmnt(Stmnt st) throws CompileError {
        List<Integer> prevBreakList = this.breakList;
        List<Integer> prevContList = this.continueList;
        this.breakList = new ArrayList<Integer>();
        this.continueList = new ArrayList<Integer>();
        Stmnt init = (Stmnt)st.head();
        ASTList p = st.tail();
        ASTree expr = p.head();
        p = p.tail();
        Stmnt update = (Stmnt)p.head();
        Stmnt body = (Stmnt)p.tail();
        if (init != null) {
            init.accept(this);
        }
        int pc = this.bytecode.currentPc();
        int pc2 = 0;
        if (expr != null) {
            if (this.compileBooleanExpr(false, expr)) {
                this.continueList = prevContList;
                this.breakList = prevBreakList;
                this.hasReturned = false;
                return;
            }
            pc2 = this.bytecode.currentPc();
            this.bytecode.addIndex(0);
        }
        if (body != null) {
            body.accept(this);
        }
        int pc3 = this.bytecode.currentPc();
        if (update != null) {
            update.accept(this);
        }
        this.bytecode.addOpcode(167);
        this.bytecode.addIndex(pc - this.bytecode.currentPc() + 1);
        int pc4 = this.bytecode.currentPc();
        if (expr != null) {
            this.bytecode.write16bit(pc2, pc4 - pc2 + 1);
        }
        this.patchGoto(this.breakList, pc4);
        this.patchGoto(this.continueList, pc3);
        this.continueList = prevContList;
        this.breakList = prevBreakList;
        this.hasReturned = false;
    }

    private void atSwitchStmnt(Stmnt st) throws CompileError {
        boolean isString = false;
        if (this.typeChecker != null) {
            this.doTypeCheck(st.head());
            isString = this.typeChecker.exprType == 307 && this.typeChecker.arrayDim == 0 && jvmJavaLangString.equals(this.typeChecker.className);
        }
        this.compileExpr(st.head());
        int tmpVar = -1;
        if (isString) {
            tmpVar = this.getMaxLocals();
            this.incMaxLocals(1);
            this.bytecode.addAstore(tmpVar);
            this.bytecode.addAload(tmpVar);
            this.bytecode.addInvokevirtual(jvmJavaLangString, "hashCode", "()I");
        }
        List<Integer> prevBreakList = this.breakList;
        this.breakList = new ArrayList<Integer>();
        int opcodePc = this.bytecode.currentPc();
        this.bytecode.addOpcode(171);
        int npads = 3 - (opcodePc & 3);
        while (npads-- > 0) {
            this.bytecode.add(0);
        }
        Stmnt body = (Stmnt)st.tail();
        int npairs = 0;
        for (ASTList list = body; list != null; list = list.tail()) {
            if (((Stmnt)list.head()).getOperator() != 304) continue;
            ++npairs;
        }
        int opcodePc2 = this.bytecode.currentPc();
        this.bytecode.addGap(4);
        this.bytecode.add32bit(npairs);
        this.bytecode.addGap(npairs * 8);
        long[] pairs = new long[npairs];
        ArrayList<Integer> gotoDefaults = new ArrayList<Integer>();
        int ipairs = 0;
        int defaultPc = -1;
        for (ASTList list = body; list != null; list = list.tail()) {
            Stmnt label = (Stmnt)list.head();
            int op = label.getOperator();
            if (op == 310) {
                defaultPc = this.bytecode.currentPc();
            } else if (op != 304) {
                CodeGen.fatal();
            } else {
                int curPos = this.bytecode.currentPc();
                long caseLabel = isString ? (long)this.computeStringLabel(label.head(), tmpVar, gotoDefaults) : (long)this.computeLabel(label.head());
                pairs[ipairs++] = (caseLabel << 32) + ((long)(curPos - opcodePc) & 0xFFFFFFFFFFFFFFFFL);
            }
            this.hasReturned = false;
            ((Stmnt)label.tail()).accept(this);
        }
        Arrays.sort(pairs);
        int pc = opcodePc2 + 8;
        for (int i = 0; i < npairs; pc += 8, ++i) {
            this.bytecode.write32bit(pc, (int)(pairs[i] >>> 32));
            this.bytecode.write32bit(pc + 4, (int)pairs[i]);
        }
        if (defaultPc < 0 || this.breakList.size() > 0) {
            this.hasReturned = false;
        }
        int endPc = this.bytecode.currentPc();
        if (defaultPc < 0) {
            defaultPc = endPc;
        }
        this.bytecode.write32bit(opcodePc2, defaultPc - opcodePc);
        Iterator<Integer> iterator = gotoDefaults.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                this.patchGoto(this.breakList, endPc);
                this.breakList = prevBreakList;
                return;
            }
            int addr = iterator.next();
            this.bytecode.write16bit(addr, defaultPc - addr + 1);
        }
    }

    private int computeLabel(ASTree expr) throws CompileError {
        this.doTypeCheck(expr);
        expr = TypeChecker.stripPlusExpr(expr);
        if (!(expr instanceof IntConst)) throw new CompileError("bad case label");
        return (int)((IntConst)expr).get();
    }

    private int computeStringLabel(ASTree expr, int tmpVar, List<Integer> gotoDefaults) throws CompileError {
        this.doTypeCheck(expr);
        expr = TypeChecker.stripPlusExpr(expr);
        if (!(expr instanceof StringL)) throw new CompileError("bad case label");
        String label = ((StringL)expr).get();
        this.bytecode.addAload(tmpVar);
        this.bytecode.addLdc(label);
        this.bytecode.addInvokevirtual(jvmJavaLangString, "equals", "(Ljava/lang/Object;)Z");
        this.bytecode.addOpcode(153);
        Integer pc = this.bytecode.currentPc();
        this.bytecode.addIndex(0);
        gotoDefaults.add(pc);
        return label.hashCode();
    }

    private void atBreakStmnt(Stmnt st, boolean notCont) throws CompileError {
        if (st.head() != null) {
            throw new CompileError("sorry, not support labeled break or continue");
        }
        this.bytecode.addOpcode(167);
        Integer pc = this.bytecode.currentPc();
        this.bytecode.addIndex(0);
        if (notCont) {
            this.breakList.add(pc);
            return;
        }
        this.continueList.add(pc);
    }

    protected void atReturnStmnt(Stmnt st) throws CompileError {
        this.atReturnStmnt2(st.getLeft());
    }

    protected final void atReturnStmnt2(ASTree result) throws CompileError {
        int op;
        if (result == null) {
            op = 177;
        } else {
            int type;
            this.compileExpr(result);
            op = this.arrayDim > 0 ? 176 : ((type = this.exprType) == 312 ? 175 : (type == 317 ? 174 : (type == 326 ? 173 : (CodeGen.isRefType(type) ? 176 : 172))));
        }
        ReturnHook har = this.returnHooks;
        while (true) {
            if (har == null) {
                this.bytecode.addOpcode(op);
                this.hasReturned = true;
                return;
            }
            if (har.doit(this.bytecode, op)) {
                this.hasReturned = true;
                return;
            }
            har = har.next;
        }
    }

    private void atThrowStmnt(Stmnt st) throws CompileError {
        ASTree e = st.getLeft();
        this.compileExpr(e);
        if (this.exprType != 307) throw new CompileError("bad throw statement");
        if (this.arrayDim > 0) {
            throw new CompileError("bad throw statement");
        }
        this.bytecode.addOpcode(191);
        this.hasReturned = true;
    }

    protected void atTryStmnt(Stmnt st) throws CompileError {
        this.hasReturned = false;
    }

    private void atSyncStmnt(Stmnt st) throws CompileError {
        int nbreaks = CodeGen.getListSize(this.breakList);
        int ncontinues = CodeGen.getListSize(this.continueList);
        this.compileExpr(st.head());
        if (this.exprType != 307 && this.arrayDim == 0) {
            throw new CompileError("bad type expr for synchronized block");
        }
        Bytecode bc = this.bytecode;
        final int var = bc.getMaxLocals();
        bc.incMaxLocals(1);
        bc.addOpcode(89);
        bc.addAstore(var);
        bc.addOpcode(194);
        ReturnHook rh = new ReturnHook(this){

            @Override
            protected boolean doit(Bytecode b, int opcode) {
                b.addAload(var);
                b.addOpcode(195);
                return false;
            }
        };
        int pc = bc.currentPc();
        Stmnt body = (Stmnt)st.tail();
        if (body != null) {
            body.accept(this);
        }
        int pc2 = bc.currentPc();
        int pc3 = 0;
        if (!this.hasReturned) {
            rh.doit(bc, 0);
            bc.addOpcode(167);
            pc3 = bc.currentPc();
            bc.addIndex(0);
        }
        if (pc < pc2) {
            int pc4 = bc.currentPc();
            rh.doit(bc, 0);
            bc.addOpcode(191);
            bc.addExceptionHandler(pc, pc2, pc4, 0);
        }
        if (!this.hasReturned) {
            bc.write16bit(pc3, bc.currentPc() - pc3 + 1);
        }
        rh.remove(this);
        if (CodeGen.getListSize(this.breakList) != nbreaks) throw new CompileError("sorry, cannot break/continue in synchronized block");
        if (CodeGen.getListSize(this.continueList) == ncontinues) return;
        throw new CompileError("sorry, cannot break/continue in synchronized block");
    }

    private static int getListSize(List<Integer> list) {
        if (list == null) {
            return 0;
        }
        int n = list.size();
        return n;
    }

    private static boolean isPlusPlusExpr(ASTree expr) {
        if (!(expr instanceof Expr)) return false;
        int op = ((Expr)expr).getOperator();
        if (op == 362) return true;
        if (op == 363) return true;
        return false;
    }

    @Override
    public void atDeclarator(Declarator d) throws CompileError {
        d.setLocalVar(this.getMaxLocals());
        d.setClassName(this.resolveClassName(d.getClassName()));
        int size = CodeGen.is2word(d.getType(), d.getArrayDim()) ? 2 : 1;
        this.incMaxLocals(size);
        ASTree init = d.getInitializer();
        if (init == null) return;
        this.doTypeCheck(init);
        this.atVariableAssign(null, 61, null, d, init, false);
    }

    @Override
    public abstract void atNewExpr(NewExpr var1) throws CompileError;

    @Override
    public abstract void atArrayInit(ArrayInit var1) throws CompileError;

    @Override
    public void atAssignExpr(AssignExpr expr) throws CompileError {
        this.atAssignExpr(expr, true);
    }

    protected void atAssignExpr(AssignExpr expr, boolean doDup) throws CompileError {
        Expr e;
        int op = expr.getOperator();
        ASTree left = expr.oprand1();
        ASTree right = expr.oprand2();
        if (left instanceof Variable) {
            this.atVariableAssign(expr, op, (Variable)left, ((Variable)left).getDeclarator(), right, doDup);
            return;
        }
        if (left instanceof Expr && (e = (Expr)left).getOperator() == 65) {
            this.atArrayAssign(expr, op, (Expr)left, right, doDup);
            return;
        }
        this.atFieldAssign(expr, op, left, right, doDup);
    }

    protected static void badAssign(Expr expr) throws CompileError {
        String msg;
        if (expr == null) {
            msg = "incompatible type for assignment";
            throw new CompileError(msg);
        }
        msg = "incompatible type for " + expr.getName();
        throw new CompileError(msg);
    }

    private void atVariableAssign(Expr expr, int op, Variable var, Declarator d, ASTree right, boolean doDup) throws CompileError {
        int varType = d.getType();
        int varArray = d.getArrayDim();
        String varClass = d.getClassName();
        int varNo = this.getLocalVar(d);
        if (op != 61) {
            this.atVariable(var);
        }
        if (expr == null && right instanceof ArrayInit) {
            this.atArrayVariableAssign((ArrayInit)right, varType, varArray, varClass);
        } else {
            this.atAssignCore(expr, op, right, varType, varArray, varClass);
        }
        if (doDup) {
            if (CodeGen.is2word(varType, varArray)) {
                this.bytecode.addOpcode(92);
            } else {
                this.bytecode.addOpcode(89);
            }
        }
        if (varArray > 0) {
            this.bytecode.addAstore(varNo);
        } else if (varType == 312) {
            this.bytecode.addDstore(varNo);
        } else if (varType == 317) {
            this.bytecode.addFstore(varNo);
        } else if (varType == 326) {
            this.bytecode.addLstore(varNo);
        } else if (CodeGen.isRefType(varType)) {
            this.bytecode.addAstore(varNo);
        } else {
            this.bytecode.addIstore(varNo);
        }
        this.exprType = varType;
        this.arrayDim = varArray;
        this.className = varClass;
    }

    protected abstract void atArrayVariableAssign(ArrayInit var1, int var2, int var3, String var4) throws CompileError;

    private void atArrayAssign(Expr expr, int op, Expr array, ASTree right, boolean doDup) throws CompileError {
        this.arrayAccess(array.oprand1(), array.oprand2());
        if (op != 61) {
            this.bytecode.addOpcode(92);
            this.bytecode.addOpcode(CodeGen.getArrayReadOp(this.exprType, this.arrayDim));
        }
        int aType = this.exprType;
        int aDim = this.arrayDim;
        String cname = this.className;
        this.atAssignCore(expr, op, right, aType, aDim, cname);
        if (doDup) {
            if (CodeGen.is2word(aType, aDim)) {
                this.bytecode.addOpcode(94);
            } else {
                this.bytecode.addOpcode(91);
            }
        }
        this.bytecode.addOpcode(CodeGen.getArrayWriteOp(aType, aDim));
        this.exprType = aType;
        this.arrayDim = aDim;
        this.className = cname;
    }

    protected abstract void atFieldAssign(Expr var1, int var2, ASTree var3, ASTree var4, boolean var5) throws CompileError;

    protected void atAssignCore(Expr expr, int op, ASTree right, int type, int dim, String cname) throws CompileError {
        if (op == 354 && dim == 0 && type == 307) {
            this.atStringPlusEq(expr, type, dim, cname, right);
        } else {
            right.accept(this);
            if (this.invalidDim(this.exprType, this.arrayDim, this.className, type, dim, cname, false) || op != 61 && dim > 0) {
                CodeGen.badAssign(expr);
            }
            if (op != 61) {
                int token = assignOps[op - 351];
                int k = CodeGen.lookupBinOp(token);
                if (k < 0) {
                    CodeGen.fatal();
                }
                this.atArithBinExpr(expr, token, k, type);
            }
        }
        if (op == 61) {
            if (dim != 0) return;
            if (CodeGen.isRefType(type)) return;
        }
        this.atNumCastExpr(this.exprType, type);
    }

    private void atStringPlusEq(Expr expr, int type, int dim, String cname, ASTree right) throws CompileError {
        if (!jvmJavaLangString.equals(cname)) {
            CodeGen.badAssign(expr);
        }
        this.convToString(type, dim);
        right.accept(this);
        this.convToString(this.exprType, this.arrayDim);
        this.bytecode.addInvokevirtual(javaLangString, "concat", "(Ljava/lang/String;)Ljava/lang/String;");
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = jvmJavaLangString;
    }

    private boolean invalidDim(int srcType, int srcDim, String srcClass, int destType, int destDim, String destClass, boolean isCast) {
        if (srcDim == destDim) return false;
        if (srcType == 412) {
            return false;
        }
        if (destDim == 0 && destType == 307 && jvmJavaLangObject.equals(destClass)) {
            return false;
        }
        if (!isCast) return true;
        if (srcDim != 0) return true;
        if (srcType != 307) return true;
        if (!jvmJavaLangObject.equals(srcClass)) return true;
        return false;
    }

    @Override
    public void atCondExpr(CondExpr expr) throws CompileError {
        if (this.booleanExpr(false, expr.condExpr())) {
            expr.elseExpr().accept(this);
            return;
        }
        int pc = this.bytecode.currentPc();
        this.bytecode.addIndex(0);
        expr.thenExpr().accept(this);
        int dim1 = this.arrayDim;
        this.bytecode.addOpcode(167);
        int pc2 = this.bytecode.currentPc();
        this.bytecode.addIndex(0);
        this.bytecode.write16bit(pc, this.bytecode.currentPc() - pc + 1);
        expr.elseExpr().accept(this);
        if (dim1 != this.arrayDim) {
            throw new CompileError("type mismatch in ?:");
        }
        this.bytecode.write16bit(pc2, this.bytecode.currentPc() - pc2 + 1);
    }

    static int lookupBinOp(int token) {
        int[] code = binOp;
        int s = code.length;
        int k = 0;
        while (k < s) {
            if (code[k] == token) {
                return k;
            }
            k += 5;
        }
        return -1;
    }

    @Override
    public void atBinExpr(BinExpr expr) throws CompileError {
        int token = expr.getOperator();
        int k = CodeGen.lookupBinOp(token);
        if (k >= 0) {
            expr.oprand1().accept(this);
            ASTree right = expr.oprand2();
            if (right == null) {
                return;
            }
            int type1 = this.exprType;
            int dim1 = this.arrayDim;
            String cname1 = this.className;
            right.accept(this);
            if (dim1 != this.arrayDim) {
                throw new CompileError("incompatible array types");
            }
            if (token == 43 && dim1 == 0 && (type1 == 307 || this.exprType == 307)) {
                this.atStringConcatExpr(expr, type1, dim1, cname1);
                return;
            }
            this.atArithBinExpr(expr, token, k, type1);
            return;
        }
        if (!this.booleanExpr(true, expr)) {
            this.bytecode.addIndex(7);
            this.bytecode.addIconst(0);
            this.bytecode.addOpcode(167);
            this.bytecode.addIndex(4);
        }
        this.bytecode.addIconst(1);
    }

    private void atArithBinExpr(Expr expr, int token, int index, int type1) throws CompileError {
        int op;
        if (this.arrayDim != 0) {
            CodeGen.badTypes(expr);
        }
        int type2 = this.exprType;
        if (token == 364 || token == 366 || token == 370) {
            if (type2 == 324 || type2 == 334 || type2 == 306 || type2 == 303) {
                this.exprType = type1;
            } else {
                CodeGen.badTypes(expr);
            }
        } else {
            this.convertOprandTypes(type1, type2, expr);
        }
        int p = CodeGen.typePrecedence(this.exprType);
        if (p >= 0 && (op = binOp[index + p + 1]) != 0) {
            if (p == 3 && this.exprType != 301) {
                this.exprType = 324;
            }
            this.bytecode.addOpcode(op);
            return;
        }
        CodeGen.badTypes(expr);
    }

    private void atStringConcatExpr(Expr expr, int type1, int dim1, String cname1) throws CompileError {
        boolean type2IsString;
        int type2 = this.exprType;
        int dim2 = this.arrayDim;
        boolean type2Is2 = CodeGen.is2word(type2, dim2);
        boolean bl = type2IsString = type2 == 307 && jvmJavaLangString.equals(this.className);
        if (type2Is2) {
            this.convToString(type2, dim2);
        }
        if (CodeGen.is2word(type1, dim1)) {
            this.bytecode.addOpcode(91);
            this.bytecode.addOpcode(87);
        } else {
            this.bytecode.addOpcode(95);
        }
        this.convToString(type1, dim1);
        this.bytecode.addOpcode(95);
        if (!type2Is2 && !type2IsString) {
            this.convToString(type2, dim2);
        }
        this.bytecode.addInvokevirtual(javaLangString, "concat", "(Ljava/lang/String;)Ljava/lang/String;");
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = jvmJavaLangString;
    }

    private void convToString(int type, int dim) throws CompileError {
        String method = "valueOf";
        if (CodeGen.isRefType(type) || dim > 0) {
            this.bytecode.addInvokestatic(javaLangString, "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
            return;
        }
        if (type == 312) {
            this.bytecode.addInvokestatic(javaLangString, "valueOf", "(D)Ljava/lang/String;");
            return;
        }
        if (type == 317) {
            this.bytecode.addInvokestatic(javaLangString, "valueOf", "(F)Ljava/lang/String;");
            return;
        }
        if (type == 326) {
            this.bytecode.addInvokestatic(javaLangString, "valueOf", "(J)Ljava/lang/String;");
            return;
        }
        if (type == 301) {
            this.bytecode.addInvokestatic(javaLangString, "valueOf", "(Z)Ljava/lang/String;");
            return;
        }
        if (type == 306) {
            this.bytecode.addInvokestatic(javaLangString, "valueOf", "(C)Ljava/lang/String;");
            return;
        }
        if (type == 344) {
            throw new CompileError("void type expression");
        }
        this.bytecode.addInvokestatic(javaLangString, "valueOf", "(I)Ljava/lang/String;");
    }

    private boolean booleanExpr(boolean branchIf, ASTree expr) throws CompileError {
        int op = CodeGen.getCompOperator(expr);
        if (op == 358) {
            BinExpr bexpr = (BinExpr)expr;
            int type1 = this.compileOprands(bexpr);
            this.compareExpr(branchIf, bexpr.getOperator(), type1, bexpr);
        } else {
            if (op == 33) {
                boolean bl;
                if (!branchIf) {
                    bl = true;
                    return this.booleanExpr(bl, ((Expr)expr).oprand1());
                }
                bl = false;
                return this.booleanExpr(bl, ((Expr)expr).oprand1());
            }
            boolean isAndAnd = op == 369;
            if (isAndAnd || op == 368) {
                BinExpr bexpr;
                if (this.booleanExpr(!isAndAnd, (bexpr = (BinExpr)expr).oprand1())) {
                    this.exprType = 301;
                    this.arrayDim = 0;
                    return true;
                }
                int pc = this.bytecode.currentPc();
                this.bytecode.addIndex(0);
                if (this.booleanExpr(isAndAnd, bexpr.oprand2())) {
                    this.bytecode.addOpcode(167);
                }
                this.bytecode.write16bit(pc, this.bytecode.currentPc() - pc + 3);
                if (branchIf != isAndAnd) {
                    this.bytecode.addIndex(6);
                    this.bytecode.addOpcode(167);
                }
            } else {
                if (CodeGen.isAlwaysBranch(expr, branchIf)) {
                    this.exprType = 301;
                    this.arrayDim = 0;
                    return true;
                }
                expr.accept(this);
                if (this.exprType != 301) throw new CompileError("boolean expr is required");
                if (this.arrayDim != 0) {
                    throw new CompileError("boolean expr is required");
                }
                this.bytecode.addOpcode(branchIf ? 154 : 153);
            }
        }
        this.exprType = 301;
        this.arrayDim = 0;
        return false;
    }

    private static boolean isAlwaysBranch(ASTree expr, boolean branchIf) {
        if (!(expr instanceof Keyword)) return false;
        int t = ((Keyword)expr).get();
        if (branchIf) {
            if (t != 410) return false;
            return true;
        }
        if (t != 411) return false;
        return true;
    }

    static int getCompOperator(ASTree expr) throws CompileError {
        if (!(expr instanceof Expr)) return 32;
        Expr bexpr = (Expr)expr;
        int token = bexpr.getOperator();
        if (token == 33) {
            return 33;
        }
        if (!(bexpr instanceof BinExpr)) return token;
        if (token == 368) return token;
        if (token == 369) return token;
        if (token == 38) return token;
        if (token == 124) return token;
        return 358;
    }

    private int compileOprands(BinExpr expr) throws CompileError {
        expr.oprand1().accept(this);
        int type1 = this.exprType;
        int dim1 = this.arrayDim;
        expr.oprand2().accept(this);
        if (dim1 != this.arrayDim) {
            if (type1 != 412 && this.exprType != 412) {
                throw new CompileError("incompatible array types");
            }
            if (this.exprType == 412) {
                this.arrayDim = dim1;
            }
        }
        if (type1 != 412) return type1;
        return this.exprType;
    }

    private void compareExpr(boolean branchIf, int token, int type1, BinExpr expr) throws CompileError {
        int n;
        int p;
        if (this.arrayDim == 0) {
            this.convertOprandTypes(type1, this.exprType, expr);
        }
        if ((p = CodeGen.typePrecedence(this.exprType)) == -1 || this.arrayDim > 0) {
            if (token == 358) {
                this.bytecode.addOpcode(branchIf ? 165 : 166);
                return;
            }
            if (token != 350) {
                CodeGen.badTypes(expr);
                return;
            }
            this.bytecode.addOpcode(branchIf ? 166 : 165);
            return;
        }
        if (p != 3) {
            if (p == 0) {
                if (token == 60 || token == 357) {
                    this.bytecode.addOpcode(152);
                } else {
                    this.bytecode.addOpcode(151);
                }
            } else if (p == 1) {
                if (token == 60 || token == 357) {
                    this.bytecode.addOpcode(150);
                } else {
                    this.bytecode.addOpcode(149);
                }
            } else if (p == 2) {
                this.bytecode.addOpcode(148);
            } else {
                CodeGen.fatal();
            }
        } else {
            int n2;
            int[] op = ifOp;
            int i = 0;
            while (true) {
                if (i >= op.length) {
                    CodeGen.badTypes(expr);
                    return;
                }
                if (op[i] == token) {
                    if (branchIf) {
                        n2 = 1;
                        break;
                    }
                    n2 = 2;
                    break;
                }
                i += 3;
            }
            this.bytecode.addOpcode(op[i + n2]);
            return;
        }
        int[] op = ifOp2;
        int i = 0;
        while (true) {
            if (i >= op.length) {
                CodeGen.badTypes(expr);
                return;
            }
            if (op[i] == token) {
                if (branchIf) {
                    n = 1;
                    break;
                }
                n = 2;
                break;
            }
            i += 3;
        }
        this.bytecode.addOpcode(op[i + n]);
    }

    protected static void badTypes(Expr expr) throws CompileError {
        throw new CompileError("invalid types for " + expr.getName());
    }

    protected static boolean isRefType(int type) {
        if (type == 307) return true;
        if (type == 412) return true;
        return false;
    }

    private static int typePrecedence(int type) {
        if (type == 312) {
            return 0;
        }
        if (type == 317) {
            return 1;
        }
        if (type == 326) {
            return 2;
        }
        if (CodeGen.isRefType(type)) {
            return -1;
        }
        if (type != 344) return 3;
        return -1;
    }

    static boolean isP_INT(int type) {
        if (CodeGen.typePrecedence(type) != 3) return false;
        return true;
    }

    static boolean rightIsStrong(int type1, int type2) {
        int type1_p = CodeGen.typePrecedence(type1);
        int type2_p = CodeGen.typePrecedence(type2);
        if (type1_p < 0) return false;
        if (type2_p < 0) return false;
        if (type1_p <= type2_p) return false;
        return true;
    }

    private void convertOprandTypes(int type1, int type2, Expr expr) throws CompileError {
        int result_type;
        int op;
        boolean rightStrong;
        int type1_p = CodeGen.typePrecedence(type1);
        int type2_p = CodeGen.typePrecedence(type2);
        if (type2_p < 0 && type1_p < 0) {
            return;
        }
        if (type2_p < 0 || type1_p < 0) {
            CodeGen.badTypes(expr);
        }
        if (type1_p <= type2_p) {
            rightStrong = false;
            this.exprType = type1;
            op = castOp[type2_p * 4 + type1_p];
            result_type = type1_p;
        } else {
            rightStrong = true;
            op = castOp[type1_p * 4 + type2_p];
            result_type = type2_p;
        }
        if (!rightStrong) {
            if (op == 0) return;
            this.bytecode.addOpcode(op);
            return;
        }
        if (result_type == 0 || result_type == 2) {
            if (type1_p == 0 || type1_p == 2) {
                this.bytecode.addOpcode(94);
            } else {
                this.bytecode.addOpcode(93);
            }
            this.bytecode.addOpcode(88);
            this.bytecode.addOpcode(op);
            this.bytecode.addOpcode(94);
            this.bytecode.addOpcode(88);
            return;
        }
        if (result_type != 1) {
            CodeGen.fatal();
            return;
        }
        if (type1_p == 2) {
            this.bytecode.addOpcode(91);
            this.bytecode.addOpcode(87);
        } else {
            this.bytecode.addOpcode(95);
        }
        this.bytecode.addOpcode(op);
        this.bytecode.addOpcode(95);
    }

    @Override
    public void atCastExpr(CastExpr expr) throws CompileError {
        String cname = this.resolveClassName(expr.getClassName());
        String toClass = this.checkCastExpr(expr, cname);
        int srcType = this.exprType;
        this.exprType = expr.getType();
        this.arrayDim = expr.getArrayDim();
        this.className = cname;
        if (toClass == null) {
            this.atNumCastExpr(srcType, this.exprType);
            return;
        }
        this.bytecode.addCheckcast(toClass);
    }

    @Override
    public void atInstanceOfExpr(InstanceOfExpr expr) throws CompileError {
        String cname = this.resolveClassName(expr.getClassName());
        String toClass = this.checkCastExpr(expr, cname);
        this.bytecode.addInstanceof(toClass);
        this.exprType = 301;
        this.arrayDim = 0;
    }

    private String checkCastExpr(CastExpr expr, String name) throws CompileError {
        String msg = "invalid cast";
        ASTree oprand = expr.getOprand();
        int dim = expr.getArrayDim();
        int type = expr.getType();
        oprand.accept(this);
        int srcType = this.exprType;
        int srcDim = this.arrayDim;
        if (this.invalidDim(srcType, this.arrayDim, this.className, type, dim, name, true)) throw new CompileError("invalid cast");
        if (srcType == 344) throw new CompileError("invalid cast");
        if (type == 344) {
            throw new CompileError("invalid cast");
        }
        if (type == 307) {
            if (CodeGen.isRefType(srcType)) return CodeGen.toJvmArrayName(name, dim);
            if (srcDim != 0) return CodeGen.toJvmArrayName(name, dim);
            throw new CompileError("invalid cast");
        }
        if (dim <= 0) return null;
        return CodeGen.toJvmTypeName(type, dim);
    }

    void atNumCastExpr(int srcType, int destType) throws CompileError {
        if (srcType == destType) {
            return;
        }
        int stype = CodeGen.typePrecedence(srcType);
        int dtype = CodeGen.typePrecedence(destType);
        int op = 0 <= stype && stype < 3 ? castOp[stype * 4 + dtype] : 0;
        int op2 = destType == 312 ? 135 : (destType == 317 ? 134 : (destType == 326 ? 133 : (destType == 334 ? 147 : (destType == 306 ? 146 : (destType == 303 ? 145 : 0)))));
        if (op != 0) {
            this.bytecode.addOpcode(op);
        }
        if (op != 0 && op != 136 && op != 139) {
            if (op != 142) return;
        }
        if (op2 == 0) return;
        this.bytecode.addOpcode(op2);
    }

    @Override
    public void atExpr(Expr expr) throws CompileError {
        int token = expr.getOperator();
        ASTree oprand = expr.oprand1();
        if (token == 46) {
            String member = ((Symbol)expr.oprand2()).get();
            if (member.equals("class")) {
                this.atClassObject(expr);
                return;
            }
            this.atFieldRead(expr);
            return;
        }
        if (token == 35) {
            this.atFieldRead(expr);
            return;
        }
        if (token == 65) {
            this.atArrayRead(oprand, expr.oprand2());
            return;
        }
        if (token == 362 || token == 363) {
            this.atPlusPlus(token, oprand, expr, true);
            return;
        }
        if (token == 33) {
            if (!this.booleanExpr(false, expr)) {
                this.bytecode.addIndex(7);
                this.bytecode.addIconst(1);
                this.bytecode.addOpcode(167);
                this.bytecode.addIndex(4);
            }
            this.bytecode.addIconst(0);
            return;
        }
        if (token == 67) {
            CodeGen.fatal();
            return;
        }
        expr.oprand1().accept(this);
        int type = CodeGen.typePrecedence(this.exprType);
        if (this.arrayDim > 0) {
            CodeGen.badType(expr);
        }
        if (token == 45) {
            if (type == 0) {
                this.bytecode.addOpcode(119);
                return;
            }
            if (type == 1) {
                this.bytecode.addOpcode(118);
                return;
            }
            if (type == 2) {
                this.bytecode.addOpcode(117);
                return;
            }
            if (type == 3) {
                this.bytecode.addOpcode(116);
                this.exprType = 324;
                return;
            }
            CodeGen.badType(expr);
            return;
        }
        if (token == 126) {
            if (type == 3) {
                this.bytecode.addIconst(-1);
                this.bytecode.addOpcode(130);
                this.exprType = 324;
                return;
            }
            if (type == 2) {
                this.bytecode.addLconst(-1L);
                this.bytecode.addOpcode(131);
                return;
            }
            CodeGen.badType(expr);
            return;
        }
        if (token == 43) {
            if (type != -1) return;
            CodeGen.badType(expr);
            return;
        }
        CodeGen.fatal();
    }

    protected static void badType(Expr expr) throws CompileError {
        throw new CompileError("invalid type for " + expr.getName());
    }

    @Override
    public abstract void atCallExpr(CallExpr var1) throws CompileError;

    protected abstract void atFieldRead(ASTree var1) throws CompileError;

    public void atClassObject(Expr expr) throws CompileError {
        ASTree op1 = expr.oprand1();
        if (!(op1 instanceof Symbol)) {
            throw new CompileError("fatal error: badly parsed .class expr");
        }
        String cname = ((Symbol)op1).get();
        if (cname.startsWith("[")) {
            String name2;
            String name;
            int i = cname.indexOf("[L");
            if (i >= 0 && !(name = cname.substring(i + 2, cname.length() - 1)).equals(name2 = this.resolveClassName(name))) {
                name2 = MemberResolver.jvmToJavaName(name2);
                StringBuffer sbuf = new StringBuffer();
                while (i-- >= 0) {
                    sbuf.append('[');
                }
                sbuf.append('L').append(name2).append(';');
                cname = sbuf.toString();
            }
        } else {
            cname = this.resolveClassName(MemberResolver.javaToJvmName(cname));
            cname = MemberResolver.jvmToJavaName(cname);
        }
        this.atClassObject2(cname);
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/Class";
    }

    protected void atClassObject2(String cname) throws CompileError {
        int start = this.bytecode.currentPc();
        this.bytecode.addLdc(cname);
        this.bytecode.addInvokestatic("java.lang.Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
        int end = this.bytecode.currentPc();
        this.bytecode.addOpcode(167);
        int pc = this.bytecode.currentPc();
        this.bytecode.addIndex(0);
        this.bytecode.addExceptionHandler(start, end, this.bytecode.currentPc(), "java.lang.ClassNotFoundException");
        this.bytecode.growStack(1);
        this.bytecode.addInvokestatic("com.viaversion.viaversion.libs.javassist.runtime.DotClass", "fail", "(Ljava/lang/ClassNotFoundException;)Ljava/lang/NoClassDefFoundError;");
        this.bytecode.addOpcode(191);
        this.bytecode.write16bit(pc, this.bytecode.currentPc() - pc + 1);
    }

    public void atArrayRead(ASTree array, ASTree index) throws CompileError {
        this.arrayAccess(array, index);
        this.bytecode.addOpcode(CodeGen.getArrayReadOp(this.exprType, this.arrayDim));
    }

    protected void arrayAccess(ASTree array, ASTree index) throws CompileError {
        array.accept(this);
        int type = this.exprType;
        int dim = this.arrayDim;
        if (dim == 0) {
            throw new CompileError("bad array access");
        }
        String cname = this.className;
        index.accept(this);
        if (CodeGen.typePrecedence(this.exprType) != 3) throw new CompileError("bad array index");
        if (this.arrayDim > 0) {
            throw new CompileError("bad array index");
        }
        this.exprType = type;
        this.arrayDim = dim - 1;
        this.className = cname;
    }

    protected static int getArrayReadOp(int type, int dim) {
        if (dim > 0) {
            return 50;
        }
        switch (type) {
            case 312: {
                return 49;
            }
            case 317: {
                return 48;
            }
            case 326: {
                return 47;
            }
            case 324: {
                return 46;
            }
            case 334: {
                return 53;
            }
            case 306: {
                return 52;
            }
            case 301: 
            case 303: {
                return 51;
            }
        }
        return 50;
    }

    protected static int getArrayWriteOp(int type, int dim) {
        if (dim > 0) {
            return 83;
        }
        switch (type) {
            case 312: {
                return 82;
            }
            case 317: {
                return 81;
            }
            case 326: {
                return 80;
            }
            case 324: {
                return 79;
            }
            case 334: {
                return 86;
            }
            case 306: {
                return 85;
            }
            case 301: 
            case 303: {
                return 84;
            }
        }
        return 83;
    }

    private void atPlusPlus(int token, ASTree oprand, Expr expr, boolean doDup) throws CompileError {
        Expr e;
        boolean isPost;
        boolean bl = isPost = oprand == null;
        if (isPost) {
            oprand = expr.oprand2();
        }
        if (oprand instanceof Variable) {
            int delta;
            Declarator d = ((Variable)oprand).getDeclarator();
            int t = this.exprType = d.getType();
            this.arrayDim = d.getArrayDim();
            int var = this.getLocalVar(d);
            if (this.arrayDim > 0) {
                CodeGen.badType(expr);
            }
            if (t == 312) {
                this.bytecode.addDload(var);
                if (doDup && isPost) {
                    this.bytecode.addOpcode(92);
                }
                this.bytecode.addDconst(1.0);
                this.bytecode.addOpcode(token == 362 ? 99 : 103);
                if (doDup && !isPost) {
                    this.bytecode.addOpcode(92);
                }
                this.bytecode.addDstore(var);
                return;
            }
            if (t == 326) {
                this.bytecode.addLload(var);
                if (doDup && isPost) {
                    this.bytecode.addOpcode(92);
                }
                this.bytecode.addLconst(1L);
                this.bytecode.addOpcode(token == 362 ? 97 : 101);
                if (doDup && !isPost) {
                    this.bytecode.addOpcode(92);
                }
                this.bytecode.addLstore(var);
                return;
            }
            if (t == 317) {
                this.bytecode.addFload(var);
                if (doDup && isPost) {
                    this.bytecode.addOpcode(89);
                }
                this.bytecode.addFconst(1.0f);
                this.bytecode.addOpcode(token == 362 ? 98 : 102);
                if (doDup && !isPost) {
                    this.bytecode.addOpcode(89);
                }
                this.bytecode.addFstore(var);
                return;
            }
            if (t != 303 && t != 306 && t != 334 && t != 324) {
                CodeGen.badType(expr);
                return;
            }
            if (doDup && isPost) {
                this.bytecode.addIload(var);
            }
            int n = delta = token == 362 ? 1 : -1;
            if (var > 255) {
                this.bytecode.addOpcode(196);
                this.bytecode.addOpcode(132);
                this.bytecode.addIndex(var);
                this.bytecode.addIndex(delta);
            } else {
                this.bytecode.addOpcode(132);
                this.bytecode.add(var);
                this.bytecode.add(delta);
            }
            if (!doDup) return;
            if (isPost) return;
            this.bytecode.addIload(var);
            return;
        }
        if (oprand instanceof Expr && (e = (Expr)oprand).getOperator() == 65) {
            this.atArrayPlusPlus(token, isPost, e, doDup);
            return;
        }
        this.atFieldPlusPlus(token, isPost, oprand, expr, doDup);
    }

    public void atArrayPlusPlus(int token, boolean isPost, Expr expr, boolean doDup) throws CompileError {
        this.arrayAccess(expr.oprand1(), expr.oprand2());
        int t = this.exprType;
        int dim = this.arrayDim;
        if (dim > 0) {
            CodeGen.badType(expr);
        }
        this.bytecode.addOpcode(92);
        this.bytecode.addOpcode(CodeGen.getArrayReadOp(t, this.arrayDim));
        int dup_code = CodeGen.is2word(t, dim) ? 94 : 91;
        this.atPlusPlusCore(dup_code, doDup, token, isPost, expr);
        this.bytecode.addOpcode(CodeGen.getArrayWriteOp(t, dim));
    }

    protected void atPlusPlusCore(int dup_code, boolean doDup, int token, boolean isPost, Expr expr) throws CompileError {
        int t = this.exprType;
        if (doDup && isPost) {
            this.bytecode.addOpcode(dup_code);
        }
        if (t == 324 || t == 303 || t == 306 || t == 334) {
            this.bytecode.addIconst(1);
            this.bytecode.addOpcode(token == 362 ? 96 : 100);
            this.exprType = 324;
        } else if (t == 326) {
            this.bytecode.addLconst(1L);
            this.bytecode.addOpcode(token == 362 ? 97 : 101);
        } else if (t == 317) {
            this.bytecode.addFconst(1.0f);
            this.bytecode.addOpcode(token == 362 ? 98 : 102);
        } else if (t == 312) {
            this.bytecode.addDconst(1.0);
            this.bytecode.addOpcode(token == 362 ? 99 : 103);
        } else {
            CodeGen.badType(expr);
        }
        if (!doDup) return;
        if (isPost) return;
        this.bytecode.addOpcode(dup_code);
    }

    protected abstract void atFieldPlusPlus(int var1, boolean var2, ASTree var3, Expr var4, boolean var5) throws CompileError;

    @Override
    public abstract void atMember(Member var1) throws CompileError;

    @Override
    public void atVariable(Variable v) throws CompileError {
        Declarator d = v.getDeclarator();
        this.exprType = d.getType();
        this.arrayDim = d.getArrayDim();
        this.className = d.getClassName();
        int var = this.getLocalVar(d);
        if (this.arrayDim > 0) {
            this.bytecode.addAload(var);
            return;
        }
        switch (this.exprType) {
            case 307: {
                this.bytecode.addAload(var);
                return;
            }
            case 326: {
                this.bytecode.addLload(var);
                return;
            }
            case 317: {
                this.bytecode.addFload(var);
                return;
            }
            case 312: {
                this.bytecode.addDload(var);
                return;
            }
        }
        this.bytecode.addIload(var);
    }

    @Override
    public void atKeyword(Keyword k) throws CompileError {
        this.arrayDim = 0;
        int token = k.get();
        switch (token) {
            case 410: {
                this.bytecode.addIconst(1);
                this.exprType = 301;
                return;
            }
            case 411: {
                this.bytecode.addIconst(0);
                this.exprType = 301;
                return;
            }
            case 412: {
                this.bytecode.addOpcode(1);
                this.exprType = 412;
                return;
            }
            case 336: 
            case 339: {
                if (this.inStaticMethod) {
                    String string;
                    StringBuilder stringBuilder = new StringBuilder().append("not-available: ");
                    if (token == 339) {
                        string = "this";
                        throw new CompileError(stringBuilder.append(string).toString());
                    }
                    string = "super";
                    throw new CompileError(stringBuilder.append(string).toString());
                }
                this.bytecode.addAload(0);
                this.exprType = 307;
                if (token == 339) {
                    this.className = this.getThisName();
                    return;
                }
                this.className = this.getSuperName();
                return;
            }
        }
        CodeGen.fatal();
    }

    @Override
    public void atStringL(StringL s) throws CompileError {
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = jvmJavaLangString;
        this.bytecode.addLdc(s.get());
    }

    @Override
    public void atIntConst(IntConst i) throws CompileError {
        this.arrayDim = 0;
        long value = i.get();
        int type = i.getType();
        if (type != 402 && type != 401) {
            this.exprType = 326;
            this.bytecode.addLconst(value);
            return;
        }
        this.exprType = type == 402 ? 324 : 306;
        this.bytecode.addIconst((int)value);
    }

    @Override
    public void atDoubleConst(DoubleConst d) throws CompileError {
        this.arrayDim = 0;
        if (d.getType() == 405) {
            this.exprType = 312;
            this.bytecode.addDconst(d.get());
            return;
        }
        this.exprType = 317;
        this.bytecode.addFconst((float)d.get());
    }

    protected static abstract class ReturnHook {
        ReturnHook next;

        protected abstract boolean doit(Bytecode var1, int var2);

        protected ReturnHook(CodeGen gen) {
            this.next = gen.returnHooks;
            gen.returnHooks = this;
        }

        protected void remove(CodeGen gen) {
            gen.returnHooks = this.next;
        }
    }
}

