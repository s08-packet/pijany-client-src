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

class IntegerInfo
extends ConstInfo {
    static final int tag = 3;
    int value;

    public IntegerInfo(int v, int index) {
        super(index);
        this.value = v;
    }

    public IntegerInfo(DataInputStream in, int index) throws IOException {
        super(index);
        this.value = in.readInt();
    }

    public int hashCode() {
        return this.value;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof IntegerInfo)) return false;
        if (((IntegerInfo)obj).value != this.value) return false;
        return true;
    }

    @Override
    public int getTag() {
        return 3;
    }

    @Override
    public int copy(ConstPool src, ConstPool dest, Map<String, String> map) {
        return dest.addIntegerInfo(this.value);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(3);
        out.writeInt(this.value);
    }

    @Override
    public void print(PrintWriter out) {
        out.print("Integer ");
        out.println(this.value);
    }
}

