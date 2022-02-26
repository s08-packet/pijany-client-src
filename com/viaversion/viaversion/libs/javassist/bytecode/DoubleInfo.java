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

class DoubleInfo
extends ConstInfo {
    static final int tag = 6;
    double value;

    public DoubleInfo(double d, int index) {
        super(index);
        this.value = d;
    }

    public DoubleInfo(DataInputStream in, int index) throws IOException {
        super(index);
        this.value = in.readDouble();
    }

    public int hashCode() {
        long v = Double.doubleToLongBits(this.value);
        return (int)(v ^ v >>> 32);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof DoubleInfo)) return false;
        if (((DoubleInfo)obj).value != this.value) return false;
        return true;
    }

    @Override
    public int getTag() {
        return 6;
    }

    @Override
    public int copy(ConstPool src, ConstPool dest, Map<String, String> map) {
        return dest.addDoubleInfo(this.value);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(6);
        out.writeDouble(this.value);
    }

    @Override
    public void print(PrintWriter out) {
        out.print("Double ");
        out.println(this.value);
    }
}

