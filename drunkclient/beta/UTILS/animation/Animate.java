/*
 * Decompiled with CFR 0.152.
 */
package drunkclient.beta.UTILS.animation;

import drunkclient.beta.UTILS.animation.Easing;

public class Animate {
    private float value = 0.0f;
    private float min = 0.0f;
    private float max = 1.0f;
    private float speed = 50.0f;
    private float time;
    private boolean reversed = false;
    private Easing ease = Easing.LINEAR;

    public void reset() {
        this.time = this.min;
    }

    public Animate update() {
        if (this.reversed) {
            if (this.time > this.min) {
                this.time -= 0.001f * this.speed;
            }
        } else if (this.time < this.max) {
            this.time += 0.001f * this.speed;
        }
        this.time = this.clamp(this.time, this.min, this.max);
        this.value = this.getEase().ease(this.time, this.min, this.max, this.max);
        return this;
    }

    public Animate setValue(float value) {
        this.value = value;
        return this;
    }

    public Animate setMin(float min) {
        this.min = min;
        return this;
    }

    public Animate setMax(float max) {
        this.max = max;
        return this;
    }

    public Animate setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public Animate setReversed(boolean reversed) {
        this.reversed = reversed;
        return this;
    }

    public Animate setEase(Easing ease) {
        this.ease = ease;
        return this;
    }

    public float getValue() {
        return this.value;
    }

    public float getMin() {
        return this.min;
    }

    public float getMax() {
        return this.max;
    }

    public float getSpeed() {
        return this.speed;
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public Easing getEase() {
        return this.ease;
    }

    private float clamp(float num, float min, float max) {
        float f;
        if (num < min) {
            f = min;
            return f;
        }
        if (num > max) {
            f = max;
            return f;
        }
        f = num;
        return f;
    }
}

