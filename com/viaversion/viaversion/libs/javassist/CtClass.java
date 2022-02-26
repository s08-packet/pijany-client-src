/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist;

import com.viaversion.viaversion.libs.javassist.CannotCompileException;
import com.viaversion.viaversion.libs.javassist.ClassMap;
import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.CodeConverter;
import com.viaversion.viaversion.libs.javassist.CtBehavior;
import com.viaversion.viaversion.libs.javassist.CtConstructor;
import com.viaversion.viaversion.libs.javassist.CtField;
import com.viaversion.viaversion.libs.javassist.CtMethod;
import com.viaversion.viaversion.libs.javassist.CtPrimitiveType;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.ClassFile;
import com.viaversion.viaversion.libs.javassist.bytecode.Descriptor;
import com.viaversion.viaversion.libs.javassist.compiler.AccessorMaker;
import com.viaversion.viaversion.libs.javassist.expr.ExprEditor;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.Collection;

public abstract class CtClass {
    protected String qualifiedName;
    public static String debugDump = null;
    public static final String version = "3.28.0-GA";
    static final String javaLangObject = "java.lang.Object";
    public static CtClass booleanType;
    public static CtClass charType;
    public static CtClass byteType;
    public static CtClass shortType;
    public static CtClass intType;
    public static CtClass longType;
    public static CtClass floatType;
    public static CtClass doubleType;
    public static CtClass voidType;
    static CtClass[] primitiveTypes;

    public static void main(String[] args) {
        System.out.println("Javassist version 3.28.0-GA");
        System.out.println("Copyright (C) 1999-2021 Shigeru Chiba. All Rights Reserved.");
    }

    protected CtClass(String name) {
        this.qualifiedName = name;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(this.getClass().getName());
        buf.append("@");
        buf.append(Integer.toHexString(this.hashCode()));
        buf.append("[");
        this.extendToString(buf);
        buf.append("]");
        return buf.toString();
    }

    protected void extendToString(StringBuffer buffer) {
        buffer.append(this.getName());
    }

    public ClassPool getClassPool() {
        return null;
    }

    public ClassFile getClassFile() {
        this.checkModify();
        return this.getClassFile2();
    }

    public ClassFile getClassFile2() {
        return null;
    }

    public AccessorMaker getAccessorMaker() {
        return null;
    }

    public URL getURL() throws NotFoundException {
        throw new NotFoundException(this.getName());
    }

    public boolean isModified() {
        return false;
    }

    public boolean isFrozen() {
        return true;
    }

    public void freeze() {
    }

    void checkModify() throws RuntimeException {
        if (!this.isFrozen()) return;
        throw new RuntimeException(this.getName() + " class is frozen");
    }

    public void defrost() {
        throw new RuntimeException("cannot defrost " + this.getName());
    }

    public boolean isPrimitive() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isKotlin() {
        return this.hasAnnotation("kotlin.Metadata");
    }

    public CtClass getComponentType() throws NotFoundException {
        return null;
    }

    public boolean subtypeOf(CtClass clazz) throws NotFoundException {
        if (this == clazz) return true;
        if (this.getName().equals(clazz.getName())) return true;
        return false;
    }

    public String getName() {
        return this.qualifiedName;
    }

    public final String getSimpleName() {
        String qname = this.qualifiedName;
        int index = qname.lastIndexOf(46);
        if (index >= 0) return qname.substring(index + 1);
        return qname;
    }

    public final String getPackageName() {
        String qname = this.qualifiedName;
        int index = qname.lastIndexOf(46);
        if (index >= 0) return qname.substring(0, index);
        return null;
    }

    public void setName(String name) {
        this.checkModify();
        if (name == null) return;
        this.qualifiedName = name;
    }

    public String getGenericSignature() {
        return null;
    }

    public void setGenericSignature(String sig) {
        this.checkModify();
    }

    public void replaceClassName(String oldName, String newName) {
        this.checkModify();
    }

    public void replaceClassName(ClassMap map) {
        this.checkModify();
    }

    public synchronized Collection<String> getRefClasses() {
        ClassFile cf = this.getClassFile2();
        if (cf == null) return null;
        ClassMap cm = new ClassMap(){
            private static final long serialVersionUID = 1L;

            @Override
            public String put(String oldname, String newname) {
                return this.put0(oldname, newname);
            }

            @Override
            public String get(Object jvmClassName) {
                String n = 1.toJavaName((String)jvmClassName);
                this.put0(n, n);
                return null;
            }

            @Override
            public void fix(String name) {
            }
        };
        cf.getRefClasses(cm);
        return cm.values();
    }

    public boolean isInterface() {
        return false;
    }

    public boolean isAnnotation() {
        return false;
    }

    public boolean isEnum() {
        return false;
    }

    public int getModifiers() {
        return 0;
    }

    public boolean hasAnnotation(Class<?> annotationType) {
        return this.hasAnnotation(annotationType.getName());
    }

    public boolean hasAnnotation(String annotationTypeName) {
        return false;
    }

