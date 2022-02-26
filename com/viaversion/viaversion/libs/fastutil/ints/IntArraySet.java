/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.fastutil.ints;

import com.viaversion.viaversion.libs.fastutil.SafeMath;
import com.viaversion.viaversion.libs.fastutil.ints.AbstractIntSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntArrays;
import com.viaversion.viaversion.libs.fastutil.ints.IntCollection;
import com.viaversion.viaversion.libs.fastutil.ints.IntIterator;
import com.viaversion.viaversion.libs.fastutil.ints.IntOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSpliterator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.IntConsumer;

public class IntArraySet
extends AbstractIntSet
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    private transient int[] a;
    private int size;

    public IntArraySet(int[] a) {
        this.a = a;
        this.size = a.length;
    }

    public IntArraySet() {
        this.a = IntArrays.EMPTY_ARRAY;
    }

    public IntArraySet(int capacity) {
        this.a = new int[capacity];
    }

    public IntArraySet(IntCollection c) {
        this(c.size());
        this.addAll(c);
    }

    public IntArraySet(Collection<? extends Integer> c) {
        this(c.size());
        this.addAll(c);
    }

    public IntArraySet(IntSet c) {
        this(c.size());
        int i = 0;
        IntIterator intIterator = c.iterator();
        while (true) {
            int x;
            if (!intIterator.hasNext()) {
                this.size = i;
                return;
            }
            this.a[i] = x = ((Integer)intIterator.next()).intValue();
            ++i;
        }
    }

    public IntArraySet(Set<? extends Integer> c) {
        this(c.size());
        int i = 0;
        Iterator<? extends Integer> iterator = c.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                this.size = i;
                return;
            }
            Integer x = iterator.next();
            this.a[i] = x;
            ++i;
        }
    }

    public IntArraySet(int[] a, int size) {
        this.a = a;
        this.size = size;
        if (size <= a.length) return;
        throw new IllegalArgumentException("The provided size (" + size + ") is larger than or equal to the array size (" + a.length + ")");
    }

    public static IntArraySet of() {
        return IntArraySet.ofUnchecked();
    }

    public static IntArraySet of(int e) {
        return IntArraySet.ofUnchecked(e);
    }

    public static IntArraySet of(int ... a) {
        if (a.length == 2) {
            if (a[0] != a[1]) return IntArraySet.ofUnchecked(a);
            throw new IllegalArgumentException("Duplicate element: " + a[1]);
        }
        if (a.length <= 2) return IntArraySet.ofUnchecked(a);
        IntOpenHashSet.of(a);
        return IntArraySet.ofUnchecked(a);
    }

    public static IntArraySet ofUnchecked() {
        return new IntArraySet();
    }

    public static IntArraySet ofUnchecked(int ... a) {
        return new IntArraySet(a);
    }

    private int findKey(int o) {
        int i = this.size;
        do {
            if (i-- == 0) return -1;
        } while (this.a[i] != o);
        return i;
    }

    @Override
    public IntIterator iterator() {
        return new IntIterator(){
            int next = 0;

            @Override
            public boolean hasNext() {
                if (this.next >= IntArraySet.this.size) return false;
                return true;
            }

            @Override
            public int nextInt() {
                if (this.hasNext()) return IntArraySet.this.a[this.next++];
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                int tail = IntArraySet.this.size-- - this.next--;
                System.arraycopy(IntArraySet.this.a, this.next + 1, IntArraySet.this.a, this.next, tail);
            }

            @Override
            public int skip(int n) {
                if (n < 0) {
                    throw new IllegalArgumentException("Argument must be nonnegative: " + n);
                }
                int remaining = IntArraySet.this.size - this.next;
                if (n < remaining) {
                    this.next += n;
                    return n;
                }
                n = remaining;
                this.next = IntArraySet.this.size;
                return n;
            }
        };
    }

    @Override
    public IntSpliterator spliterator() {
        return new Spliterator();
    }

    @Override
    public boolean contains(int k) {
        if (this.findKey(k) == -1) return false;
        return true;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean remove(int k) {
        int pos = this.findKey(k);
        if (pos == -1) {
            return false;
        }
        int tail = this.size - pos - 1;
        int i = 0;
        while (true) {
            if (i >= tail) {
                --this.size;
                return true;
            }
            this.a[pos + i] = this.a[pos + i + 1];
            ++i;
        }
    }

    @Override
    public boolean add(int k) {
        int pos = this.findKey(k);
        if (pos != -1) {
            return false;
        }
        if (this.size == this.a.length) {
            int[] b = new int[this.size == 0 ? 2 : this.size * 2];
            int i = this.size;
            while (i-- != 0) {
                b[i] = this.a[i];
            }
            this.a = b;
        }
        this.a[this.size++] = k;
        return true;
    }

    @Override
    public void clear() {
        this.size = 0;
    }

    @Override
    public boolean isEmpty() {
        if (this.size != 0) return false;
        return true;
    }

    @Override
    public int[] toIntArray() {
        return Arrays.copyOf(this.a, this.size);
    }

    @Override
    public int[] toArray(int[] a) {
        if (a == null || a.length < this.size) {
            a = new int[this.size];
        }
        System.arraycopy(this.a, 0, a, 0, this.size);
        return a;
    }

    public IntArraySet clone() {
        IntArraySet c;
        try {
            c = (IntArraySet)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.a = (int[])this.a.clone();
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        int i = 0;
        while (i < this.size) {
            s.writeInt(this.a[i]);
            ++i;
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.a = new int[this.size];
        int i = 0;
        while (i < this.size) {
            this.a[i] = s.readInt();
            ++i;
        }
    }

    private final class Spliterator
    implements IntSpliterator {
        boolean hasSplit = false;
        int pos;
        int max;

        public Spliterator() {
            this(0, intArraySet.size, false);
        }

        private Spliterator(int pos, int max, boolean hasSplit) {
            assert (pos <= max) : "pos " + pos + " must be <= max " + max;
            this.pos = pos;
            this.max = max;
            this.hasSplit = hasSplit;
        }

        private int getWorkingMax() {
            int n;
            if (this.hasSplit) {
                n = this.max;
                return n;
            }
            n = IntArraySet.this.size;
            return n;
        }

        @Override
        public int characteristics() {
            return 16721;
        }

        @Override
        public long estimateSize() {
            return this.getWorkingMax() - this.pos;
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            if (this.pos >= this.getWorkingMax()) {
                return false;
            }
            action.accept(IntArraySet.this.a[this.pos++]);
            return true;
        }

        @Override
        public void forEachRemaining(IntConsumer action) {
            int max = this.getWorkingMax();
            while (this.pos < max) {
                action.accept(IntArraySet.this.a[this.pos]);
                ++this.pos;
            }
        }

        @Override
        public long skip(long n) {
            if (n < 0L) {
                throw new IllegalArgumentException("Argument must be nonnegative: " + n);
            }
            int max = this.getWorkingMax();
            if (this.pos >= max) {
                return 0L;
            }
            int remaining = max - this.pos;
            if (n < (long)remaining) {
                this.pos = SafeMath.safeLongToInt((long)this.pos + n);
                return n;
            }
            n = remaining;
            this.pos = max;
            return n;
        }

        @Override
        public IntSpliterator trySplit() {
            int myNewPos;
            int max = this.getWorkingMax();
            int retLen = max - this.pos >> 1;
            if (retLen <= 1) {
                return null;
            }
            this.max = max;
            int retMax = myNewPos = this.pos + retLen;
            int oldPos = this.pos;
            this.pos = myNewPos;
            this.hasSplit = true;
            return new Spliterator(oldPos, retMax, true);
        }
    }
}

