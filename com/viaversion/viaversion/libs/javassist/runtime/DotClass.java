/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.runtime;

public class DotClass {
    public static NoClassDefFoundError fail(ClassNotFoundException e) {
        return new NoClassDefFoundError(e.getMessage());
    }
}

