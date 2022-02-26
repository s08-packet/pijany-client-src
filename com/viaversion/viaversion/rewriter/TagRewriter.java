/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.rewriter;

import com.viaversion.viaversion.api.data.MappingData;
import com.viaversion.viaversion.api.minecraft.RegistryType;
import com.viaversion.viaversion.api.minecraft.TagData;
import com.viaversion.viaversion.api.minecraft.entities.EntityType;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandler;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrayList;
import com.viaversion.viaversion.rewriter.IdRewriteFunction;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TagRewriter {
    private static final int[] EMPTY_ARRAY = new int[0];
    private final Protocol protocol;
    private final Map<RegistryType, List<TagData>> newTags = new EnumMap<RegistryType, List<TagData>>(RegistryType.class);

    public TagRewriter(Protocol protocol) {
        this.protocol = protocol;
    }

    public void loadFromMappingData() {
        RegistryType[] registryTypeArray = RegistryType.getValues();
        int n = registryTypeArray.length;
        int n2 = 0;
        while (n2 < n) {
            RegistryType type = registryTypeArray[n2];
            List<TagData> tags = this.protocol.getMappingData().getTags(type);
            if (tags != null) {
                this.getOrComputeNewTags(type).addAll(tags);
            }
            ++n2;
        }
    }

    public void addEmptyTag(RegistryType tagType, String tagId) {
        this.getOrComputeNewTags(tagType).add(new TagData(tagId, EMPTY_ARRAY));
    }

    public void addEmptyTags(RegistryType tagType, String ... tagIds) {
        List<TagData> tagList = this.getOrComputeNewTags(tagType);
        String[] stringArray = tagIds;
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String id = stringArray[n2];
            tagList.add(new TagData(id, EMPTY_ARRAY));
            ++n2;
        }
    }

    public void addEntityTag(String tagId, EntityType ... entities) {
        int[] ids = new int[entities.length];
        int i = 0;
        while (true) {
            if (i >= entities.length) {
                this.addTagRaw(RegistryType.ENTITY, tagId, ids);
                return;
            }
            ids[i] = entities[i].getId();
            ++i;
        }
    }

    public void addTag(RegistryType tagType, String tagId, int ... unmappedIds) {
        List<TagData> newTags = this.getOrComputeNewTags(tagType);
        IdRewriteFunction rewriteFunction = this.getRewriter(tagType);
        int i = 0;
        while (true) {
            if (i >= unmappedIds.length) {
                newTags.add(new TagData(tagId, unmappedIds));
                return;
            }
            int oldId = unmappedIds[i];
            unmappedIds[i] = rewriteFunction.rewrite(oldId);
            ++i;
        }
    }

    public void addTagRaw(RegistryType tagType, String tagId, int ... ids) {
        this.getOrComputeNewTags(tagType).add(new TagData(tagId, ids));
    }

    public void register(ClientboundPacketType packetType, final @Nullable RegistryType readUntilType) {
        this.protocol.registerClientbound(packetType, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.handler(TagRewriter.this.getHandler(readUntilType));
            }
        });
    }

    public void registerGeneric(ClientboundPacketType packetType) {
        this.protocol.registerClientbound(packetType, new PacketRemapper(){

            @Override
            public void registerMap() {
                this.handler(TagRewriter.this.getGenericHandler());
            }
        });
    }

    public PacketHandler getHandler(@Nullable RegistryType readUntilType) {
        return wrapper -> {
            RegistryType[] registryTypeArray = RegistryType.getValues();
            int n = registryTypeArray.length;
            int n2 = 0;
            while (n2 < n) {
                RegistryType type = registryTypeArray[n2];
                this.handle(wrapper, this.getRewriter(type), this.getNewTags(type));
                if (type == readUntilType) {
                    return;
                }
                ++n2;
            }
        };
    }

    public PacketHandler getGenericHandler() {
        return wrapper -> {
            int length = wrapper.passthrough(Type.VAR_INT);
            int i = 0;
            while (i < length) {
                RegistryType type;
                String registryKey = wrapper.passthrough(Type.STRING);
                if (registryKey.startsWith("minecraft:")) {
                    registryKey = registryKey.substring(10);
                }
                if ((type = RegistryType.getByKey(registryKey)) != null) {
                    this.handle(wrapper, this.getRewriter(type), this.getNewTags(type));
                } else {
                    this.handle(wrapper, null, null);
                }
                ++i;
            }
        };
    }

    public void handle(PacketWrapper wrapper, @Nullable IdRewriteFunction rewriteFunction, @Nullable List<TagData> newTags) throws Exception {
        int tagsSize = wrapper.read(Type.VAR_INT);
        wrapper.write(Type.VAR_INT, newTags != null ? tagsSize + newTags.size() : tagsSize);
        for (int i = 0; i < tagsSize; ++i) {
            wrapper.passthrough(Type.STRING);
            int[] ids = wrapper.read(Type.VAR_INT_ARRAY_PRIMITIVE);
            if (rewriteFunction != null) {
                IntArrayList idList = new IntArrayList(ids.length);
                for (int id : ids) {
                    int mappedId = rewriteFunction.rewrite(id);
                    if (mappedId == -1) continue;
                    idList.add(mappedId);
                }
                wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, idList.toArray(EMPTY_ARRAY));
                continue;
            }
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, ids);
        }
        if (newTags == null) return;
        Iterator<TagData> iterator = newTags.iterator();
        while (iterator.hasNext()) {
            TagData tag = iterator.next();
            wrapper.write(Type.STRING, tag.identifier());
            wrapper.write(Type.VAR_INT_ARRAY_PRIMITIVE, tag.entries());
        }
    }

    public @Nullable List<TagData> getNewTags(RegistryType tagType) {
        return this.newTags.get((Object)tagType);
    }

    public List<TagData> getOrComputeNewTags(RegistryType tagType) {
        return this.newTags.computeIfAbsent(tagType, type -> new ArrayList());
    }

    public @Nullable IdRewriteFunction getRewriter(RegistryType tagType) {
        MappingData mappingData = this.protocol.getMappingData();
        switch (tagType) {
            case BLOCK: {
                if (mappingData == null) return null;
                if (mappingData.getBlockMappings() == null) return null;
                IdRewriteFunction idRewriteFunction = mappingData::getNewBlockId;
                return idRewriteFunction;
            }
            case ITEM: {
                if (mappingData == null) return null;
                if (mappingData.getItemMappings() == null) return null;
                IdRewriteFunction idRewriteFunction = mappingData::getNewItemId;
                return idRewriteFunction;
            }
            case ENTITY: {
                if (this.protocol.getEntityRewriter() == null) return null;
                IdRewriteFunction idRewriteFunction = id -> this.protocol.getEntityRewriter().newEntityId(id);
                return idRewriteFunction;
            }
        }
        return null;
    }
}

