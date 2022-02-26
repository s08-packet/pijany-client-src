/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerZoom
extends GenLayer {
    public GenLayerZoom(long p_i2134_1_, GenLayer p_i2134_3_) {
        super(p_i2134_1_);
        this.parent = p_i2134_3_;
    }

    @Override
    public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
        int i = areaX >> 1;
        int j = areaY >> 1;
        int k = (areaWidth >> 1) + 2;
        int l = (areaHeight >> 1) + 2;
        int[] aint = this.parent.getInts(i, j, k, l);
        int i1 = k - 1 << 1;
        int j1 = l - 1 << 1;
        int[] aint1 = IntCache.getIntCache(i1 * j1);
        for (int k1 = 0; k1 < l - 1; ++k1) {
            int i2;
            int l1 = (k1 << 1) * i1;
            int j2 = aint[i2 + 0 + (k1 + 0) * k];
            int k2 = aint[i2 + 0 + (k1 + 1) * k];
            for (i2 = 0; i2 < k - 1; ++i2) {
                this.initChunkSeed(i2 + i << 1, k1 + j << 1);
                int l2 = aint[i2 + 1 + (k1 + 0) * k];
                int i3 = aint[i2 + 1 + (k1 + 1) * k];
                aint1[l1] = j2;
                aint1[l1++ + i1] = this.selectRandom(j2, k2);
                aint1[l1] = this.selectRandom(j2, l2);
                aint1[l1++ + i1] = this.selectModeOrRandom(j2, l2, k2, i3);
                j2 = l2;
                k2 = i3;
            }
        }
        int[] aint2 = IntCache.getIntCache(areaWidth * areaHeight);
        int j3 = 0;
        while (j3 < areaHeight) {
            System.arraycopy(aint1, (j3 + (areaY & 1)) * i1 + (areaX & 1), aint2, j3 * areaWidth, areaWidth);
            ++j3;
        }
        return aint2;
    }

    public static GenLayer magnify(long p_75915_0_, GenLayer p_75915_2_, int p_75915_3_) {
        GenLayer genlayer = p_75915_2_;
        int i = 0;
        while (i < p_75915_3_) {
            genlayer = new GenLayerZoom(p_75915_0_ + (long)i, genlayer);
            ++i;
        }
        return genlayer;
    }
}

