/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.title;

import com.viaversion.viaversion.libs.kyori.adventure.text.Component;
import com.viaversion.viaversion.libs.kyori.adventure.title.Title;
import com.viaversion.viaversion.libs.kyori.adventure.title.TitlePart;
import com.viaversion.viaversion.libs.kyori.examination.ExaminableProperty;
import com.viaversion.viaversion.libs.kyori.examination.string.StringExaminer;
import java.time.Duration;
import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

final class TitleImpl
implements Title {
    private final Component title;
    private final Component subtitle;
    @Nullable
    private final Title.Times times;

    TitleImpl(@NotNull Component title, @NotNull Component subtitle, @Nullable Title.Times times) {
        this.title = title;
        this.subtitle = subtitle;
        this.times = times;
    }

    @Override
    @NotNull
    public Component title() {
        return this.title;
    }

    @Override
    @NotNull
    public Component subtitle() {
        return this.subtitle;
    }

    @Override
    @Nullable
    public Title.Times times() {
        return this.times;
    }

    @Override
    public <T> @UnknownNullability T part(@NotNull TitlePart<T> part) {
        if (part == TitlePart.TITLE) {
            return (T)this.title;
        }
        if (part == TitlePart.SUBTITLE) {
            return (T)this.subtitle;
        }
        if (part != TitlePart.TIMES) throw new IllegalArgumentException("Don't know what " + part + " is.");
        return (T)this.times;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) return false;
        if (this.getClass() != other.getClass()) {
            return false;
        }
        TitleImpl that = (TitleImpl)other;
        if (!this.title.equals(that.title)) return false;
        if (!this.subtitle.equals(that.subtitle)) return false;
        if (!Objects.equals(this.times, that.times)) return false;
        return true;
    }

    public int hashCode() {
        int result = this.title.hashCode();
        result = 31 * result + this.subtitle.hashCode();
        return 31 * result + Objects.hashCode(this.times);
    }

    @Override
    @NotNull
    public Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(ExaminableProperty.of("title", this.title), ExaminableProperty.of("subtitle", this.subtitle), ExaminableProperty.of("times", this.times));
    }

    public String toString() {
        return this.examine(StringExaminer.simpleEscaping());
    }

    static class TimesImpl
    implements Title.Times {
        private final Duration fadeIn;
        private final Duration stay;
        private final Duration fadeOut;

        TimesImpl(Duration fadeIn, Duration stay, Duration fadeOut) {
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }

        @Override
        @NotNull
        public Duration fadeIn() {
            return this.fadeIn;
        }

        @Override
        @NotNull
        public Duration stay() {
            return this.stay;
        }

        @Override
        @NotNull
        public Duration fadeOut() {
            return this.fadeOut;
        }

        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (other == null) return false;
            if (this.getClass() != other.getClass()) {
                return false;
            }
            TimesImpl that = (TimesImpl)other;
            if (!this.fadeIn.equals(that.fadeIn)) return false;
            if (!this.stay.equals(that.stay)) return false;
            if (!this.fadeOut.equals(that.fadeOut)) return false;
            return true;
        }

        public int hashCode() {
            int result = this.fadeIn.hashCode();
            result = 31 * result + this.stay.hashCode();
            return 31 * result + this.fadeOut.hashCode();
        }

        @Override
        @NotNull
        public Stream<? extends ExaminableProperty> examinableProperties() {
            return Stream.of(ExaminableProperty.of("fadeIn", this.fadeIn), ExaminableProperty.of("stay", this.stay), ExaminableProperty.of("fadeOut", this.fadeOut));
        }

        public String toString() {
            return this.examine(StringExaminer.simpleEscaping());
        }
    }
}

