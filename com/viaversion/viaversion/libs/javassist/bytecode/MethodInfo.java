/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode;

import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.bytecode.AnnotationDefaultAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.AttributeInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.BadBytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.ByteArray;
import com.viaversion.viaversion.libs.javassist.bytecode.ClassFile;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeIterator;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.Descriptor;
import com.viaversion.viaversion.libs.javassist.bytecode.ExceptionsAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.LineNumberAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.StackMap;
import com.viaversion.viaversion.libs.javassist.bytecode.StackMapTable;
import com.viaversion.viaversion.libs.javassist.bytecode.stackmap.MapMaker;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MethodInfo {
    ConstPool constPool;
    int accessFlags;
    int name;
    String cachedName;
    int descriptor;
    List<AttributeInfo> attribute;
    public static boolean doPreverify = false;
    public static final String nameInit = "<init>";
    public static final String nameClinit = "<clinit>";

    private MethodInfo(ConstPool cp) {
        this.constPool = cp;
        this.attribute = null;
    }

    public MethodInfo(ConstPool cp, String methodname, String desc) {
        this(cp);
        this.accessFlags = 0;
        this.name = cp.addUtf8Info(methodname);
        this.cachedName = methodname;
        this.descriptor = this.constPool.addUtf8Info(desc);
    }

    MethodInfo(ConstPool cp, DataInputStream in) throws IOException {
        this(cp);
        this.read(in);
    }

    public MethodInfo(ConstPool cp, String methodname, MethodInfo src, Map<String, String> classnameMap) throws BadBytecode {
        this(cp);
        this.read(src, methodname, classnameMap);
    }

    public String toString() {
        return this.getName() + " " + this.getDescriptor();
    }

    void compact(ConstPool cp) {
        this.name = cp.addUtf8Info(this.getName());
        this.descriptor = cp.addUtf8Info(this.getDescriptor());
        this.attribute = AttributeInfo.copyAll(this.attribute, cp);
        this.constPool = cp;
    }

    void prune(ConstPool cp) {
        AttributeInfo signature;
        ExceptionsAttribute ea;
        AnnotationDefaultAttribute defaultAttribute;
        AttributeInfo parameterVisibleAnnotations;
        AttributeInfo parameterInvisibleAnnotations;
        AttributeInfo visibleAnnotations;
        ArrayList<AttributeInfo> newAttributes = new ArrayList<AttributeInfo>();
        AttributeInfo invisibleAnnotations = this.getAttribute("RuntimeInvisibleAnnotations");
        if (invisibleAnnotations != null) {
            invisibleAnnotations = invisibleAnnotations.copy(cp, null);
            newAttributes.add(invisibleAnnotations);
        }
        if ((visibleAnnotations = this.getAttribute("RuntimeVisibleAnnotations")) != null) {
            visibleAnnotations = visibleAnnotations.copy(cp, null);
            newAttributes.add(visibleAnnotations);
        }
        if ((parameterInvisibleAnnotations = this.getAttribute("RuntimeInvisibleParameterAnnotations")) != null) {
            parameterInvisibleAnnotations = parameterInvisibleAnnotations.copy(cp, null);
            newAttributes.add(parameterInvisibleAnnotations);
        }
        if ((parameterVisibleAnnotations = this.getAttribute("RuntimeVisibleParameterAnnotations")) != null) {
            parameterVisibleAnnotations = parameterVisibleAnnotations.copy(cp, null);
            newAttributes.add(parameterVisibleAnnotations);
        }
        if ((defaultAttribute = (AnnotationDefaultAttribute)this.getAttribute("AnnotationDefault")) != null) {
            newAttributes.add(defaultAttribute);
        }
        if ((ea = this.getExceptionsAttribute()) != null) {
            newAttributes.add(ea);
        }
        if ((signature = this.getAttribute("Signature")) != null) {
            signature = signature.copy(cp, null);
            newAttributes.add(signature);
        }
        this.attribute = newAttributes;
        this.name = cp.addUtf8Info(this.getName());
        this.descriptor = cp.addUtf8Info(this.getDescriptor());
        this.constPool = cp;
    }

    public String getName() {
        if (this.cachedName != null) return this.cachedName;
        this.cachedName = this.constPool.getUtf8Info(this.name);
        return this.cachedName;
    }

    public void setName(String newName) {
        this.name = this.constPool.addUtf8Info(newName);
        this.cachedName = newName;
    }

    public boolean isMethod() {
        String n = this.getName();
        if (n.equals(nameInit)) return false;
        if (n.equals(nameClinit)) return false;
        return true;
    }

    public ConstPool getConstPool() {
        return this.constPool;
    }

    public boolean isConstructor() {
        return this.getName().equals(nameInit);
    }

    public boolean isStaticInitializer() {
        return this.getName().equals(nameClinit);
    }

    public int getAccessFlags() {
        return this.accessFlags;
    }

    public void setAccessFlags(int acc) {
        this.accessFlags = acc;
    }

    public String getDescriptor() {
        return this.constPool.getUtf8Info(this.descriptor);
    }

    public void setDescriptor(String desc) {
        if (desc.equals(this.getDescriptor())) return;
        this.descriptor = this.constPool.addUtf8Info(desc);
    }

    public List<AttributeInfo> getAttributes() {
        if (this.attribute != null) return this.attribute;
        this.attribute = new ArrayList<AttributeInfo>();
        return this.attribute;
    }

    public AttributeInfo getAttribute(String name) {
        return AttributeInfo.lookup(this.attribute, name);
    }

    public AttributeInfo removeAttribute(String name) {
        return AttributeInfo.remove(this.attribute, name);
    }

    public void addAttribute(AttributeInfo info) {
        if (this.attribute == null) {
            this.attribute = new ArrayList<AttributeInfo>();
        }
        AttributeInfo.remove(this.attribute, info.getName());
        this.attribute.add(info);
    }

    public ExceptionsAttribute getExceptionsAttribute() {
        AttributeInfo info = AttributeInfo.lookup(this.attribute, "Exceptions");
        return (ExceptionsAttribute)info;
    }

    public CodeAttribute getCodeAttribute() {
        AttributeInfo info = AttributeInfo.lookup(this.attribute, "Code");
        return (CodeAttribute)info;
    }

    public void removeExceptionsAttribute() {
        AttributeInfo.remove(this.attribute, "Exceptions");
    }

    public void setExceptionsAttribute(ExceptionsAttribute cattr) {
        this.removeExceptionsAttribute();
        if (this.attribute == null) {
            this.attribute = new ArrayList<AttributeInfo>();
        }
        this.attribute.add(cattr);
    }

    public void removeCodeAttribute() {
        AttributeInfo.remove(this.attribute, "Code");
    }

    public void setCodeAttribute(CodeAttribute cattr) {
        this.removeCodeAttribute();
        if (this.attribute == null) {
            this.attribute = new ArrayList<AttributeInfo>();
        }
        this.attribute.add(cattr);
    }

    public void rebuildStackMapIf6(ClassPool pool, ClassFile cf) throws BadBytecode {
        if (cf.getMajorVersion() >= 50) {
            this.rebuildStackMap(pool);
        }
        if (!doPreverify) return;
        this.rebuildStackMapForME(pool);
    }

    public void rebuildStackMap(ClassPool pool) throws BadBytecode {
        CodeAttribute ca = this.getCodeAttribute();
        if (ca == null) return;
        StackMapTable smt = MapMaker.make(pool, this);
        ca.setAttribute(smt);
    }

    public void rebuildStackMapForME(ClassPool pool) throws BadBytecode {
        CodeAttribute ca = this.getCodeAttribute();
        if (ca == null) return;
        StackMap sm = MapMaker.make2(pool, this);
        ca.setAttribute(sm);
    }

    public int getLineNumber(int pos) {
        CodeAttribute ca = this.getCodeAttribute();
        if (ca == null) {
            return -1;
        }
        LineNumberAttribute ainfo = (LineNumberAttribute)ca.getAttribute("LineNumberTable");
        if (ainfo != null) return ainfo.toLineNumber(pos);
        return -1;
    }

    public void setSuperclass(String superclass) throws BadBytecode {
        if (!this.isConstructor()) {
            return;
        }
        CodeAttribute ca = this.getCodeAttribute();
        byte[] code = ca.getCode();
        CodeIterator iterator = ca.iterator();
        int pos = iterator.skipSuperConstructor();
        if (pos < 0) return;
        ConstPool cp = this.constPool;
        int mref = ByteArray.readU16bit(code, pos + 1);
        int nt = cp.getMethodrefNameAndType(mref);
        int sc = cp.addClassInfo(superclass);
        int mref2 = cp.addMethodrefInfo(sc, nt);
        ByteArray.write16bit(mref2, code, pos + 1);
    }

    private void read(MethodInfo src, String methodname, Map<String, String> classnames) {
        CodeAttribute cattr;
        ConstPool destCp = this.constPool;
        this.accessFlags = src.accessFlags;
        this.name = destCp.addUtf8Info(methodname);
        this.cachedName = methodname;
        ConstPool srcCp = src.constPool;
        String desc = srcCp.getUtf8Info(src.descriptor);
        String desc2 = Descriptor.rename(desc, classnames);
        this.descriptor = destCp.addUtf8Info(desc2);
        this.attribute = new ArrayList<AttributeInfo>();
        ExceptionsAttribute eattr = src.getExceptionsAttribute();
        if (eattr != null) {
            this.attribute.add(eattr.copy(destCp, classnames));
        }
        if ((cattr = src.getCodeAttribute()) == null) return;
        this.attribute.add(cattr.copy(destCp, classnames));
    }

    private void read(DataInputStream in) throws IOException {
        this.accessFlags = in.readUnsignedShort();
        this.name = in.readUnsignedShort();
        this.descriptor = in.readUnsignedShort();
        int n = in.readUnsignedShort();
        this.attribute = new ArrayList<AttributeInfo>();
        int i = 0;
        while (i < n) {
            this.attribute.add(AttributeInfo.read(this.constPool, in));
            ++i;
        }
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(this.accessFlags);
        out.writeShort(this.name);
        out.writeShort(this.descriptor);
        if (this.attribute == null) {
            out.writeShort(0);
            return;
        }
        out.writeShort(this.attribute.size());
        AttributeInfo.writeAll(this.attribute, out);
    }
}

