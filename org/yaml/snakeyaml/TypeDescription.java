/*
 * Decompiled with CFR 0.152.
 */
package org.yaml.snakeyaml;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertySubstitute;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

public class TypeDescription {
    private static final Logger log = Logger.getLogger(TypeDescription.class.getPackage().getName());
    private final Class<? extends Object> type;
    private Class<?> impl;
    private Tag tag;
    private transient Set<Property> dumpProperties;
    private transient PropertyUtils propertyUtils;
    private transient boolean delegatesChecked;
    private Map<String, PropertySubstitute> properties = Collections.emptyMap();
    protected Set<String> excludes = Collections.emptySet();
    protected String[] includes = null;
    protected BeanAccess beanAccess;

    public TypeDescription(Class<? extends Object> clazz, Tag tag) {
        this(clazz, tag, null);
    }

    public TypeDescription(Class<? extends Object> clazz, Tag tag, Class<?> impl) {
        this.type = clazz;
        this.tag = tag;
        this.impl = impl;
        this.beanAccess = null;
    }

    public TypeDescription(Class<? extends Object> clazz, String tag) {
        this(clazz, new Tag(tag), null);
    }

    public TypeDescription(Class<? extends Object> clazz) {
        this(clazz, null, null);
    }

    public TypeDescription(Class<? extends Object> clazz, Class<?> impl) {
        this(clazz, null, impl);
    }

    public Tag getTag() {
        return this.tag;
    }

    @Deprecated
    public void setTag(Tag tag) {
        this.tag = tag;
    }

    @Deprecated
    public void setTag(String tag) {
        this.setTag(new Tag(tag));
    }

    public Class<? extends Object> getType() {
        return this.type;
    }

    @Deprecated
    public void putListPropertyType(String property, Class<? extends Object> type) {
        this.addPropertyParameters(property, type);
    }

    @Deprecated
    public Class<? extends Object> getListPropertyType(String property) {
        if (!this.properties.containsKey(property)) return null;
        Class<?>[] typeArguments = this.properties.get(property).getActualTypeArguments();
        if (typeArguments == null) return null;
        if (typeArguments.length <= 0) return null;
        return typeArguments[0];
    }

    @Deprecated
    public void putMapPropertyType(String property, Class<? extends Object> key, Class<? extends Object> value) {
        this.addPropertyParameters(property, key, value);
    }

    @Deprecated
    public Class<? extends Object> getMapKeyType(String property) {
        if (!this.properties.containsKey(property)) return null;
        Class<?>[] typeArguments = this.properties.get(property).getActualTypeArguments();
        if (typeArguments == null) return null;
        if (typeArguments.length <= 0) return null;
        return typeArguments[0];
    }

    @Deprecated
    public Class<? extends Object> getMapValueType(String property) {
        if (!this.properties.containsKey(property)) return null;
        Class<?>[] typeArguments = this.properties.get(property).getActualTypeArguments();
        if (typeArguments == null) return null;
        if (typeArguments.length <= 1) return null;
        return typeArguments[1];
    }

    public void addPropertyParameters(String pName, Class<?> ... classes) {
        if (!this.properties.containsKey(pName)) {
            this.substituteProperty(pName, null, null, null, classes);
            return;
        }
        PropertySubstitute pr = this.properties.get(pName);
        pr.setActualTypeArguments(classes);
    }

    public String toString() {
        return "TypeDescription for " + this.getType() + " (tag='" + this.getTag() + "')";
    }

    private void checkDelegates() {
        Collection<PropertySubstitute> values = this.properties.values();
        Iterator<PropertySubstitute> i$ = values.iterator();
        while (true) {
            if (!i$.hasNext()) {
                this.delegatesChecked = true;
                return;
            }
            PropertySubstitute p = i$.next();
            try {
                p.setDelegate(this.discoverProperty(p.getName()));
            }
            catch (YAMLException e) {
            }
        }
    }

    private Property discoverProperty(String name) {
        if (this.propertyUtils == null) return null;
        if (this.beanAccess != null) return this.propertyUtils.getProperty(this.type, name, this.beanAccess);
        return this.propertyUtils.getProperty(this.type, name);
    }

