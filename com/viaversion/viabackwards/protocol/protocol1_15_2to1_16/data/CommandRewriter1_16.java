/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viabackwards.protocol.protocol1_15_2to1_16.data;

import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.rewriter.CommandRewriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CommandRewriter1_16
extends CommandRewriter {
    public CommandRewriter1_16(Protocol protocol) {
        super(protocol);
    }

    @Override
    protected @Nullable String handleArgumentType(String argumentType) {
        if (!argumentType.equals("minecraft:uuid")) return super.handleArgumentType(argumentType);
        return "minecraft:game_profile";
    }
}

