/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ItemBucket
extends Item {
    public Block isFull;

    public ItemBucket(Block containedBlock) {
        this.maxStackSize = 1;
        this.isFull = containedBlock;
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
        boolean flag = this.isFull == Blocks.air;
        MovingObjectPosition movingobjectposition = this.getMovingObjectPositionFromPlayer(worldIn, playerIn, flag);
        if (movingobjectposition == null) {
            return itemStackIn;
        }
        if (movingobjectposition.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return itemStackIn;
        BlockPos blockpos = movingobjectposition.getBlockPos();
        if (!worldIn.isBlockModifiable(playerIn, blockpos)) {
            return itemStackIn;
        }
        if (flag) {
            if (!playerIn.canPlayerEdit(blockpos.offset(movingobjectposition.sideHit), movingobjectposition.sideHit, itemStackIn)) {
                return itemStackIn;
            }
            IBlockState iblockstate = worldIn.getBlockState(blockpos);
            Material material = iblockstate.getBlock().getMaterial();
            if (material == Material.water && iblockstate.getValue(BlockLiquid.LEVEL) == 0) {
                worldIn.setBlockToAir(blockpos);
                playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
                return this.fillBucket(itemStackIn, playerIn, Items.water_bucket);
            }
            if (material != Material.lava) return itemStackIn;
            if (iblockstate.getValue(BlockLiquid.LEVEL) != 0) return itemStackIn;
            worldIn.setBlockToAir(blockpos);
            playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
            return this.fillBucket(itemStackIn, playerIn, Items.lava_bucket);
        }
        if (this.isFull == Blocks.air) {
            return new ItemStack(Items.bucket);
        }
        BlockPos blockpos1 = blockpos.offset(movingobjectposition.sideHit);
        if (!playerIn.canPlayerEdit(blockpos1, movingobjectposition.sideHit, itemStackIn)) {
            return itemStackIn;
        }
        if (!this.tryPlaceContainedLiquid(worldIn, blockpos1)) return itemStackIn;
        if (playerIn.capabilities.isCreativeMode) return itemStackIn;
        playerIn.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
        return new ItemStack(Items.bucket);
    }

    private ItemStack fillBucket(ItemStack emptyBuckets, EntityPlayer player, Item fullBucket) {
        if (player.capabilities.isCreativeMode) {
            return emptyBuckets;
        }
        if (--emptyBuckets.stackSize <= 0) {
            return new ItemStack(fullBucket);
        }
        if (player.inventory.addItemStackToInventory(new ItemStack(fullBucket))) return emptyBuckets;
        player.dropPlayerItemWithRandomChoice(new ItemStack(fullBucket, 1, 0), false);
        return emptyBuckets;
    }

    public boolean tryPlaceContainedLiquid(World worldIn, BlockPos pos) {
        boolean flag;
        if (this.isFull == Blocks.air) {
            return false;
        }
        Material material = worldIn.getBlockState(pos).getBlock().getMaterial();
        boolean bl = flag = !material.isSolid();
        if (!worldIn.isAirBlock(pos) && !flag) {
            return false;
        }
        if (worldIn.provider.doesWaterVaporize() && this.isFull == Blocks.flowing_water) {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            worldIn.playSoundEffect((float)i + 0.5f, (float)j + 0.5f, (float)k + 0.5f, "random.fizz", 0.5f, 2.6f + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8f);
            int l = 0;
            while (l < 8) {
                worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double)i + Math.random(), (double)j + Math.random(), (double)k + Math.random(), 0.0, 0.0, 0.0, new int[0]);
                ++l;
            }
            return true;
        }
        if (!worldIn.isRemote && flag && !material.isLiquid()) {
            worldIn.destroyBlock(pos, true);
        }
        worldIn.setBlockState(pos, this.isFull.getDefaultState(), 3);
        return true;
    }
}

