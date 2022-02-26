/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block;

import drunkclient.beta.Client;
import drunkclient.beta.IMPL.Module.impl.render.Xray;
import java.util.Random;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;

public abstract class BlockLeaves
extends BlockLeavesBase {
    public static final PropertyBool DECAYABLE = PropertyBool.create("decayable");
    public static final PropertyBool CHECK_DECAY = PropertyBool.create("check_decay");
    int[] surroundings;
    protected int iconIndex;
    protected boolean isTransparent;

    public BlockLeaves() {
        super(Material.leaves, false);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setHardness(0.2f);
        this.setLightOpacity(1);
        this.setStepSound(soundTypeGrass);
    }

    @Override
    public int getBlockColor() {
        return ColorizerFoliage.getFoliageColor(0.5, 1.0);
    }

    @Override
    public int getRenderColor(IBlockState state) {
        return ColorizerFoliage.getFoliageColorBasic();
    }

    @Override
    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
        return BiomeColorHelper.getFoliageColorAtPos(worldIn, pos);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        int i1;
        int l;
        int i = 1;
        int j = i + 1;
        int k = pos.getX();
        if (!worldIn.isAreaLoaded(new BlockPos(k - j, (l = pos.getY()) - j, (i1 = pos.getZ()) - j), new BlockPos(k + j, l + j, i1 + j))) return;
        int j1 = -i;
        while (j1 <= i) {
            for (int k1 = -i; k1 <= i; ++k1) {
                for (int l1 = -i; l1 <= i; ++l1) {
                    BlockPos blockpos = pos.add(j1, k1, l1);
                    IBlockState iblockstate = worldIn.getBlockState(blockpos);
                    if (iblockstate.getBlock().getMaterial() != Material.leaves || iblockstate.getValue(CHECK_DECAY).booleanValue()) continue;
                    worldIn.setBlockState(blockpos, iblockstate.withProperty(CHECK_DECAY, true), 4);
                }
            }
            ++j1;
        }
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote != false) return;
        if (state.getValue(BlockLeaves.CHECK_DECAY) == false) return;
        if (state.getValue(BlockLeaves.DECAYABLE) == false) return;
        i = 4;
        j = i + 1;
        k = pos.getX();
        l = pos.getY();
        i1 = pos.getZ();
        j1 = 32;
        k1 = j1 * j1;
        l1 = j1 / 2;
        if (this.surroundings == null) {
            this.surroundings = new int[j1 * j1 * j1];
        }
        if (!worldIn.isAreaLoaded(new BlockPos(k - j, l - j, i1 - j), new BlockPos(k + j, l + j, i1 + j))) ** GOTO lbl40
        blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        i2 = -i;
        block0: while (true) {
            if (i2 > i) break;
            j2 = -i;
            while (true) {
                if (j2 <= i) {
                } else {
                    ++i2;
                    continue block0;
                }
                for (k2 = -i; k2 <= i; ++k2) {
                    block = worldIn.getBlockState(blockpos$mutableblockpos.func_181079_c(k + i2, l + j2, i1 + k2)).getBlock();
                    if (block != Blocks.log && block != Blocks.log2) {
                        if (block.getMaterial() == Material.leaves) {
                            this.surroundings[(i2 + l1) * k1 + (j2 + l1) * j1 + k2 + l1] = -2;
                            continue;
                        }
                        this.surroundings[(i2 + l1) * k1 + (j2 + l1) * j1 + k2 + l1] = -1;
                        continue;
                    }
                    this.surroundings[(i2 + l1) * k1 + (j2 + l1) * j1 + k2 + l1] = 0;
                }
                ++j2;
            }
            break;
        }
        i3 = 1;
        block3: while (true) {
            block23: {
                if (i3 <= 4) break block23;
lbl40:
                // 2 sources

                if ((l2 = this.surroundings[l1 * k1 + l1 * j1 + l1]) >= 0) {
                    worldIn.setBlockState(pos, state.withProperty(BlockLeaves.CHECK_DECAY, false), 4);
                    return;
                }
                this.destroy(worldIn, pos);
                return;
            }
            j3 = -i;
            while (true) {
                if (j3 <= i) {
                } else {
                    ++i3;
                    continue block3;
                }
                for (k3 = -i; k3 <= i; ++k3) {
                    for (l3 = -i; l3 <= i; ++l3) {
                        if (this.surroundings[(j3 + l1) * k1 + (k3 + l1) * j1 + l3 + l1] != i3 - 1) continue;
                        if (this.surroundings[(j3 + l1 - 1) * k1 + (k3 + l1) * j1 + l3 + l1] == -2) {
                            this.surroundings[(j3 + l1 - 1) * k1 + (k3 + l1) * j1 + l3 + l1] = i3;
                        }
                        if (this.surroundings[(j3 + l1 + 1) * k1 + (k3 + l1) * j1 + l3 + l1] == -2) {
                            this.surroundings[(j3 + l1 + 1) * k1 + (k3 + l1) * j1 + l3 + l1] = i3;
                        }
                        if (this.surroundings[(j3 + l1) * k1 + (k3 + l1 - 1) * j1 + l3 + l1] == -2) {
                            this.surroundings[(j3 + l1) * k1 + (k3 + l1 - 1) * j1 + l3 + l1] = i3;
                        }
                        if (this.surroundings[(j3 + l1) * k1 + (k3 + l1 + 1) * j1 + l3 + l1] == -2) {
                            this.surroundings[(j3 + l1) * k1 + (k3 + l1 + 1) * j1 + l3 + l1] = i3;
                        }
                        if (this.surroundings[(j3 + l1) * k1 + (k3 + l1) * j1 + (l3 + l1 - 1)] == -2) {
                            this.surroundings[(j3 + l1) * k1 + (k3 + l1) * j1 + (l3 + l1 - 1)] = i3;
                        }
                        if (this.surroundings[(j3 + l1) * k1 + (k3 + l1) * j1 + l3 + l1 + 1] != -2) continue;
                        this.surroundings[(j3 + l1) * k1 + (k3 + l1) * j1 + l3 + l1 + 1] = i3;
                    }
                }
                ++j3;
            }
            break;
        }
    }

    @Override
    public void randomDisplayTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.canLightningStrike(pos.up())) return;
        if (World.doesBlockHaveSolidTopSurface(worldIn, pos.down())) return;
        if (rand.nextInt(15) != 1) return;
        double d0 = (float)pos.getX() + rand.nextFloat();
        double d1 = (double)pos.getY() - 0.05;
        double d2 = (float)pos.getZ() + rand.nextFloat();
        worldIn.spawnParticle(EnumParticleTypes.DRIP_WATER, d0, d1, d2, 0.0, 0.0, 0.0, new int[0]);
    }

    private void destroy(World worldIn, BlockPos pos) {
        this.dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
        worldIn.setBlockToAir(pos);
    }

    @Override
    public int quantityDropped(Random random) {
        if (random.nextInt(20) != 0) return 0;
        return 1;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.sapling);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (worldIn.isRemote) return;
        int i = this.getSaplingDropChance(state);
        if (fortune > 0 && (i -= 2 << fortune) < 10) {
            i = 10;
        }
        if (worldIn.rand.nextInt(i) == 0) {
            Item item = this.getItemDropped(state, worldIn.rand, fortune);
            BlockLeaves.spawnAsEntity(worldIn, pos, new ItemStack(item, 1, this.damageDropped(state)));
        }
        i = 200;
        if (fortune > 0 && (i -= 10 << fortune) < 40) {
            i = 40;
        }
        this.dropApple(worldIn, pos, state, i);
    }

    protected void dropApple(World worldIn, BlockPos pos, IBlockState state, int chance) {
    }

    protected int getSaplingDropChance(IBlockState state) {
        return 20;
    }

    @Override
    public boolean isOpaqueCube() {
        if (this.fancyGraphics) return false;
        return true;
    }

    public void setGraphicsLevel(boolean fancy) {
        this.isTransparent = fancy;
        this.fancyGraphics = fancy;
        this.iconIndex = fancy ? 0 : 1;
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        EnumWorldBlockLayer enumWorldBlockLayer;
        if (Client.instance.getModuleManager().getModuleByClass(Xray.class).isEnabled()) {
            return EnumWorldBlockLayer.TRANSLUCENT;
        }
        if (this.isTransparent) {
            enumWorldBlockLayer = EnumWorldBlockLayer.CUTOUT_MIPPED;
            return enumWorldBlockLayer;
        }
        enumWorldBlockLayer = EnumWorldBlockLayer.SOLID;
        return enumWorldBlockLayer;
    }

    @Override
    public boolean isVisuallyOpaque() {
        return false;
    }

    public abstract BlockPlanks.EnumType getWoodType(int var1);
}

