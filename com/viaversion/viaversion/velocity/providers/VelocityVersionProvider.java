/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.velocitypowered.api.network.ProtocolVersion
 *  com.velocitypowered.api.proxy.ServerConnection
 *  io.netty.channel.ChannelHandler
 */
package com.viaversion.viaversion.velocity.providers;

import com.velocitypowered.api.proxy.ServerConnection;
import com.viaversion.viaversion.VelocityPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocols.base.BaseVersionProvider;
import com.viaversion.viaversion.velocity.platform.VelocityViaInjector;
import com.viaversion.viaversion.velocity.service.ProtocolDetectorService;
import io.netty.channel.ChannelHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.IntStream;

public class VelocityVersionProvider
extends BaseVersionProvider {
    private static Method getAssociation;

    @Override
    public int getClosestServerProtocol(UserConnection user) throws Exception {
        int n;
        if (user.isClientSide()) {
            n = this.getBackProtocol(user);
            return n;
        }
        n = this.getFrontProtocol(user);
        return n;
    }

    private int getBackProtocol(UserConnection user) throws Exception {
        ChannelHandler mcHandler = user.getChannel().pipeline().get("handler");
        return ProtocolDetectorService.getProtocolId(((ServerConnection)getAssociation.invoke(mcHandler, new Object[0])).getServerInfo().getName());
    }

    private int getFrontProtocol(UserConnection user) throws Exception {
        int[] compatibleProtocols;
        int playerVersion = user.getProtocolInfo().getProtocolVersion();
        IntStream versions = com.velocitypowered.api.network.ProtocolVersion.SUPPORTED_VERSIONS.stream().mapToInt(com.velocitypowered.api.network.ProtocolVersion::getProtocol);
        if (VelocityViaInjector.getPlayerInfoForwardingMode != null && ((Enum)VelocityViaInjector.getPlayerInfoForwardingMode.invoke(VelocityPlugin.PROXY.getConfiguration(), new Object[0])).name().equals("MODERN")) {
            versions = versions.filter(ver -> {
                if (ver < ProtocolVersion.v1_13.getVersion()) return false;
                return true;
            });
        }
        if (Arrays.binarySearch(compatibleProtocols = versions.toArray(), playerVersion) >= 0) {
            return playerVersion;
        }
        if (playerVersion < compatibleProtocols[0]) {
            return compatibleProtocols[0];
        }
        int i = compatibleProtocols.length - 1;
        while (true) {
            if (i < 0) {
                Via.getPlatform().getLogger().severe("Panic, no protocol id found for " + playerVersion);
                return playerVersion;
            }
            int protocol = compatibleProtocols[i];
            if (playerVersion > protocol && ProtocolVersion.isRegistered(protocol)) {
                return protocol;
            }
            --i;
        }
    }

    static {
        try {
            getAssociation = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection").getMethod("getAssociation", new Class[0]);
            return;
        }
        catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}

