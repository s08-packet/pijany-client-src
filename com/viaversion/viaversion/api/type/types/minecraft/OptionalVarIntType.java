/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package com.viaversion.viaversion.api.type.types.minecraft;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class OptionalVarIntType
extends Type<Integer> {
    public OptionalVarIntType() {
        super(Integer.class);
    }

    @Override
    public Integer read(ByteBuf buffer) throws Exception {
        int read = Type.VAR_INT.readPrimitive(buffer);
        if (read != 0) return read - 1;
        return null;
    }

    @Override
    public void write(ByteBuf buffer, Integer object) throws Exception {
        if (object == null) {
            Type.VAR_INT.writePrimitive(buffer, 0);
            return;
        }
        Type.VAR_INT.writePrimitive(buffer, object + 1);
    }
}

