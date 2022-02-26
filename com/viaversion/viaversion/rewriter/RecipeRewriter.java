/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class RecipeRewriter {
    protected final Protocol protocol;
    protected final Map<String, RecipeConsumer> recipeHandlers = new HashMap<String, RecipeConsumer>();

    protected RecipeRewriter(Protocol protocol) {
        this.protocol = protocol;
    }

    public void handle(PacketWrapper wrapper, String type) throws Exception {
        RecipeConsumer handler = this.recipeHandlers.get(type);
        if (handler == null) return;
        handler.accept(wrapper);
    }

    public void registerDefaultHandler(ClientboundPacketType packetType) {
        this.protocol.registerClientbound(packetType, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.handler(wrapper -> {
                    int size = wrapper.passthrough(Type.VAR_INT);
                    int i = 0;
                    while (i < size) {
                        String type = wrapper.passthrough(Type.STRING).replace("minecraft:", "");
                        String id = wrapper.passthrough(Type.STRING);
                        RecipeRewriter.this.handle(wrapper, type);
                        ++i;
                    }
                });
            }
        });
    }

    protected void rewrite(@Nullable Item item) {
        if (this.protocol.getItemRewriter() == null) return;
        this.protocol.getItemRewriter().handleItemToClient(item);
    }

    @FunctionalInterface
    public static interface RecipeConsumer {
        public void accept(PacketWrapper var1) throws Exception;
    }
}

