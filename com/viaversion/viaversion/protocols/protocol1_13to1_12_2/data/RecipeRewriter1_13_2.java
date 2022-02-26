/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.protocols.protocol1_13to1_12_2.data;

import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.rewriter.RecipeRewriter;

public class RecipeRewriter1_13_2
extends RecipeRewriter {
    public RecipeRewriter1_13_2(Protocol protocol) {
        super(protocol);
        this.recipeHandlers.put("crafting_shapeless", this::handleCraftingShapeless);
        this.recipeHandlers.put("crafting_shaped", this::handleCraftingShaped);
        this.recipeHandlers.put("smelting", this::handleSmelting);
    }

    public void handleSmelting(PacketWrapper wrapper) throws Exception {
        Item[] items;
        wrapper.passthrough(Type.STRING);
        Item[] itemArray = items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT);
        int n = itemArray.length;
        int n2 = 0;
        while (true) {
            if (n2 >= n) {
                this.rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                wrapper.passthrough(Type.FLOAT);
                wrapper.passthrough(Type.VAR_INT);
                return;
            }
            Item item = itemArray[n2];
            this.rewrite(item);
            ++n2;
        }
    }

    public void handleCraftingShaped(PacketWrapper wrapper) throws Exception {
        int ingredientsNo = wrapper.passthrough(Type.VAR_INT) * wrapper.passthrough(Type.VAR_INT);
        wrapper.passthrough(Type.STRING);
        int j = 0;
        while (true) {
            Item[] items;
            if (j >= ingredientsNo) {
                this.rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                return;
            }
            for (Item item : items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT)) {
                this.rewrite(item);
            }
            ++j;
        }
    }

    public void handleCraftingShapeless(PacketWrapper wrapper) throws Exception {
        wrapper.passthrough(Type.STRING);
        int ingredientsNo = wrapper.passthrough(Type.VAR_INT);
        int j = 0;
        while (true) {
            Item[] items;
            if (j >= ingredientsNo) {
                this.rewrite(wrapper.passthrough(Type.FLAT_VAR_INT_ITEM));
                return;
            }
            for (Item item : items = wrapper.passthrough(Type.FLAT_VAR_INT_ITEM_ARRAY_VAR_INT)) {
                this.rewrite(item);
            }
            ++j;
        }
    }
}

