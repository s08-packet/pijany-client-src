/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public abstract class BlockStateBase
implements IBlockState {
    private static final Joiner COMMA_JOINER = Joiner.on(',');
    private static final Function MAP_ENTRY_TO_STRING = new Function(){
        private static final String __OBFID = "CL_00002031";

        @Override
        public String apply(Map.Entry p_apply_1_) {
            if (p_apply_1_ == null) {
                return "<NULL>";
            }
            IProperty iproperty = (IProperty)p_apply_1_.getKey();
            return iproperty.getName() + "=" + iproperty.getName((Comparable)p_apply_1_.getValue());
        }

        @Override
        public Object apply(Object p_apply_1_) {
            return this.apply((Map.Entry)p_apply_1_);
        }
    };
    private static final String __OBFID = "CL_00002032";
    private int blockId = -1;
    private int blockStateId = -1;
    private int metadata = -1;
    private ResourceLocation blockLocation = null;

    public int getBlockId() {
        if (this.blockId >= 0) return this.blockId;
        this.blockId = Block.getIdFromBlock(this.getBlock());
        return this.blockId;
    }

    public int getBlockStateId() {
        if (this.blockStateId >= 0) return this.blockStateId;
        this.blockStateId = Block.getStateId(this);
        return this.blockStateId;
    }

    public int getMetadata() {
        if (this.metadata >= 0) return this.metadata;
        this.metadata = this.getBlock().getMetaFromState(this);
        return this.metadata;
    }

    public ResourceLocation getBlockLocation() {
        if (this.blockLocation != null) return this.blockLocation;
        this.blockLocation = (ResourceLocation)Block.blockRegistry.getNameForObject(this.getBlock());
        return this.blockLocation;
    }

    public IBlockState cycleProperty(IProperty property) {
        return this.withProperty(property, (Comparable)BlockStateBase.cyclePropertyValue(property.getAllowedValues(), this.getValue(property)));
    }

    protected static Object cyclePropertyValue(Collection values, Object currentValue) {
        Iterator iterator = values.iterator();
        do {
            if (!iterator.hasNext()) return iterator.next();
        } while (!iterator.next().equals(currentValue));
        if (!iterator.hasNext()) return values.iterator().next();
        return iterator.next();
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(Block.blockRegistry.getNameForObject(this.getBlock()));
        if (this.getProperties().isEmpty()) return stringbuilder.toString();
        stringbuilder.append("[");
        COMMA_JOINER.appendTo(stringbuilder, Iterables.transform(this.getProperties().entrySet(), MAP_ENTRY_TO_STRING));
        stringbuilder.append("]");
        return stringbuilder.toString();
    }
}

