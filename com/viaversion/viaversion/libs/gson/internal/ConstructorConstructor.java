/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.gson.internal;

import com.viaversion.viaversion.libs.gson.InstanceCreator;
import com.viaversion.viaversion.libs.gson.JsonIOException;
import com.viaversion.viaversion.libs.gson.internal.LinkedTreeMap;
import com.viaversion.viaversion.libs.gson.internal.ObjectConstructor;
import com.viaversion.viaversion.libs.gson.internal.UnsafeAllocator;
import com.viaversion.viaversion.libs.gson.internal.reflect.ReflectionAccessor;
import com.viaversion.viaversion.libs.gson.reflect.TypeToken;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public final class ConstructorConstructor {
    private final Map<Type, InstanceCreator<?>> instanceCreators;
    private final ReflectionAccessor accessor = ReflectionAccessor.getInstance();

    public ConstructorConstructor(Map<Type, InstanceCreator<?>> instanceCreators) {
        this.instanceCreators = instanceCreators;
    }

    public <T> ObjectConstructor<T> get(TypeToken<T> typeToken) {
        final Type type = typeToken.getType();
        Class<T> rawType = typeToken.getRawType();
        final InstanceCreator<?> typeCreator = this.instanceCreators.get(type);
        if (typeCreator != null) {
            return new ObjectConstructor<T>(){

                @Override
                public T construct() {
                    return typeCreator.createInstance(type);
                }
            };
        }
        final InstanceCreator<?> rawTypeCreator = this.instanceCreators.get(rawType);
        if (rawTypeCreator != null) {
            return new ObjectConstructor<T>(){

                @Override
                public T construct() {
                    return rawTypeCreator.createInstance(type);
                }
            };
        }
        ObjectConstructor<T> defaultConstructor = this.newDefaultConstructor(rawType);
        if (defaultConstructor != null) {
            return defaultConstructor;
        }
        ObjectConstructor<T> defaultImplementation = this.newDefaultImplementationConstructor(type, rawType);
        if (defaultImplementation == null) return this.newUnsafeAllocator(type, rawType);
        return defaultImplementation;
    }

    private <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> rawType) {
        try {
            final Constructor<T> constructor = rawType.getDeclaredConstructor(new Class[0]);
            if (constructor.isAccessible()) return new ObjectConstructor<T>(){

                @Override
                public T construct() {
                    try {
                        Object[] args = null;
                        return constructor.newInstance(args);
                    }
                    catch (InstantiationException e) {
                        throw new RuntimeException("Failed to invoke " + constructor + " with no args", e);
                    }
                    catch (InvocationTargetException e) {
                        throw new RuntimeException("Failed to invoke " + constructor + " with no args", e.getTargetException());
                    }
                    catch (IllegalAccessException e) {
                        throw new AssertionError((Object)e);
                    }
                }
            };
            this.accessor.makeAccessible(constructor);
            return new /* invalid duplicate definition of identical inner class */;
        }
        catch (NoSuchMethodException e) {
            return null;
        }
    }

    private <T> ObjectConstructor<T> newDefaultImplementationConstructor(final Type type, Class<? super T> rawType) {
        if (Collection.class.isAssignableFrom(rawType)) {
            if (SortedSet.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>(){

                    @Override
                    public T construct() {
                        return new TreeSet();
                    }
                };
            }
            if (EnumSet.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>(){

                    @Override
                    public T construct() {
                        if (!(type instanceof ParameterizedType)) throw new JsonIOException("Invalid EnumSet type: " + type.toString());
                        Type elementType = ((ParameterizedType)type).getActualTypeArguments()[0];
                        if (!(elementType instanceof Class)) throw new JsonIOException("Invalid EnumSet type: " + type.toString());
                        return EnumSet.noneOf((Class)elementType);
                    }
                };
            }
            if (Set.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>(){

                    @Override
                    public T construct() {
                        return new LinkedHashSet();
                    }
                };
            }
            if (!Queue.class.isAssignableFrom(rawType)) return new ObjectConstructor<T>(){

                @Override
                public T construct() {
                    return new ArrayList();
                }
            };
            return new ObjectConstructor<T>(){

                @Override
                public T construct() {
                    return new ArrayDeque();
                }
            };
        }
        if (!Map.class.isAssignableFrom(rawType)) return null;
        if (ConcurrentNavigableMap.class.isAssignableFrom(rawType)) {
            return new ObjectConstructor<T>(){

                @Override
                public T construct() {
                    return new ConcurrentSkipListMap();
                }
            };
        }
        if (ConcurrentMap.class.isAssignableFrom(rawType)) {
            return new ObjectConstructor<T>(){

                @Override
                public T construct() {
                    return new ConcurrentHashMap();
                }
            };
        }
        if (SortedMap.class.isAssignableFrom(rawType)) {
            return new ObjectConstructor<T>(){

                @Override
                public T construct() {
                    return new TreeMap();
                }
            };
        }
        if (!(type instanceof ParameterizedType)) return new ObjectConstructor<T>(){

            @Override
            public T construct() {
                return new LinkedTreeMap();
            }
        };
        if (String.class.isAssignableFrom(TypeToken.get(((ParameterizedType)type).getActualTypeArguments()[0]).getRawType())) return new /* invalid duplicate definition of identical inner class */;
        return new ObjectConstructor<T>(){

            @Override
            public T construct() {
                return new LinkedHashMap();
            }
        };
    }

    private <T> ObjectConstructor<T> newUnsafeAllocator(final Type type, final Class<? super T> rawType) {
        return new ObjectConstructor<T>(){
            private final UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();

            @Override
            public T construct() {
                try {
                    Object newInstance = this.unsafeAllocator.newInstance(rawType);
                    return newInstance;
                }
                catch (Exception e) {
                    throw new RuntimeException("Unable to invoke no-args constructor for " + type + ". Registering an InstanceCreator with Gson for this type may fix this problem.", e);
                }
            }
        };
    }

    public String toString() {
        return this.instanceCreators.toString();
    }
}

