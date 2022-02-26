/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.protocol.AbstractProtocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.protocol.remapper.ValueTransformer;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.IntTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.protocols.protocol1_9_1_2to1_9_3_4.types.Chunk1_9_3_4Type;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ClientboundPackets1_9_3;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.ServerboundPackets1_9_3;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.chunks.FakeTileEntity;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.types.Chunk1_9_1_2Type;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ServerboundPackets1_9;

public class Protocol1_9_3To1_9_1_2
extends AbstractProtocol<ClientboundPackets1_9, ClientboundPackets1_9_3, ServerboundPackets1_9, ServerboundPackets1_9_3> {
    public static final ValueTransformer<Short, Short> ADJUST_PITCH = new ValueTransformer<Short, Short>((Type)Type.UNSIGNED_BYTE, (Type)Type.UNSIGNED_BYTE){

        @Override
        public Short transform(PacketWrapper wrapper, Short inputValue) throws Exception {
            return (short)Math.round((float)inputValue.shortValue() / 63.5f * 63.0f);
        }
    };

    public Protocol1_9_3To1_9_1_2() {
        super(ClientboundPackets1_9.class, ClientboundPackets1_9_3.class, ServerboundPackets1_9.class, ServerboundPackets1_9_3.class);
    }

    @Override
    protected void registerPackets() {
        this.registerClientbound(ClientboundPackets1_9.UPDATE_SIGN, null, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.handler(new PacketHandler(){

                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        Position position = wrapper.read(Type.POSITION);
                        JsonElement[] lines = new JsonElement[4];
                        for (int i = 0; i < 4; ++i) {
                            lines[i] = wrapper.read(Type.COMPONENT);
                        }
                        wrapper.clearInputBuffer();
                        wrapper.setId(9);
                        wrapper.write(Type.POSITION, position);
                        wrapper.write(Type.UNSIGNED_BYTE, (short)9);
                        CompoundTag tag = new CompoundTag();
                        tag.put("id", new StringTag("Sign"));
                        tag.put("x", new IntTag(position.x()));
                        tag.put("y", new IntTag(position.y()));
                        tag.put("z", new IntTag(position.z()));
                        int i = 0;
                        while (true) {
                            if (i >= lines.length) {
                                wrapper.write(Type.NBT, tag);
                                return;
                            }
                            tag.put("Text" + (i + 1), new StringTag(lines[i].toString()));
                            ++i;
                        }
                    }
                });
            }
        });
        this.registerClientbound(ClientboundPackets1_9.CHUNK_DATA, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.handler(new PacketHandler(){

                    /*
                     * Unable to fully structure code
                     */
                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        clientWorld = wrapper.user().get(ClientWorld.class);
                        oldType = new Chunk1_9_1_2Type(clientWorld);
                        newType = new Chunk1_9_3_4Type(clientWorld);
                        chunk = wrapper.read(oldType);
                        wrapper.write(newType, chunk);
                        tags = chunk.getBlockEntities();
                        i = 0;
                        block0: while (true) {
                            if (i >= chunk.getSections().length) return;
                            section = chunk.getSections()[i];
                            if (section == null) ** GOTO lbl-1000
                            y = 0;
                            while (true) {
                                if (y < 16) {
                                } else lbl-1000:
                                // 2 sources

                                {
                                    ++i;
                                    continue block0;
                                }
                                for (z = 0; z < 16; ++z) {
                                    for (x = 0; x < 16; ++x) {
                                        block = section.getBlockWithoutData(x, y, z);
                                        if (!FakeTileEntity.hasBlock(block)) continue;
                                        tags.add(FakeTileEntity.getFromBlock(x + (chunk.getX() << 4), y + (i << 4), z + (chunk.getZ() << 4), block));
                                    }
                                }
                                ++y;
                            }
                            break;
                        }
                    }
                });
            }
        });
        this.registerClientbound(ClientboundPackets1_9.JOIN_GAME, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.map(Type.INT);
                this.map(Type.UNSIGNED_BYTE);
                this.map(Type.INT);
                this.handler(new PacketHandler(){

                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                        int dimensionId = wrapper.get(Type.INT, 1);
                        clientWorld.setEnvironment(dimensionId);
                    }
                });
            }
        });
        this.registerClientbound(ClientboundPackets1_9.RESPAWN, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.map(Type.INT);
                this.handler(new PacketHandler(){

                    @Override
                    public void handle(PacketWrapper wrapper) throws Exception {
                        ClientWorld clientWorld = wrapper.user().get(ClientWorld.class);
                        int dimensionId = wrapper.get(Type.INT, 0);
                        clientWorld.setEnvironment(dimensionId);
                    }
                });
            }
        });
        this.registerClientbound(ClientboundPackets1_9.SOUND, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.map(Type.VAR_INT);
                this.map(Type.VAR_INT);
                this.map(Type.INT);
                this.map(Type.INT);
                this.map(Type.INT);
                this.map(Type.FLOAT);
                this.map(ADJUST_PITCH);
            }
        });
    }

    @Override
    public void init(UserConnection user) {
        if (user.has(ClientWorld.class)) return;
        user.put(new ClientWorld(user));
    }
}

