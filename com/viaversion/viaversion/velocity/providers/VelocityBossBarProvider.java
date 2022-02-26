/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.velocity.providers;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.BossBarProvider;
import com.viaversion.viaversion.velocity.storage.VelocityStorage;
import java.util.UUID;

public class VelocityBossBarProvider
extends BossBarProvider {
    @Override
    public void handleAdd(UserConnection user, UUID barUUID) {
        if (!user.has(VelocityStorage.class)) return;
        VelocityStorage storage = user.get(VelocityStorage.class);
        if (storage.getBossbar() == null) return;
        storage.getBossbar().add(barUUID);
    }

    @Override
    public void handleRemove(UserConnection user, UUID barUUID) {
        if (!user.has(VelocityStorage.class)) return;
        VelocityStorage storage = user.get(VelocityStorage.class);
        if (storage.getBossbar() == null) return;
        storage.getBossbar().remove(barUUID);
    }
}

