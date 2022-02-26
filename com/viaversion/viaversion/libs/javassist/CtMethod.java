/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist;

import com.viaversion.viaversion.libs.javassist.CannotCompileException;
import com.viaversion.viaversion.libs.javassist.ClassMap;
import com.viaversion.viaversion.libs.javassist.CtBehavior;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.CtNewMethod;
import com.viaversion.viaversion.libs.javassist.CtNewWrappedMethod;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.BadBytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.Bytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeIterator;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.Descriptor;
import com.viaversion.viaversion.libs.javassist.bytecode.MethodInfo;

public final class CtMethod
extends CtBehavior {
    protected String cachedStringRep = null;

    CtMethod(MethodInfo minfo, CtClass declaring) {
        super(declaring, minfo);
    }

    public CtMethod(CtClass returnType, String mname, CtClass[] parameters, CtClass declaring) {
        this(null, declaring);
        ConstPool cp = declaring.getClassFile2().getConstPool();
        String desc = Descriptor.ofMethod(returnType, parameters);
        this.methodInfo = new MethodInfo(cp, mname, desc);
        this.setModifiers(1025);
    }

    public CtMethod(CtMethod src, CtClass declaring, ClassMap map) throws CannotCompileException {
        this(null, declaring);
        this.copy(src, false, map);
    }

    public static CtMethod make(String src, CtClass declaring) throws CannotCompileException {
        return CtNewMethod.make(src, declaring);
    }

    public static CtMethod make(MethodInfo minfo, CtClass declaring) throws CannotCompileException {
        if (declaring.getClassFile2().getConstPool() == minfo.getConstPool()) return new CtMethod(minfo, declaring);
        throw new CannotCompileException("bad declaring class");
    }

    public int hashCode() {
        return this.getStringRep().hashCode();
    }

    @Override
    void nameReplaced() {
        this.cachedStringRep = null;
    }

    final String getStringRep() {
        if (this.cachedStringRep != null) return this.cachedStringRep;
        this.cachedStringRep = this.methodInfo.getName() + Descriptor.getParamDescriptor(this.methodInfo.getDescriptor());
        return this.cachedStringRep;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof CtMethod)) return false;
        if (!((CtMethod)obj).getStringRep().equals(this.getStringRep())) return false;
        return true;
    }

    @Override
    public String getLongName() {
        return this.getDeclaringClass().getName() + "." + this.getName() + Descriptor.toString(this.getSignature());
    }

    @Override
    public String getName() {
        return this.methodInfo.getName();
    }

    public void setName(String newname) {
        this.declaringClass.checkModify();
        this.methodInfo.setName(newname);
    }

    public CtClass getReturnType() throws NotFoundException {
        return this.getReturnType0();
    }

    @Override
    public boolean isEmpty() {
        CodeAttribute ca = this.getMethodInfo2().getCodeAttribute();
        if (ca == null) {
            if ((this.getModifiers() & 0x400) == 0) return false;
            return true;
        }
        CodeIterator it = ca.iterator();
        try {
            if (!it.hasNext()) return false;
            if (it.byteAt(it.next()) != 177) return false;
            if (it.hasNext()) return false;
            return true;
        }
        catch (BadBytecode badBytecode) {
            return false;
        }
    }

    public void setBody(CtMethod src, ClassMap map) throws CannotCompileException {
        CtMethod.setBody0(src.declaringClass, src.methodInfo, this.declaringClass, this.methodInfo, map);
    }

    public void setWrappedBody(CtMethod mbody, ConstParameter constParam) throws CannotCompileException {
        CtClass retType;
        CtClass[] params;
        this.declaringClass.checkModify();
        CtClass clazz = this.getDeclaringClass();
        try {
            params = this.getParameterTypes();
            retType = this.getReturnType();
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        Bytecode code = CtNewWrappedMethod.makeBody(clazz, clazz.getClassFile2(), mbody, params, retType, constParam);
        CodeAttribute cattr = code.toCodeAttribute();
        this.methodInfo.setCodeAttribute(cattr);
        this.methodInfo.setAccessFlags(this.methodInfo.getAccessFlags() & 0xFFFFFBFF);
    }

    static class StringConstParameter
    extends ConstParameter {
        String param;

        StringConstParameter(String s) {
            this.param = s;
        }

        @Override
        int compile(Bytecode code) throws CannotCompileException {
            code.addLdc(this.param);
            return 1;
        }

        @Override
        String descriptor() {
            return "([Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;";
        }

        @Override
        String constDescriptor() {
            return "([Ljava/lang/Object;Ljava/lang/String;)V";
        }
    }

    static class LongConstParameter
    extends ConstParameter {
        long param;

        LongConstParameter(long l) {
            this.param = l;
        }

        @Override
        int compile(Bytecode code) throws CannotCompileException {
            code.addLconst(this.param);
            return 2;
        }

        @Override
        String descriptor() {
            return "([Ljava/lang/Object;J)Ljava/lang/Object;";
        }

        @Override
        String constDescriptor() {
            return "([Ljava/lang/Object;J)V";
        }
    }

    static class IntConstParameter
    extends ConstParameter {
        int param;

        IntConstParameter(int i) {
            this.param = i;
        }

        @Override
        int compile(Bytecode code) throws CannotCompileException {
            code.addIconst(this.param);
            return 1;
        }

        @Override
        String descriptor() {
            return "([Ljava/lang/Object;I)Ljava/lang/Object;";
        }

        @Override
        String constDescriptor() {
            return "([Ljava/lang/Object;I)V";
        }
    }

    public static class ConstParameter {
        public static ConstParameter integer(int i) {
            return new IntConstParameter(i);
        }

        public static ConstParameter integer(long i) {
            return new LongConstParameter(i);
        }

        public static ConstParameter string(String s) {
            return new StringConstParameter(s);
        }

        ConstParameter() {
        }

        int compile(Bytecode code) throws CannotCompileException {
            return 0;
        }

        String descriptor() {
            return ConstParameter.defaultDescriptor();
        }

        static String defaultDescriptor() {
            return "([Ljava/lang/Object;)Ljava/lang/Object;";
        }

        String constDescriptor() {
            return ConstParameter.defaultConstDescriptor();
        }

        static String defaultConstDescriptor() {
            return "([Ljava/lang/Object;)V";
        }
    }
}

