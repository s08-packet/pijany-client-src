/*
 * Decompiled with CFR 0.152.
 */
package org.yaml.snakeyaml.scanner;

import java.util.Arrays;

public final class Constant {
    private static final String ALPHA_S = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
    private static final String LINEBR_S = "\n\u0085\u2028\u2029";
    private static final String FULL_LINEBR_S = "\r\n\u0085\u2028\u2029";
    private static final String NULL_OR_LINEBR_S = "\u0000\r\n\u0085\u2028\u2029";
    private static final String NULL_BL_LINEBR_S = " \u0000\r\n\u0085\u2028\u2029";
    private static final String NULL_BL_T_LINEBR_S = "\t \u0000\r\n\u0085\u2028\u2029";
    private static final String NULL_BL_T_S = "\u0000 \t";
    private static final String URI_CHARS_S = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_-;/?:@&=+$,_.!~*'()[]%";
    public static final Constant LINEBR = new Constant("\n\u0085\u2028\u2029");
    public static final Constant FULL_LINEBR = new Constant("\r\n\u0085\u2028\u2029");
    public static final Constant NULL_OR_LINEBR = new Constant("\u0000\r\n\u0085\u2028\u2029");
    public static final Constant NULL_BL_LINEBR = new Constant(" \u0000\r\n\u0085\u2028\u2029");
    public static final Constant NULL_BL_T_LINEBR = new Constant("\t \u0000\r\n\u0085\u2028\u2029");
    public static final Constant NULL_BL_T = new Constant("\u0000 \t");
    public static final Constant URI_CHARS = new Constant("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_-;/?:@&=+$,_.!~*'()[]%");
    public static final Constant ALPHA = new Constant("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_");
    private String content;
    boolean[] contains = new boolean[128];
    boolean noASCII = false;

    private Constant(String content) {
        Arrays.fill(this.contains, false);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (true) {
            if (i >= content.length()) {
                if (sb.length() <= 0) return;
                this.noASCII = true;
                this.content = sb.toString();
                return;
            }
            int c = content.codePointAt(i);
            if (c < 128) {
                this.contains[c] = true;
            } else {
                sb.appendCodePoint(c);
            }
            ++i;
        }
    }

    public boolean has(int c) {
        if (c < 128) {
            boolean bl = this.contains[c];
            return bl;
        }
        if (!this.noASCII) return false;
        if (this.content.indexOf(c, 0) == -1) return false;
        return true;
    }

    public boolean hasNo(int c) {
        if (this.has(c)) return false;
        return true;
    }

    public boolean has(int c, String additional) {
        if (this.has(c)) return true;
        if (additional.indexOf(c, 0) != -1) return true;
        return false;
    }

    public boolean hasNo(int c, String additional) {
        if (this.has(c, additional)) return false;
        return true;
    }
}

