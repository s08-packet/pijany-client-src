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

public class VarIntType
extends Type<Integer>
implements TypeConverter<Integer> {
    public VarIntType() {
        super("VarInt", Integer.class);
    }

    public int readPrimitive(ByteBuf buffer) {
        byte in;
        int out = 0;
        int bytes = 0;
        do {
            in = buffer.readByte();
            out |= (in & 0x7F) << bytes++ * 7;
            if (bytes <= 5) continue;
            throw new RuntimeException("VarInt too big");
        } while ((in & 0x80) == 128);
        return out;
    }

    public void writePrimitive(ByteBuf buffer, int object) {
        do {
            int part = object & 0x7F;
            if ((object >>>= 7) != 0) {
                part |= 0x80;
            }
            buffer.writeByte(part);
        } while (object != 0);
    }

    @Override
    @Deprecated
    public Integer read(ByteBuf buffer) {
        return this.readPrimitive(buffer);
    }

    @Override
    @Deprecated
    public void write(ByteBuf buffer, Integer object) {
        this.writePrimitive(buffer, object);
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

