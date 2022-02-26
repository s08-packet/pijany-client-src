/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode;

import com.viaversion.viaversion.libs.javassist.bytecode.AnnotationDefaultAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.AnnotationsAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.BootstrapMethodsAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstantAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.DeprecatedAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.EnclosingMethodAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.ExceptionsAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.InnerClassesAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.LineNumberAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.LocalVariableAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.LocalVariableTypeAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.MethodParametersAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.NestHostAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.NestMembersAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.ParameterAnnotationsAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.SignatureAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.SourceFileAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.StackMap;
import com.viaversion.viaversion.libs.javassist.bytecode.StackMapTable;
import com.viaversion.viaversion.libs.javassist.bytecode.SyntheticAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.TypeAnnotationsAttribute;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AttributeInfo {
    protected ConstPool constPool;
    int name;
    byte[] info;

    protected AttributeInfo(ConstPool cp, int attrname, byte[] attrinfo) {
        this.constPool = cp;
        this.name = attrname;
        this.info = attrinfo;
    }

    protected AttributeInfo(ConstPool cp, String attrname) {
        this(cp, attrname, (byte[])null);
    }

    public AttributeInfo(ConstPool cp, String attrname, byte[] attrinfo) {
        this(cp, cp.addUtf8Info(attrname), attrinfo);
    }

    protected AttributeInfo(ConstPool cp, int n, DataInputStream in) throws IOException {
        this.constPool = cp;
        this.name = n;
        int len = in.readInt();
        this.info = new byte[len];
        if (len <= 0) return;
        in.readFully(this.info);
    }

    static AttributeInfo read(ConstPool cp, DataInputStream in) throws IOException {
        int name = in.readUnsignedShort();
        String nameStr = cp.getUtf8Info(name);
        char first = nameStr.charAt(0);
        if (first < 'E') {
            if (nameStr.equals("AnnotationDefault")) {
                return new AnnotationDefaultAttribute(cp, name, in);
            }
            if (nameStr.equals("BootstrapMethods")) {
                return new BootstrapMethodsAttribute(cp, name, in);
            }
            if (nameStr.equals("Code")) {
                return new CodeAttribute(cp, name, in);
            }
            if (nameStr.equals("ConstantValue")) {
                return new ConstantAttribute(cp, name, in);
            }
            if (nameStr.equals("Deprecated")) {
                return new DeprecatedAttribute(cp, name, in);
            }
        }
        if (first < 'M') {
            if (nameStr.equals("EnclosingMethod")) {
                return new EnclosingMethodAttribute(cp, name, in);
            }
            if (nameStr.equals("Exceptions")) {
                return new ExceptionsAttribute(cp, name, in);
            }
            if (nameStr.equals("InnerClasses")) {
                return new InnerClassesAttribute(cp, name, in);
            }
            if (nameStr.equals("LineNumberTable")) {
                return new LineNumberAttribute(cp, name, in);
            }
            if (nameStr.equals("LocalVariableTable")) {
                return new LocalVariableAttribute(cp, name, in);
            }
            if (nameStr.equals("LocalVariableTypeTable")) {
                return new LocalVariableTypeAttribute(cp, name, in);
            }
        }
        if (first < 'S') {
            if (nameStr.equals("MethodParameters")) {
                return new MethodParametersAttribute(cp, name, in);
            }
            if (nameStr.equals("NestHost")) {
                return new NestHostAttribute(cp, name, in);
            }
            if (nameStr.equals("NestMembers")) {
                return new NestMembersAttribute(cp, name, in);
            }
            if (nameStr.equals("RuntimeVisibleAnnotations")) return new AnnotationsAttribute(cp, name, in);
            if (nameStr.equals("RuntimeInvisibleAnnotations")) {
                return new AnnotationsAttribute(cp, name, in);
            }
            if (nameStr.equals("RuntimeVisibleParameterAnnotations")) return new ParameterAnnotationsAttribute(cp, name, in);
            if (nameStr.equals("RuntimeInvisibleParameterAnnotations")) {
                return new ParameterAnnotationsAttribute(cp, name, in);
            }
            if (nameStr.equals("RuntimeVisibleTypeAnnotations")) return new TypeAnnotationsAttribute(cp, name, in);
            if (nameStr.equals("RuntimeInvisibleTypeAnnotations")) {
                return new TypeAnnotationsAttribute(cp, name, in);
            }
        }
        if (first < 'S') return new AttributeInfo(cp, name, in);
        if (nameStr.equals("Signature")) {
            return new SignatureAttribute(cp, name, in);
        }
        if (nameStr.equals("SourceFile")) {
            return new SourceFileAttribute(cp, name, in);
        }
        if (nameStr.equals("Synthetic")) {
            return new SyntheticAttribute(cp, name, in);
        }
        if (nameStr.equals("StackMap")) {
            return new StackMap(cp, name, in);
        }
        if (!nameStr.equals("StackMapTable")) return new AttributeInfo(cp, name, in);
        return new StackMapTable(cp, name, in);
    }

    public String getName() {
        return this.constPool.getUtf8Info(this.name);
    }

    public ConstPool getConstPool() {
        return this.constPool;
    }

    public int length() {
        return this.info.length + 6;
    }

    public byte[] get() {
        return this.info;
    }

    public void set(byte[] newinfo) {
        this.info = newinfo;
    }

    public AttributeInfo copy(ConstPool newCp, Map<String, String> classnames) {
        return new AttributeInfo(newCp, this.getName(), Arrays.copyOf(this.info, this.info.length));
    }

    void write(DataOutputStream out) throws IOException {
        out.writeShort(this.name);
        out.writeInt(this.info.length);
        if (this.info.length <= 0) return;
        out.write(this.info);
    }

    static int getLength(List<AttributeInfo> attributes) {
        int size = 0;
        Iterator<AttributeInfo> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            AttributeInfo attr = iterator.next();
            size += attr.length();
        }
        return size;
    }

    static AttributeInfo lookup(List<AttributeInfo> attributes, String name) {
        AttributeInfo ai;
        if (attributes == null) {
            return null;
        }
        Iterator<AttributeInfo> iterator = attributes.iterator();
        do {
            if (!iterator.hasNext()) return null;
        } while (!(ai = iterator.next()).getName().equals(name));
        return ai;
    }

    static synchronized AttributeInfo remove(List<AttributeInfo> attributes, String name) {
        AttributeInfo ai;
        if (attributes == null) {
            return null;
        }
        Iterator<AttributeInfo> iterator = attributes.iterator();
        do {
            if (!iterator.hasNext()) return null;
        } while (!(ai = iterator.next()).getName().equals(name) || !attributes.remove(ai));
        return ai;
    }

    static void writeAll(List<AttributeInfo> attributes, DataOutputStream out) throws IOException {
        if (attributes == null) {
            return;
        }
        Iterator<AttributeInfo> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            AttributeInfo attr = iterator.next();
            attr.write(out);
        }
    }

    static List<AttributeInfo> copyAll(List<AttributeInfo> attributes, ConstPool cp) {
        if (attributes == null) {
            return null;
        }
        ArrayList<AttributeInfo> newList = new ArrayList<AttributeInfo>();
        Iterator<AttributeInfo> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            AttributeInfo attr = iterator.next();
            newList.add(attr.copy(cp, null));
        }
        return newList;
    }

    void renameClass(String oldname, String newname) {
    }

    void renameClass(Map<String, String> classnames) {
    }

    static void renameClass(List<AttributeInfo> attributes, String oldname, String newname) {
        if (attributes == null) {
            return;
        }
        Iterator<AttributeInfo> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            AttributeInfo ai = iterator.next();
            ai.renameClass(oldname, newname);
        }
    }

    static void renameClass(List<AttributeInfo> attributes, Map<String, String> classnames) {
        if (attributes == null) {
            return;
        }
        Iterator<AttributeInfo> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            AttributeInfo ai = iterator.next();
            ai.renameClass(classnames);
        }
    }

    void getRefClasses(Map<String, String> classnames) {
    }

    static void getRefClasses(List<AttributeInfo> attributes, Map<String, String> classnames) {
        if (attributes == null) {
            return;
        }
        Iterator<AttributeInfo> iterator = attributes.iterator();
        while (iterator.hasNext()) {
            AttributeInfo ai = iterator.next();
            ai.getRefClasses(classnames);
        }
    }
}

