/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.text.flattener;

import com.viaversion.viaversion.libs.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface FlattenerListener {
    default public void pushStyle(@NotNull Style style) {
    }

    public void component(@NotNull String var1);

    default public void popStyle(@NotNull Style style) {
    }
}

