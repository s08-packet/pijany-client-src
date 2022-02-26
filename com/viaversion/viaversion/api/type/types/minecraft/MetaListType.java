/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package com.viaversion.viaversion.api.type.types.minecraft;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.minecraft.MetaListTypeTemplate;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class MetaListType
extends MetaListTypeTemplate {
    private final Type<Metadata> type;

    public MetaListType(Type<Metadata> type) {
        Preconditions.checkNotNull(type);
        this.type = type;
    }

    @Override
    public List<Metadata> read(ByteBuf buffer) throws Exception {
        Metadata meta;
        ArrayList<Metadata> list = new ArrayList<Metadata>();
        do {
            if ((meta = (Metadata)this.type.read(buffer)) == null) continue;
            list.add(meta);
        } while (meta != null);
        return list;
    }

    @Override
    public void write(ByteBuf buffer, List<Metadata> object) throws Exception {
        Iterator<Metadata> iterator = object.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                this.type.write(buffer, null);
                return;
            }
            Metadata metadata = iterator.next();
            this.type.write(buffer, metadata);
        }
    }
}

