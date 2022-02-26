/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.sound;

import com.viaversion.viaversion.libs.kyori.adventure.key.Key;
import com.viaversion.viaversion.libs.kyori.adventure.sound.Sound;
import com.viaversion.viaversion.libs.kyori.adventure.sound.SoundStop;
import com.viaversion.viaversion.libs.kyori.examination.ExaminableProperty;
import com.viaversion.viaversion.libs.kyori.examination.string.StringExaminer;
import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class SoundStopImpl
implements SoundStop {
    static final SoundStop ALL = new SoundStopImpl(null){

        @Override
        @Nullable
        public Key sound() {
            return null;
        }
    };
    private final @Nullable Sound.Source source;

    SoundStopImpl(@Nullable Sound.Source source) {
        this.source = source;
    }

    @Override
    public @Nullable Sound.Source source() {
        return this.source;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SoundStopImpl)) {
            return false;
        }
        SoundStopImpl that = (SoundStopImpl)other;
        if (!Objects.equals(this.sound(), that.sound())) return false;
        if (!Objects.equals((Object)this.source, (Object)that.source)) return false;
        return true;
    }

    public int hashCode() {
        int result = Objects.hashCode(this.sound());
        return 31 * result + Objects.hashCode((Object)this.source);
    }

    @Override
    @NotNull
    public Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(ExaminableProperty.of("name", this.sound()), ExaminableProperty.of("source", (Object)this.source));
    }

    public String toString() {
        return this.examine(StringExaminer.simpleEscaping());
    }
}

