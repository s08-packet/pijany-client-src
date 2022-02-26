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

public class ShortType
extends Type<Short>
implements TypeConverter<Short> {
    public ShortType() {
        super(Short.class);
    }

    public short readPrimitive(ByteBuf buffer) {
        return buffer.readShort();
    }

    public void writePrimitive(ByteBuf buffer, short object) {
        buffer.writeShort((int)object);
    }

    @Override
    @Deprecated
    public Short read(ByteBuf buffer) {
        return buffer.readShort();
    }

    @Override
    @Deprecated
    public void write(ByteBuf buffer, Short object) {
        buffer.writeShort((int)object.shortValue());
    }

    @Override
    public Short from(Object o) {
        short s;
        if (o instanceof Number) {
            return ((Number)o).shortValue();
        }
        if (!(o instanceof Boolean)) return (Short)o;
        if (((Boolean)o).booleanValue()) {
            s = 1;
            return s;
        }
        s = 0;
        return s;
    }
}

