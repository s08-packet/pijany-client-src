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

public class ByteType
extends Type<Byte>
implements TypeConverter<Byte> {
    public ByteType() {
        super(Byte.class);
    }

    public byte readPrimitive(ByteBuf buffer) {
        return buffer.readByte();
    }

    public void writePrimitive(ByteBuf buffer, byte object) {
        buffer.writeByte((int)object);
    }

    @Override
    @Deprecated
    public Byte read(ByteBuf buffer) {
        return buffer.readByte();
    }

    @Override
    @Deprecated
    public void write(ByteBuf buffer, Byte object) {
        buffer.writeByte((int)object.byteValue());
    }

    @Override
    public Byte from(Object o) {
        byte by;
        if (o instanceof Number) {
            return ((Number)o).byteValue();
        }
        if (!(o instanceof Boolean)) return (Byte)o;
        if (((Boolean)o).booleanValue()) {
            by = 1;
            return by;
        }
        by = 0;
        return by;
    }
}

