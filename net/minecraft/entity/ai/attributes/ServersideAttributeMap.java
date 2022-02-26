/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.server.management.LowerStringMap;

public class ServersideAttributeMap
extends BaseAttributeMap {
    private final Set<IAttributeInstance> attributeInstanceSet = Sets.newHashSet();
    protected final Map<String, IAttributeInstance> descriptionToAttributeInstanceMap = new LowerStringMap<IAttributeInstance>();

    @Override
    public ModifiableAttributeInstance getAttributeInstance(IAttribute attribute) {
        return (ModifiableAttributeInstance)super.getAttributeInstance(attribute);
    }

    @Override
    public ModifiableAttributeInstance getAttributeInstanceByName(String attributeName) {
        IAttributeInstance iattributeinstance = super.getAttributeInstanceByName(attributeName);
        if (iattributeinstance != null) return (ModifiableAttributeInstance)iattributeinstance;
        iattributeinstance = this.descriptionToAttributeInstanceMap.get(attributeName);
        return (ModifiableAttributeInstance)iattributeinstance;
    }

    @Override
    public IAttributeInstance registerAttribute(IAttribute attribute) {
        IAttributeInstance iattributeinstance = super.registerAttribute(attribute);
        if (!(attribute instanceof RangedAttribute)) return iattributeinstance;
        if (((RangedAttribute)attribute).getDescription() == null) return iattributeinstance;
        this.descriptionToAttributeInstanceMap.put(((RangedAttribute)attribute).getDescription(), iattributeinstance);
        return iattributeinstance;
    }

    @Override
    protected IAttributeInstance func_180376_c(IAttribute p_180376_1_) {
        return new ModifiableAttributeInstance(this, p_180376_1_);
    }

    @Override
    public void func_180794_a(IAttributeInstance p_180794_1_) {
        if (p_180794_1_.getAttribute().getShouldWatch()) {
            this.attributeInstanceSet.add(p_180794_1_);
        }
        Iterator iterator = this.field_180377_c.get(p_180794_1_.getAttribute()).iterator();
        while (iterator.hasNext()) {
            IAttribute iattribute = (IAttribute)iterator.next();
            ModifiableAttributeInstance modifiableattributeinstance = this.getAttributeInstance(iattribute);
            if (modifiableattributeinstance == null) continue;
            modifiableattributeinstance.flagForUpdate();
        }
    }

    public Set<IAttributeInstance> getAttributeInstanceSet() {
        return this.attributeInstanceSet;
    }

    public Collection<IAttributeInstance> getWatchedAttributes() {
        HashSet<IAttributeInstance> set = Sets.newHashSet();
        Iterator<IAttributeInstance> iterator = this.getAllAttributes().iterator();
        while (iterator.hasNext()) {
            IAttributeInstance iattributeinstance = iterator.next();
            if (!iattributeinstance.getAttribute().getShouldWatch()) continue;
            set.add(iattributeinstance);
        }
        return set;
    }
}

