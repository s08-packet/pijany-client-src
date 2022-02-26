/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode;

import com.viaversion.viaversion.libs.javassist.bytecode.ConstInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.Descriptor;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

class ClassInfo
extends ConstInfo {
    static final int tag = 7;
    int name;

    public ClassInfo(int className, int index) {
        super(index);
        this.name = className;
    }

    public ClassInfo(DataInputStream in, int index) throws IOException {
        super(index);
        this.name = in.readUnsignedShort();
    }

    public int hashCode() {
        return this.name;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ClassInfo)) return false;
        if (((ClassInfo)obj).name != this.name) return false;
        return true;
    }

    @Override
    public int getTag() {
        return 7;
    }

    @Override
    public String getClassName(ConstPool cp) {
        return cp.getUtf8Info(this.name);
    }

    @Override
    public void renameClass(ConstPool cp, String oldName, String newName, Map<ConstInfo, ConstInfo> cache) {
        String s;
        String nameStr = cp.getUtf8Info(this.name);
        String newNameStr = null;
        if (nameStr.equals(oldName)) {
            newNameStr = newName;
        } else if (nameStr.charAt(0) == '[' && nameStr != (s = Descriptor.rename(nameStr, oldName, newName))) {
            newNameStr = s;
        }
        if (newNameStr == null) return;
        if (cache == null) {
            this.name = cp.addUtf8Info(newNameStr);
            return;
        }
        cache.remove(this);
        this.name = cp.addUtf8Info(newNameStr);
        cache.put(this, this);
    }

    @Override
    public void renameClass(ConstPool cp, Map<String, String> map, Map<ConstInfo, ConstInfo> cache) {
        String oldName = cp.getUtf8Info(this.name);
        String newName = null;
        if (oldName.charAt(0) == '[') {
            String s = Descriptor.rename(oldName, map);
            if (oldName != s) {
                newName = s;
            }
        } else {
            String s = map.get(oldName);
            if (s != null && !s.equals(oldName)) {
                newName = s;
            }
        }
        if (newName == null) return;
        if (cache == null) {
            this.name = cp.addUtf8Info(newName);
            return;
        }
        cache.remove(this);
        this.name = cp.addUtf8Info(newName);
        cache.put(this, this);
    }

    @Override
    public int copy(ConstPool src, ConstPool dest, Map<String, String> map) {
        String classname = src.getUtf8Info(this.name);
        if (map == null) return dest.addClassInfo(classname);
        String newname = map.get(classname);
        if (newname == null) return dest.addClassInfo(classname);
        classname = newname;
        return dest.addClassInfo(classname);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(7);
        out.writeShort(this.name);
    }

    @Override
    public void print(PrintWriter out) {
        out.print("Class #");
        out.println(this.name);
    }
}

