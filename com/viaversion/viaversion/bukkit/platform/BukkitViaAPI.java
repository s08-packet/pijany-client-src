/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 */
package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viaversion.ViaAPIBase;
import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.bukkit.util.ProtocolSupportUtil;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BukkitViaAPI
extends ViaAPIBase<Player> {
    private final ViaVersionPlugin plugin;

    public BukkitViaAPI(ViaVersionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getPlayerVersion(Player player) {
        return this.getPlayerVersion(player.getUniqueId());
    }

    @Override
    public int getPlayerVersion(UUID uuid) {
        UserConnection connection = Via.getManager().getConnectionManager().getConnectedClient(uuid);
        if (connection != null) return connection.getProtocolInfo().getProtocolVersion();
        Player player = Bukkit.getPlayer((UUID)uuid);
        if (player == null) return -1;
        if (!this.isProtocolSupport()) return -1;
        return ProtocolSupportUtil.getProtocolVersion(player);
    }

    @Override
    public void sendRawPacket(Player player, ByteBuf packet) throws IllegalArgumentException {
        this.sendRawPacket(player.getUniqueId(), packet);
    }

    public boolean isCompatSpigotBuild() {
        return this.plugin.isCompatSpigotBuild();
    }

    public boolean isProtocolSupport() {
        return this.plugin.isProtocolSupport();
    }
}

