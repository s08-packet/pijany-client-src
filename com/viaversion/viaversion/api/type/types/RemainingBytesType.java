/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;

public class RemainingBytesType
extends Type<byte[]> {
    public RemainingBytesType() {
        super(byte[].class);
    }

    @Override
    public byte[] read(ByteBuf buffer) {
        byte[] array = new byte[buffer.readableBytes()];
        buffer.readBytes(array);
        return array;
    }

    @Override
    public void write(ByteBuf buffer, byte[] object) {
        buffer.writeBytes(object);
    }
}