    public Object getAnnotation(Class<?> clz) throws ClassNotFoundException {
        return null;
    }

    public Object[] getAnnotations() throws ClassNotFoundException {
        return new Object[0];
    }

    public Object[] getAvailableAnnotations() {
        return new Object[0];
    }

    public CtClass[] getDeclaredClasses() throws NotFoundException {
        return this.getNestedClasses();
    }

    public CtClass[] getNestedClasses() throws NotFoundException {
        return new CtClass[0];
    }

    public void setModifiers(int mod) {
        this.checkModify();
    }

    public boolean subclassOf(CtClass superclass) {
        return false;
    }

    public CtClass getSuperclass() throws NotFoundException {
        return null;
    }

    public void setSuperclass(CtClass clazz) throws CannotCompileException {
        this.checkModify();
    }

    public CtClass[] getInterfaces() throws NotFoundException {
        return new CtClass[0];
    }

    public void setInterfaces(CtClass[] list) {
        this.checkModify();
    }

    public void addInterface(CtClass anInterface) {
        this.checkModify();
    }

    public CtClass getDeclaringClass() throws NotFoundException {
        return null;
    }

    @Deprecated
    public final CtMethod getEnclosingMethod() throws NotFoundException {
        CtBehavior b = this.getEnclosingBehavior();
        if (b == null) {
            return null;
        }
        if (!(b instanceof CtMethod)) throw new NotFoundException(b.getLongName() + " is enclosing " + this.getName());
        return (CtMethod)b;
    }

    public CtBehavior getEnclosingBehavior() throws NotFoundException {
        return null;
    }

    public CtClass makeNestedClass(String name, boolean isStatic) {
        throw new RuntimeException(this.getName() + " is not a class");
    }

    public CtField[] getFields() {
        return new CtField[0];
    }

    public CtField getField(String name) throws NotFoundException {
        return this.getField(name, null);
    }

    public CtField getField(String name, String desc) throws NotFoundException {
        throw new NotFoundException(name);
    }

    CtField getField2(String name, String desc) {
        return null;
    }

    public CtField[] getDeclaredFields() {
        return new CtField[0];
    }

    public CtField getDeclaredField(String name) throws NotFoundException {
        throw new NotFoundException(name);
    }

    public CtField getDeclaredField(String name, String desc) throws NotFoundException {
        throw new NotFoundException(name);
    }

    public CtBehavior[] getDeclaredBehaviors() {
        return new CtBehavior[0];
    }

    public CtConstructor[] getConstructors() {
        return new CtConstructor[0];
    }

    public CtConstructor getConstructor(String desc) throws NotFoundException {
        throw new NotFoundException("no such constructor");
    }

    public CtConstructor[] getDeclaredConstructors() {
        return new CtConstructor[0];
    }

    public CtConstructor getDeclaredConstructor(CtClass[] params) throws NotFoundException {
        String desc = Descriptor.ofConstructor(params);
        return this.getConstructor(desc);
    }

    public CtConstructor getClassInitializer() {
        return null;
    }

    public CtMethod[] getMethods() {
        return new CtMethod[0];
    }

    public CtMethod getMethod(String name, String desc) throws NotFoundException {
        throw new NotFoundException(name);
    }

    public CtMethod[] getDeclaredMethods() {
        return new CtMethod[0];
    }

    public CtMethod getDeclaredMethod(String name, CtClass[] params) throws NotFoundException {
        throw new NotFoundException(name);
    }

    public CtMethod[] getDeclaredMethods(String name) throws NotFoundException {
        throw new NotFoundException(name);
    }

    public CtMethod getDeclaredMethod(String name) throws NotFoundException {
        throw new NotFoundException(name);
    }

    public CtConstructor makeClassInitializer() throws CannotCompileException {
        throw new CannotCompileException("not a class");
    }

    public void addConstructor(CtConstructor c) throws CannotCompileException {
        this.checkModify();
    }

    public void removeConstructor(CtConstructor c) throws NotFoundException {
        this.checkModify();
    }

    public void addMethod(CtMethod m) throws CannotCompileException {
        this.checkModify();
    }

    public void removeMethod(CtMethod m) throws NotFoundException {
        this.checkModify();
    }

    public void addField(CtField f) throws CannotCompileException {
        this.addField(f, (CtField.Initializer)null);
    }

    public void addField(CtField f, String init) throws CannotCompileException {
        this.checkModify();
    }

    public void addField(CtField f, CtField.Initializer init) throws CannotCompileException {
        this.checkModify();
    }

    public void removeField(CtField f) throws NotFoundException {
        this.checkModify();
    }

    public byte[] getAttribute(String name) {
        return null;
    }

    public void setAttribute(String name, byte[] data) {
        this.checkModify();
    }

    public void instrument(CodeConverter converter) throws CannotCompileException {
        this.checkModify();
    }

    public void instrument(ExprEditor editor) throws CannotCompileException {
        this.checkModify();
    }

    public Class<?> toClass() throws CannotCompileException {
        return this.getClassPool().toClass(this);
    }

