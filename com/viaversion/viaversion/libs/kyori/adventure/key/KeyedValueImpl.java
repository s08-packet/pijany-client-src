/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.key;

import com.viaversion.viaversion.libs.kyori.adventure.key.Key;
import com.viaversion.viaversion.libs.kyori.adventure.key.KeyedValue;
import com.viaversion.viaversion.libs.kyori.examination.Examinable;
import com.viaversion.viaversion.libs.kyori.examination.ExaminableProperty;
import com.viaversion.viaversion.libs.kyori.examination.string.StringExaminer;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class KeyedValueImpl<T>
implements Examinable,
KeyedValue<T> {
    private final Key key;
    private final T value;

    KeyedValueImpl(Key key, T value) {
        this.key = key;
        this.value = value;
    }

    @Override
    @NotNull
    public Key key() {
        return this.key;
    }

    @Override
    @NotNull
    public T value() {
        return this.value;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) return false;
        if (this.getClass() != other.getClass()) {
            return false;
        }
        KeyedValueImpl that = (KeyedValueImpl)other;
        if (!this.key.equals(that.key)) return false;
        if (!this.value.equals(that.value)) return false;
        return true;
    }

    public int hashCode() {
        int result = this.key.hashCode();
        return 31 * result + this.value.hashCode();
    }

    @Override
    @NotNull
    public Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(ExaminableProperty.of("key", this.key), ExaminableProperty.of("value", this.value));
    }

    public String toString() {
        return this.examine(StringExaminer.simpleEscaping());
    }
}

