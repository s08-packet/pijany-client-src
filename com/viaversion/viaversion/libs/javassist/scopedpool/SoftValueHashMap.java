/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.scopedpool;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SoftValueHashMap<K, V>
implements Map<K, V> {
    private Map<K, SoftValueRef<K, V>> hash;
    private ReferenceQueue<V> queue = new ReferenceQueue();

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        this.processQueue();
        HashSet<Map.Entry<K, V>> ret = new HashSet<Map.Entry<K, V>>();
        Iterator<Map.Entry<K, SoftValueRef<K, V>>> iterator = this.hash.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<K, SoftValueRef<K, V>> e = iterator.next();
            ret.add(new AbstractMap.SimpleImmutableEntry(e.getKey(), e.getValue().get()));
        }
        return ret;
    }

    private void processQueue() {
        Reference<V> ref;
        if (this.hash.isEmpty()) return;
        while ((ref = this.queue.poll()) != null) {
            if (!(ref instanceof SoftValueRef)) continue;
            SoftValueRef que = (SoftValueRef)ref;
            if (ref != this.hash.get(que.key)) continue;
            this.hash.remove(que.key);
        }
    }

    public SoftValueHashMap(int initialCapacity, float loadFactor) {
        this.hash = new ConcurrentHashMap<K, SoftValueRef<K, V>>(initialCapacity, loadFactor);
    }

    public SoftValueHashMap(int initialCapacity) {
        this.hash = new ConcurrentHashMap<K, SoftValueRef<K, V>>(initialCapacity);
    }

    public SoftValueHashMap() {
        this.hash = new ConcurrentHashMap<K, SoftValueRef<K, V>>();
    }

    public SoftValueHashMap(Map<K, V> t) {
        this(Math.max(2 * t.size(), 11), 0.75f);
        this.putAll(t);
    }

    @Override
    public int size() {
        this.processQueue();
        return this.hash.size();
    }

    @Override
    public boolean isEmpty() {
        this.processQueue();
        return this.hash.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        this.processQueue();
        return this.hash.containsKey(key);
    }

    @Override
    public V get(Object key) {
        this.processQueue();
        return this.valueOrNull(this.hash.get(key));
    }

    @Override
    public V put(K key, V value) {
        this.processQueue();
        return this.valueOrNull(this.hash.put(key, SoftValueRef.create(key, value, this.queue)));
    }

    @Override
    public V remove(Object key) {
        this.processQueue();
        return this.valueOrNull(this.hash.remove(key));
    }

    @Override
    public void clear() {
        this.processQueue();
        this.hash.clear();
    }

    @Override
    public boolean containsValue(Object arg0) {
        SoftValueRef<K, V> e;
        this.processQueue();
        if (null == arg0) {
            return false;
        }
        Iterator<SoftValueRef<K, V>> iterator = this.hash.values().iterator();
        do {
            if (!iterator.hasNext()) return false;
        } while (null == (e = iterator.next()) || !arg0.equals(e.get()));
        return true;
    }

    @Override
    public Set<K> keySet() {
        this.processQueue();
        return this.hash.keySet();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> arg0) {
        this.processQueue();
        Iterator<K> iterator = arg0.keySet().iterator();
        while (iterator.hasNext()) {
            K key = iterator.next();
            this.put(key, arg0.get(key));
        }
    }

    @Override
    public Collection<V> values() {
        this.processQueue();
        ArrayList ret = new ArrayList();
        Iterator<SoftValueRef<K, V>> iterator = this.hash.values().iterator();
        while (iterator.hasNext()) {
            SoftValueRef<K, V> e = iterator.next();
            ret.add(e.get());
        }
        return ret;
    }

    private V valueOrNull(SoftValueRef<K, V> rtn) {
        if (null != rtn) return (V)rtn.get();
        return null;
    }

    private static class SoftValueRef<K, V>
    extends SoftReference<V> {
        public K key;

        private SoftValueRef(K key, V val, ReferenceQueue<V> q) {
            super(val, q);
            this.key = key;
        }

        private static <K, V> SoftValueRef<K, V> create(K key, V val, ReferenceQueue<V> q) {
            if (val != null) return new SoftValueRef<K, V>(key, val, q);
            return null;
        }
    }
}

