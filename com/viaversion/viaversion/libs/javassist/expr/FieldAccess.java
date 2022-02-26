/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.expr;

import com.viaversion.viaversion.libs.javassist.CannotCompileException;
import com.viaversion.viaversion.libs.javassist.CtBehavior;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.CtField;
import com.viaversion.viaversion.libs.javassist.CtPrimitiveType;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.BadBytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.Bytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeIterator;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.Descriptor;
import com.viaversion.viaversion.libs.javassist.bytecode.MethodInfo;
import com.viaversion.viaversion.libs.javassist.compiler.CompileError;
import com.viaversion.viaversion.libs.javassist.compiler.Javac;
import com.viaversion.viaversion.libs.javassist.compiler.JvstCodeGen;
import com.viaversion.viaversion.libs.javassist.compiler.JvstTypeChecker;
import com.viaversion.viaversion.libs.javassist.compiler.ProceedHandler;
import com.viaversion.viaversion.libs.javassist.compiler.ast.ASTList;
import com.viaversion.viaversion.libs.javassist.expr.Expr;

public class FieldAccess
extends Expr {
    int opcode;

    protected FieldAccess(int pos, CodeIterator i, CtClass declaring, MethodInfo m, int op) {
        super(pos, i, declaring, m);
        this.opcode = op;
    }

    @Override
    public CtBehavior where() {
        return super.where();
    }

    @Override
    public int getLineNumber() {
        return super.getLineNumber();
    }

    @Override
    public String getFileName() {
        return super.getFileName();
    }

    public boolean isStatic() {
        return FieldAccess.isStatic(this.opcode);
    }

    static boolean isStatic(int c) {
        if (c == 178) return true;
        if (c == 179) return true;
        return false;
    }

    public boolean isReader() {
        if (this.opcode == 180) return true;
        if (this.opcode == 178) return true;
        return false;
    }

    public boolean isWriter() {
        if (this.opcode == 181) return true;
        if (this.opcode == 179) return true;
        return false;
    }

    private CtClass getCtClass() throws NotFoundException {
        return this.thisClass.getClassPool().get(this.getClassName());
    }

    public String getClassName() {
        int index = this.iterator.u16bitAt(this.currentPos + 1);
        return this.getConstPool().getFieldrefClassName(index);
    }

    public String getFieldName() {
        int index = this.iterator.u16bitAt(this.currentPos + 1);
        return this.getConstPool().getFieldrefName(index);
    }

    public CtField getField() throws NotFoundException {
        CtClass cc = this.getCtClass();
        int index = this.iterator.u16bitAt(this.currentPos + 1);
        ConstPool cp = this.getConstPool();
        return cc.getField(cp.getFieldrefName(index), cp.getFieldrefType(index));
    }

    @Override
    public CtClass[] mayThrow() {
        return super.mayThrow();
    }

    public String getSignature() {
        int index = this.iterator.u16bitAt(this.currentPos + 1);
        return this.getConstPool().getFieldrefType(index);
    }

    @Override
    public void replace(String statement) throws CannotCompileException {
        this.thisClass.getClassFile();
        ConstPool constPool = this.getConstPool();
        int pos = this.currentPos;
        int index = this.iterator.u16bitAt(pos + 1);
        Javac jc = new Javac(this.thisClass);
        CodeAttribute ca = this.iterator.get();
        try {
            CtClass retType;
            CtClass[] params;
            CtClass fieldType = Descriptor.toCtClass(constPool.getFieldrefType(index), this.thisClass.getClassPool());
            boolean read = this.isReader();
            if (read) {
                params = new CtClass[]{};
                retType = fieldType;
            } else {
                params = new CtClass[]{fieldType};
                retType = CtClass.voidType;
            }
            int paramVar = ca.getMaxLocals();
            jc.recordParams(constPool.getFieldrefClassName(index), params, true, paramVar, this.withinStatic());
            boolean included = FieldAccess.checkResultValue(retType, statement);
            if (read) {
                included = true;
            }
            int retVar = jc.recordReturnType(retType, included);
            if (read) {
                jc.recordProceed(new ProceedForRead(retType, this.opcode, index, paramVar));
            } else {
                jc.recordType(fieldType);
                jc.recordProceed(new ProceedForWrite(params[0], this.opcode, index, paramVar));
            }
            Bytecode bytecode = jc.getBytecode();
            FieldAccess.storeStack(params, this.isStatic(), paramVar, bytecode);
            jc.recordLocalVariables(ca, pos);
            if (included) {
                if (retType == CtClass.voidType) {
                    bytecode.addOpcode(1);
                    bytecode.addAstore(retVar);
                } else {
                    bytecode.addConstZero(retType);
                    bytecode.addStore(retVar, retType);
                }
            }
            jc.compileStmnt(statement);
            if (read) {
                bytecode.addLoad(retVar, retType);
            }
            this.replace0(pos, bytecode, 3);
            return;
        }
        catch (CompileError e) {
            throw new CannotCompileException(e);
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        catch (BadBytecode e) {
            throw new CannotCompileException("broken method");
        }
    }

    static class ProceedForWrite
    implements ProceedHandler {
        CtClass fieldType;
        int opcode;
        int targetVar;
        int index;

        ProceedForWrite(CtClass type, int op, int i, int var) {
            this.fieldType = type;
            this.targetVar = var;
            this.opcode = op;
            this.index = i;
        }

        @Override
        public void doit(JvstCodeGen gen, Bytecode bytecode, ASTList args) throws CompileError {
            int stack;
            if (gen.getMethodArgsLength(args) != 1) {
                throw new CompileError("$proceed() cannot take more than one parameter for field writing");
            }
            if (FieldAccess.isStatic(this.opcode)) {
                stack = 0;
            } else {
                stack = -1;
                bytecode.addAload(this.targetVar);
            }
            gen.atMethodArgs(args, new int[1], new int[1], new String[1]);
            gen.doNumCast(this.fieldType);
            stack = this.fieldType instanceof CtPrimitiveType ? (stack -= ((CtPrimitiveType)this.fieldType).getDataSize()) : --stack;
            bytecode.add(this.opcode);
            bytecode.addIndex(this.index);
            bytecode.growStack(stack);
            gen.setType(CtClass.voidType);
            gen.addNullIfVoid();
        }

        @Override
        public void setReturnType(JvstTypeChecker c, ASTList args) throws CompileError {
            c.atMethodArgs(args, new int[1], new int[1], new String[1]);
            c.setType(CtClass.voidType);
            c.addNullIfVoid();
        }
    }

    static class ProceedForRead
    implements ProceedHandler {
        CtClass fieldType;
        int opcode;
        int targetVar;
        int index;

        ProceedForRead(CtClass type, int op, int i, int var) {
            this.fieldType = type;
            this.targetVar = var;
            this.opcode = op;
            this.index = i;
        }

        @Override
        public void doit(JvstCodeGen gen, Bytecode bytecode, ASTList args) throws CompileError {
            int stack;
            if (args != null && !gen.isParamListName(args)) {
                throw new CompileError("$proceed() cannot take a parameter for field reading");
            }
            if (FieldAccess.isStatic(this.opcode)) {
                stack = 0;
            } else {
                stack = -1;
                bytecode.addAload(this.targetVar);
            }
            stack = this.fieldType instanceof CtPrimitiveType ? (stack += ((CtPrimitiveType)this.fieldType).getDataSize()) : ++stack;
            bytecode.add(this.opcode);
            bytecode.addIndex(this.index);
            bytecode.growStack(stack);
            gen.setType(this.fieldType);
        }

        @Override
        public void setReturnType(JvstTypeChecker c, ASTList args) throws CompileError {
            c.setType(this.fieldType);
        }
    }
}

