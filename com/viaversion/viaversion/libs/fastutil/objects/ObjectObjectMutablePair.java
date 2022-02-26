/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.fastutil.objects;

import com.viaversion.viaversion.libs.fastutil.Pair;
import java.io.Serializable;
import java.util.Objects;

public class ObjectObjectMutablePair<K, V>
implements Pair<K, V>,
Serializable {
    private static final long serialVersionUID = 0L;
    protected K left;
    protected V right;

    public ObjectObjectMutablePair(K left, V right) {
        this.left = left;
        this.right = right;
    }

    public static <K, V> ObjectObjectMutablePair<K, V> of(K left, V right) {
        return new ObjectObjectMutablePair<K, V>(left, right);
    }

    @Override
    public K left() {
        return this.left;
    }

    @Override
    public ObjectObjectMutablePair<K, V> left(K l) {
        this.left = l;
        return this;
    }

    @Override
    public V right() {
        return this.right;
    }

    @Override
    public ObjectObjectMutablePair<K, V> right(V r) {
        this.right = r;
        return this;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Pair)) return false;
        if (!Objects.equals(this.left, ((Pair)other).left())) return false;
        if (!Objects.equals(this.right, ((Pair)other).right())) return false;
        return true;
    }

    public int hashCode() {
        int n;
        int n2 = (this.left == null ? 0 : this.left.hashCode()) * 19;
        if (this.right == null) {
            n = 0;
            return n2 + n;
        }
        n = this.right.hashCode();
        return n2 + n;
    }

    public String toString() {
        return "<" + this.left() + "," + this.right() + ">";
    }
}

