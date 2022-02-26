/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.compiler;

import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.CtField;
import com.viaversion.viaversion.libs.javassist.Modifier;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.FieldInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.MethodInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.Opcode;
import com.viaversion.viaversion.libs.javassist.compiler.CodeGen;
import com.viaversion.viaversion.libs.javassist.compiler.CompileError;
import com.viaversion.viaversion.libs.javassist.compiler.MemberResolver;
import com.viaversion.viaversion.libs.javassist.compiler.NoFieldException;
import com.viaversion.viaversion.libs.javassist.compiler.TokenId;
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
import com.viaversion.viaversion.libs.javassist.compiler.ast.InstanceOfExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.IntConst;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Keyword;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Member;
import com.viaversion.viaversion.libs.javassist.compiler.ast.NewExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.StringL;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Symbol;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Variable;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Visitor;

public class TypeChecker
extends Visitor
implements Opcode,
TokenId {
    static final String javaLangObject = "java.lang.Object";
    static final String jvmJavaLangObject = "java/lang/Object";
    static final String jvmJavaLangString = "java/lang/String";
    static final String jvmJavaLangClass = "java/lang/Class";
    protected int exprType;
    protected int arrayDim;
    protected String className;
    protected MemberResolver resolver;
    protected CtClass thisClass;
    protected MethodInfo thisMethod;

    public TypeChecker(CtClass cc, ClassPool cp) {
        this.resolver = new MemberResolver(cp);
        this.thisClass = cc;
        this.thisMethod = null;
    }

    protected static String argTypesToString(int[] types, int[] dims, String[] cnames) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('(');
        int n = types.length;
        if (n > 0) {
            int i = 0;
            while (true) {
                TypeChecker.typeToString(sbuf, types[i], dims[i], cnames[i]);
                if (++i >= n) break;
                sbuf.append(',');
            }
        }
        sbuf.append(')');
        return sbuf.toString();
    }

    protected static StringBuffer typeToString(StringBuffer sbuf, int type, int dim, String cname) {
        String s;
        if (type == 307) {
            s = MemberResolver.jvmToJavaName(cname);
        } else if (type == 412) {
            s = "Object";
        } else {
            try {
                s = MemberResolver.getTypeName(type);
            }
            catch (CompileError e) {
                s = "?";
            }
        }
        sbuf.append(s);
        while (dim-- > 0) {
            sbuf.append("[]");
        }
        return sbuf;
    }

    public void setThisMethod(MethodInfo m) {
        this.thisMethod = m;
    }

    protected static void fatal() throws CompileError {
        throw new CompileError("fatal");
    }

    protected String getThisName() {
        return MemberResolver.javaToJvmName(this.thisClass.getName());
    }

    protected String getSuperName() throws CompileError {
        return MemberResolver.javaToJvmName(MemberResolver.getSuperclass(this.thisClass).getName());
    }

    protected String resolveClassName(ASTList name) throws CompileError {
        return this.resolver.resolveClassName(name);
    }

    protected String resolveClassName(String jvmName) throws CompileError {
        return this.resolver.resolveJvmClassName(jvmName);
    }

    @Override
    public void atNewExpr(NewExpr expr) throws CompileError {
        if (expr.isArray()) {
            this.atNewArrayExpr(expr);
            return;
        }
        CtClass clazz = this.resolver.lookupClassByName(expr.getClassName());
        String cname = clazz.getName();
        ASTList args = expr.getArguments();
        this.atMethodCallCore(clazz, "<init>", args);
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = MemberResolver.javaToJvmName(cname);
    }

    public void atNewArrayExpr(NewExpr expr) throws CompileError {
        int type = expr.getArrayType();
        ASTList size = expr.getArraySize();
        ASTList classname = expr.getClassName();
        ArrayInit init = expr.getInitializer();
        if (init != null) {
            ((ASTree)init).accept(this);
        }
        if (size.length() > 1) {
            this.atMultiNewArray(type, classname, size);
            return;
        }
        ASTree sizeExpr = size.head();
        if (sizeExpr != null) {
            sizeExpr.accept(this);
        }
        this.exprType = type;
        this.arrayDim = 1;
        if (type == 307) {
            this.className = this.resolveClassName(classname);
            return;
        }
        this.className = null;
    }

    @Override
    public void atArrayInit(ArrayInit init) throws CompileError {
        ASTList list = init;
        while (list != null) {
            ASTree h = list.head();
            list = list.tail();
            if (h == null) continue;
            h.accept(this);
        }
    }

    protected void atMultiNewArray(int type, ASTList classname, ASTList size) throws CompileError {
        ASTree s;
        int dim = size.length();
        int count = 0;
        while (size != null && (s = size.head()) != null) {
            ++count;
            s.accept(this);
            size = size.tail();
        }
        this.exprType = type;
        this.arrayDim = dim;
        if (type == 307) {
            this.className = this.resolveClassName(classname);
            return;
        }
        this.className = null;
    }

    @Override
    public void atAssignExpr(AssignExpr expr) throws CompileError {
        Expr e;
        int op = expr.getOperator();
        ASTree left = expr.oprand1();
        ASTree right = expr.oprand2();
        if (left instanceof Variable) {
            this.atVariableAssign(expr, op, (Variable)left, ((Variable)left).getDeclarator(), right);
            return;
        }
        if (left instanceof Expr && (e = (Expr)left).getOperator() == 65) {
            this.atArrayAssign(expr, op, (Expr)left, right);
            return;
        }
        this.atFieldAssign(expr, op, left, right);
    }

    private void atVariableAssign(Expr expr, int op, Variable var, Declarator d, ASTree right) throws CompileError {
        int varType = d.getType();
        int varArray = d.getArrayDim();
        String varClass = d.getClassName();
        if (op != 61) {
            this.atVariable(var);
        }
        right.accept(this);
        this.exprType = varType;
        this.arrayDim = varArray;
        this.className = varClass;
    }

    private void atArrayAssign(Expr expr, int op, Expr array, ASTree right) throws CompileError {
        this.atArrayRead(array.oprand1(), array.oprand2());
        int aType = this.exprType;
        int aDim = this.arrayDim;
        String cname = this.className;
        right.accept(this);
        this.exprType = aType;
        this.arrayDim = aDim;
        this.className = cname;
    }

    protected void atFieldAssign(Expr expr, int op, ASTree left, ASTree right) throws CompileError {
        CtField f = this.fieldAccess(left);
        this.atFieldRead(f);
        int fType = this.exprType;
        int fDim = this.arrayDim;
        String cname = this.className;
        right.accept(this);
        this.exprType = fType;
        this.arrayDim = fDim;
        this.className = cname;
    }

    @Override
    public void atCondExpr(CondExpr expr) throws CompileError {
        this.booleanExpr(expr.condExpr());
        expr.thenExpr().accept(this);
        int type1 = this.exprType;
        int dim1 = this.arrayDim;
        String cname1 = this.className;
        expr.elseExpr().accept(this);
        if (dim1 != 0) return;
        if (dim1 != this.arrayDim) return;
        if (CodeGen.rightIsStrong(type1, this.exprType)) {
            expr.setThen(new CastExpr(this.exprType, 0, expr.thenExpr()));
            return;
        }
        if (!CodeGen.rightIsStrong(this.exprType, type1)) return;
        expr.setElse(new CastExpr(type1, 0, expr.elseExpr()));
        this.exprType = type1;
    }

    @Override
    public void atBinExpr(BinExpr expr) throws CompileError {
        int token = expr.getOperator();
        int k = CodeGen.lookupBinOp(token);
        if (k < 0) {
            this.booleanExpr(expr);
            return;
        }
        if (token == 43) {
            Expr e = this.atPlusExpr(expr);
            if (e == null) return;
            e = CallExpr.makeCall(Expr.make(46, (ASTree)e, (ASTree)new Member("toString")), null);
            expr.setOprand1(e);
            expr.setOprand2(null);
            this.className = jvmJavaLangString;
            return;
        }
        ASTree left = expr.oprand1();
        ASTree right = expr.oprand2();
        left.accept(this);
        int type1 = this.exprType;
        right.accept(this);
        if (this.isConstant(expr, token, left, right)) return;
        this.computeBinExprType(expr, token, type1);
    }

    private Expr atPlusExpr(BinExpr expr) throws CompileError {
        ASTree left = expr.oprand1();
        ASTree right = expr.oprand2();
        if (right == null) {
            left.accept(this);
            return null;
        }
        if (TypeChecker.isPlusExpr(left)) {
            Expr newExpr = this.atPlusExpr((BinExpr)left);
            if (newExpr != null) {
                right.accept(this);
                this.exprType = 307;
                this.arrayDim = 0;
                this.className = "java/lang/StringBuffer";
                return TypeChecker.makeAppendCall(newExpr, right);
            }
        } else {
            left.accept(this);
        }
        int type1 = this.exprType;
        int dim1 = this.arrayDim;
        String cname = this.className;
        right.accept(this);
        if (this.isConstant(expr, 43, left, right)) {
            return null;
        }
        if (type1 == 307 && dim1 == 0 && jvmJavaLangString.equals(cname) || this.exprType == 307 && this.arrayDim == 0 && jvmJavaLangString.equals(this.className)) {
            ASTList sbufClass = ASTList.make(new Symbol("java"), new Symbol("lang"), new Symbol("StringBuffer"));
            NewExpr e = new NewExpr(sbufClass, null);
            this.exprType = 307;
            this.arrayDim = 0;
            this.className = "java/lang/StringBuffer";
            return TypeChecker.makeAppendCall(TypeChecker.makeAppendCall(e, left), right);
        }
        this.computeBinExprType(expr, 43, type1);
        return null;
    }

    private boolean isConstant(BinExpr expr, int op, ASTree left, ASTree right) throws CompileError {
        left = TypeChecker.stripPlusExpr(left);
        right = TypeChecker.stripPlusExpr(right);
        ASTree newExpr = null;
        if (left instanceof StringL && right instanceof StringL && op == 43) {
            newExpr = new StringL(((StringL)left).get() + ((StringL)right).get());
        } else if (left instanceof IntConst) {
            newExpr = ((IntConst)left).compute(op, right);
        } else if (left instanceof DoubleConst) {
            newExpr = ((DoubleConst)left).compute(op, right);
        }
        if (newExpr == null) {
            return false;
        }
        expr.setOperator(43);
        expr.setOprand1(newExpr);
        expr.setOprand2(null);
        newExpr.accept(this);
        return true;
    }

    static ASTree stripPlusExpr(ASTree expr) {
        if (expr instanceof BinExpr) {
            BinExpr e = (BinExpr)expr;
            if (e.getOperator() != 43) return expr;
            if (e.oprand2() != null) return expr;
            return e.getLeft();
        }
        if (!(expr instanceof Expr)) {
            if (!(expr instanceof Member)) return expr;
            ASTree cexpr = TypeChecker.getConstantFieldValue((Member)expr);
            if (cexpr == null) return expr;
            return cexpr;
        }
        Expr e = (Expr)expr;
        int op = e.getOperator();
        if (op == 35) {
            ASTree cexpr = TypeChecker.getConstantFieldValue((Member)e.oprand2());
            if (cexpr == null) return expr;
            return cexpr;
        }
        if (op != 43) return expr;
        if (e.getRight() != null) return expr;
        return e.getLeft();
    }

    private static ASTree getConstantFieldValue(Member mem) {
        return TypeChecker.getConstantFieldValue(mem.getField());
    }

    public static ASTree getConstantFieldValue(CtField f) {
        int n;
        if (f == null) {
            return null;
        }
        Object value = f.getConstantValue();
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return new StringL((String)value);
        }
        if (value instanceof Double || value instanceof Float) {
            int token = value instanceof Double ? 405 : 404;
            return new DoubleConst(((Number)value).doubleValue(), token);
        }
        if (value instanceof Number) {
            int token = value instanceof Long ? 403 : 402;
            return new IntConst(((Number)value).longValue(), token);
        }
        if (!(value instanceof Boolean)) return null;
        if (((Boolean)value).booleanValue()) {
            n = 410;
            return new Keyword(n);
        }
        n = 411;
        return new Keyword(n);
    }

    private static boolean isPlusExpr(ASTree expr) {
        if (!(expr instanceof BinExpr)) return false;
        BinExpr bexpr = (BinExpr)expr;
        int token = bexpr.getOperator();
        if (token != 43) return false;
        return true;
    }

    private static Expr makeAppendCall(ASTree target, ASTree arg) {
        return CallExpr.makeCall(Expr.make(46, target, (ASTree)new Member("append")), new ASTList(arg));
    }

    private void computeBinExprType(BinExpr expr, int token, int type1) throws CompileError {
        int type2 = this.exprType;
        if (token == 364 || token == 366 || token == 370) {
            this.exprType = type1;
        } else {
            this.insertCast(expr, type1, type2);
        }
        if (!CodeGen.isP_INT(this.exprType)) return;
        if (this.exprType == 301) return;
        this.exprType = 324;
    }

    private void booleanExpr(ASTree expr) throws CompileError {
        int op = CodeGen.getCompOperator(expr);
        if (op == 358) {
            BinExpr bexpr = (BinExpr)expr;
            bexpr.oprand1().accept(this);
            int type1 = this.exprType;
            int dim1 = this.arrayDim;
            bexpr.oprand2().accept(this);
            if (dim1 == 0 && this.arrayDim == 0) {
                this.insertCast(bexpr, type1, this.exprType);
            }
        } else if (op == 33) {
            ((Expr)expr).oprand1().accept(this);
        } else if (op == 369 || op == 368) {
            BinExpr bexpr = (BinExpr)expr;
            bexpr.oprand1().accept(this);
            bexpr.oprand2().accept(this);
        } else {
            expr.accept(this);
        }
        this.exprType = 301;
        this.arrayDim = 0;
    }

    private void insertCast(BinExpr expr, int type1, int type2) throws CompileError {
        if (CodeGen.rightIsStrong(type1, type2)) {
            expr.setLeft(new CastExpr(type2, 0, expr.oprand1()));
            return;
        }
        this.exprType = type1;
    }

    @Override
    public void atCastExpr(CastExpr expr) throws CompileError {
        String cname = this.resolveClassName(expr.getClassName());
        expr.getOprand().accept(this);
        this.exprType = expr.getType();
        this.arrayDim = expr.getArrayDim();
        this.className = cname;
    }

    @Override
    public void atInstanceOfExpr(InstanceOfExpr expr) throws CompileError {
        expr.getOprand().accept(this);
        this.exprType = 301;
        this.arrayDim = 0;
    }

    @Override
    public void atExpr(Expr expr) throws CompileError {
        int token = expr.getOperator();
        ASTree oprand = expr.oprand1();
        if (token == 46) {
            String member = ((Symbol)expr.oprand2()).get();
            if (member.equals("length")) {
                try {
                    this.atArrayLength(expr);
                    return;
                }
                catch (NoFieldException nfe) {
                    this.atFieldRead(expr);
                    return;
                }
            }
            if (member.equals("class")) {
                this.atClassObject(expr);
                return;
            }
            this.atFieldRead(expr);
            return;
        }
        if (token == 35) {
            String member = ((Symbol)expr.oprand2()).get();
            if (member.equals("class")) {
                this.atClassObject(expr);
                return;
            }
            this.atFieldRead(expr);
            return;
        }
        if (token == 65) {
            this.atArrayRead(oprand, expr.oprand2());
            return;
        }
        if (token == 362 || token == 363) {
            this.atPlusPlus(token, oprand, expr);
            return;
        }
        if (token == 33) {
            this.booleanExpr(expr);
            return;
        }
        if (token == 67) {
            TypeChecker.fatal();
            return;
        }
        oprand.accept(this);
        if (this.isConstant(expr, token, oprand)) return;
        if (token != 45) {
            if (token != 126) return;
        }
        if (!CodeGen.isP_INT(this.exprType)) return;
        this.exprType = 324;
    }

    private boolean isConstant(Expr expr, int op, ASTree oprand) {
        if ((oprand = TypeChecker.stripPlusExpr(oprand)) instanceof IntConst) {
            IntConst c = (IntConst)oprand;
            long v = c.get();
            if (op == 45) {
                v = -v;
            } else {
                if (op != 126) return false;
                v ^= 0xFFFFFFFFFFFFFFFFL;
            }
            c.set(v);
        } else {
            if (!(oprand instanceof DoubleConst)) return false;
            DoubleConst c = (DoubleConst)oprand;
            if (op != 45) return false;
            c.set(-c.get());
        }
        expr.setOperator(43);
        return true;
    }

    @Override
    public void atCallExpr(CallExpr expr) throws CompileError {
        String mname = null;
        CtClass targetClass = null;
        ASTree method = expr.oprand1();
        ASTList args = (ASTList)expr.oprand2();
        if (method instanceof Member) {
            mname = ((Member)method).get();
            targetClass = this.thisClass;
        } else if (method instanceof Keyword) {
            mname = "<init>";
            targetClass = ((Keyword)method).get() == 336 ? MemberResolver.getSuperclass(this.thisClass) : this.thisClass;
        } else if (method instanceof Expr) {
            Expr e = (Expr)method;
            mname = ((Symbol)e.oprand2()).get();
            int op = e.getOperator();
            if (op == 35) {
                targetClass = this.resolver.lookupClass(((Symbol)e.oprand1()).get(), false);
            } else if (op == 46) {
                ASTree target = e.oprand1();
                String classFollowedByDotSuper = TypeChecker.isDotSuper(target);
                if (classFollowedByDotSuper != null) {
                    targetClass = MemberResolver.getSuperInterface(this.thisClass, classFollowedByDotSuper);
                } else {
                    try {
                        target.accept(this);
                    }
                    catch (NoFieldException nfe) {
                        if (nfe.getExpr() != target) {
                            throw nfe;
                        }
                        this.exprType = 307;
                        this.arrayDim = 0;
                        this.className = nfe.getField();
                        e.setOperator(35);
                        e.setOprand1(new Symbol(MemberResolver.jvmToJavaName(this.className)));
                    }
                    if (this.arrayDim > 0) {
                        targetClass = this.resolver.lookupClass(javaLangObject, true);
                    } else if (this.exprType == 307) {
                        targetClass = this.resolver.lookupClassByJvmName(this.className);
                    } else {
                        TypeChecker.badMethod();
                    }
                }
            } else {
                TypeChecker.badMethod();
            }
        } else {
            TypeChecker.fatal();
        }
        MemberResolver.Method minfo = this.atMethodCallCore(targetClass, mname, args);
        expr.setMethod(minfo);
    }

    private static void badMethod() throws CompileError {
        throw new CompileError("bad method");
    }

    static String isDotSuper(ASTree target) {
        if (!(target instanceof Expr)) return null;
        Expr e = (Expr)target;
        if (e.getOperator() != 46) return null;
        ASTree right = e.oprand2();
        if (!(right instanceof Keyword)) return null;
        if (((Keyword)right).get() != 336) return null;
        return ((Symbol)e.oprand1()).get();
    }

    public MemberResolver.Method atMethodCallCore(CtClass targetClass, String mname, ASTList args) throws CompileError {
        String msg;
        int nargs = this.getMethodArgsLength(args);
        int[] types = new int[nargs];
        int[] dims = new int[nargs];
        String[] cnames = new String[nargs];
        this.atMethodArgs(args, types, dims, cnames);
        MemberResolver.Method found = this.resolver.lookupMethod(targetClass, this.thisClass, this.thisMethod, mname, types, dims, cnames);
        if (found != null) {
            String desc = found.info.getDescriptor();
            this.setReturnType(desc);
            return found;
        }
        String clazz = targetClass.getName();
        String signature = TypeChecker.argTypesToString(types, dims, cnames);
        if (mname.equals("<init>")) {
            msg = "cannot find constructor " + clazz + signature;
            throw new CompileError(msg);
        }
        msg = mname + signature + " not found in " + clazz;
        throw new CompileError(msg);
    }

    public int getMethodArgsLength(ASTList args) {
        return ASTList.length(args);
    }

    public void atMethodArgs(ASTList args, int[] types, int[] dims, String[] cnames) throws CompileError {
        int i = 0;
        while (args != null) {
            ASTree a = args.head();
            a.accept(this);
            types[i] = this.exprType;
            dims[i] = this.arrayDim;
            cnames[i] = this.className;
            ++i;
            args = args.tail();
        }
    }

    void setReturnType(String desc) throws CompileError {
        int i = desc.indexOf(41);
        if (i < 0) {
            TypeChecker.badMethod();
        }
        char c = desc.charAt(++i);
        int dim = 0;
        while (c == '[') {
            ++dim;
            c = desc.charAt(++i);
        }
        this.arrayDim = dim;
        if (c != 'L') {
            this.exprType = MemberResolver.descToType(c);
            this.className = null;
            return;
        }
        int j = desc.indexOf(59, i + 1);
        if (j < 0) {
            TypeChecker.badMethod();
        }
        this.exprType = 307;
        this.className = desc.substring(i + 1, j);
    }

    private void atFieldRead(ASTree expr) throws CompileError {
        this.atFieldRead(this.fieldAccess(expr));
    }

    private void atFieldRead(CtField f) throws CompileError {
        FieldInfo finfo = f.getFieldInfo2();
        String type = finfo.getDescriptor();
        int i = 0;
        int dim = 0;
        char c = type.charAt(i);
        while (c == '[') {
            ++dim;
            c = type.charAt(++i);
        }
        this.arrayDim = dim;
        this.exprType = MemberResolver.descToType(c);
        if (c == 'L') {
            this.className = type.substring(i + 1, type.indexOf(59, i + 1));
            return;
        }
        this.className = null;
    }

    protected CtField fieldAccess(ASTree expr) throws CompileError {
        if (expr instanceof Member) {
            Member mem = (Member)expr;
            String name = mem.get();
            try {
                CtField f = this.thisClass.getField(name);
                if (!Modifier.isStatic(f.getModifiers())) return f;
                mem.setField(f);
                return f;
            }
            catch (NotFoundException e) {
                throw new NoFieldException(name, expr);
            }
        }
        if (!(expr instanceof Expr)) throw new CompileError("bad field access");
        Expr e = (Expr)expr;
        int op = e.getOperator();
        if (op == 35) {
            Member mem = (Member)e.oprand2();
            CtField f = this.resolver.lookupField(((Symbol)e.oprand1()).get(), mem);
            mem.setField(f);
            return f;
        }
        if (op != 46) throw new CompileError("bad field access");
        try {
            e.oprand1().accept(this);
        }
        catch (NoFieldException nfe) {
            if (nfe.getExpr() == e.oprand1()) return this.fieldAccess2(e, nfe.getField());
            throw nfe;
        }
        CompileError err = null;
        try {
            if (this.exprType == 307 && this.arrayDim == 0) {
                return this.resolver.lookupFieldByJvmName(this.className, (Symbol)e.oprand2());
            }
        }
        catch (CompileError ce) {
            err = ce;
        }
        ASTree oprnd1 = e.oprand1();
        if (oprnd1 instanceof Symbol) {
            return this.fieldAccess2(e, ((Symbol)oprnd1).get());
        }
        if (err == null) throw new CompileError("bad field access");
        throw err;
    }

    private CtField fieldAccess2(Expr e, String jvmClassName) throws CompileError {
        Member fname = (Member)e.oprand2();
        CtField f = this.resolver.lookupFieldByJvmName2(jvmClassName, fname, e);
        e.setOperator(35);
        e.setOprand1(new Symbol(MemberResolver.jvmToJavaName(jvmClassName)));
        fname.setField(f);
        return f;
    }

    public void atClassObject(Expr expr) throws CompileError {
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = jvmJavaLangClass;
    }

    public void atArrayLength(Expr expr) throws CompileError {
        expr.oprand1().accept(this);
        if (this.arrayDim == 0) {
            throw new NoFieldException("length", expr);
        }
        this.exprType = 324;
        this.arrayDim = 0;
    }

    public void atArrayRead(ASTree array, ASTree index) throws CompileError {
        array.accept(this);
        int type = this.exprType;
        int dim = this.arrayDim;
        String cname = this.className;
        index.accept(this);
        this.exprType = type;
        this.arrayDim = dim - 1;
        this.className = cname;
    }

    private void atPlusPlus(int token, ASTree oprand, Expr expr) throws CompileError {
        Expr e;
        boolean isPost;
        boolean bl = isPost = oprand == null;
        if (isPost) {
            oprand = expr.oprand2();
        }
        if (oprand instanceof Variable) {
            Declarator d = ((Variable)oprand).getDeclarator();
            this.exprType = d.getType();
            this.arrayDim = d.getArrayDim();
            return;
        }
        if (oprand instanceof Expr && (e = (Expr)oprand).getOperator() == 65) {
            this.atArrayRead(e.oprand1(), e.oprand2());
            int t = this.exprType;
            if (t != 324 && t != 303 && t != 306) {
                if (t != 334) return;
            }
            this.exprType = 324;
            return;
        }
        this.atFieldPlusPlus(oprand);
    }

    protected void atFieldPlusPlus(ASTree oprand) throws CompileError {
        CtField f = this.fieldAccess(oprand);
        this.atFieldRead(f);
        int t = this.exprType;
        if (t != 324 && t != 303 && t != 306) {
            if (t != 334) return;
        }
        this.exprType = 324;
    }

    @Override
    public void atMember(Member mem) throws CompileError {
        this.atFieldRead(mem);
    }

    @Override
    public void atVariable(Variable v) throws CompileError {
        Declarator d = v.getDeclarator();
        this.exprType = d.getType();
        this.arrayDim = d.getArrayDim();
        this.className = d.getClassName();
    }

    @Override
    public void atKeyword(Keyword k) throws CompileError {
        this.arrayDim = 0;
        int token = k.get();
        switch (token) {
            case 410: 
            case 411: {
                this.exprType = 301;
                return;
            }
            case 412: {
                this.exprType = 412;
                return;
            }
            case 336: 
            case 339: {
                this.exprType = 307;
                if (token == 339) {
                    this.className = this.getThisName();
                    return;
                }
                this.className = this.getSuperName();
                return;
            }
        }
        TypeChecker.fatal();
    }

    @Override
    public void atStringL(StringL s) throws CompileError {
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = jvmJavaLangString;
    }

    @Override
    public void atIntConst(IntConst i) throws CompileError {
        this.arrayDim = 0;
        int type = i.getType();
        if (type != 402 && type != 401) {
            this.exprType = 326;
            return;
        }
        this.exprType = type == 402 ? 324 : 306;
    }

    @Override
    public void atDoubleConst(DoubleConst d) throws CompileError {
        this.arrayDim = 0;
        if (d.getType() == 405) {
            this.exprType = 312;
            return;
        }
        this.exprType = 317;
    }
}

