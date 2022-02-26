/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode.analysis;

import com.viaversion.viaversion.libs.javassist.bytecode.analysis.Type;

public class Frame {
    private Type[] locals;
    private Type[] stack;
    private int top;
    private boolean jsrMerged;
    private boolean retMerged;

    public Frame(int locals, int stack) {
        this.locals = new Type[locals];
        this.stack = new Type[stack];
    }

    public Type getLocal(int index) {
        return this.locals[index];
    }

    public void setLocal(int index, Type type) {
        this.locals[index] = type;
    }

    public Type getStack(int index) {
        return this.stack[index];
    }

    public void setStack(int index, Type type) {
        this.stack[index] = type;
    }

    public void clearStack() {
        this.top = 0;
    }

    public int getTopIndex() {
        return this.top - 1;
    }

    public int localsLength() {
        return this.locals.length;
    }

    public Type peek() {
        if (this.top >= 1) return this.stack[this.top - 1];
        throw new IndexOutOfBoundsException("Stack is empty");
    }

    public Type pop() {
        if (this.top >= 1) return this.stack[--this.top];
        throw new IndexOutOfBoundsException("Stack is empty");
    }

    public void push(Type type) {
        this.stack[this.top++] = type;
    }

    public Frame copy() {
        Frame frame = new Frame(this.locals.length, this.stack.length);
        System.arraycopy(this.locals, 0, frame.locals, 0, this.locals.length);
        System.arraycopy(this.stack, 0, frame.stack, 0, this.stack.length);
        frame.top = this.top;
        return frame;
    }

    public Frame copyStack() {
        Frame frame = new Frame(this.locals.length, this.stack.length);
        System.arraycopy(this.stack, 0, frame.stack, 0, this.stack.length);
        frame.top = this.top;
        return frame;
    }

    public boolean mergeStack(Frame frame) {
        boolean changed = false;
        if (this.top != frame.top) {
            throw new RuntimeException("Operand stacks could not be merged, they are different sizes!");
        }
        int i = 0;
        while (i < this.top) {
            if (this.stack[i] != null) {
                Type prev = this.stack[i];
                Type merged = prev.merge(frame.stack[i]);
                if (merged == Type.BOGUS) {
                    throw new RuntimeException("Operand stacks could not be merged due to differing primitive types: pos = " + i);
                }
                this.stack[i] = merged;
                if (!merged.equals(prev) || merged.popChanged()) {
                    changed = true;
                }
            }
            ++i;
        }
        return changed;
    }

    public boolean merge(Frame frame) {
        boolean changed = false;
        int i = 0;
        while (i < this.locals.length) {
            if (this.locals[i] != null) {
                Type merged;
                Type prev = this.locals[i];
                this.locals[i] = merged = prev.merge(frame.locals[i]);
                if (!merged.equals(prev) || merged.popChanged()) {
                    changed = true;
                }
            } else if (frame.locals[i] != null) {
                this.locals[i] = frame.locals[i];
                changed = true;
            }
            ++i;
        }
        return changed |= this.mergeStack(frame);
    }

    public String toString() {
        int i;
        StringBuffer buffer = new StringBuffer();
        buffer.append("locals = [");
        for (i = 0; i < this.locals.length; ++i) {
            buffer.append(this.locals[i] == null ? "empty" : this.locals[i].toString());
            if (i >= this.locals.length - 1) continue;
            buffer.append(", ");
        }
        buffer.append("] stack = [");
        i = 0;
        while (true) {
            if (i >= this.top) {
                buffer.append("]");
                return buffer.toString();
            }
            buffer.append(this.stack[i]);
            if (i < this.top - 1) {
                buffer.append(", ");
            }
            ++i;
        }
    }

    boolean isJsrMerged() {
        return this.jsrMerged;
    }

    void setJsrMerged(boolean jsrMerged) {
        this.jsrMerged = jsrMerged;
    }

    boolean isRetMerged() {
        return this.retMerged;
    }

    void setRetMerged(boolean retMerged) {
        this.retMerged = retMerged;
    }
}

