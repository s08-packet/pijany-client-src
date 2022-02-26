/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.opennbt.tag.builtin;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;

public abstract class Tag
implements Cloneable {
    public abstract Object getValue();

    public abstract void read(DataInput var1) throws IOException;

    public abstract void write(DataOutput var1) throws IOException;

    public abstract int getTagId();

    public abstract Tag clone();

    public String toString() {
        String value = "";
        if (this.getValue() == null) return this.getClass().getSimpleName() + " { " + value + " }";
        value = this.getValue().toString();
        if (!this.getValue().getClass().isArray()) return this.getClass().getSimpleName() + " { " + value + " }";
        StringBuilder build = new StringBuilder();
        build.append("[");
        int index = 0;
        while (true) {
            if (index >= Array.getLength(this.getValue())) {
                build.append("]");
                value = build.toString();
                return this.getClass().getSimpleName() + " { " + value + " }";
            }
            if (index > 0) {
                build.append(", ");
            }
            build.append(Array.get(this.getValue(), index));
            ++index;
        }
    }
}

