/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode.analysis;

import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.bytecode.analysis.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiType
extends Type {
    private Map<String, CtClass> interfaces;
    private Type resolved;
    private Type potentialClass;
    private MultiType mergeSource;
    private boolean changed = false;

    public MultiType(Map<String, CtClass> interfaces) {
        this(interfaces, null);
    }

    public MultiType(Map<String, CtClass> interfaces, Type potentialClass) {
        super(null);
        this.interfaces = interfaces;
        this.potentialClass = potentialClass;
    }

    @Override
    public CtClass getCtClass() {
        if (this.resolved == null) return Type.OBJECT.getCtClass();
        return this.resolved.getCtClass();
    }

    @Override
    public Type getComponent() {
        return null;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    boolean popChanged() {
        boolean changed = this.changed;
        this.changed = false;
        return changed;
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public boolean isAssignableTo(Type type) {
        Map<String, CtClass> map;
        if (this.resolved != null) {
            return type.isAssignableFrom(this.resolved);
        }
        if (Type.OBJECT.equals(type)) {
            return true;
        }
        if (this.potentialClass != null && !type.isAssignableFrom(this.potentialClass)) {
            this.potentialClass = null;
        }
        if ((map = this.mergeMultiAndSingle(this, type)).size() == 1 && this.potentialClass == null) {
            this.resolved = Type.get(map.values().iterator().next());
            this.propogateResolved();
            return true;
        }
        if (map.size() >= 1) {
            this.interfaces = map;
            this.propogateState();
            return true;
        }
        if (this.potentialClass == null) return false;
        this.resolved = this.potentialClass;
        this.propogateResolved();
        return true;
    }

    private void propogateState() {
        MultiType source = this.mergeSource;
        while (source != null) {
            source.interfaces = this.interfaces;
            source.potentialClass = this.potentialClass;
            source = source.mergeSource;
        }
    }

    private void propogateResolved() {
        MultiType source = this.mergeSource;
        while (source != null) {
            source.resolved = this.resolved;
            source = source.mergeSource;
        }
    }

    @Override
    public boolean isReference() {
        return true;
    }

    private Map<String, CtClass> getAllMultiInterfaces(MultiType type) {
        HashMap<String, CtClass> map = new HashMap<String, CtClass>();
        Iterator<CtClass> iterator = type.interfaces.values().iterator();
        while (iterator.hasNext()) {
            CtClass intf = iterator.next();
            map.put(intf.getName(), intf);
            this.getAllInterfaces(intf, map);
        }
        return map;
    }

    private Map<String, CtClass> mergeMultiInterfaces(MultiType type1, MultiType type2) {
        Map<String, CtClass> map1 = this.getAllMultiInterfaces(type1);
        Map<String, CtClass> map2 = this.getAllMultiInterfaces(type2);
        return this.findCommonInterfaces(map1, map2);
    }

    private Map<String, CtClass> mergeMultiAndSingle(MultiType multi, Type single) {
        Map<String, CtClass> map1 = this.getAllMultiInterfaces(multi);
        Map<String, CtClass> map2 = this.getAllInterfaces(single.getCtClass(), null);
        return this.findCommonInterfaces(map1, map2);
    }

    private boolean inMergeSource(MultiType source) {
        while (source != null) {
            if (source == this) {
                return true;
            }
            source = source.mergeSource;
        }
        return false;
    }

    @Override
    public Type merge(Type type) {
        Map<String, CtClass> merged;
        Type mergePotential;
        if (this == type) {
            return this;
        }
        if (type == UNINIT) {
            return this;
        }
        if (type == BOGUS) {
            return BOGUS;
        }
        if (type == null) {
            return this;
        }
        if (this.resolved != null) {
            return this.resolved.merge(type);
        }
        if (this.potentialClass != null && (!(mergePotential = this.potentialClass.merge(type)).equals(this.potentialClass) || mergePotential.popChanged())) {
            this.potentialClass = Type.OBJECT.equals(mergePotential) ? null : mergePotential;
            this.changed = true;
        }
        if (type instanceof MultiType) {
            MultiType multi = (MultiType)type;
            if (multi.resolved != null) {
                merged = this.mergeMultiAndSingle(this, multi.resolved);
            } else {
                merged = this.mergeMultiInterfaces(multi, this);
                if (!this.inMergeSource(multi)) {
                    this.mergeSource = multi;
                }
            }
        } else {
            merged = this.mergeMultiAndSingle(this, type);
        }
        if (merged.size() > 1 || merged.size() == 1 && this.potentialClass != null) {
            if (merged.size() != this.interfaces.size()) {
                this.changed = true;
            } else if (!this.changed) {
                for (String key : merged.keySet()) {
                    if (this.interfaces.containsKey(key)) continue;
                    this.changed = true;
                }
            }
            this.interfaces = merged;
            this.propogateState();
            return this;
        }
        this.resolved = merged.size() == 1 ? Type.get(merged.values().iterator().next()) : (this.potentialClass != null ? this.potentialClass : OBJECT);
        this.propogateResolved();
        return this.resolved;
    }

    @Override
    public int hashCode() {
        if (this.resolved == null) return this.interfaces.keySet().hashCode();
        return this.resolved.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MultiType)) {
            return false;
        }
        MultiType multi = (MultiType)o;
        if (this.resolved != null) {
            return this.resolved.equals(multi.resolved);
        }
        if (multi.resolved == null) return this.interfaces.keySet().equals(multi.interfaces.keySet());
        return false;
    }

    @Override
    public String toString() {
        if (this.resolved != null) {
            return this.resolved.toString();
        }
        StringBuffer buffer = new StringBuffer("{");
        for (String key : this.interfaces.keySet()) {
            buffer.append(key).append(", ");
        }
        if (this.potentialClass != null) {
            buffer.append("*").append(this.potentialClass.toString());
        } else {
            buffer.setLength(buffer.length() - 2);
        }
        buffer.append("}");
        return buffer.toString();
    }
}

