/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.nbt;

import com.viaversion.viaversion.libs.kyori.adventure.nbt.BinaryTag;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.BinaryTagType;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.BinaryTagTypes;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.ByteArrayBinaryTag;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.CompoundBinaryTag;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.IntArrayBinaryTag;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.ListBinaryTagImpl;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.ListTagBuilder;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.ListTagSetter;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.LongArrayBinaryTag;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.NumberBinaryTag;
import com.viaversion.viaversion.libs.kyori.adventure.nbt.StringBinaryTag;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public interface ListBinaryTag
extends ListTagSetter<ListBinaryTag, BinaryTag>,
BinaryTag,
Iterable<BinaryTag> {
    @NotNull
    public static ListBinaryTag empty() {
        return ListBinaryTagImpl.EMPTY;
    }

    @NotNull
    public static ListBinaryTag from(@NotNull Iterable<? extends BinaryTag> tags) {
        return ((Builder)ListBinaryTag.builder().add(tags)).build();
    }

    @NotNull
    public static Builder<BinaryTag> builder() {
        return new ListTagBuilder<BinaryTag>();
    }

    @NotNull
    public static <T extends BinaryTag> Builder<T> builder(@NotNull BinaryTagType<T> type) {
        if (type != BinaryTagTypes.END) return new ListTagBuilder(type);
        throw new IllegalArgumentException("Cannot create a list of " + BinaryTagTypes.END);
    }

    @NotNull
    public static ListBinaryTag of(@NotNull BinaryTagType<? extends BinaryTag> type, @NotNull List<BinaryTag> tags) {
        if (tags.isEmpty()) {
            return ListBinaryTag.empty();
        }
        if (type != BinaryTagTypes.END) return new ListBinaryTagImpl(type, tags);
        throw new IllegalArgumentException("Cannot create a list of " + BinaryTagTypes.END);
    }

    @NotNull
    default public BinaryTagType<ListBinaryTag> type() {
        return BinaryTagTypes.LIST;
    }

    @Deprecated
    @NotNull
    default public BinaryTagType<? extends BinaryTag> listType() {
        return this.elementType();
    }

    @NotNull
    public BinaryTagType<? extends BinaryTag> elementType();

    public int size();

    @NotNull
    public BinaryTag get(@Range(from=0L, to=0x7FFFFFFFL) int var1);

    @NotNull
    public ListBinaryTag set(int var1, @NotNull BinaryTag var2, @Nullable Consumer<? super BinaryTag> var3);

    @NotNull
    public ListBinaryTag remove(int var1, @Nullable Consumer<? super BinaryTag> var2);

    default public byte getByte(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        return this.getByte(index, (byte)0);
    }

    default public byte getByte(@Range(from=0L, to=0x7FFFFFFFL) int index, byte defaultValue) {
        BinaryTag tag = this.get(index);
        if (!tag.type().numeric()) return defaultValue;
        return ((NumberBinaryTag)tag).byteValue();
    }

    default public short getShort(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        return this.getShort(index, (short)0);
    }

    default public short getShort(@Range(from=0L, to=0x7FFFFFFFL) int index, short defaultValue) {
        BinaryTag tag = this.get(index);
        if (!tag.type().numeric()) return defaultValue;
        return ((NumberBinaryTag)tag).shortValue();
    }

    default public int getInt(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        return this.getInt(index, 0);
    }

    default public int getInt(@Range(from=0L, to=0x7FFFFFFFL) int index, int defaultValue) {
        BinaryTag tag = this.get(index);
        if (!tag.type().numeric()) return defaultValue;
        return ((NumberBinaryTag)tag).intValue();
    }

    default public long getLong(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        return this.getLong(index, 0L);
    }

    default public long getLong(@Range(from=0L, to=0x7FFFFFFFL) int index, long defaultValue) {
        BinaryTag tag = this.get(index);
        if (!tag.type().numeric()) return defaultValue;
        return ((NumberBinaryTag)tag).longValue();
    }

    default public float getFloat(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        return this.getFloat(index, 0.0f);
    }

    default public float getFloat(@Range(from=0L, to=0x7FFFFFFFL) int index, float defaultValue) {
        BinaryTag tag = this.get(index);
        if (!tag.type().numeric()) return defaultValue;
        return ((NumberBinaryTag)tag).floatValue();
    }

    default public double getDouble(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        return this.getDouble(index, 0.0);
    }

    default public double getDouble(@Range(from=0L, to=0x7FFFFFFFL) int index, double defaultValue) {
        BinaryTag tag = this.get(index);
        if (!tag.type().numeric()) return defaultValue;
        return ((NumberBinaryTag)tag).doubleValue();
    }

    default public byte @NotNull [] getByteArray(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        BinaryTag tag = this.get(index);
        if (tag.type() != BinaryTagTypes.BYTE_ARRAY) return new byte[0];
        return ((ByteArrayBinaryTag)tag).value();
    }

    default public byte @NotNull [] getByteArray(@Range(from=0L, to=0x7FFFFFFFL) int index, byte @NotNull [] defaultValue) {
        BinaryTag tag = this.get(index);
        if (tag.type() != BinaryTagTypes.BYTE_ARRAY) return defaultValue;
        return ((ByteArrayBinaryTag)tag).value();
    }

    @NotNull
    default public String getString(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        return this.getString(index, "");
    }

    @NotNull
    default public String getString(@Range(from=0L, to=0x7FFFFFFFL) int index, @NotNull String defaultValue) {
        BinaryTag tag = this.get(index);
        if (tag.type() != BinaryTagTypes.STRING) return defaultValue;
        return ((StringBinaryTag)tag).value();
    }

    @NotNull
    default public ListBinaryTag getList(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        return this.getList(index, null, ListBinaryTag.empty());
    }

    @NotNull
    default public ListBinaryTag getList(@Range(from=0L, to=0x7FFFFFFFL) int index, @Nullable BinaryTagType<?> elementType) {
        return this.getList(index, elementType, ListBinaryTag.empty());
    }

    @NotNull
    default public ListBinaryTag getList(@Range(from=0L, to=0x7FFFFFFFL) int index, @NotNull ListBinaryTag defaultValue) {
        return this.getList(index, null, defaultValue);
    }

    @NotNull
    default public ListBinaryTag getList(@Range(from=0L, to=0x7FFFFFFFL) int index, @Nullable BinaryTagType<?> elementType, @NotNull ListBinaryTag defaultValue) {
        BinaryTag tag = this.get(index);
        if (tag.type() != BinaryTagTypes.LIST) return defaultValue;
        ListBinaryTag list = (ListBinaryTag)tag;
        if (elementType == null) return list;
        if (list.elementType() != elementType) return defaultValue;
        return list;
    }

    @NotNull
    default public CompoundBinaryTag getCompound(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        return this.getCompound(index, CompoundBinaryTag.empty());
    }

    @NotNull
    default public CompoundBinaryTag getCompound(@Range(from=0L, to=0x7FFFFFFFL) int index, @NotNull CompoundBinaryTag defaultValue) {
        BinaryTag tag = this.get(index);
        if (tag.type() != BinaryTagTypes.COMPOUND) return defaultValue;
        return (CompoundBinaryTag)tag;
    }

    default public int @NotNull [] getIntArray(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        BinaryTag tag = this.get(index);
        if (tag.type() != BinaryTagTypes.INT_ARRAY) return new int[0];
        return ((IntArrayBinaryTag)tag).value();
    }

    default public int @NotNull [] getIntArray(@Range(from=0L, to=0x7FFFFFFFL) int index, int @NotNull [] defaultValue) {
        BinaryTag tag = this.get(index);
        if (tag.type() != BinaryTagTypes.INT_ARRAY) return defaultValue;
        return ((IntArrayBinaryTag)tag).value();
    }

    default public long @NotNull [] getLongArray(@Range(from=0L, to=0x7FFFFFFFL) int index) {
        BinaryTag tag = this.get(index);
        if (tag.type() != BinaryTagTypes.LONG_ARRAY) return new long[0];
        return ((LongArrayBinaryTag)tag).value();
    }

    default public long @NotNull [] getLongArray(@Range(from=0L, to=0x7FFFFFFFL) int index, long @NotNull [] defaultValue) {
        BinaryTag tag = this.get(index);
        if (tag.type() != BinaryTagTypes.LONG_ARRAY) return defaultValue;
        return ((LongArrayBinaryTag)tag).value();
    }

    @NotNull
    public Stream<BinaryTag> stream();

    public static interface Builder<T extends BinaryTag>
    extends ListTagSetter<Builder<T>, T> {
        @NotNull
        public ListBinaryTag build();
    }
}

