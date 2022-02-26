/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode;

import com.viaversion.viaversion.libs.javassist.bytecode.AttributeInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstantAttribute;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FieldInfo {
    ConstPool constPool;
    int accessFlags;
    int name;
    String cachedName;
    String cachedType;
    int descriptor;
    List<AttributeInfo> attribute;

    private FieldInfo(ConstPool cp) {
        this.constPool = cp;
        this.accessFlags = 0;
        this.attribute = null;
    }

    public FieldInfo(ConstPool cp, String fieldName, String desc) {
        this(cp);
        this.name = cp.addUtf8Info(fieldName);
        this.cachedName = fieldName;
        this.descriptor = cp.addUtf8Info(desc);
    }

    FieldInfo(ConstPool cp, DataInputStream in) throws IOException {
        this(cp);
        this.read(in);
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
        int index;
        AttributeInfo signature;
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
        if ((signature = this.getAttribute("Signature")) != null) {
            signature = signature.copy(cp, null);
            newAttributes.add(signature);
        }
        if ((index = this.getConstantValue()) != 0) {
            index = this.constPool.copy(index, cp, null);
            newAttributes.add(new ConstantAttribute(cp, index));
        }
        this.attribute = newAttributes;
        this.name = cp.addUtf8Info(this.getName());
        this.descriptor = cp.addUtf8Info(this.getDescriptor());
        this.constPool = cp;
    }

    public ConstPool getConstPool() {
        return this.constPool;
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

    public int getConstantValue() {
        if ((this.accessFlags & 8) == 0) {
            return 0;
        }
        ConstantAttribute attr = (ConstantAttribute)this.getAttribute("ConstantValue");
        if (attr != null) return attr.getConstantValue();
        return 0;
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

