/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types;

import com.viaversion.viaversion.api.minecraft.item.DataItem;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import io.netty.buffer.ByteBuf;

public class ItemType
extends Type<Item> {
    private final boolean compressed;

    public ItemType(boolean compressed) {
        super(Item.class);
        this.compressed = compressed;
    }

    @Override
    public Item read(ByteBuf buffer) throws Exception {
        int readerIndex = buffer.readerIndex();
        short id = buffer.readShort();
        if (id < 0) {
            return null;
        }
        DataItem item = new DataItem();
        item.setIdentifier(id);
        item.setAmount(buffer.readByte());
        item.setData(buffer.readShort());
        item.setTag((CompoundTag)(this.compressed ? Types1_7_6_10.COMPRESSED_NBT : Types1_7_6_10.NBT).read(buffer));
        return item;
    }

    @Override
    public void write(ByteBuf buffer, Item item) throws Exception {
        if (item == null) {
            buffer.writeShort(-1);
            return;
        }
        buffer.writeShort(item.identifier());
        buffer.writeByte(item.amount());
        buffer.writeShort((int)item.data());
        (this.compressed ? Types1_7_6_10.COMPRESSED_NBT : Types1_7_6_10.NBT).write(buffer, item.tag());
    }
}

