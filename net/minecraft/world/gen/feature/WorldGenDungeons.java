/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package net.minecraft.world.gen.feature;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenDungeons
extends WorldGenerator {
    private static final Logger field_175918_a = LogManager.getLogger();
    private static final String[] SPAWNERTYPES = new String[]{"Skeleton", "Zombie", "Zombie", "Spider"};
    private static final List<WeightedRandomChestContent> CHESTCONTENT = Lists.newArrayList(new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 10), new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 4, 10), new WeightedRandomChestContent(Items.bread, 0, 1, 1, 10), new WeightedRandomChestContent(Items.wheat, 0, 1, 4, 10), new WeightedRandomChestContent(Items.gunpowder, 0, 1, 4, 10), new WeightedRandomChestContent(Items.string, 0, 1, 4, 10), new WeightedRandomChestContent(Items.bucket, 0, 1, 1, 10), new WeightedRandomChestContent(Items.golden_apple, 0, 1, 1, 1), new WeightedRandomChestContent(Items.redstone, 0, 1, 4, 10), new WeightedRandomChestContent(Items.record_13, 0, 1, 1, 4), new WeightedRandomChestContent(Items.record_cat, 0, 1, 1, 4), new WeightedRandomChestContent(Items.name_tag, 0, 1, 1, 10), new WeightedRandomChestContent(Items.golden_horse_armor, 0, 1, 1, 2), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 5), new WeightedRandomChestContent(Items.diamond_horse_armor, 0, 1, 1, 1));

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        int i = 3;
        int j = rand.nextInt(2) + 2;
        int k = -j - 1;
        int l = j + 1;
        int i1 = -1;
        int j1 = 4;
        int k1 = rand.nextInt(2) + 2;
        int l1 = -k1 - 1;
        int i2 = k1 + 1;
        int j2 = 0;
        int k2 = k;
        block0: while (true) {
            if (k2 > l) break;
            int l2 = -1;
            while (true) {
                if (l2 <= 4) {
                } else {
                    ++k2;
                    continue block0;
                }
                for (int i3 = l1; i3 <= i2; ++i3) {
                    BlockPos blockpos = position.add(k2, l2, i3);
                    Material material = worldIn.getBlockState(blockpos).getBlock().getMaterial();
                    boolean flag = material.isSolid();
                    if (l2 == -1 && !flag) {
                        return false;
                    }
                    if (l2 == 4 && !flag) {
                        return false;
                    }
                    if (k2 != k && k2 != l && i3 != l1 && i3 != i2 || l2 != 0 || !worldIn.isAirBlock(blockpos) || !worldIn.isAirBlock(blockpos.up())) continue;
                    ++j2;
                }
                ++l2;
            }
            break;
        }
        if (j2 < true) return false;
        if (j2 > 5) return false;
        int k3 = k;
        block3: while (true) {
            if (k3 > l) break;
            int i4 = 3;
            while (true) {
                if (i4 >= -1) {
                } else {
                    ++k3;
                    continue block3;
                }
                for (int k4 = l1; k4 <= i2; ++k4) {
                    BlockPos blockpos1 = position.add(k3, i4, k4);
                    if (k3 != k && i4 != -1 && k4 != l1 && k3 != l && i4 != 4 && k4 != i2) {
                        if (worldIn.getBlockState(blockpos1).getBlock() == Blocks.chest) continue;
                        worldIn.setBlockToAir(blockpos1);
                        continue;
                    }
                    if (blockpos1.getY() >= 0 && !worldIn.getBlockState(blockpos1.down()).getBlock().getMaterial().isSolid()) {
                        worldIn.setBlockToAir(blockpos1);
                        continue;
                    }
                    if (!worldIn.getBlockState(blockpos1).getBlock().getMaterial().isSolid() || worldIn.getBlockState(blockpos1).getBlock() == Blocks.chest) continue;
                    if (i4 == -1 && rand.nextInt(4) != 0) {
                        worldIn.setBlockState(blockpos1, Blocks.mossy_cobblestone.getDefaultState(), 2);
                        continue;
                    }
                    worldIn.setBlockState(blockpos1, Blocks.cobblestone.getDefaultState(), 2);
                }
                --i4;
            }
            break;
        }
        int l3 = 0;
        while (true) {
            if (l3 < 2) {
            } else {
                worldIn.setBlockState(position, Blocks.mob_spawner.getDefaultState(), 2);
                TileEntity tileentity = worldIn.getTileEntity(position);
                if (tileentity instanceof TileEntityMobSpawner) {
                    ((TileEntityMobSpawner)tileentity).getSpawnerBaseLogic().setEntityName(this.pickMobSpawner(rand));
                    return true;
                }
                field_175918_a.error("Failed to fetch mob spawner entity at (" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")");
                return true;
            }
            for (int j4 = 0; j4 < 3; ++j4) {
                int j5;
                int i5;
                int l4 = position.getX() + rand.nextInt(j * 2 + 1) - j;
                BlockPos blockpos2 = new BlockPos(l4, i5 = position.getY(), j5 = position.getZ() + rand.nextInt(k1 * 2 + 1) - k1);
                if (!worldIn.isAirBlock(blockpos2)) continue;
                int j3 = 0;
                for (Object enumfacing0 : EnumFacing.Plane.HORIZONTAL) {
                    EnumFacing enumfacing = (EnumFacing)enumfacing0;
                    if (!worldIn.getBlockState(blockpos2.offset(enumfacing)).getBlock().getMaterial().isSolid()) continue;
                    ++j3;
                }
                if (j3 != true) continue;
                worldIn.setBlockState(blockpos2, Blocks.chest.correctFacing(worldIn, blockpos2, Blocks.chest.getDefaultState()), 2);
                List<WeightedRandomChestContent> list = WeightedRandomChestContent.func_177629_a(CHESTCONTENT, Items.enchanted_book.getRandom(rand));
                TileEntity tileentity1 = worldIn.getTileEntity(blockpos2);
                if (!(tileentity1 instanceof TileEntityChest)) break;
                WeightedRandomChestContent.generateChestContents(rand, list, (TileEntityChest)tileentity1, 8);
                break;
            }
            ++l3;
        }
    }

    private String pickMobSpawner(Random p_76543_1_) {
        return SPAWNERTYPES[p_76543_1_.nextInt(SPAWNERTYPES.length)];
    }
}