    public Class<?> toClass(Class<?> neighbor) throws CannotCompileException {
        return this.getClassPool().toClass(this, neighbor);
    }

    public Class<?> toClass(MethodHandles.Lookup lookup) throws CannotCompileException {
        return this.getClassPool().toClass(this, lookup);
    }

    public Class<?> toClass(ClassLoader loader, ProtectionDomain domain) throws CannotCompileException {
        ClassPool cp = this.getClassPool();
        if (loader != null) return cp.toClass(this, null, loader, domain);
        loader = cp.getClassLoader();
        return cp.toClass(this, null, loader, domain);
    }

    @Deprecated
    public final Class<?> toClass(ClassLoader loader) throws CannotCompileException {
        return this.getClassPool().toClass(this, null, loader, null);
    }

    public void detach() {
        ClassPool cp = this.getClassPool();
        CtClass obj = cp.removeCached(this.getName());
        if (obj == this) return;
        cp.cacheCtClass(this.getName(), obj, false);
    }

    public boolean stopPruning(boolean stop) {
        return true;
    }

    public void prune() {
    }

    void incGetCounter() {
    }

    public void rebuildClassFile() {
    }

    public byte[] toBytecode() throws IOException, CannotCompileException {
        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(barray);){
            this.toBytecode(out);
            return barray.toByteArray();
        }
    }

    public void writeFile() throws NotFoundException, IOException, CannotCompileException {
        this.writeFile(".");
    }

    public void writeFile(String directoryName) throws CannotCompileException, IOException {
        try (DataOutputStream out = this.makeFileOutput(directoryName);){
            this.toBytecode(out);
            return;
        }
    }

    protected DataOutputStream makeFileOutput(String directoryName) {
        String classname = this.getName();
        String filename = directoryName + File.separatorChar + classname.replace('.', File.separatorChar) + ".class";
        int pos = filename.lastIndexOf(File.separatorChar);
        if (pos <= 0) return new DataOutputStream(new BufferedOutputStream(new DelayedFileOutputStream(filename)));
        String dir = filename.substring(0, pos);
        if (dir.equals(".")) return new DataOutputStream(new BufferedOutputStream(new DelayedFileOutputStream(filename)));
        new File(dir).mkdirs();
        return new DataOutputStream(new BufferedOutputStream(new DelayedFileOutputStream(filename)));
    }

    public void debugWriteFile() {
        this.debugWriteFile(".");
    }

    public void debugWriteFile(String directoryName) {
        try {
            boolean p = this.stopPruning(true);
            this.writeFile(directoryName);
            this.defrost();
            this.stopPruning(p);
            return;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void toBytecode(DataOutputStream out) throws CannotCompileException, IOException {
        throw new CannotCompileException("not a class");
    }

    public String makeUniqueName(String prefix) {
        throw new RuntimeException("not available in " + this.getName());
    }

    void compress() {
    }

    static {
        primitiveTypes = new CtClass[9];
        CtClass.primitiveTypes[0] = booleanType = new CtPrimitiveType("boolean", 'Z', "java.lang.Boolean", "booleanValue", "()Z", 172, 4, 1);
        CtClass.primitiveTypes[1] = charType = new CtPrimitiveType("char", 'C', "java.lang.Character", "charValue", "()C", 172, 5, 1);
        CtClass.primitiveTypes[2] = byteType = new CtPrimitiveType("byte", 'B', "java.lang.Byte", "byteValue", "()B", 172, 8, 1);
        CtClass.primitiveTypes[3] = shortType = new CtPrimitiveType("short", 'S', "java.lang.Short", "shortValue", "()S", 172, 9, 1);
        CtClass.primitiveTypes[4] = intType = new CtPrimitiveType("int", 'I', "java.lang.Integer", "intValue", "()I", 172, 10, 1);
        CtClass.primitiveTypes[5] = longType = new CtPrimitiveType("long", 'J', "java.lang.Long", "longValue", "()J", 173, 11, 2);
        CtClass.primitiveTypes[6] = floatType = new CtPrimitiveType("float", 'F', "java.lang.Float", "floatValue", "()F", 174, 6, 1);
        CtClass.primitiveTypes[7] = doubleType = new CtPrimitiveType("double", 'D', "java.lang.Double", "doubleValue", "()D", 175, 7, 2);
        CtClass.primitiveTypes[8] = voidType = new CtPrimitiveType("void", 'V', "java.lang.Void", null, null, 177, 0, 0);
    }

    static class DelayedFileOutputStream
    extends OutputStream {
        private FileOutputStream file = null;
        private String filename;

        DelayedFileOutputStream(String name) {
            this.filename = name;
        }

        private void init() throws IOException {
            if (this.file != null) return;
            this.file = new FileOutputStream(this.filename);
        }

        @Override
        public void write(int b) throws IOException {
            this.init();
            this.file.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            this.init();
            this.file.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            this.init();
            this.file.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            this.init();
            this.file.flush();
        }

        @Override
        public void close() throws IOException {
            this.init();
            this.file.close();
        }
    }
}

