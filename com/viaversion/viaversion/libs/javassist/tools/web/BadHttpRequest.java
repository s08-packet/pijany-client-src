/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.tools.web;

public class BadHttpRequest
extends Exception {
    private static final long serialVersionUID = 1L;
    private Exception e;

    public BadHttpRequest() {
        this.e = null;
    }

    public BadHttpRequest(Exception _e) {
        this.e = _e;
    }

    @Override
    public String toString() {
        if (this.e != null) return this.e.toString();
        return super.toString();
    }
}

