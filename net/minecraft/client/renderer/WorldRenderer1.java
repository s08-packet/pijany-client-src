/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.google.common.primitives.Floats;
import java.util.Comparator;
import net.minecraft.client.renderer.WorldRenderer;

class WorldRenderer1
implements Comparator {
    final float[] field_181659_a;
    final WorldRenderer field_181660_b;

    WorldRenderer1(WorldRenderer p_i46500_1_, float[] p_i46500_2_) {
        this.field_181660_b = p_i46500_1_;
        this.field_181659_a = p_i46500_2_;
    }

    public int compare(Integer p_compare_1_, Integer p_compare_2_) {
        return Floats.compare(this.field_181659_a[p_compare_2_], this.field_181659_a[p_compare_1_]);
    }

    public int compare(Object p_compare_1_, Object p_compare_2_) {
        return this.compare((Integer)p_compare_1_, (Integer)p_compare_2_);
    }
}

