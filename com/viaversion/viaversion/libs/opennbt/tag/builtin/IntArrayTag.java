/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.opennbt.tag.builtin;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class IntArrayTag
extends Tag {
    public static final int ID = 11;
    private int[] value;

    public IntArrayTag() {
        this(new int[0]);
    }

    public IntArrayTag(int[] value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public int[] getValue() {
        return this.value;
    }

    public void setValue(int[] value) {
        Preconditions.checkNotNull(value);
        this.value = value;
    }

    public int getValue(int index) {
        return this.value[index];
    }

    public void setValue(int index, int value) {
        this.value[index] = value;
    }

    public int length() {
        return this.value.length;
    }

    @Override
    public void read(DataInput in) throws IOException {
        this.value = new int[in.readInt()];
        int index = 0;
        while (index < this.value.length) {
            this.value[index] = in.readInt();
            ++index;
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(this.value.length);
        int[] nArray = this.value;
        int n = nArray.length;
        int n2 = 0;
        while (n2 < n) {
            int i = nArray[n2];
            out.writeInt(i);
            ++n2;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) return false;
        if (this.getClass() != o.getClass()) {
            return false;
        }
        IntArrayTag that = (IntArrayTag)o;
        return Arrays.equals(this.value, that.value);
    }

    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    @Override
    public final IntArrayTag clone() {
        return new IntArrayTag((int[])this.value.clone());
    }

    @Override
    public int getTagId() {
        return 11;
    }
}

