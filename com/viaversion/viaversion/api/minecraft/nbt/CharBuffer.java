/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.api.minecraft.nbt;

import com.viaversion.viaversion.api.minecraft.nbt.StringTagParseException;

final class CharBuffer {
    private final CharSequence sequence;
    private int index;

    CharBuffer(CharSequence sequence) {
        this.sequence = sequence;
    }

    public char peek() {
        return this.sequence.charAt(this.index);
    }

    public char peek(int offset) {
        return this.sequence.charAt(this.index + offset);
    }

    public char take() {
        return this.sequence.charAt(this.index++);
    }

    public boolean advance() {
        ++this.index;
        return this.hasMore();
    }

    public boolean hasMore() {
        if (this.index >= this.sequence.length()) return false;
        return true;
    }

    public boolean hasMore(int offset) {
        if (this.index + offset >= this.sequence.length()) return false;
        return true;
    }

    public CharSequence takeUntil(char until) throws StringTagParseException {
        until = Character.toLowerCase(until);
        int endIdx = -1;
        for (int idx = this.index; idx < this.sequence.length(); ++idx) {
            if (this.sequence.charAt(idx) == '\\') {
                ++idx;
                continue;
            }
            if (Character.toLowerCase(this.sequence.charAt(idx)) != until) continue;
            endIdx = idx;
            break;
        }
        if (endIdx == -1) {
            throw this.makeError("No occurrence of " + until + " was found");
        }
        CharSequence result = this.sequence.subSequence(this.index, endIdx);
        this.index = endIdx + 1;
        return result;
    }

    public CharBuffer expect(char expectedChar) throws StringTagParseException {
        this.skipWhitespace();
        if (!this.hasMore()) {
            throw this.makeError("Expected character '" + expectedChar + "' but got EOF");
        }
        if (this.peek() != expectedChar) {
            throw this.makeError("Expected character '" + expectedChar + "' but got '" + this.peek() + "'");
        }
        this.take();
        return this;
    }

    public boolean takeIf(char token) {
        this.skipWhitespace();
        if (!this.hasMore()) return false;
        if (this.peek() != token) return false;
        this.advance();
        return true;
    }

    public CharBuffer skipWhitespace() {
        while (this.hasMore()) {
            if (!Character.isWhitespace(this.peek())) return this;
            this.advance();
        }
        return this;
    }

    public StringTagParseException makeError(String message) {
        return new StringTagParseException(message, this.sequence, this.index);
    }
}

