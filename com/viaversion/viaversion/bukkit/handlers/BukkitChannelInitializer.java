/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelInitializer
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  io.netty.handler.codec.MessageToByteEncoder
 */
package com.viaversion.viaversion.bukkit.handlers;

import com.viaversion.viaversion.bukkit.classgenerator.ClassGenerator;
import com.viaversion.viaversion.bukkit.platform.PaperViaInjector;
import com.viaversion.viaversion.classgenerator.generated.HandlerConstructor;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.platform.WrappedChannelInitializer;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import java.lang.reflect.Method;

public class BukkitChannelInitializer
extends ChannelInitializer<Channel>
implements WrappedChannelInitializer {
    private static final Method INIT_CHANNEL_METHOD;
    private final ChannelInitializer<Channel> original;

    public BukkitChannelInitializer(ChannelInitializer<Channel> oldInit) {
        this.original = oldInit;
    }

    @Deprecated
    public ChannelInitializer<Channel> getOriginal() {
        return this.original;
    }

    protected void initChannel(Channel channel) throws Exception {
        INIT_CHANNEL_METHOD.invoke(this.original, channel);
        BukkitChannelInitializer.afterChannelInitialize(channel);
    }

    public static void afterChannelInitialize(Channel channel) {
        UserConnectionImpl connection = new UserConnectionImpl(channel);
        new ProtocolPipelineImpl(connection);
        if (PaperViaInjector.PAPER_PACKET_LIMITER) {
            connection.setPacketLimiterEnabled(false);
        }
        HandlerConstructor constructor = ClassGenerator.getConstructor();
        MessageToByteEncoder encoder = constructor.newEncodeHandler(connection, (MessageToByteEncoder)channel.pipeline().get("encoder"));
        ByteToMessageDecoder decoder = constructor.newDecodeHandler(connection, (ByteToMessageDecoder)channel.pipeline().get("decoder"));
        channel.pipeline().replace("encoder", "encoder", (ChannelHandler)encoder);
        channel.pipeline().replace("decoder", "decoder", (ChannelHandler)decoder);
    }

    @Override
    public ChannelInitializer<Channel> original() {
        return this.original;
    }

    static {
        try {
            INIT_CHANNEL_METHOD = ChannelInitializer.class.getDeclaredMethod("initChannel", Channel.class);
            INIT_CHANNEL_METHOD.setAccessible(true);
            return;
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}

