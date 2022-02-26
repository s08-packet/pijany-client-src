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

class Utf8Info
extends ConstInfo {
    static final int tag = 1;
    String string;

    public Utf8Info(String utf8, int index) {
        super(index);
        this.string = utf8;
    }

    public Utf8Info(DataInputStream in, int index) throws IOException {
        super(index);
        this.string = in.readUTF();
    }

    public int hashCode() {
        return this.string.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Utf8Info)) return false;
        if (!((Utf8Info)obj).string.equals(this.string)) return false;
        return true;
    }

    @Override
    public int getTag() {
        return 1;
    }

    @Override
    public int copy(ConstPool src, ConstPool dest, Map<String, String> map) {
        return dest.addUtf8Info(this.string);
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(1);
        out.writeUTF(this.string);
    }

    @Override
    public void print(PrintWriter out) {
        out.print("UTF8 \"");
        out.print(this.string);
        out.println("\"");
    }
}

