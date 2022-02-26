/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode;

import com.viaversion.viaversion.libs.javassist.bytecode.ConstInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

class ModuleInfo
extends ConstInfo {
    static final int tag = 19;
    int name;

    public ModuleInfo(int moduleName, int index) {
        super(index);
        this.name = moduleName;
    }

    public ModuleInfo(DataInputStream in, int index) throws IOException {
        super(index);
        this.name = in.readUnsignedShort();
    }

    public int hashCode() {
        return this.name;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ModuleInfo)) return false;
        if (((ModuleInfo)obj).name != this.name) return false;
        return true;
    }

    @Override
    public int getTag() {
        return 19;
    }

    public String getModuleName(ConstPool cp) {
        return cp.getUtf8Info(this.name);
    }

    @Override
    public int copy(ConstPool src, ConstPool dest, Map<String, String> map) {
        String moduleName = src.getUtf8Info(this.name);
        int newName = dest.addUtf8Info(moduleName);
        return dest.addModuleInfo(newName);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(19);
        out.writeShort(this.name);
    }

    @Override
    public void print(PrintWriter out) {
        out.print("Module #");
        out.println(this.name);
    }
}

