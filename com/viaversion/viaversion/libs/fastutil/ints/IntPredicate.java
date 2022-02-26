/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.fastutil.ints;

import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface IntPredicate
extends Predicate<Integer>,
java.util.function.IntPredicate {
    @Override
    @Deprecated
    default public boolean test(Integer t) {
        return this.test(t.intValue());
    }

    @Override
    default public IntPredicate and(java.util.function.IntPredicate other) {
        Objects.requireNonNull(other);
        return t -> {
            if (!this.test(t)) return false;
            if (!other.test(t)) return false;
            return true;
        };
    }

    default public IntPredicate and(IntPredicate other) {
        return this.and((java.util.function.IntPredicate)other);
    }

    @Override
    @Deprecated
    default public Predicate<Integer> and(Predicate<? super Integer> other) {
        return Predicate.super.and(other);
    }

    @Override
    default public IntPredicate negate() {
        return t -> {
            if (this.test(t)) return false;
            return true;
        };
    }

    @Override
    default public IntPredicate or(java.util.function.IntPredicate other) {
        Objects.requireNonNull(other);
        return t -> {
            if (this.test(t)) return true;
            if (other.test(t)) return true;
            return false;
        };
    }

    default public IntPredicate or(IntPredicate other) {
        return this.or((java.util.function.IntPredicate)other);
    }

    @Override
    @Deprecated
    default public Predicate<Integer> or(Predicate<? super Integer> other) {
        return Predicate.super.or(other);
    }
}

