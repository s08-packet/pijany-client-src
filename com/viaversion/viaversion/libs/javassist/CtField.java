/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist;

import com.viaversion.viaversion.libs.javassist.CannotCompileException;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.CtClassType;
import com.viaversion.viaversion.libs.javassist.CtMember;
import com.viaversion.viaversion.libs.javassist.CtNewWrappedMethod;
import com.viaversion.viaversion.libs.javassist.CtPrimitiveType;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.AccessFlag;
import com.viaversion.viaversion.libs.javassist.bytecode.AnnotationsAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.AttributeInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.Bytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.ClassFile;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.Descriptor;
import com.viaversion.viaversion.libs.javassist.bytecode.FieldInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.SignatureAttribute;
import com.viaversion.viaversion.libs.javassist.compiler.CompileError;
import com.viaversion.viaversion.libs.javassist.compiler.Javac;
import com.viaversion.viaversion.libs.javassist.compiler.SymbolTable;
import com.viaversion.viaversion.libs.javassist.compiler.ast.ASTree;
import com.viaversion.viaversion.libs.javassist.compiler.ast.DoubleConst;
import com.viaversion.viaversion.libs.javassist.compiler.ast.IntConst;
import com.viaversion.viaversion.libs.javassist.compiler.ast.StringL;
import java.util.Iterator;
import java.util.List;