    public Property getProperty(String name) {
        Property property;
        if (!this.delegatesChecked) {
            this.checkDelegates();
        }
        if (this.properties.containsKey(name)) {
            property = this.properties.get(name);
            return property;
        }
        property = this.discoverProperty(name);
        return property;
    }

    public void substituteProperty(String pName, Class<?> pType, String getter, String setter, Class<?> ... argParams) {
        this.substituteProperty(new PropertySubstitute(pName, pType, getter, setter, argParams));
    }

    public void substituteProperty(PropertySubstitute substitute) {
        if (Collections.EMPTY_MAP == this.properties) {
            this.properties = new LinkedHashMap<String, PropertySubstitute>();
        }
        substitute.setTargetType(this.type);
        this.properties.put(substitute.getName(), substitute);
    }

    public void setPropertyUtils(PropertyUtils propertyUtils) {
        this.propertyUtils = propertyUtils;
    }

    public void setIncludes(String ... propNames) {
        this.includes = propNames != null && propNames.length > 0 ? propNames : null;
    }

    public void setExcludes(String ... propNames) {
        if (propNames != null && propNames.length > 0) {
            this.excludes = new HashSet<String>();
            String[] arr$ = propNames;
            int len$ = arr$.length;
            int i$ = 0;
            while (i$ < len$) {
                String name = arr$[i$];
                this.excludes.add(name);
                ++i$;
            }
            return;
        }
        this.excludes = Collections.emptySet();
    }

    /*
     * WARNING - void declaration
     */
    public Set<Property> getProperties() {
        Set<Property> readableProps;
        if (this.dumpProperties != null) {
            return this.dumpProperties;
        }
        if (this.propertyUtils == null) return null;
        if (this.includes != null) {
            void var3_7;
            this.dumpProperties = new LinkedHashSet<Property>();
            String[] arr$ = this.includes;
            int len$ = arr$.length;
            boolean bl = false;
            while (var3_7 < len$) {
                String propertyName = arr$[var3_7];
                if (!this.excludes.contains(propertyName)) {
                    this.dumpProperties.add(this.getProperty(propertyName));
                }
                ++var3_7;
            }
            return this.dumpProperties;
        }
        Set<Property> set = readableProps = this.beanAccess == null ? this.propertyUtils.getProperties(this.type) : this.propertyUtils.getProperties(this.type, this.beanAccess);
        if (this.properties.isEmpty()) {
            if (this.excludes.isEmpty()) {
                this.dumpProperties = readableProps;
                return this.dumpProperties;
            }
            this.dumpProperties = new LinkedHashSet<Property>();
            Iterator<Property> i$ = readableProps.iterator();
            while (i$.hasNext()) {
                Property property = i$.next();
                if (this.excludes.contains(property.getName())) continue;
                this.dumpProperties.add(property);
            }
            return this.dumpProperties;
        }
        if (!this.delegatesChecked) {
            this.checkDelegates();
        }
        this.dumpProperties = new LinkedHashSet<Property>();
        for (Property property : this.properties.values()) {
            if (this.excludes.contains(property.getName()) || !property.isReadable()) continue;
            this.dumpProperties.add(property);
        }
        Iterator<Property> i$ = readableProps.iterator();
        while (i$.hasNext()) {
            Property property = i$.next();
            if (this.excludes.contains(property.getName())) continue;
            this.dumpProperties.add(property);
        }
        return this.dumpProperties;
    }

    public boolean setupPropertyType(String key, Node valueNode) {
        return false;
    }

    public boolean setProperty(Object targetBean, String propertyName, Object value) throws Exception {
        return false;
    }

    public Object newInstance(Node node) {
        if (this.impl == null) return null;
        try {
            Constructor<?> c = this.impl.getDeclaredConstructor(new Class[0]);
            c.setAccessible(true);
            return c.newInstance(new Object[0]);
        }
        catch (Exception e) {
            log.fine(e.getLocalizedMessage());
            this.impl = null;
        }
        return null;
    }

    public Object newInstance(String propertyName, Node node) {
        return null;
    }

    public Object finalizeConstruction(Object obj) {
        return obj;
    }
}

