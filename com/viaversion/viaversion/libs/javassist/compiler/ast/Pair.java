/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.compiler.ast;

import com.viaversion.viaversion.libs.javassist.compiler.CompileError;
import com.viaversion.viaversion.libs.javassist.compiler.ast.ASTree;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Visitor;

public class Pair
extends ASTree {
    private static final long serialVersionUID = 1L;
    protected ASTree left;
    protected ASTree right;

    public Pair(ASTree _left, ASTree _right) {
        this.left = _left;
        this.right = _right;
    }

    @Override
    public void accept(Visitor v) throws CompileError {
        v.atPair(this);
    }

    @Override
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("(<Pair> ");
        sbuf.append(this.left == null ? "<null>" : this.left.toString());
        sbuf.append(" . ");
        sbuf.append(this.right == null ? "<null>" : this.right.toString());
        sbuf.append(')');
        return sbuf.toString();
    }

    @Override
    public ASTree getLeft() {
        return this.left;
    }

    @Override
    public ASTree getRight() {
        return this.right;
    }

    @Override
    public void setLeft(ASTree _left) {
        this.left = _left;
    }

    @Override
    public void setRight(ASTree _right) {
        this.right = _right;
    }
}