public class CtField
extends CtMember {
    static final String javaLangString = "java.lang.String";
    protected FieldInfo fieldInfo;

    public CtField(CtClass type, String name, CtClass declaring) throws CannotCompileException {
        this(Descriptor.of(type), name, declaring);
    }

    public CtField(CtField src, CtClass declaring) throws CannotCompileException {
        this(src.fieldInfo.getDescriptor(), src.fieldInfo.getName(), declaring);
        FieldInfo fi = this.fieldInfo;
        fi.setAccessFlags(src.fieldInfo.getAccessFlags());
        ConstPool cp = fi.getConstPool();
        List<AttributeInfo> attributes = src.fieldInfo.getAttributes();
        Iterator<AttributeInfo> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            AttributeInfo ainfo = iterator.next();
            fi.addAttribute(ainfo.copy(cp, null));
        }
    }

    private CtField(String typeDesc, String name, CtClass clazz) throws CannotCompileException {
        super(clazz);
        ClassFile cf = clazz.getClassFile2();
        if (cf == null) {
            throw new CannotCompileException("bad declaring class: " + clazz.getName());
        }
        this.fieldInfo = new FieldInfo(cf.getConstPool(), name, typeDesc);
    }

    CtField(FieldInfo fi, CtClass clazz) {
        super(clazz);
        this.fieldInfo = fi;
    }

    @Override
    public String toString() {
        return this.getDeclaringClass().getName() + "." + this.getName() + ":" + this.fieldInfo.getDescriptor();
    }

    @Override
    protected void extendToString(StringBuffer buffer) {
        buffer.append(' ');
        buffer.append(this.getName());
        buffer.append(' ');
        buffer.append(this.fieldInfo.getDescriptor());
    }

    protected ASTree getInitAST() {
        return null;
    }

    Initializer getInit() {
        ASTree tree = this.getInitAST();
        if (tree != null) return Initializer.byExpr(tree);
        return null;
    }

    public static CtField make(String src, CtClass declaring) throws CannotCompileException {
        Javac compiler = new Javac(declaring);
        try {
            CtMember obj = compiler.compile(src);
            if (!(obj instanceof CtField)) throw new CannotCompileException("not a field");
            return (CtField)obj;
        }
        catch (CompileError e) {
            throw new CannotCompileException(e);
        }
    }

    public FieldInfo getFieldInfo() {
        this.declaringClass.checkModify();
        return this.fieldInfo;
    }

    public FieldInfo getFieldInfo2() {
        return this.fieldInfo;
    }

    @Override
    public CtClass getDeclaringClass() {
        return super.getDeclaringClass();
    }

    @Override
    public String getName() {
        return this.fieldInfo.getName();
    }

    public void setName(String newName) {
        this.declaringClass.checkModify();
        this.fieldInfo.setName(newName);
    }

    @Override
    public int getModifiers() {
        return AccessFlag.toModifier(this.fieldInfo.getAccessFlags());
    }

    @Override
    public void setModifiers(int mod) {
        this.declaringClass.checkModify();
        this.fieldInfo.setAccessFlags(AccessFlag.of(mod));
    }

    @Override
    public boolean hasAnnotation(String typeName) {
        FieldInfo fi = this.getFieldInfo2();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)fi.getAttribute("RuntimeInvisibleAnnotations");
        AnnotationsAttribute ainfo2 = (AnnotationsAttribute)fi.getAttribute("RuntimeVisibleAnnotations");
        return CtClassType.hasAnnotationType(typeName, this.getDeclaringClass().getClassPool(), ainfo, ainfo2);
    }

    @Override
    public Object getAnnotation(Class<?> clz) throws ClassNotFoundException {
        FieldInfo fi = this.getFieldInfo2();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)fi.getAttribute("RuntimeInvisibleAnnotations");
        AnnotationsAttribute ainfo2 = (AnnotationsAttribute)fi.getAttribute("RuntimeVisibleAnnotations");
        return CtClassType.getAnnotationType(clz, this.getDeclaringClass().getClassPool(), ainfo, ainfo2);
    }

    @Override
    public Object[] getAnnotations() throws ClassNotFoundException {
        return this.getAnnotations(false);
    }

    @Override
    public Object[] getAvailableAnnotations() {
        try {
            return this.getAnnotations(true);
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    private Object[] getAnnotations(boolean ignoreNotFound) throws ClassNotFoundException {
        FieldInfo fi = this.getFieldInfo2();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)fi.getAttribute("RuntimeInvisibleAnnotations");
        AnnotationsAttribute ainfo2 = (AnnotationsAttribute)fi.getAttribute("RuntimeVisibleAnnotations");
        return CtClassType.toAnnotationType(ignoreNotFound, this.getDeclaringClass().getClassPool(), ainfo, ainfo2);
    }

    @Override
    public String getSignature() {
        return this.fieldInfo.getDescriptor();
    }

    @Override
    public String getGenericSignature() {
        SignatureAttribute sa = (SignatureAttribute)this.fieldInfo.getAttribute("Signature");
        if (sa == null) {
            return null;
        }
        String string = sa.getSignature();
        return string;
    }

    @Override
    public void setGenericSignature(String sig) {
        this.declaringClass.checkModify();
        this.fieldInfo.addAttribute(new SignatureAttribute(this.fieldInfo.getConstPool(), sig));
    }

    public CtClass getType() throws NotFoundException {
        return Descriptor.toCtClass(this.fieldInfo.getDescriptor(), this.declaringClass.getClassPool());
    }

    public void setType(CtClass clazz) {
        this.declaringClass.checkModify();
        this.fieldInfo.setDescriptor(Descriptor.of(clazz));
    }

    public Object getConstantValue() {
        int index = this.fieldInfo.getConstantValue();
        if (index == 0) {
            return null;
        }
        ConstPool cp = this.fieldInfo.getConstPool();
        switch (cp.getTag(index)) {
            case 5: {
                return cp.getLongInfo(index);
            }
            case 4: {
                return Float.valueOf(cp.getFloatInfo(index));
            }
            case 6: {
                return cp.getDoubleInfo(index);
            }
            case 3: {
                boolean bl;
                int value = cp.getIntegerInfo(index);
                if (!"Z".equals(this.fieldInfo.getDescriptor())) return value;
                if (value != 0) {
                    bl = true;
                    return bl;
                }
                bl = false;
                return bl;
            }
            case 8: {
                return cp.getStringInfo(index);
            }
        }
        throw new RuntimeException("bad tag: " + cp.getTag(index) + " at " + index);
    }

    @Override
    public byte[] getAttribute(String name) {
        AttributeInfo ai = this.fieldInfo.getAttribute(name);
        if (ai != null) return ai.get();
        return null;
    }

    @Override
    public void setAttribute(String name, byte[] data) {
        this.declaringClass.checkModify();
        this.fieldInfo.addAttribute(new AttributeInfo(this.fieldInfo.getConstPool(), name, data));
    }

    static class MultiArrayInitializer
    extends Initializer {
        CtClass type;
        int[] dim;

        MultiArrayInitializer(CtClass t, int[] d) {
            this.type = t;
            this.dim = d;
        }

        @Override
        void check(String desc) throws CannotCompileException {
            if (desc.charAt(0) == '[') return;
            throw new CannotCompileException("type mismatch");
        }

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            code.addAload(0);
            int s = code.addMultiNewarray(type, this.dim);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return s + 1;
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            int s = code.addMultiNewarray(type, this.dim);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return s;
        }
    }

    static class ArrayInitializer
    extends Initializer {
        CtClass type;
        int size;

        ArrayInitializer(CtClass t, int s) {
            this.type = t;
            this.size = s;
        }

        private void addNewarray(Bytecode code) {
            if (this.type.isPrimitive()) {
                code.addNewarray(((CtPrimitiveType)this.type).getArrayType(), this.size);
                return;
            }
            code.addAnewarray(this.type, this.size);
        }

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            code.addAload(0);
            this.addNewarray(code);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            this.addNewarray(code);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 1;
        }
    }

    static class StringInitializer
    extends Initializer {
        String value;

        StringInitializer(String v) {
            this.value = v;
        }

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addLdc(this.value);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            code.addLdc(this.value);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 1;
        }

        @Override
        int getConstantValue(ConstPool cp, CtClass type) {
            if (!type.getName().equals(CtField.javaLangString)) return 0;
            return cp.addStringInfo(this.value);
        }
    }

    static class DoubleInitializer
    extends Initializer {
        double value;

        DoubleInitializer(double v) {
            this.value = v;
        }

        @Override
        void check(String desc) throws CannotCompileException {
            if (desc.equals("D")) return;
            throw new CannotCompileException("type mismatch");
        }

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addLdc2w(this.value);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 3;
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            code.addLdc2w(this.value);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }

        @Override
        int getConstantValue(ConstPool cp, CtClass type) {
            if (type != CtClass.doubleType) return 0;
            return cp.addDoubleInfo(this.value);
        }
    }

    static class FloatInitializer
    extends Initializer {
        float value;

        FloatInitializer(float v) {
            this.value = v;
        }

        @Override
        void check(String desc) throws CannotCompileException {
            if (desc.equals("F")) return;
            throw new CannotCompileException("type mismatch");
        }

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addFconst(this.value);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 3;
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            code.addFconst(this.value);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }

        @Override
        int getConstantValue(ConstPool cp, CtClass type) {
            if (type != CtClass.floatType) return 0;
            return cp.addFloatInfo(this.value);
        }
    }

    static class LongInitializer
    extends Initializer {
        long value;

        LongInitializer(long v) {
            this.value = v;
        }

        @Override
        void check(String desc) throws CannotCompileException {
            if (desc.equals("J")) return;
            throw new CannotCompileException("type mismatch");
        }

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addLdc2w(this.value);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 3;
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            code.addLdc2w(this.value);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }

        @Override
        int getConstantValue(ConstPool cp, CtClass type) {
            if (type != CtClass.longType) return 0;
            return cp.addLongInfo(this.value);
        }
    }

    static class IntInitializer
    extends Initializer {
        int value;

        IntInitializer(int v) {
            this.value = v;
        }

        @Override
        void check(String desc) throws CannotCompileException {
            char c = desc.charAt(0);
            if (c == 'I') return;
            if (c == 'S') return;
            if (c == 'B') return;
            if (c == 'C') return;
            if (c == 'Z') return;
            throw new CannotCompileException("type mismatch");
        }

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addIconst(this.value);
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return 2;
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            code.addIconst(this.value);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return 1;
        }

        @Override
        int getConstantValue(ConstPool cp, CtClass type) {
            return cp.addIntegerInfo(this.value);
        }
    }

    static class MethodInitializer
    extends NewInitializer {
        String methodName;

        MethodInitializer() {
        }

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addAload(0);
            int stacksize = this.stringParams == null ? 2 : this.compileStringParameter(code) + 2;
            if (this.withConstructorParams) {
                stacksize += CtNewWrappedMethod.compileParameterList(code, parameters, 1);
            }
            String typeDesc = Descriptor.of(type);
            String mDesc = this.getDescriptor() + typeDesc;
            code.addInvokestatic(this.objectType, this.methodName, mDesc);
            code.addPutfield(Bytecode.THIS, name, typeDesc);
            return stacksize;
        }

        private String getDescriptor() {
            String desc3 = "(Ljava/lang/Object;[Ljava/lang/String;[Ljava/lang/Object;)";
            if (this.stringParams == null) {
                if (!this.withConstructorParams) return "(Ljava/lang/Object;)";
                return "(Ljava/lang/Object;[Ljava/lang/Object;)";
            }
            if (!this.withConstructorParams) return "(Ljava/lang/Object;[Ljava/lang/String;)";
            return "(Ljava/lang/Object;[Ljava/lang/String;[Ljava/lang/Object;)";
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            String desc;
            int stacksize = 1;
            if (this.stringParams == null) {
                desc = "()";
            } else {
                desc = "([Ljava/lang/String;)";
                stacksize += this.compileStringParameter(code);
            }
            String typeDesc = Descriptor.of(type);
            code.addInvokestatic(this.objectType, this.methodName, desc + typeDesc);
            code.addPutstatic(Bytecode.THIS, name, typeDesc);
            return stacksize;
        }
    }

    static class NewInitializer
    extends Initializer {
        CtClass objectType;
        String[] stringParams;
        boolean withConstructorParams;

        NewInitializer() {
        }

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            code.addAload(0);
            code.addNew(this.objectType);
            code.add(89);
            code.addAload(0);
            int stacksize = this.stringParams == null ? 4 : this.compileStringParameter(code) + 4;
            if (this.withConstructorParams) {
                stacksize += CtNewWrappedMethod.compileParameterList(code, parameters, 1);
            }
            code.addInvokespecial(this.objectType, "<init>", this.getDescriptor());
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return stacksize;
        }

        private String getDescriptor() {
            String desc3 = "(Ljava/lang/Object;[Ljava/lang/String;[Ljava/lang/Object;)V";
            if (this.stringParams == null) {
                if (!this.withConstructorParams) return "(Ljava/lang/Object;)V";
                return "(Ljava/lang/Object;[Ljava/lang/Object;)V";
            }
            if (!this.withConstructorParams) return "(Ljava/lang/Object;[Ljava/lang/String;)V";
            return "(Ljava/lang/Object;[Ljava/lang/String;[Ljava/lang/Object;)V";
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            String desc;
            code.addNew(this.objectType);
            code.add(89);
            int stacksize = 2;
            if (this.stringParams == null) {
                desc = "()V";
            } else {
                desc = "([Ljava/lang/String;)V";
                stacksize += this.compileStringParameter(code);
            }
            code.addInvokespecial(this.objectType, "<init>", desc);
            code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
            return stacksize;
        }

        protected final int compileStringParameter(Bytecode code) throws CannotCompileException {
            int nparam = this.stringParams.length;
            code.addIconst(nparam);
            code.addAnewarray(CtField.javaLangString);
            int j = 0;
            while (j < nparam) {
                code.add(89);
                code.addIconst(j);
                code.addLdc(this.stringParams[j]);
                code.add(83);
                ++j;
            }
            return 4;
        }
    }

    static class ParamInitializer
    extends Initializer {
        int nthParam;

        ParamInitializer() {
        }

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            if (parameters == null) return 0;
            if (this.nthParam >= parameters.length) return 0;
            code.addAload(0);
            int nth = ParamInitializer.nthParamToLocal(this.nthParam, parameters, false);
            int s = code.addLoad(nth, type) + 1;
            code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
            return s;
        }

        static int nthParamToLocal(int nth, CtClass[] params, boolean isStatic) {
            CtClass longType = CtClass.longType;
            CtClass doubleType = CtClass.doubleType;
            int k = isStatic ? 0 : 1;
            int i = 0;
            while (i < nth) {
                CtClass type = params[i];
                k = type == longType || type == doubleType ? (k += 2) : ++k;
                ++i;
            }
            return k;
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            return 0;
        }
    }

    static class PtreeInitializer
    extends CodeInitializer0 {
        private ASTree expression;

        PtreeInitializer(ASTree expr) {
            this.expression = expr;
        }

        @Override
        void compileExpr(Javac drv) throws CompileError {
            drv.compileExpr(this.expression);
        }

        @Override
        int getConstantValue(ConstPool cp, CtClass type) {
            return this.getConstantValue2(cp, type, this.expression);
        }
    }

    static class CodeInitializer
    extends CodeInitializer0 {
        private String expression;

        CodeInitializer(String expr) {
            this.expression = expr;
        }

        @Override
        void compileExpr(Javac drv) throws CompileError {
            drv.compileExpr(this.expression);
        }

        @Override
        int getConstantValue(ConstPool cp, CtClass type) {
            try {
                ASTree t = Javac.parseExpr(this.expression, new SymbolTable());
                return this.getConstantValue2(cp, type, t);
            }
            catch (CompileError e) {
                return 0;
            }
        }
    }

    static abstract class CodeInitializer0
    extends Initializer {
        CodeInitializer0() {
        }

        abstract void compileExpr(Javac var1) throws CompileError;

        @Override
        int compile(CtClass type, String name, Bytecode code, CtClass[] parameters, Javac drv) throws CannotCompileException {
            try {
                code.addAload(0);
                this.compileExpr(drv);
                code.addPutfield(Bytecode.THIS, name, Descriptor.of(type));
                return code.getMaxStack();
            }
            catch (CompileError e) {
                throw new CannotCompileException(e);
            }
        }

        @Override
        int compileIfStatic(CtClass type, String name, Bytecode code, Javac drv) throws CannotCompileException {
            try {
                this.compileExpr(drv);
                code.addPutstatic(Bytecode.THIS, name, Descriptor.of(type));
                return code.getMaxStack();
            }
            catch (CompileError e) {
                throw new CannotCompileException(e);
            }
        }

        int getConstantValue2(ConstPool cp, CtClass type, ASTree tree) {
            if (!type.isPrimitive()) {
                if (!(tree instanceof StringL)) return 0;
                if (!type.getName().equals(CtField.javaLangString)) return 0;
                return cp.addStringInfo(((StringL)tree).get());
            }
            if (tree instanceof IntConst) {
                long value = ((IntConst)tree).get();
                if (type == CtClass.doubleType) {
                    return cp.addDoubleInfo(value);
                }
                if (type == CtClass.floatType) {
                    return cp.addFloatInfo(value);
                }
                if (type == CtClass.longType) {
                    return cp.addLongInfo(value);
                }
                if (type == CtClass.voidType) return 0;
                return cp.addIntegerInfo((int)value);
            }
            if (!(tree instanceof DoubleConst)) return 0;
            double value = ((DoubleConst)tree).get();
            if (type == CtClass.floatType) {
                return cp.addFloatInfo((float)value);
            }
            if (type != CtClass.doubleType) return 0;
            return cp.addDoubleInfo(value);
        }
    }

    public static abstract class Initializer {
        public static Initializer constant(int i) {
            return new IntInitializer(i);
        }

        public static Initializer constant(boolean b) {
            int n;
            if (b) {
                n = 1;
                return new IntInitializer(n);
            }
            n = 0;
            return new IntInitializer(n);
        }

        public static Initializer constant(long l) {
            return new LongInitializer(l);
        }

        public static Initializer constant(float l) {
            return new FloatInitializer(l);
        }

        public static Initializer constant(double d) {
            return new DoubleInitializer(d);
        }

        public static Initializer constant(String s) {
            return new StringInitializer(s);
        }

        public static Initializer byParameter(int nth) {
            ParamInitializer i = new ParamInitializer();
            i.nthParam = nth;
            return i;
        }

        public static Initializer byNew(CtClass objectType) {
            NewInitializer i = new NewInitializer();
            i.objectType = objectType;
            i.stringParams = null;
            i.withConstructorParams = false;
            return i;
        }

        public static Initializer byNew(CtClass objectType, String[] stringParams) {
            NewInitializer i = new NewInitializer();
            i.objectType = objectType;
            i.stringParams = stringParams;
            i.withConstructorParams = false;
            return i;
        }

        public static Initializer byNewWithParams(CtClass objectType) {
            NewInitializer i = new NewInitializer();
            i.objectType = objectType;
            i.stringParams = null;
            i.withConstructorParams = true;
            return i;
        }

        public static Initializer byNewWithParams(CtClass objectType, String[] stringParams) {
            NewInitializer i = new NewInitializer();
            i.objectType = objectType;
            i.stringParams = stringParams;
            i.withConstructorParams = true;
            return i;
        }

        public static Initializer byCall(CtClass methodClass, String methodName) {
            MethodInitializer i = new MethodInitializer();
            i.objectType = methodClass;
            i.methodName = methodName;
            i.stringParams = null;
            i.withConstructorParams = false;
            return i;
        }

        public static Initializer byCall(CtClass methodClass, String methodName, String[] stringParams) {
            MethodInitializer i = new MethodInitializer();
            i.objectType = methodClass;
            i.methodName = methodName;
            i.stringParams = stringParams;
            i.withConstructorParams = false;
            return i;
        }

        public static Initializer byCallWithParams(CtClass methodClass, String methodName) {
            MethodInitializer i = new MethodInitializer();
            i.objectType = methodClass;
            i.methodName = methodName;
            i.stringParams = null;
            i.withConstructorParams = true;
            return i;
        }

        public static Initializer byCallWithParams(CtClass methodClass, String methodName, String[] stringParams) {
            MethodInitializer i = new MethodInitializer();
            i.objectType = methodClass;
            i.methodName = methodName;
            i.stringParams = stringParams;
            i.withConstructorParams = true;
            return i;
        }

        public static Initializer byNewArray(CtClass type, int size) throws NotFoundException {
            return new ArrayInitializer(type.getComponentType(), size);
        }

        public static Initializer byNewArray(CtClass type, int[] sizes) {
            return new MultiArrayInitializer(type, sizes);
        }

        public static Initializer byExpr(String source) {
            return new CodeInitializer(source);
        }

        static Initializer byExpr(ASTree source) {
            return new PtreeInitializer(source);
        }

        void check(String desc) throws CannotCompileException {
        }

        abstract int compile(CtClass var1, String var2, Bytecode var3, CtClass[] var4, Javac var5) throws CannotCompileException;

        abstract int compileIfStatic(CtClass var1, String var2, Bytecode var3, Javac var4) throws CannotCompileException;

        int getConstantValue(ConstPool cp, CtClass type) {
            return 0;
        }
    }
}

