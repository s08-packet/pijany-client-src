/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderDebug;
import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.FlatGeneratorInfo;

public abstract class WorldProvider {
    public static final float[] moonPhaseFactors = new float[]{1.0f, 0.75f, 0.5f, 0.25f, 0.0f, 0.25f, 0.5f, 0.75f};
    protected World worldObj;
    private WorldType terrainType;
    private String generatorSettings;
    protected WorldChunkManager worldChunkMgr;
    protected boolean isHellWorld;
    protected boolean hasNoSky;
    protected final float[] lightBrightnessTable = new float[16];
    protected int dimensionId;
    private final float[] colorsSunriseSunset = new float[4];

    public final void registerWorld(World worldIn) {
        this.worldObj = worldIn;
        this.terrainType = worldIn.getWorldInfo().getTerrainType();
        this.generatorSettings = worldIn.getWorldInfo().getGeneratorOptions();
        this.registerWorldChunkManager();
        this.generateLightBrightnessTable();
    }

    protected void generateLightBrightnessTable() {
        float f = 0.0f;
        int i = 0;
        while (i <= 15) {
            float f1 = 1.0f - (float)i / 15.0f;
            this.lightBrightnessTable[i] = (1.0f - f1) / (f1 * 3.0f + 1.0f) * (1.0f - f) + f;
            ++i;
        }
    }

    protected void registerWorldChunkManager() {
        WorldType worldtype = this.worldObj.getWorldInfo().getTerrainType();
        if (worldtype == WorldType.FLAT) {
            FlatGeneratorInfo flatgeneratorinfo = FlatGeneratorInfo.createFlatGeneratorFromString(this.worldObj.getWorldInfo().getGeneratorOptions());
            this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.getBiomeFromBiomeList(flatgeneratorinfo.getBiome(), BiomeGenBase.field_180279_ad), 0.5f);
            return;
        }
        if (worldtype == WorldType.DEBUG_WORLD) {
            this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.plains, 0.0f);
            return;
        }
        this.worldChunkMgr = new WorldChunkManager(this.worldObj);
    }

    public IChunkProvider createChunkGenerator() {
        IChunkProvider iChunkProvider;
        if (this.terrainType == WorldType.FLAT) {
            iChunkProvider = new ChunkProviderFlat(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings);
            return iChunkProvider;
        }
        if (this.terrainType == WorldType.DEBUG_WORLD) {
            iChunkProvider = new ChunkProviderDebug(this.worldObj);
            return iChunkProvider;
        }
        if (this.terrainType == WorldType.CUSTOMIZED) {
            iChunkProvider = new ChunkProviderGenerate(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings);
            return iChunkProvider;
        }
        iChunkProvider = new ChunkProviderGenerate(this.worldObj, this.worldObj.getSeed(), this.worldObj.getWorldInfo().isMapFeaturesEnabled(), this.generatorSettings);
        return iChunkProvider;
    }

    public boolean canCoordinateBeSpawn(int x, int z) {
        if (this.worldObj.getGroundAboveSeaLevel(new BlockPos(x, 0, z)) != Blocks.grass) return false;
        return true;
    }

    public float calculateCelestialAngle(long p_76563_1_, float p_76563_3_) {
        int i = (int)(p_76563_1_ % 24000L);
        float f = ((float)i + p_76563_3_) / 24000.0f - 0.25f;
        if (f < 0.0f) {
            f += 1.0f;
        }
        if (f > 1.0f) {
            f -= 1.0f;
        }
        f = 1.0f - (float)((Math.cos((double)f * Math.PI) + 1.0) / 2.0);
        f += (f - f) / 3.0f;
        return f;
    }

    public int getMoonPhase(long p_76559_1_) {
        return (int)(p_76559_1_ / 24000L % 8L + 8L) % 8;
    }

    public boolean isSurfaceWorld() {
        return true;
    }

    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        float f2;
        float f = 0.4f;
        float f1 = MathHelper.cos(celestialAngle * (float)Math.PI * 2.0f) - 0.0f;
        if (!(f1 >= (f2 = -0.0f) - f)) return null;
        if (!(f1 <= f2 + f)) return null;
        float f3 = (f1 - f2) / f * 0.5f + 0.5f;
        float f4 = 1.0f - (1.0f - MathHelper.sin(f3 * (float)Math.PI)) * 0.99f;
        f4 *= f4;
        this.colorsSunriseSunset[0] = f3 * 0.3f + 0.7f;
        this.colorsSunriseSunset[1] = f3 * f3 * 0.7f + 0.2f;
        this.colorsSunriseSunset[2] = f3 * f3 * 0.0f + 0.2f;
        this.colorsSunriseSunset[3] = f4;
        return this.colorsSunriseSunset;
    }

    public Vec3 getFogColor(float p_76562_1_, float p_76562_2_) {
        float f = MathHelper.cos(p_76562_1_ * (float)Math.PI * 2.0f) * 2.0f + 0.5f;
        f = MathHelper.clamp_float(f, 0.0f, 1.0f);
        return new Vec3(f1 *= f * 0.94f + 0.06f, f2 *= f * 0.94f + 0.06f, f3 *= f * 0.91f + 0.09f);
    }

    public boolean canRespawnHere() {
        return true;
    }

    public static WorldProvider getProviderForDimension(int dimension) {
        WorldProvider worldProvider;
        if (dimension == -1) {
            worldProvider = new WorldProviderHell();
            return worldProvider;
        }
        if (dimension == 0) {
            worldProvider = new WorldProviderSurface();
            return worldProvider;
        }
        if (dimension != 1) return null;
        worldProvider = new WorldProviderEnd();
        return worldProvider;
    }

    public float getCloudHeight() {
        return 128.0f;
    }

    public boolean isSkyColored() {
        return true;
    }

    public BlockPos getSpawnCoordinate() {
        return null;
    }

    public int getAverageGroundLevel() {
        if (this.terrainType == WorldType.FLAT) {
            return 4;
        }
        int n = this.worldObj.func_181545_F() + 1;
        return n;
    }

    public double getVoidFogYFactor() {
        if (this.terrainType != WorldType.FLAT) return 0.03125;
        return 1.0;
    }

    public boolean doesXZShowFog(int x, int z) {
        return false;
    }

    public abstract String getDimensionName();

    public abstract String getInternalNameSuffix();

    public WorldChunkManager getWorldChunkManager() {
        return this.worldChunkMgr;
    }

    public boolean doesWaterVaporize() {
        return this.isHellWorld;
    }

    public boolean getHasNoSky() {
        return this.hasNoSky;
    }

    public float[] getLightBrightnessTable() {
        return this.lightBrightnessTable;
    }

    public int getDimensionId() {
        return this.dimensionId;
    }

    public WorldBorder getWorldBorder() {
        return new WorldBorder();
    }
}

