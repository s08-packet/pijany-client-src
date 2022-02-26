/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelInitializer
 *  org.bukkit.Bukkit
 *  org.bukkit.plugin.PluginDescriptionFile
 */
package com.viaversion.viaversion.bukkit.platform;

import com.viaversion.viaversion.bukkit.handlers.BukkitChannelInitializer;
import com.viaversion.viaversion.bukkit.platform.PaperViaInjector;
import com.viaversion.viaversion.bukkit.util.NMSUtil;
import com.viaversion.viaversion.platform.LegacyViaInjector;
import com.viaversion.viaversion.platform.WrappedChannelInitializer;
import com.viaversion.viaversion.util.ReflectionUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BukkitViaInjector
extends LegacyViaInjector {
    private boolean protocolLib;

    @Override
    public void inject() throws ReflectiveOperationException {
        if (PaperViaInjector.PAPER_INJECTION_METHOD) {
            PaperViaInjector.setPaperChannelInitializeListener();
            return;
        }
        super.inject();
    }

    @Override
    public void uninject() throws ReflectiveOperationException {
        if (PaperViaInjector.PAPER_INJECTION_METHOD) {
            PaperViaInjector.removePaperChannelInitializeListener();
            return;
        }
        super.uninject();
    }

    @Override
    public int getServerProtocolVersion() throws ReflectiveOperationException {
        if (PaperViaInjector.PAPER_PROTOCOL_METHOD) {
            return Bukkit.getUnsafe().getProtocolVersion();
        }
        Class<?> serverClazz = NMSUtil.nms("MinecraftServer", "net.minecraft.server.MinecraftServer");
        Object server = ReflectionUtil.invokeStatic(serverClazz, "getServer");
        Class<?> pingClazz = NMSUtil.nms("ServerPing", "net.minecraft.network.protocol.status.ServerPing");
        Object ping = null;
        for (Field field : serverClazz.getDeclaredFields()) {
            if (field.getType() != pingClazz) continue;
            field.setAccessible(true);
            ping = field.get(server);
            break;
        }
        Class<?> serverDataClass = NMSUtil.nms("ServerPing$ServerData", "net.minecraft.network.protocol.status.ServerPing$ServerData");
        Object serverData = null;
        for (Field field : pingClazz.getDeclaredFields()) {
            if (field.getType() != serverDataClass) continue;
            field.setAccessible(true);
            serverData = field.get(ping);
            break;
        }
        Field[] fieldArray = serverDataClass.getDeclaredFields();
        int n = fieldArray.length;
        int n2 = 0;
        while (n2 < n) {
            Field field;
            field = fieldArray[n2];
            if (field.getType() == Integer.TYPE) {
                field.setAccessible(true);
                int protocolVersion = (Integer)field.get(serverData);
                if (protocolVersion != -1) {
                    return protocolVersion;
                }
            }
            ++n2;
        }
        throw new RuntimeException("Failed to get server");
    }

    @Override
    public String getDecoderName() {
        if (!this.protocolLib) return "decoder";
        return "protocol_lib_decoder";
    }

    @Override
    protected @Nullable Object getServerConnection() throws ReflectiveOperationException {
        Class<?> serverClass = NMSUtil.nms("MinecraftServer", "net.minecraft.server.MinecraftServer");
        Class<?> connectionClass = NMSUtil.nms("ServerConnection", "net.minecraft.server.network.ServerConnection");
        Object server = ReflectionUtil.invokeStatic(serverClass, "getServer");
        Method[] methodArray = serverClass.getDeclaredMethods();
        int n = methodArray.length;
        int n2 = 0;
        while (n2 < n) {
            Object connection;
            Method method = methodArray[n2];
            if (method.getReturnType() == connectionClass && method.getParameterTypes().length == 0 && (connection = method.invoke(server, new Object[0])) != null) {
                return connection;
            }
            ++n2;
        }
        return null;
    }

    @Override
    protected WrappedChannelInitializer createChannelInitializer(ChannelInitializer<Channel> oldInitializer) {
        return new BukkitChannelInitializer(oldInitializer);
    }

    @Override
    protected void blame(ChannelHandler bootstrapAcceptor) throws ReflectiveOperationException {
        ClassLoader classLoader = bootstrapAcceptor.getClass().getClassLoader();
        if (!classLoader.getClass().getName().equals("org.bukkit.plugin.java.PluginClassLoader")) throw new RuntimeException("Unable to find core component 'childHandler', please check your plugins. issue: " + bootstrapAcceptor.getClass().getName());
        PluginDescriptionFile description = ReflectionUtil.get(classLoader, "description", PluginDescriptionFile.class);
        throw new RuntimeException("Unable to inject, due to " + bootstrapAcceptor.getClass().getName() + ", try without the plugin " + description.getName() + "?");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean isBinded() {
        if (PaperViaInjector.PAPER_INJECTION_METHOD) {
            return true;
        }
        try {
            Object connection = this.getServerConnection();
            if (connection == null) {
                return false;
            }
            Field[] fieldArray = connection.getClass().getDeclaredFields();
            int n = fieldArray.length;
            int n2 = 0;
            while (n2 < n) {
                Field field = fieldArray[n2];
                if (List.class.isAssignableFrom(field.getType())) {
                    List value;
                    field.setAccessible(true);
                    List list = value = (List)field.get(connection);
                    synchronized (list) {
                        if (!value.isEmpty() && value.get(0) instanceof ChannelFuture) {
                            return true;
                        }
                    }
                }
                ++n2;
            }
            return false;
        }
        catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setProtocolLib(boolean protocolLib) {
        this.protocolLib = protocolLib;
    }
}

