/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.TypeConverter;
import io.netty.buffer.ByteBuf;

public class UnsignedShortType
extends Type<Integer>
implements TypeConverter<Integer> {
    public UnsignedShortType() {
        super(Integer.class);
    }

    @Override
    public Integer read(ByteBuf buffer) {
        return buffer.readUnsignedShort();
    }

    @Override
    public void write(ByteBuf buffer, Integer object) {
        buffer.writeShort(object.intValue());
    }

    @Override
    public Integer from(Object o) {
        int n;
        if (o instanceof Number) {
            return ((Number)o).intValue();
        }
        if (!(o instanceof Boolean)) return (Integer)o;
        if (((Boolean)o).booleanValue()) {
            n = 1;
            return n;
        }
        n = 0;
        return n;
    }
}

