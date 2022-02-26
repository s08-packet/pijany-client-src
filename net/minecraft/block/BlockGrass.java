/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block;

import drunkclient.beta.Client;
import drunkclient.beta.IMPL.Module.impl.render.Xray;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;

public class BlockGrass
extends Block
implements IGrowable {
    public static final PropertyBool SNOWY = PropertyBool.create("snowy");

    protected BlockGrass() {
        super(Material.grass);
        this.setDefaultState(this.blockState.getBaseState().withProperty(SNOWY, false));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        boolean bl;
        Block block = worldIn.getBlockState(pos.up()).getBlock();
        if (block != Blocks.snow && block != Blocks.snow_layer) {
            bl = false;
            return state.withProperty(SNOWY, bl);
        }
        bl = true;
        return state.withProperty(SNOWY, bl);
    }

    @Override
    public int getBlockColor() {
        return ColorizerGrass.getGrassColor(0.5, 1.0);
    }

    @Override
    public int getRenderColor(IBlockState state) {
        return this.getBlockColor();
    }

    @Override
    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
        return BiomeColorHelper.getGrassColorAtPos(worldIn, pos);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.isRemote) return;
        if (worldIn.getLightFromNeighbors(pos.up()) < 4 && worldIn.getBlockState(pos.up()).getBlock().getLightOpacity() > 2) {
            worldIn.setBlockState(pos, Blocks.dirt.getDefaultState());
            return;
        }
        if (worldIn.getLightFromNeighbors(pos.up()) < 9) return;
        int i = 0;
        while (i < 4) {
            BlockPos blockpos = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);
            Block block = worldIn.getBlockState(blockpos.up()).getBlock();
            IBlockState iblockstate = worldIn.getBlockState(blockpos);
            if (iblockstate.getBlock() == Blocks.dirt && iblockstate.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT && worldIn.getLightFromNeighbors(blockpos.up()) >= 4 && block.getLightOpacity() <= 2) {
                worldIn.setBlockState(blockpos, Blocks.grass.getDefaultState());
            }
            ++i;
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Blocks.dirt.getItemDropped(Blocks.dirt.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), rand, fortune);
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        BlockPos blockpos = pos.up();
        int i = 0;
        while (i < 128) {
            BlockPos blockpos1 = blockpos;
            int j = 0;
            while (true) {
                if (j >= i / 16) {
                    if (worldIn.getBlockState((BlockPos)blockpos1).getBlock().blockMaterial != Material.air) break;
                    if (rand.nextInt(8) == 0) {
                        IBlockState iblockstate;
                        BlockFlower.EnumFlowerType blockflower$enumflowertype = worldIn.getBiomeGenForCoords(blockpos1).pickRandomFlower(rand, blockpos1);
                        BlockFlower blockflower = blockflower$enumflowertype.getBlockType().getBlock();
                        if (!blockflower.canBlockStay(worldIn, blockpos1, iblockstate = blockflower.getDefaultState().withProperty(blockflower.getTypeProperty(), blockflower$enumflowertype))) break;
                        worldIn.setBlockState(blockpos1, iblockstate, 3);
                        break;
                    }
                    IBlockState iblockstate1 = Blocks.tallgrass.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
                    if (!Blocks.tallgrass.canBlockStay(worldIn, blockpos1, iblockstate1)) break;
                    worldIn.setBlockState(blockpos1, iblockstate1, 3);
                    break;
                }
                if (worldIn.getBlockState((blockpos1 = blockpos1.add(rand.nextInt(3) - 1, (rand.nextInt(3) - 1) * rand.nextInt(3) / 2, rand.nextInt(3) - 1)).down()).getBlock() != Blocks.grass || worldIn.getBlockState(blockpos1).getBlock().isNormalCube()) break;
                ++j;
            }
            ++i;
        }
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        if (!Client.instance.getModuleManager().getModuleByClass(Xray.class).isEnabled()) return EnumWorldBlockLayer.CUTOUT_MIPPED;
        return EnumWorldBlockLayer.TRANSLUCENT;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, SNOWY);
    }
}

