/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viabackwards.api.rewriters;

import com.viaversion.viabackwards.api.BackwardsProtocol;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.Tag;
import com.viaversion.viaversion.rewriter.ItemRewriter;
import java.util.Iterator;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class ItemRewriterBase<T extends BackwardsProtocol>
extends ItemRewriter<T> {
    protected final String nbtTagName;
    protected final boolean jsonNameFormat;

    protected ItemRewriterBase(T protocol, boolean jsonNameFormat) {
        super(protocol);
        this.jsonNameFormat = jsonNameFormat;
        this.nbtTagName = "VB|" + protocol.getClass().getSimpleName();
    }

    @Override
    public @Nullable Item handleItemToServer(@Nullable Item item) {
        if (item == null) {
            return null;
        }
        super.handleItemToServer(item);
        this.restoreDisplayTag(item);
        return item;
    }

    protected boolean hasBackupTag(CompoundTag displayTag, String tagName) {
        return displayTag.contains(this.nbtTagName + "|o" + tagName);
    }

    protected void saveStringTag(CompoundTag displayTag, StringTag original, String name) {
        String backupName = this.nbtTagName + "|o" + name;
        if (displayTag.contains(backupName)) return;
        displayTag.put(backupName, new StringTag(original.getValue()));
    }

    protected void saveListTag(CompoundTag displayTag, ListTag original, String name) {
        String backupName = this.nbtTagName + "|o" + name;
        if (displayTag.contains(backupName)) return;
        ListTag listTag = new ListTag();
        Iterator iterator = original.getValue().iterator();
        while (true) {
            if (!iterator.hasNext()) {
                displayTag.put(backupName, listTag);
                return;
            }
            Tag tag = (Tag)iterator.next();
            listTag.add(tag.clone());
        }
    }

    protected void restoreDisplayTag(Item item) {
        if (item.tag() == null) {
            return;
        }
        CompoundTag display = (CompoundTag)item.tag().get("display");
        if (display == null) return;
        if (display.remove(this.nbtTagName + "|customName") != null) {
            display.remove("Name");
        } else {
            this.restoreStringTag(display, "Name");
        }
        this.restoreListTag(display, "Lore");
    }

    protected void restoreStringTag(CompoundTag tag, String tagName) {
        StringTag original = (StringTag)tag.remove(this.nbtTagName + "|o" + tagName);
        if (original == null) return;
        tag.put(tagName, new StringTag(original.getValue()));
    }

    protected void restoreListTag(CompoundTag tag, String tagName) {
        ListTag original = (ListTag)tag.remove(this.nbtTagName + "|o" + tagName);
        if (original == null) return;
        tag.put(tagName, new ListTag((List<Tag>)original.getValue()));
    }

    public String getNbtTagName() {
        return this.nbtTagName;
    }
}

