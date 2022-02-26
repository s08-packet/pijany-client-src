/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package com.viaversion.viaversion.api.type.types.minecraft;

import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.types.minecraft.BaseItemType;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import io.netty.buffer.ByteBuf;

public class ItemType
extends BaseItemType {
    public ItemType() {
        super("Item");
    }

    @Override
    public Item read(ByteBuf buffer) throws Exception {
        short id = buffer.readShort();
        if (id < 0) {
            return null;
        }
        DataItem item = new DataItem();
        item.setIdentifier(id);
        item.setAmount(buffer.readByte());
        item.setData(buffer.readShort());
        item.setTag((CompoundTag)NBT.read(buffer));
        return item;
    }

    @Override
    public void write(ByteBuf buffer, Item object) throws Exception {
        if (object == null) {
            buffer.writeShort(-1);
            return;
        }
        buffer.writeShort(object.identifier());
        buffer.writeByte(object.amount());
        buffer.writeShort((int)object.data());
        NBT.write(buffer, object.tag());
    }
}

