/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 */
package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.chunks;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.CustomByteType;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ReplacementRegistry1_7_6_10to1_8;
import de.gerrygames.viarewind.replacement.Replacement;
import de.gerrygames.viarewind.storage.BlockState;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.zip.Deflater;

public class ChunkPacketTransformer {
    private static byte[] transformChunkData(byte[] data, int primaryBitMask, boolean skyLight, boolean groundUp) {
        int dataSize = 0;
        ByteBuf buf = Unpooled.buffer();
        ByteBuf blockDataBuf = Unpooled.buffer();
        for (int i = 0; i < 16; ++i) {
            if ((primaryBitMask & 1 << i) == 0) continue;
            byte tmp = 0;
            for (int j = 0; j < 4096; ++j) {
                int meta;
                short blockData = (short)((data[dataSize + 1] & 0xFF) << 8 | data[dataSize] & 0xFF);
                dataSize += 2;
                int id = BlockState.extractId(blockData);
                Replacement replace = ReplacementRegistry1_7_6_10to1_8.getReplacement(id, meta = BlockState.extractData(blockData));
                if (replace != null) {
                    id = replace.getId();
                    meta = replace.replaceData(meta);
                }
                buf.writeByte(id);
                if (j % 2 == 0) {
                    tmp = (byte)(meta & 0xF);
                    continue;
                }
                blockDataBuf.writeByte(tmp | (meta & 0xF) << 4);
            }
        }
        buf.writeBytes(blockDataBuf);
        blockDataBuf.release();
        int columnCount = Integer.bitCount(primaryBitMask);
        buf.writeBytes(data, dataSize, 2048 * columnCount);
        dataSize += 2048 * columnCount;
        if (skyLight) {
            buf.writeBytes(data, dataSize, 2048 * columnCount);
            dataSize += 2048 * columnCount;
        }
        if (groundUp && dataSize + 256 <= data.length) {
            buf.writeBytes(data, dataSize, 256);
            dataSize += 256;
        }
        data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        buf.release();
        return data;
    }

    private static int calcSize(int i, boolean flag, boolean flag1) {
        int j = i * 2 * 16 * 16 * 16;
        int k = i * 16 * 16 * 16 / 2;
        int l = flag ? i * 16 * 16 * 16 / 2 : 0;
        int i1 = flag1 ? 256 : 0;
        return j + k + l + i1;
    }

    public static void transformChunkBulk(PacketWrapper packetWrapper) throws Exception {
        boolean skyLightSent = packetWrapper.read(Type.BOOLEAN);
        int columnCount = packetWrapper.read(Type.VAR_INT);
        int[] chunkX = new int[columnCount];
        int[] chunkZ = new int[columnCount];
        int[] primaryBitMask = new int[columnCount];
        byte[][] data = new byte[columnCount][];
        for (int i = 0; i < columnCount; ++i) {
            chunkX[i] = packetWrapper.read(Type.INT);
            chunkZ[i] = packetWrapper.read(Type.INT);
            primaryBitMask[i] = packetWrapper.read(Type.UNSIGNED_SHORT);
        }
        int totalSize = 0;
        for (int i = 0; i < columnCount; totalSize += data[i].length, ++i) {
            int size = ChunkPacketTransformer.calcSize(Integer.bitCount(primaryBitMask[i]), skyLightSent, true);
            CustomByteType customByteType = new CustomByteType(size);
            data[i] = ChunkPacketTransformer.transformChunkData(packetWrapper.read(customByteType), primaryBitMask[i], skyLightSent, true);
        }
        packetWrapper.write(Type.SHORT, (short)columnCount);
        byte[] buildBuffer = new byte[totalSize];
        int bufferLocation = 0;
        for (int i = 0; i < columnCount; bufferLocation += data[i].length, ++i) {
            System.arraycopy(data[i], 0, buildBuffer, bufferLocation, data[i].length);
        }
        Deflater deflater = new Deflater(4);
        deflater.reset();
        deflater.setInput(buildBuffer);
        deflater.finish();
        byte[] buffer = new byte[buildBuffer.length + 100];
        int compressedSize = deflater.deflate(buffer);
        byte[] finalBuffer = new byte[compressedSize];
        System.arraycopy(buffer, 0, finalBuffer, 0, compressedSize);
        packetWrapper.write(Type.INT, compressedSize);
        packetWrapper.write(Type.BOOLEAN, skyLightSent);
        CustomByteType customByteType = new CustomByteType(compressedSize);
        packetWrapper.write(customByteType, finalBuffer);
        int i = 0;
        while (i < columnCount) {
            packetWrapper.write(Type.INT, chunkX[i]);
            packetWrapper.write(Type.INT, chunkZ[i]);
            packetWrapper.write(Type.SHORT, (short)primaryBitMask[i]);
            packetWrapper.write(Type.SHORT, (short)0);
            ++i;
        }
    }
}

