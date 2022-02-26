/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.Logger
 */
package net.minecraft.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.apache.logging.log4j.Logger;

public class Util {
    public static EnumOS getOSType() {
        EnumOS enumOS;
        String s = System.getProperty("os.name").toLowerCase();
        if (s.contains("win")) {
            enumOS = EnumOS.WINDOWS;
            return enumOS;
        }
        if (s.contains("mac")) {
            enumOS = EnumOS.OSX;
            return enumOS;
        }
        if (s.contains("solaris")) {
            enumOS = EnumOS.SOLARIS;
            return enumOS;
        }
        if (s.contains("sunos")) {
            enumOS = EnumOS.SOLARIS;
            return enumOS;
        }
        if (s.contains("linux")) {
            enumOS = EnumOS.LINUX;
            return enumOS;
        }
        if (s.contains("unix")) {
            enumOS = EnumOS.LINUX;
            return enumOS;
        }
        enumOS = EnumOS.UNKNOWN;
        return enumOS;
    }

    public static <V> V func_181617_a(FutureTask<V> p_181617_0_, Logger p_181617_1_) {
        try {
            p_181617_0_.run();
            return p_181617_0_.get();
        }
        catch (ExecutionException executionexception) {
            p_181617_1_.fatal("Error executing task", (Throwable)executionexception);
            return null;
        }
        catch (InterruptedException interruptedexception) {
            p_181617_1_.fatal("Error executing task", (Throwable)interruptedexception);
        }
        return null;
    }

    public static enum EnumOS {
        LINUX,
        SOLARIS,
        WINDOWS,
        OSX,
        UNKNOWN;

    }
}

