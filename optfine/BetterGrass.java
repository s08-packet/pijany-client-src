/*
 * Decompiled with CFR 0.152.
 */
package optfine;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockMycelium;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import optfine.BlockModelUtils;
import optfine.Config;

public class BetterGrass {
    private static IBakedModel modelEmpty = new SimpleBakedModel(new ArrayList<BakedQuad>(), new ArrayList<List<BakedQuad>>(), false, false, null, null);
    private static IBakedModel modelCubeMycelium = null;
    private static IBakedModel modelCubeGrassSnowy = null;
    private static IBakedModel modelCubeGrass = null;

    public static void update() {
        modelCubeGrass = BlockModelUtils.makeModelCube("minecraft:blocks/grass_top", 0);
        modelCubeGrassSnowy = BlockModelUtils.makeModelCube("minecraft:blocks/snow", -1);
        modelCubeMycelium = BlockModelUtils.makeModelCube("minecraft:blocks/mycelium_top", -1);
    }

    public static List getFaceQuads(IBlockAccess p_getFaceQuads_0_, Block p_getFaceQuads_1_, BlockPos p_getFaceQuads_2_, EnumFacing p_getFaceQuads_3_, List p_getFaceQuads_4_) {
        boolean flag;
        if (p_getFaceQuads_3_ == EnumFacing.UP) return p_getFaceQuads_4_;
        if (p_getFaceQuads_3_ == EnumFacing.DOWN) return p_getFaceQuads_4_;
        if (p_getFaceQuads_1_ instanceof BlockMycelium) {
            List<BakedQuad> list;
            if (!Config.isBetterGrassFancy()) {
                list = modelCubeMycelium.getFaceQuads(p_getFaceQuads_3_);
                return list;
            }
            if (BetterGrass.getBlockAt(p_getFaceQuads_2_.down(), p_getFaceQuads_3_, p_getFaceQuads_0_) == Blocks.mycelium) {
                list = modelCubeMycelium.getFaceQuads(p_getFaceQuads_3_);
                return list;
            }
            list = p_getFaceQuads_4_;
            return list;
        }
        if (!(p_getFaceQuads_1_ instanceof BlockGrass)) return p_getFaceQuads_4_;
        Block block = p_getFaceQuads_0_.getBlockState(p_getFaceQuads_2_.up()).getBlock();
        boolean bl = flag = block == Blocks.snow || block == Blocks.snow_layer;
        if (!Config.isBetterGrassFancy()) {
            if (!flag) return modelCubeGrass.getFaceQuads(p_getFaceQuads_3_);
            return modelCubeGrassSnowy.getFaceQuads(p_getFaceQuads_3_);
        }
        if (flag) {
            if (BetterGrass.getBlockAt(p_getFaceQuads_2_, p_getFaceQuads_3_, p_getFaceQuads_0_) != Blocks.snow_layer) return p_getFaceQuads_4_;
            return modelCubeGrassSnowy.getFaceQuads(p_getFaceQuads_3_);
        }
        if (BetterGrass.getBlockAt(p_getFaceQuads_2_.down(), p_getFaceQuads_3_, p_getFaceQuads_0_) != Blocks.grass) return p_getFaceQuads_4_;
        return modelCubeGrass.getFaceQuads(p_getFaceQuads_3_);
    }

    private static Block getBlockAt(BlockPos p_getBlockAt_0_, EnumFacing p_getBlockAt_1_, IBlockAccess p_getBlockAt_2_) {
        BlockPos blockpos = p_getBlockAt_0_.offset(p_getBlockAt_1_);
        return p_getBlockAt_2_.getBlockState(blockpos).getBlock();
    }
}

