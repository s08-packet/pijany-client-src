/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.data;

public class AnimationFrame {
    private final int frameIndex;
    private final int frameTime;

    public AnimationFrame(int p_i1307_1_) {
        this(p_i1307_1_, -1);
    }

    public AnimationFrame(int p_i1308_1_, int p_i1308_2_) {
        this.frameIndex = p_i1308_1_;
        this.frameTime = p_i1308_2_;
    }

    public boolean hasNoTime() {
        if (this.frameTime != -1) return false;
        return true;
    }

    public int getFrameTime() {
        return this.frameTime;
    }

    public int getFrameIndex() {
        return this.frameIndex;
    }
}

