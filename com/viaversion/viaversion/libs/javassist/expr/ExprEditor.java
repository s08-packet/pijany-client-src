/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.expr;

import com.viaversion.viaversion.libs.javassist.CannotCompileException;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.bytecode.BadBytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeIterator;
import com.viaversion.viaversion.libs.javassist.bytecode.ExceptionTable;
import com.viaversion.viaversion.libs.javassist.bytecode.MethodInfo;
import com.viaversion.viaversion.libs.javassist.expr.Cast;
import com.viaversion.viaversion.libs.javassist.expr.ConstructorCall;
import com.viaversion.viaversion.libs.javassist.expr.Expr;
import com.viaversion.viaversion.libs.javassist.expr.FieldAccess;
import com.viaversion.viaversion.libs.javassist.expr.Handler;
import com.viaversion.viaversion.libs.javassist.expr.Instanceof;
import com.viaversion.viaversion.libs.javassist.expr.MethodCall;
import com.viaversion.viaversion.libs.javassist.expr.NewArray;
import com.viaversion.viaversion.libs.javassist.expr.NewExpr;

public class ExprEditor {
    public boolean doit(CtClass clazz, MethodInfo minfo) throws CannotCompileException {
        CodeAttribute codeAttr = minfo.getCodeAttribute();
        if (codeAttr == null) {
            return false;
        }
        CodeIterator iterator = codeAttr.iterator();
        boolean edited = false;
        LoopContext context = new LoopContext(codeAttr.getMaxLocals());
        while (iterator.hasNext()) {
            if (!this.loopBody(iterator, clazz, minfo, context)) continue;
            edited = true;
        }
        ExceptionTable et = codeAttr.getExceptionTable();
        int n = et.size();
        for (int i = 0; i < n; ++i) {
            Handler h = new Handler(et, i, iterator, clazz, minfo);
            this.edit(h);
            if (!h.edited()) continue;
            edited = true;
            context.updateMax(h.locals(), h.stack());
        }
        if (codeAttr.getMaxLocals() < context.maxLocals) {
            codeAttr.setMaxLocals(context.maxLocals);
        }
        codeAttr.setMaxStack(codeAttr.getMaxStack() + context.maxStack);
        try {
            if (!edited) return edited;
            minfo.rebuildStackMapIf6(clazz.getClassPool(), clazz.getClassFile2());
            return edited;
        }
        catch (BadBytecode b) {
            throw new CannotCompileException(b.getMessage(), b);
        }
    }

    boolean doit(CtClass clazz, MethodInfo minfo, LoopContext context, CodeIterator iterator, int endPos) throws CannotCompileException {
        boolean edited = false;
        while (iterator.hasNext()) {
            if (iterator.lookAhead() >= endPos) return edited;
            int size = iterator.getCodeLength();
            if (!this.loopBody(iterator, clazz, minfo, context)) continue;
            edited = true;
            int size2 = iterator.getCodeLength();
            if (size == size2) continue;
            endPos += size2 - size;
        }
        return edited;
    }

    final boolean loopBody(CodeIterator iterator, CtClass clazz, MethodInfo minfo, LoopContext context) throws CannotCompileException {
        try {
            Expr expr = null;
            int pos = iterator.next();
            int c = iterator.byteAt(pos);
            if (c >= 178) {
                if (c < 188) {
                    if (c == 184 || c == 185 || c == 182) {
                        expr = new MethodCall(pos, iterator, clazz, minfo);
                        this.edit((MethodCall)expr);
                    } else if (c == 180 || c == 178 || c == 181 || c == 179) {
                        expr = new FieldAccess(pos, iterator, clazz, minfo, c);
                        this.edit((FieldAccess)expr);
                    } else if (c == 187) {
                        int index = iterator.u16bitAt(pos + 1);
                        context.newList = new NewOp(context.newList, pos, minfo.getConstPool().getClassInfo(index));
                    } else if (c == 183) {
                        NewOp newList = context.newList;
                        if (newList != null && minfo.getConstPool().isConstructor(newList.type, iterator.u16bitAt(pos + 1)) > 0) {
                            expr = new NewExpr(pos, iterator, clazz, minfo, newList.type, newList.pos);
                            this.edit((NewExpr)expr);
                            context.newList = newList.next;
                        } else {
                            MethodCall mcall = new MethodCall(pos, iterator, clazz, minfo);
                            if (mcall.getMethodName().equals("<init>")) {
                                ConstructorCall ccall = new ConstructorCall(pos, iterator, clazz, minfo);
                                expr = ccall;
                                this.edit(ccall);
                            } else {
                                expr = mcall;
                                this.edit(mcall);
                            }
                        }
                    }
                } else if (c == 188 || c == 189 || c == 197) {
                    expr = new NewArray(pos, iterator, clazz, minfo, c);
                    this.edit((NewArray)expr);
                } else if (c == 193) {
                    expr = new Instanceof(pos, iterator, clazz, minfo);
                    this.edit((Instanceof)expr);
                } else if (c == 192) {
                    expr = new Cast(pos, iterator, clazz, minfo);
                    this.edit((Cast)expr);
                }
            }
            if (expr == null) return false;
            if (!expr.edited()) return false;
            context.updateMax(expr.locals(), expr.stack());
            return true;
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
    }

    public void edit(NewExpr e) throws CannotCompileException {
    }

    public void edit(NewArray a) throws CannotCompileException {
    }

    public void edit(MethodCall m) throws CannotCompileException {
    }

    public void edit(ConstructorCall c) throws CannotCompileException {
    }

    public void edit(FieldAccess f) throws CannotCompileException {
    }

    public void edit(Instanceof i) throws CannotCompileException {
    }

    public void edit(Cast c) throws CannotCompileException {
    }

    public void edit(Handler h) throws CannotCompileException {
    }

    static final class LoopContext {
        NewOp newList;
        int maxLocals;
        int maxStack;

        LoopContext(int locals) {
            this.maxLocals = locals;
            this.maxStack = 0;
            this.newList = null;
        }

        void updateMax(int locals, int stack) {
            if (this.maxLocals < locals) {
                this.maxLocals = locals;
            }
            if (this.maxStack >= stack) return;
            this.maxStack = stack;
        }
    }

    static final class NewOp {
        NewOp next;
        int pos;
        String type;

        NewOp(NewOp n, int p, String t) {
            this.next = n;
            this.pos = p;
            this.type = t;
        }
    }
}

