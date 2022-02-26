/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.compiler;

import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.CtPrimitiveType;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.compiler.CodeGen;
import com.viaversion.viaversion.libs.javassist.compiler.CompileError;
import com.viaversion.viaversion.libs.javassist.compiler.JvstCodeGen;
import com.viaversion.viaversion.libs.javassist.compiler.MemberResolver;
import com.viaversion.viaversion.libs.javassist.compiler.TypeChecker;
import com.viaversion.viaversion.libs.javassist.compiler.ast.ASTList;
import com.viaversion.viaversion.libs.javassist.compiler.ast.ASTree;
import com.viaversion.viaversion.libs.javassist.compiler.ast.CallExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.CastExpr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Expr;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Member;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Symbol;

public class JvstTypeChecker
extends TypeChecker {
    private JvstCodeGen codeGen;

    public JvstTypeChecker(CtClass cc, ClassPool cp, JvstCodeGen gen) {
        super(cc, cp);
        this.codeGen = gen;
    }

    public void addNullIfVoid() {
        if (this.exprType != 344) return;
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/Object";
    }

    @Override
    public void atMember(Member mem) throws CompileError {
        String name = mem.get();
        if (name.equals(this.codeGen.paramArrayName)) {
            this.exprType = 307;
            this.arrayDim = 1;
            this.className = "java/lang/Object";
            return;
        }
        if (name.equals("$sig")) {
            this.exprType = 307;
            this.arrayDim = 1;
            this.className = "java/lang/Class";
            return;
        }
        if (!name.equals("$type") && !name.equals("$class")) {
            super.atMember(mem);
            return;
        }
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/Class";
    }

    @Override
    protected void atFieldAssign(Expr expr, int op, ASTree left, ASTree right) throws CompileError {
        if (left instanceof Member && ((Member)left).get().equals(this.codeGen.paramArrayName)) {
            right.accept(this);
            CtClass[] params = this.codeGen.paramTypeList;
            if (params == null) {
                return;
            }
            int n = params.length;
            int i = 0;
            while (i < n) {
                this.compileUnwrapValue(params[i]);
                ++i;
            }
            return;
        }
        super.atFieldAssign(expr, op, left, right);
    }

    @Override
    public void atCastExpr(CastExpr expr) throws CompileError {
        ASTree p;
        ASTList classname = expr.getClassName();
        if (classname != null && expr.getArrayDim() == 0 && (p = classname.head()) instanceof Symbol && classname.tail() == null) {
            String typename = ((Symbol)p).get();
            if (typename.equals(this.codeGen.returnCastName)) {
                this.atCastToRtype(expr);
                return;
            }
            if (typename.equals("$w")) {
                this.atCastToWrapper(expr);
                return;
            }
        }
        super.atCastExpr(expr);
    }

    protected void atCastToRtype(CastExpr expr) throws CompileError {
        CtClass returnType = this.codeGen.returnType;
        expr.getOprand().accept(this);
        if (this.exprType != 344 && !CodeGen.isRefType(this.exprType) && this.arrayDim <= 0) {
            int destType;
            if (!(returnType instanceof CtPrimitiveType)) return;
            CtPrimitiveType pt = (CtPrimitiveType)returnType;
            this.exprType = destType = MemberResolver.descToType(pt.getDescriptor());
            this.arrayDim = 0;
            this.className = null;
            return;
        }
        this.compileUnwrapValue(returnType);
    }

    protected void atCastToWrapper(CastExpr expr) throws CompileError {
        expr.getOprand().accept(this);
        if (CodeGen.isRefType(this.exprType)) return;
        if (this.arrayDim > 0) {
            return;
        }
        CtClass clazz = this.resolver.lookupClass(this.exprType, this.arrayDim, this.className);
        if (!(clazz instanceof CtPrimitiveType)) return;
        this.exprType = 307;
        this.arrayDim = 0;
        this.className = "java/lang/Object";
    }

    @Override
    public void atCallExpr(CallExpr expr) throws CompileError {
        ASTree method = expr.oprand1();
        if (method instanceof Member) {
            String name = ((Member)method).get();
            if (this.codeGen.procHandler != null && name.equals(this.codeGen.proceedName)) {
                this.codeGen.procHandler.setReturnType(this, (ASTList)expr.oprand2());
                return;
            }
            if (name.equals("$cflow")) {
                this.atCflow((ASTList)expr.oprand2());
                return;
            }
        }
        super.atCallExpr(expr);
    }

    protected void atCflow(ASTList cname) throws CompileError {
        this.exprType = 324;
        this.arrayDim = 0;
        this.className = null;
    }

    public boolean isParamListName(ASTList args) {
        if (this.codeGen.paramTypeList == null) return false;
        if (args == null) return false;
        if (args.tail() != null) return false;
        ASTree left = args.head();
        if (!(left instanceof Member)) return false;
        if (!((Member)left).get().equals(this.codeGen.paramListName)) return false;
        return true;
    }

    @Override
    public int getMethodArgsLength(ASTList args) {
        String pname = this.codeGen.paramListName;
        int n = 0;
        while (args != null) {
            ASTree a = args.head();
            if (a instanceof Member && ((Member)a).get().equals(pname)) {
                if (this.codeGen.paramTypeList != null) {
                    n += this.codeGen.paramTypeList.length;
                }
            } else {
                ++n;
            }
            args = args.tail();
        }
        return n;
    }

    @Override
    public void atMethodArgs(ASTList args, int[] types, int[] dims, String[] cnames) throws CompileError {
        CtClass[] params = this.codeGen.paramTypeList;
        String pname = this.codeGen.paramListName;
        int i = 0;
        while (args != null) {
            ASTree a = args.head();
            if (a instanceof Member && ((Member)a).get().equals(pname)) {
                if (params != null) {
                    int n = params.length;
                    for (int k = 0; k < n; ++i, ++k) {
                        CtClass p = params[k];
                        this.setType(p);
                        types[i] = this.exprType;
                        dims[i] = this.arrayDim;
                        cnames[i] = this.className;
                    }
                }
            } else {
                a.accept(this);
                types[i] = this.exprType;
                dims[i] = this.arrayDim;
                cnames[i] = this.className;
                ++i;
            }
            args = args.tail();
        }
    }

    void compileInvokeSpecial(ASTree target, String classname, String methodname, String descriptor, ASTList args) throws CompileError {
        target.accept(this);
        int nargs = this.getMethodArgsLength(args);
        this.atMethodArgs(args, new int[nargs], new int[nargs], new String[nargs]);
        this.setReturnType(descriptor);
        this.addNullIfVoid();
    }

    protected void compileUnwrapValue(CtClass type) throws CompileError {
        if (type == CtClass.voidType) {
            this.addNullIfVoid();
            return;
        }
        this.setType(type);
    }

    public void setType(CtClass type) throws CompileError {
        this.setType(type, 0);
    }

    private void setType(CtClass type, int dim) throws CompileError {
        if (type.isPrimitive()) {
            CtPrimitiveType pt = (CtPrimitiveType)type;
            this.exprType = MemberResolver.descToType(pt.getDescriptor());
            this.arrayDim = dim;
            this.className = null;
            return;
        }
        if (!type.isArray()) {
            this.exprType = 307;
            this.arrayDim = dim;
            this.className = MemberResolver.javaToJvmName(type.getName());
            return;
        }
        try {
            this.setType(type.getComponentType(), dim + 1);
            return;
        }
        catch (NotFoundException e) {
            throw new CompileError("undefined type: " + type.getName());
        }
    }
}

