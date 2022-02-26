/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelPipeline
 *  io.netty.handler.codec.ByteToMessageDecoder
 *  io.netty.handler.codec.MessageToByteEncoder
 *  io.netty.handler.codec.MessageToMessageDecoder
 */
package com.viaversion.viaversion.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PipelineUtil {
    private static Method DECODE_METHOD;
    private static Method ENCODE_METHOD;
    private static Method MTM_DECODE;

    public static List<Object> callDecode(ByteToMessageDecoder decoder, ChannelHandlerContext ctx, Object input) throws InvocationTargetException {
        ArrayList<Object> output = new ArrayList<Object>();
        try {
            DECODE_METHOD.invoke(decoder, ctx, input, output);
            return output;
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static void callEncode(MessageToByteEncoder encoder, ChannelHandlerContext ctx, Object msg, ByteBuf output) throws InvocationTargetException {
        try {
            ENCODE_METHOD.invoke(encoder, ctx, msg, output);
            return;
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static List<Object> callDecode(MessageToMessageDecoder decoder, ChannelHandlerContext ctx, Object msg) throws InvocationTargetException {
        ArrayList<Object> output = new ArrayList<Object>();
        try {
            MTM_DECODE.invoke(decoder, ctx, msg, output);
            return output;
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static boolean containsCause(Throwable t, Class<?> c) {
        while (t != null) {
            if (c.isAssignableFrom(t.getClass())) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    public static ChannelHandlerContext getContextBefore(String name, ChannelPipeline pipeline) {
        boolean mark = false;
        Iterator iterator = pipeline.names().iterator();
        while (iterator.hasNext()) {
            String s = (String)iterator.next();
            if (mark) {
                return pipeline.context(pipeline.get(s));
            }
            if (!s.equalsIgnoreCase(name)) continue;
            mark = true;
        }
        return null;
    }

    public static ChannelHandlerContext getPreviousContext(String name, ChannelPipeline pipeline) {
        String previous = null;
        Iterator iterator = pipeline.toMap().keySet().iterator();
        while (iterator.hasNext()) {
            String entry = (String)iterator.next();
            if (entry.equals(name)) {
                return pipeline.context(previous);
            }
            previous = entry;
        }
        return null;
    }

    static {
        try {
            DECODE_METHOD = ByteToMessageDecoder.class.getDeclaredMethod("decode", ChannelHandlerContext.class, ByteBuf.class, List.class);
            DECODE_METHOD.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            ENCODE_METHOD = MessageToByteEncoder.class.getDeclaredMethod("encode", ChannelHandlerContext.class, Object.class, ByteBuf.class);
            ENCODE_METHOD.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            MTM_DECODE = MessageToMessageDecoder.class.getDeclaredMethod("decode", ChannelHandlerContext.class, Object.class, List.class);
            MTM_DECODE.setAccessible(true);
            return;
        }
        catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}

