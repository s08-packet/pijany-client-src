/*
 * Decompiled with CFR 0.152.
 */
package optfine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import optfine.Config;

public class BetterSnow {
    private static IBakedModel modelSnowLayer = null;

    public static void update() {
        modelSnowLayer = Config.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(Blocks.snow_layer.getDefaultState());
    }

    public static IBakedModel getModelSnowLayer() {
        return modelSnowLayer;
    }

    public static IBlockState getStateSnowLayer() {
        return Blocks.snow_layer.getDefaultState();
    }

    public static boolean shouldRender(IBlockAccess p_shouldRender_0_, Block p_shouldRender_1_, IBlockState p_shouldRender_2_, BlockPos p_shouldRender_3_) {
        if (!BetterSnow.checkBlock(p_shouldRender_1_, p_shouldRender_2_)) {
            return false;
        }
        boolean bl = BetterSnow.hasSnowNeighbours(p_shouldRender_0_, p_shouldRender_3_);
        return bl;
    }

    private static boolean hasSnowNeighbours(IBlockAccess p_hasSnowNeighbours_0_, BlockPos p_hasSnowNeighbours_1_) {
        Block block = Blocks.snow_layer;
        if (p_hasSnowNeighbours_0_.getBlockState(p_hasSnowNeighbours_1_.north()).getBlock() != block && p_hasSnowNeighbours_0_.getBlockState(p_hasSnowNeighbours_1_.south()).getBlock() != block && p_hasSnowNeighbours_0_.getBlockState(p_hasSnowNeighbours_1_.west()).getBlock() != block && p_hasSnowNeighbours_0_.getBlockState(p_hasSnowNeighbours_1_.east()).getBlock() != block) {
            return false;
        }
        boolean bl = p_hasSnowNeighbours_0_.getBlockState(p_hasSnowNeighbours_1_.down()).getBlock().isOpaqueCube();
        return bl;
    }

    private static boolean checkBlock(Block p_checkBlock_0_, IBlockState p_checkBlock_1_) {
        if (p_checkBlock_0_.isFullCube()) {
            return false;
        }
        if (p_checkBlock_0_.isOpaqueCube()) {
            return false;
        }
        if (p_checkBlock_0_ instanceof BlockSnow) {
            return false;
        }
        if (p_checkBlock_0_ instanceof BlockBush) {
            if (p_checkBlock_0_ instanceof BlockDoublePlant) return true;
            if (p_checkBlock_0_ instanceof BlockFlower) return true;
            if (p_checkBlock_0_ instanceof BlockMushroom) return true;
            if (p_checkBlock_0_ instanceof BlockSapling) return true;
            if (p_checkBlock_0_ instanceof BlockTallGrass) return true;
        }
        if (p_checkBlock_0_ instanceof BlockFence) return true;
        if (p_checkBlock_0_ instanceof BlockFenceGate) return true;
        if (p_checkBlock_0_ instanceof BlockFlowerPot) return true;
        if (p_checkBlock_0_ instanceof BlockPane) return true;
        if (p_checkBlock_0_ instanceof BlockReed) return true;
        if (p_checkBlock_0_ instanceof BlockWall) return true;
        if (p_checkBlock_0_ instanceof BlockRedstoneTorch && p_checkBlock_1_.getValue(BlockTorch.FACING) == EnumFacing.UP) {
            return true;
        }
        if (!(p_checkBlock_0_ instanceof BlockLever)) return false;
        BlockLever.EnumOrientation object = p_checkBlock_1_.getValue(BlockLever.FACING);
        if (object == BlockLever.EnumOrientation.UP_X) return true;
        if (object != BlockLever.EnumOrientation.UP_Z) return false;
        return true;
    }
}

