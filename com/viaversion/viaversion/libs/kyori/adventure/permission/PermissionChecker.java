/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.permission;

import com.viaversion.viaversion.libs.kyori.adventure.key.Key;
import com.viaversion.viaversion.libs.kyori.adventure.permission.PermissionCheckers;
import com.viaversion.viaversion.libs.kyori.adventure.pointer.Pointer;
import com.viaversion.viaversion.libs.kyori.adventure.util.TriState;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public interface PermissionChecker
extends Predicate<String> {
    public static final Pointer<PermissionChecker> POINTER = Pointer.pointer(PermissionChecker.class, Key.key("adventure", "permission"));

    @NotNull
    public static PermissionChecker always(TriState state) {
        if (state == TriState.TRUE) {
            return PermissionCheckers.TRUE;
        }
        if (state != TriState.FALSE) return PermissionCheckers.NOT_SET;
        return PermissionCheckers.FALSE;
    }

    @NotNull
    public TriState value(String var1);

    @Override
    default public boolean test(String permission) {
        if (this.value(permission) != TriState.TRUE) return false;
        return true;
    }
}

