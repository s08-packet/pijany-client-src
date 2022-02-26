/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.sound;

import com.viaversion.viaversion.libs.kyori.adventure.sound.Sound;
import com.viaversion.viaversion.libs.kyori.adventure.sound.SoundStop;
import com.viaversion.viaversion.libs.kyori.adventure.util.ShadyPines;
import com.viaversion.viaversion.libs.kyori.examination.ExaminableProperty;
import com.viaversion.viaversion.libs.kyori.examination.string.StringExaminer;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class SoundImpl
implements Sound {
    static final Sound.Emitter EMITTER_SELF = new Sound.Emitter(){

        public String toString() {
            return "SelfSoundEmitter";
        }
    };
    private final Sound.Source source;
    private final float volume;
    private final float pitch;
    private SoundStop stop;

    SoundImpl(@NotNull Sound.Source source, float volume, float pitch) {
        this.source = source;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    @NotNull
    public Sound.Source source() {
        return this.source;
    }

    @Override
    public float volume() {
        return this.volume;
    }

    @Override
    public float pitch() {
        return this.pitch;
    }

    @Override
    @NotNull
    public SoundStop asStop() {
        if (this.stop != null) return this.stop;
        this.stop = SoundStop.namedOnSource(this.name(), this.source());
        return this.stop;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SoundImpl)) {
            return false;
        }
        SoundImpl that = (SoundImpl)other;
        if (!this.name().equals(that.name())) return false;
        if (this.source != that.source) return false;
        if (!ShadyPines.equals(this.volume, that.volume)) return false;
        if (!ShadyPines.equals(this.pitch, that.pitch)) return false;
        return true;
    }

    public int hashCode() {
        int result = this.name().hashCode();
        result = 31 * result + this.source.hashCode();
        result = 31 * result + Float.hashCode(this.volume);
        return 31 * result + Float.hashCode(this.pitch);
    }

    @Override
    @NotNull
    public Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(ExaminableProperty.of("name", this.name()), ExaminableProperty.of("source", (Object)this.source), ExaminableProperty.of("volume", this.volume), ExaminableProperty.of("pitch", this.pitch));
    }

    public String toString() {
        return this.examine(StringExaminer.simpleEscaping());
    }
}

