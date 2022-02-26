/*
 * Decompiled with CFR 0.152.
 */
package optfine;

import java.util.Properties;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import optfine.Blender;
import optfine.Config;
import optfine.TextureUtils;

public class CustomSkyLayer {
    public String source = null;
    private int startFadeIn = -1;
    private int endFadeIn = -1;
    private int startFadeOut = -1;
    private int endFadeOut = -1;
    private int blend = 1;
    private boolean rotate = false;
    private float speed = 1.0f;
    private float[] axis = DEFAULT_AXIS;
    public int textureId = -1;
    public static final float[] DEFAULT_AXIS = new float[]{1.0f, 0.0f, 0.0f};

    public CustomSkyLayer(Properties p_i31_1_, String p_i31_2_) {
        this.source = p_i31_1_.getProperty("source", p_i31_2_);
        this.startFadeIn = this.parseTime(p_i31_1_.getProperty("startFadeIn"));
        this.endFadeIn = this.parseTime(p_i31_1_.getProperty("endFadeIn"));
        this.startFadeOut = this.parseTime(p_i31_1_.getProperty("startFadeOut"));
        this.endFadeOut = this.parseTime(p_i31_1_.getProperty("endFadeOut"));
        this.blend = Blender.parseBlend(p_i31_1_.getProperty("blend"));
        this.rotate = this.parseBoolean(p_i31_1_.getProperty("rotate"), true);
        this.speed = this.parseFloat(p_i31_1_.getProperty("speed"), 1.0f);
        this.axis = this.parseAxis(p_i31_1_.getProperty("axis"), DEFAULT_AXIS);
    }

    private int parseTime(String p_parseTime_1_) {
        if (p_parseTime_1_ == null) {
            return -1;
        }
        String[] astring = Config.tokenize(p_parseTime_1_, ":");
        if (astring.length != 2) {
            Config.warn("Invalid time: " + p_parseTime_1_);
            return -1;
        }
        String s = astring[0];
        String s1 = astring[1];
        int i = Config.parseInt(s, -1);
        int j = Config.parseInt(s1, -1);
        if (i >= 0 && i <= 23 && j >= 0 && j <= 59) {
            if ((i -= 6) >= 0) return i * 1000 + (int)((double)j / 60.0 * 1000.0);
            i += 24;
            return i * 1000 + (int)((double)j / 60.0 * 1000.0);
        }
        Config.warn("Invalid time: " + p_parseTime_1_);
        return -1;
    }

    private boolean parseBoolean(String p_parseBoolean_1_, boolean p_parseBoolean_2_) {
        if (p_parseBoolean_1_ == null) {
            return p_parseBoolean_2_;
        }
        if (p_parseBoolean_1_.toLowerCase().equals("true")) {
            return true;
        }
        if (p_parseBoolean_1_.toLowerCase().equals("false")) {
            return false;
        }
        Config.warn("Unknown boolean: " + p_parseBoolean_1_);
        return p_parseBoolean_2_;
    }

    private float parseFloat(String p_parseFloat_1_, float p_parseFloat_2_) {
        if (p_parseFloat_1_ == null) {
            return p_parseFloat_2_;
        }
        float f = Config.parseFloat(p_parseFloat_1_, Float.MIN_VALUE);
        if (f != Float.MIN_VALUE) return f;
        Config.warn("Invalid value: " + p_parseFloat_1_);
        return p_parseFloat_2_;
    }

    private float[] parseAxis(String p_parseAxis_1_, float[] p_parseAxis_2_) {
        if (p_parseAxis_1_ == null) {
            return p_parseAxis_2_;
        }
        String[] astring = Config.tokenize(p_parseAxis_1_, " ");
        if (astring.length != 3) {
            Config.warn("Invalid axis: " + p_parseAxis_1_);
            return p_parseAxis_2_;
        }
        float[] afloat = new float[3];
        for (int i = 0; i < astring.length; ++i) {
            afloat[i] = Config.parseFloat(astring[i], Float.MIN_VALUE);
            if (afloat[i] == Float.MIN_VALUE) {
                Config.warn("Invalid axis: " + p_parseAxis_1_);
                return p_parseAxis_2_;
            }
            if (!(afloat[i] < -1.0f) && !(afloat[i] > 1.0f)) continue;
            Config.warn("Invalid axis values: " + p_parseAxis_1_);
            return p_parseAxis_2_;
        }
        float f2 = afloat[0];
        float f = afloat[1];
        float f1 = afloat[2];
        if (!(f2 * f2 + f * f + f1 * f1 < 1.0E-5f)) return new float[]{f1, f, -f2};
        Config.warn("Invalid axis values: " + p_parseAxis_1_);
        return p_parseAxis_2_;
    }

