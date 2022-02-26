/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.expr;

import com.viaversion.viaversion.libs.javassist.CannotCompileException;
import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.CtBehavior;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.CtConstructor;
import com.viaversion.viaversion.libs.javassist.CtPrimitiveType;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.BadBytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.Bytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.ClassFile;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeIterator;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.ExceptionTable;
import com.viaversion.viaversion.libs.javassist.bytecode.ExceptionsAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.MethodInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.Opcode;
import com.viaversion.viaversion.libs.javassist.expr.ExprEditor;
import java.util.LinkedList;
import java.util.List;

public abstract class Expr
implements Opcode {
    int currentPos;
    CodeIterator iterator;
    CtClass thisClass;
    MethodInfo thisMethod;
    boolean edited;
    int maxLocals;
    int maxStack;
    static final String javaLangObject = "java.lang.Object";

    protected Expr(int pos, CodeIterator i, CtClass declaring, MethodInfo m) {
        this.currentPos = pos;
        this.iterator = i;
        this.thisClass = declaring;
        this.thisMethod = m;
    }

    public CtClass getEnclosingClass() {
        return this.thisClass;
    }

    protected final ConstPool getConstPool() {
        return this.thisMethod.getConstPool();
    }

    protected final boolean edited() {
        return this.edited;
    }

    protected final int locals() {
        return this.maxLocals;
    }

    protected final int stack() {
        return this.maxStack;
    }

    protected final boolean withinStatic() {
        if ((this.thisMethod.getAccessFlags() & 8) == 0) return false;
        return true;
    }

    public CtBehavior where() {
        MethodInfo mi = this.thisMethod;
        CtBehavior[] cb = this.thisClass.getDeclaredBehaviors();
        for (int i = cb.length - 1; i >= 0; --i) {
            if (cb[i].getMethodInfo2() != mi) continue;
            return cb[i];
        }
        CtConstructor init = this.thisClass.getClassInitializer();
        if (init != null && init.getMethodInfo2() == mi) {
            return init;
        }
        int i = cb.length - 1;
        while (i >= 0) {
            if (this.thisMethod.getName().equals(cb[i].getMethodInfo2().getName()) && this.thisMethod.getDescriptor().equals(cb[i].getMethodInfo2().getDescriptor())) {
                return cb[i];
            }
            --i;
        }
        throw new RuntimeException("fatal: not found");
    }

    public CtClass[] mayThrow() {
        ExceptionsAttribute ea;
        ClassPool pool = this.thisClass.getClassPool();
        ConstPool cp = this.thisMethod.getConstPool();
        LinkedList<CtClass> list = new LinkedList<CtClass>();
        try {
            CodeAttribute ca = this.thisMethod.getCodeAttribute();
            ExceptionTable et = ca.getExceptionTable();
            int pos = this.currentPos;
            int n = et.size();
            for (int i = 0; i < n; ++i) {
                int t;
                if (et.startPc(i) > pos || pos >= et.endPc(i) || (t = et.catchType(i)) <= 0) continue;
                try {
                    Expr.addClass(list, pool.get(cp.getClassInfo(t)));
                    continue;
                }
                catch (NotFoundException notFoundException) {
                    // empty catch block
                }
            }
        }
        catch (NullPointerException ca) {
            // empty catch block
        }
        if ((ea = this.thisMethod.getExceptionsAttribute()) == null) return list.toArray(new CtClass[list.size()]);
        String[] exceptions = ea.getExceptions();
        if (exceptions == null) return list.toArray(new CtClass[list.size()]);
        int n = exceptions.length;
        int i = 0;
        while (i < n) {
            try {
                Expr.addClass(list, pool.get(exceptions[i]));
            }
            catch (NotFoundException notFoundException) {
                // empty catch block
            }
            ++i;
        }
        return list.toArray(new CtClass[list.size()]);
    }

    private static void addClass(List<CtClass> list, CtClass c) {
        if (list.contains(c)) {
            return;
        }
        list.add(c);
    }

    public int indexOfBytecode() {
        return this.currentPos;
    }

    public int getLineNumber() {
        return this.thisMethod.getLineNumber(this.currentPos);
    }

    public String getFileName() {
        ClassFile cf = this.thisClass.getClassFile2();
        if (cf != null) return cf.getSourceFile();
        return null;
    }

    static final boolean checkResultValue(CtClass retType, String prog) throws CannotCompileException {
        boolean hasIt = prog.indexOf("$_") >= 0;
        if (hasIt) return hasIt;
        if (retType == CtClass.voidType) return hasIt;
        throw new CannotCompileException("the resulting value is not stored in $_");
    }

    static final void storeStack(CtClass[] params, boolean isStaticCall, int regno, Bytecode bytecode) {
        Expr.storeStack0(0, params.length, params, regno + 1, bytecode);
        if (isStaticCall) {
            bytecode.addOpcode(1);
        }
        bytecode.addAstore(regno);
    }

    private static void storeStack0(int i, int n, CtClass[] params, int regno, Bytecode bytecode) {
        if (i >= n) {
            return;
        }
        CtClass c = params[i];
        int size = c instanceof CtPrimitiveType ? ((CtPrimitiveType)c).getDataSize() : 1;
        Expr.storeStack0(i + 1, n, params, regno + size, bytecode);
        bytecode.addStore(regno, c);
    }

    public abstract void replace(String var1) throws CannotCompileException;

    public void replace(String statement, ExprEditor recursive) throws CannotCompileException {
        this.replace(statement);
        if (recursive == null) return;
        this.runEditor(recursive, this.iterator);
    }

    protected void replace0(int pos, Bytecode bytecode, int size) throws BadBytecode {
        byte[] code = bytecode.get();
        this.edited = true;
        int gap = code.length - size;
        for (int i = 0; i < size; ++i) {
            this.iterator.writeByte(0, pos + i);
        }
        if (gap > 0) {
            pos = this.iterator.insertGapAt((int)pos, (int)gap, (boolean)false).position;
        }
        this.iterator.write(code, pos);
        this.iterator.insert(bytecode.getExceptionTable(), pos);
        this.maxLocals = bytecode.getMaxLocals();
        this.maxStack = bytecode.getMaxStack();
    }

    protected void runEditor(ExprEditor ed, CodeIterator oldIterator) throws CannotCompileException {
        CodeAttribute codeAttr = oldIterator.get();
        int orgLocals = codeAttr.getMaxLocals();
        int orgStack = codeAttr.getMaxStack();
        int newLocals = this.locals();
        codeAttr.setMaxStack(this.stack());
        codeAttr.setMaxLocals(newLocals);
        ExprEditor.LoopContext context = new ExprEditor.LoopContext(newLocals);
        int size = oldIterator.getCodeLength();
        int endPos = oldIterator.lookAhead();
        oldIterator.move(this.currentPos);
        if (ed.doit(this.thisClass, this.thisMethod, context, oldIterator, endPos)) {
            this.edited = true;
        }
        oldIterator.move(endPos + oldIterator.getCodeLength() - size);
        codeAttr.setMaxLocals(orgLocals);
        codeAttr.setMaxStack(orgStack);
        this.maxLocals = context.maxLocals;
        this.maxStack += context.maxStack;
    }
}

