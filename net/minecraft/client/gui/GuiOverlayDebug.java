/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.Display
 *  org.lwjgl.opengl.GL11
 */
package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk;
import optfine.Reflector;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class GuiOverlayDebug
extends Gui {
    private final Minecraft mc;
    private final FontRenderer fontRenderer;
    private static final String __OBFID = "CL_00001956";

    public GuiOverlayDebug(Minecraft mc) {
        this.mc = mc;
        this.fontRenderer = mc.fontRendererObj;
    }

    public void renderDebugInfo(ScaledResolution scaledResolutionIn) {
        this.mc.mcProfiler.startSection("debug");
        GlStateManager.pushMatrix();
        this.renderDebugInfoLeft();
        this.renderDebugInfoRight(scaledResolutionIn);
        GlStateManager.popMatrix();
        this.mc.mcProfiler.endSection();
    }

    private boolean isReducedDebug() {
        if (Minecraft.thePlayer.hasReducedDebug()) return true;
        if (this.mc.gameSettings.reducedDebugInfo) return true;
        return false;
    }

    protected void renderDebugInfoLeft() {
        List list = this.call();
        int i = 0;
        while (i < list.size()) {
            String s = (String)list.get(i);
            if (!Strings.isNullOrEmpty(s)) {
                int j = this.fontRenderer.FONT_HEIGHT;
                int k = this.fontRenderer.getStringWidth(s);
                boolean flag = true;
                int l = 2 + j * i;
                GuiOverlayDebug.drawRect(1.0f, l - 1, 2 + k + 1, l + j - 1, -1873784752);
                this.fontRenderer.drawString(s, 2.0f, l, 0xE0E0E0);
            }
            ++i;
        }
    }

    protected void renderDebugInfoRight(ScaledResolution p_175239_1_) {
        List list = this.getDebugInfoRight();
        int i = 0;
        while (i < list.size()) {
            String s = (String)list.get(i);
            if (!Strings.isNullOrEmpty(s)) {
                int j = this.fontRenderer.FONT_HEIGHT;
                int k = this.fontRenderer.getStringWidth(s);
                int l = p_175239_1_.getScaledWidth() - 2 - k;
                int i1 = 2 + j * i;
                GuiOverlayDebug.drawRect(l - 1, i1 - 1, l + k + 1, i1 + j - 1, -1873784752);
                this.fontRenderer.drawString(s, l, i1, 0xE0E0E0);
            }
            ++i;
        }
    }

    protected List call() {
        BlockPos blockpos = new BlockPos(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ);
        if (this.isReducedDebug()) {
            return Lists.newArrayList("Minecraft 1.8.8 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.theWorld.getDebugLoadedEntities(), this.mc.theWorld.getProviderName(), "", String.format("Chunk-relative: %d %d %d", blockpos.getX() & 0xF, blockpos.getY() & 0xF, blockpos.getZ() & 0xF));
        }
        Entity entity = this.mc.getRenderViewEntity();
        EnumFacing enumfacing = entity.getHorizontalFacing();
        String s = "Invalid";
        switch (GuiOverlayDebug.1.field_178907_a[enumfacing.ordinal()]) {
            case 1: {
                s = "Towards negative Z";
                break;
            }
            case 2: {
                s = "Towards positive Z";
                break;
            }
            case 3: {
                s = "Towards negative X";
                break;
            }
            case 4: {
                s = "Towards positive X";
                break;
            }
        }
        ArrayList<String> arraylist = Lists.newArrayList("Minecraft 1.8.8 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.theWorld.getDebugLoadedEntities(), this.mc.theWorld.getProviderName(), "", String.format("XYZ: %.3f / %.5f / %.3f", this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ), String.format("Block: %d %d %d", blockpos.getX(), blockpos.getY(), blockpos.getZ()), String.format("Chunk: %d %d %d in %d %d %d", blockpos.getX() & 0xF, blockpos.getY() & 0xF, blockpos.getZ() & 0xF, blockpos.getX() >> 4, blockpos.getY() >> 4, blockpos.getZ() >> 4), String.format("Facing: %s (%s) (%.1f / %.1f)", enumfacing, s, Float.valueOf(MathHelper.wrapAngleTo180_float(entity.rotationYaw)), Float.valueOf(MathHelper.wrapAngleTo180_float(entity.rotationPitch))));
        if (this.mc.theWorld != null && this.mc.theWorld.isBlockLoaded(blockpos)) {
            Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(blockpos);
            arraylist.add("Biome: " + chunk.getBiome((BlockPos)blockpos, (WorldChunkManager)this.mc.theWorld.getWorldChunkManager()).biomeName);
            arraylist.add("Light: " + chunk.getLightSubtracted(blockpos, 0) + " (" + chunk.getLightFor(EnumSkyBlock.SKY, blockpos) + " sky, " + chunk.getLightFor(EnumSkyBlock.BLOCK, blockpos) + " block)");
            DifficultyInstance difficultyinstance = this.mc.theWorld.getDifficultyForLocation(blockpos);
            if (this.mc.isIntegratedServerRunning() && this.mc.getIntegratedServer() != null) {
                EntityPlayerMP entityplayermp = this.mc.getIntegratedServer().getConfigurationManager().getPlayerByUUID(Minecraft.thePlayer.getUniqueID());
                if (entityplayermp != null) {
                    difficultyinstance = entityplayermp.worldObj.getDifficultyForLocation(new BlockPos(entityplayermp));
                }
            }
            arraylist.add(String.format("Local Difficulty: %.2f (Day %d)", Float.valueOf(difficultyinstance.getAdditionalDifficulty()), this.mc.theWorld.getWorldTime() / 24000L));
        }
        if (this.mc.entityRenderer != null && this.mc.entityRenderer.isShaderActive()) {
            arraylist.add("Shader: " + this.mc.entityRenderer.getShaderGroup().getShaderGroupName());
        }
        if (this.mc.objectMouseOver == null) return arraylist;
        if (this.mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return arraylist;
        if (this.mc.objectMouseOver.getBlockPos() == null) return arraylist;
        BlockPos blockpos1 = this.mc.objectMouseOver.getBlockPos();
        arraylist.add(String.format("Looking at: %d %d %d", blockpos1.getX(), blockpos1.getY(), blockpos1.getZ()));
        return arraylist;
    }

    protected List getDebugInfoRight() {
        long i = Runtime.getRuntime().maxMemory();
        long j = Runtime.getRuntime().totalMemory();
        long k = Runtime.getRuntime().freeMemory();
        long l = j - k;
        ArrayList<String> arraylist = Lists.newArrayList(String.format("Java: %s %dbit", System.getProperty("java.version"), this.mc.isJava64bit() ? 64 : 32), String.format("Mem: % 2d%% %03d/%03dMB", l * 100L / i, GuiOverlayDebug.bytesToMb(l), GuiOverlayDebug.bytesToMb(i)), String.format("Allocated: % 2d%% %03dMB", j * 100L / i, GuiOverlayDebug.bytesToMb(j)), "", String.format("CPU: %s", OpenGlHelper.func_183029_j()), "", String.format("Display: %dx%d (%s)", Display.getWidth(), Display.getHeight(), GL11.glGetString((int)7936)), GL11.glGetString((int)7937), GL11.glGetString((int)7938));
        if (Reflector.FMLCommonHandler_getBrandings.exists()) {
            Object object = Reflector.call(Reflector.FMLCommonHandler_instance, new Object[0]);
            arraylist.add("");
            arraylist.addAll((Collection)Reflector.call(object, Reflector.FMLCommonHandler_getBrandings, false));
        }
        if (this.isReducedDebug()) {
            return arraylist;
        }
        if (this.mc.objectMouseOver == null) return arraylist;
        if (this.mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return arraylist;
        if (this.mc.objectMouseOver.getBlockPos() == null) return arraylist;
        BlockPos blockpos = this.mc.objectMouseOver.getBlockPos();
        IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
        if (this.mc.theWorld.getWorldType() != WorldType.DEBUG_WORLD) {
            iblockstate = iblockstate.getBlock().getActualState(iblockstate, this.mc.theWorld, blockpos);
        }
        arraylist.add("");
        arraylist.add(String.valueOf(Block.blockRegistry.getNameForObject(iblockstate.getBlock())));
        Iterator iterator = ((ImmutableSet)iblockstate.getProperties().entrySet()).iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            String s = ((Comparable)entry.getValue()).toString();
            if (entry.getValue() == Boolean.TRUE) {
                s = (Object)((Object)EnumChatFormatting.GREEN) + s;
            } else if (entry.getValue() == Boolean.FALSE) {
                s = (Object)((Object)EnumChatFormatting.RED) + s;
            }
            arraylist.add(((IProperty)entry.getKey()).getName() + ": " + s);
        }
        return arraylist;
    }

    private void func_181554_e() {
        GlStateManager.disableDepth();
        FrameTimer frametimer = this.mc.func_181539_aj();
        int i = frametimer.func_181749_a();
        int j = frametimer.func_181750_b();
        long[] along = frametimer.func_181746_c();
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        int k = i;
        int l = 0;
        GuiOverlayDebug.drawRect(0.0f, scaledresolution.getScaledHeight() - 60, 240.0f, scaledresolution.getScaledHeight(), -1873784752);
        while (k != j) {
            int i1 = frametimer.func_181748_a(along[k], 30);
            int j1 = this.func_181552_c(MathHelper.clamp_int(i1, 0, 60), 0, 30, 60);
            this.drawVerticalLine(l, scaledresolution.getScaledHeight(), scaledresolution.getScaledHeight() - i1, j1);
            ++l;
            k = frametimer.func_181751_b(k + 1);
        }
        GuiOverlayDebug.drawRect(1.0f, scaledresolution.getScaledHeight() - 30 + 1, 14.0f, scaledresolution.getScaledHeight() - 30 + 10, -1873784752);
        this.fontRenderer.drawString("60", 2.0f, scaledresolution.getScaledHeight() - 30 + 2, 0xE0E0E0);
        this.drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 30, -1);
        GuiOverlayDebug.drawRect(1.0f, scaledresolution.getScaledHeight() - 60 + 1, 14.0f, scaledresolution.getScaledHeight() - 60 + 10, -1873784752);
        this.fontRenderer.drawString("30", 2.0f, scaledresolution.getScaledHeight() - 60 + 2, 0xE0E0E0);
        this.drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 60, -1);
        this.drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 1, -1);
        this.drawVerticalLine(0, scaledresolution.getScaledHeight() - 60, scaledresolution.getScaledHeight(), -1);
        this.drawVerticalLine(239, scaledresolution.getScaledHeight() - 60, scaledresolution.getScaledHeight(), -1);
        if (this.mc.gameSettings.limitFramerate <= 120) {
            this.drawHorizontalLine(0, 239, scaledresolution.getScaledHeight() - 60 + this.mc.gameSettings.limitFramerate / 2, -16711681);
        }
        GlStateManager.enableDepth();
    }

    private int func_181552_c(int p_181552_1_, int p_181552_2_, int p_181552_3_, int p_181552_4_) {
        int n;
        if (p_181552_1_ < p_181552_3_) {
            n = this.func_181553_a(-16711936, -256, (float)p_181552_1_ / (float)p_181552_3_);
            return n;
        }
        n = this.func_181553_a(-256, -65536, (float)(p_181552_1_ - p_181552_3_) / (float)(p_181552_4_ - p_181552_3_));
        return n;
    }

    private int func_181553_a(int p_181553_1_, int p_181553_2_, float p_181553_3_) {
        int i = p_181553_1_ >> 24 & 0xFF;
        int j = p_181553_1_ >> 16 & 0xFF;
        int k = p_181553_1_ >> 8 & 0xFF;
        int l = p_181553_1_ & 0xFF;
        int i1 = p_181553_2_ >> 24 & 0xFF;
        int j1 = p_181553_2_ >> 16 & 0xFF;
        int k1 = p_181553_2_ >> 8 & 0xFF;
        int l1 = p_181553_2_ & 0xFF;
        int i2 = MathHelper.clamp_int((int)((float)i + (float)(i1 - i) * p_181553_3_), 0, 255);
        int j2 = MathHelper.clamp_int((int)((float)j + (float)(j1 - j) * p_181553_3_), 0, 255);
        int k2 = MathHelper.clamp_int((int)((float)k + (float)(k1 - k) * p_181553_3_), 0, 255);
        int l2 = MathHelper.clamp_int((int)((float)l + (float)(l1 - l) * p_181553_3_), 0, 255);
        return i2 << 24 | j2 << 16 | k2 << 8 | l2;
    }

    private static long bytesToMb(long bytes) {
        return bytes / 1024L / 1024L;
    }
}

