/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.util;

import com.viaversion.viaversion.libs.kyori.adventure.util.HSVLike;
import com.viaversion.viaversion.libs.kyori.adventure.util.ShadyPines;
import com.viaversion.viaversion.libs.kyori.examination.string.StringExaminer;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

final class HSVLikeImpl
implements HSVLike {
    private final float h;
    private final float s;
    private final float v;

    HSVLikeImpl(float h, float s, float v) {
        this.h = h;
        this.s = s;
        this.v = v;
    }

    @Override
    public float h() {
        return this.h;
    }

    @Override
    public float s() {
        return this.s;
    }

    @Override
    public float v() {
        return this.v;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HSVLikeImpl)) {
            return false;
        }
        HSVLikeImpl that = (HSVLikeImpl)other;
        if (!ShadyPines.equals(that.h, this.h)) return false;
        if (!ShadyPines.equals(that.s, this.s)) return false;
        if (!ShadyPines.equals(that.v, this.v)) return false;
        return true;
    }

    public int hashCode() {
        return Objects.hash(Float.valueOf(this.h), Float.valueOf(this.s), Float.valueOf(this.v));
    }

    public String toString() {
        return this.examine(StringExaminer.simpleEscaping());
    }
}