    public boolean isValid(String p_isValid_1_) {
        if (this.source == null) {
            Config.warn("No source texture: " + p_isValid_1_);
            return false;
        }
        this.source = TextureUtils.fixResourcePath(this.source, TextureUtils.getBasePath(p_isValid_1_));
        if (this.startFadeIn >= 0 && this.endFadeIn >= 0 && this.endFadeOut >= 0) {
            int l;
            int k;
            int j;
            int i1;
            int i = this.normalizeTime(this.endFadeIn - this.startFadeIn);
            if (this.startFadeOut < 0) {
                this.startFadeOut = this.normalizeTime(this.endFadeOut - i);
            }
            if ((i1 = i + (j = this.normalizeTime(this.startFadeOut - this.endFadeIn)) + (k = this.normalizeTime(this.endFadeOut - this.startFadeOut)) + (l = this.normalizeTime(this.startFadeIn - this.endFadeOut))) != 24000) {
                Config.warn("Invalid fadeIn/fadeOut times, sum is more than 24h: " + i1);
                return false;
            }
            if (!(this.speed < 0.0f)) return true;
            Config.warn("Invalid speed: " + this.speed);
            return false;
        }
        Config.warn("Invalid times, required are: startFadeIn, endFadeIn and endFadeOut.");
        return false;
    }

    private int normalizeTime(int p_normalizeTime_1_) {
        while (true) {
            if (p_normalizeTime_1_ < 24000) {
                while (p_normalizeTime_1_ < 0) {
                    p_normalizeTime_1_ += 24000;
                }
                return p_normalizeTime_1_;
            }
            p_normalizeTime_1_ -= 24000;
        }
    }

    public void render(int p_render_1_, float p_render_2_, float p_render_3_) {
        float f = p_render_3_ * this.getFadeBrightness(p_render_1_);
        if (!((f = Config.limit(f, 0.0f, 1.0f)) >= 1.0E-4f)) return;
        GlStateManager.bindTexture(this.textureId);
        Blender.setupBlend(this.blend, f);
        GlStateManager.pushMatrix();
        if (this.rotate) {
            GlStateManager.rotate(p_render_2_ * 360.0f * this.speed, this.axis[0], this.axis[1], this.axis[2]);
        }
        Tessellator tessellator = Tessellator.getInstance();
        GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(-90.0f, 0.0f, 0.0f, 1.0f);
        this.renderSide(tessellator, 4);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
        this.renderSide(tessellator, 1);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f);
        this.renderSide(tessellator, 0);
        GlStateManager.popMatrix();
        GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
        this.renderSide(tessellator, 5);
        GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
        this.renderSide(tessellator, 2);
        GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
        this.renderSide(tessellator, 3);
        GlStateManager.popMatrix();
    }

    private float getFadeBrightness(int p_getFadeBrightness_1_) {
        if (this.timeBetween(p_getFadeBrightness_1_, this.startFadeIn, this.endFadeIn)) {
            int k = this.normalizeTime(this.endFadeIn - this.startFadeIn);
            int l = this.normalizeTime(p_getFadeBrightness_1_ - this.startFadeIn);
            return (float)l / (float)k;
        }
        if (this.timeBetween(p_getFadeBrightness_1_, this.endFadeIn, this.startFadeOut)) {
            return 1.0f;
        }
        if (!this.timeBetween(p_getFadeBrightness_1_, this.startFadeOut, this.endFadeOut)) return 0.0f;
        int i = this.normalizeTime(this.endFadeOut - this.startFadeOut);
        int j = this.normalizeTime(p_getFadeBrightness_1_ - this.startFadeOut);
        return 1.0f - (float)j / (float)i;
    }

    private void renderSide(Tessellator p_renderSide_1_, int p_renderSide_2_) {
        WorldRenderer worldrenderer = p_renderSide_1_.getWorldRenderer();
        double d0 = (double)(p_renderSide_2_ % 3) / 3.0;
        double d1 = (double)(p_renderSide_2_ / 3) / 2.0;
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-100.0, -100.0, -100.0).tex(d0, d1).endVertex();
        worldrenderer.pos(-100.0, -100.0, 100.0).tex(d0, d1 + 0.5).endVertex();
        worldrenderer.pos(100.0, -100.0, 100.0).tex(d0 + 0.3333333333333333, d1 + 0.5).endVertex();
        worldrenderer.pos(100.0, -100.0, -100.0).tex(d0 + 0.3333333333333333, d1).endVertex();
        p_renderSide_1_.draw();
    }

    public boolean isActive(int p_isActive_1_) {
        if (this.timeBetween(p_isActive_1_, this.endFadeOut, this.startFadeIn)) return false;
        return true;
    }

    private boolean timeBetween(int p_timeBetween_1_, int p_timeBetween_2_, int p_timeBetween_3_) {
        if (p_timeBetween_2_ <= p_timeBetween_3_) {
            if (p_timeBetween_1_ < p_timeBetween_2_) return false;
            if (p_timeBetween_1_ > p_timeBetween_3_) return false;
            return true;
        }
        if (p_timeBetween_1_ >= p_timeBetween_2_) return true;
        if (p_timeBetween_1_ <= p_timeBetween_3_) return true;
        return false;
    }
}

