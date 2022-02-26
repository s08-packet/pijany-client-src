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

class LongInfo
extends ConstInfo {
    static final int tag = 5;
    long value;

    public LongInfo(long l, int index) {
        super(index);
        this.value = l;
    }

    public LongInfo(DataInputStream in, int index) throws IOException {
        super(index);
        this.value = in.readLong();
    }

    public int hashCode() {
        return (int)(this.value ^ this.value >>> 32);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LongInfo)) return false;
        if (((LongInfo)obj).value != this.value) return false;
        return true;
    }

    @Override
    public int getTag() {
        return 5;
    }

    @Override
    public int copy(ConstPool src, ConstPool dest, Map<String, String> map) {
        return dest.addLongInfo(this.value);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(5);
        out.writeLong(this.value);
    }

    @Override
    public void print(PrintWriter out) {
        out.print("Long ");
        out.println(this.value);
    }
}

