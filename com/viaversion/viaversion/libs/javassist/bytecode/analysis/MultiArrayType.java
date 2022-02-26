/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode.analysis;

import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.analysis.MultiType;
import com.viaversion.viaversion.libs.javassist.bytecode.analysis.Type;

public class MultiArrayType
extends Type {
    private MultiType component;
    private int dims;

    public MultiArrayType(MultiType component, int dims) {
        super(null);
        this.component = component;
        this.dims = dims;
    }

    @Override
    public CtClass getCtClass() {
        CtClass clazz = this.component.getCtClass();
        if (clazz == null) {
            return null;
        }
        ClassPool pool = clazz.getClassPool();
        if (pool == null) {
            pool = ClassPool.getDefault();
        }
        String name = this.arrayName(clazz.getName(), this.dims);
        try {
            return pool.get(name);
        }
        catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    boolean popChanged() {
        return this.component.popChanged();
    }

    @Override
    public int getDimensions() {
        return this.dims;
    }

    @Override
    public Type getComponent() {
        Type type;
        if (this.dims == 1) {
            type = this.component;
            return type;
        }
        type = new MultiArrayType(this.component, this.dims - 1);
        return type;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isReference() {
        return true;
    }

    public boolean isAssignableTo(Type type) {
        if (MultiArrayType.eq(type.getCtClass(), Type.OBJECT.getCtClass())) {
            return true;
        }
        if (MultiArrayType.eq(type.getCtClass(), Type.CLONEABLE.getCtClass())) {
            return true;
        }
        if (MultiArrayType.eq(type.getCtClass(), Type.SERIALIZABLE.getCtClass())) {
            return true;
        }
        if (!type.isArray()) {
            return false;
        }
        Type typeRoot = this.getRootComponent(type);
        int typeDims = type.getDimensions();
        if (typeDims > this.dims) {
            return false;
        }
        if (typeDims >= this.dims) return this.component.isAssignableTo(typeRoot);
        if (MultiArrayType.eq(typeRoot.getCtClass(), Type.OBJECT.getCtClass())) {
            return true;
        }
        if (MultiArrayType.eq(typeRoot.getCtClass(), Type.CLONEABLE.getCtClass())) {
            return true;
        }
        if (!MultiArrayType.eq(typeRoot.getCtClass(), Type.SERIALIZABLE.getCtClass())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return this.component.hashCode() + this.dims;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MultiArrayType)) {
            return false;
        }
        MultiArrayType multi = (MultiArrayType)o;
        if (!this.component.equals(multi.component)) return false;
        if (this.dims != multi.dims) return false;
        return true;
    }

    @Override
    public String toString() {
        return this.arrayName(this.component.toString(), this.dims);
    }
}

