/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufInputStream
 *  io.netty.buffer.ByteBufOutputStream
 */
package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.NBTIO;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressedNBTType
extends Type<CompoundTag> {
    public CompressedNBTType() {
        super(CompoundTag.class);
    }

    @Override
    public CompoundTag read(ByteBuf buffer) throws IOException {
        short length = buffer.readShort();
        if (length <= 0) {
            return null;
        }
        ByteBuf compressed = buffer.readSlice((int)length);
        try (GZIPInputStream gzipStream = new GZIPInputStream((InputStream)new ByteBufInputStream(compressed));){
            CompoundTag compoundTag = NBTIO.readTag(gzipStream);
            return compoundTag;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(ByteBuf buffer, CompoundTag nbt) throws Exception {
        if (nbt == null) {
            buffer.writeShort(-1);
            return;
        }
        ByteBuf compressedBuf = buffer.alloc().buffer();
        try {
            try (GZIPOutputStream gzipStream = new GZIPOutputStream((OutputStream)new ByteBufOutputStream(compressedBuf));){
                NBTIO.writeTag(gzipStream, nbt);
            }
            buffer.writeShort(compressedBuf.readableBytes());
            buffer.writeBytes(compressedBuf);
            return;
        }
        finally {
            compressedBuf.release();
        }
    }
}

