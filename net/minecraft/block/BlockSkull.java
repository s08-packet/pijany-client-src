/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateHelper;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StatCollector;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSkull
extends BlockContainer {
    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool NODROP = PropertyBool.create("nodrop");
    private static final Predicate<BlockWorldState> IS_WITHER_SKELETON = new Predicate<BlockWorldState>(){

        @Override
        public boolean apply(BlockWorldState p_apply_1_) {
            if (p_apply_1_.getBlockState() == null) return false;
            if (p_apply_1_.getBlockState().getBlock() != Blocks.skull) return false;
            if (!(p_apply_1_.getTileEntity() instanceof TileEntitySkull)) return false;
            if (((TileEntitySkull)p_apply_1_.getTileEntity()).getSkullType() != 1) return false;
            return true;
        }
    };
    private BlockPattern witherBasePattern;
    private BlockPattern witherPattern;

    protected BlockSkull() {
        super(Material.circuits);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(NODROP, false));
        this.setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f);
    }

    @Override
    public String getLocalizedName() {
        return StatCollector.translateToLocal("tile.skull.skeleton.name");
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, BlockPos pos) {
        switch (2.$SwitchMap$net$minecraft$util$EnumFacing[worldIn.getBlockState(pos).getValue(FACING).ordinal()]) {
            default: {
                this.setBlockBounds(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f);
                return;
            }
            case 2: {
                this.setBlockBounds(0.25f, 0.25f, 0.5f, 0.75f, 0.75f, 1.0f);
                return;
            }
            case 3: {
                this.setBlockBounds(0.25f, 0.25f, 0.0f, 0.75f, 0.75f, 0.5f);
                return;
            }
            case 4: {
                this.setBlockBounds(0.5f, 0.25f, 0.25f, 1.0f, 0.75f, 0.75f);
                return;
            }
            case 5: 
        }
        this.setBlockBounds(0.0f, 0.25f, 0.25f, 0.5f, 0.75f, 0.75f);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
        this.setBlockBoundsBasedOnState(worldIn, pos);
        return super.getCollisionBoundingBox(worldIn, pos, state);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing()).withProperty(NODROP, false);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySkull();
    }

    @Override
    public Item getItem(World worldIn, BlockPos pos) {
        return Items.skull;
    }

    @Override
    public int getDamageValue(World worldIn, BlockPos pos) {
        int n;
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntitySkull) {
            n = ((TileEntitySkull)tileentity).getSkullType();
            return n;
        }
        n = super.getDamageValue(worldIn, pos);
        return n;
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (player.capabilities.isCreativeMode) {
            state = state.withProperty(NODROP, true);
            worldIn.setBlockState(pos, state, 4);
        }
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity;
        if (worldIn.isRemote) return;
        if (!state.getValue(NODROP).booleanValue() && (tileentity = worldIn.getTileEntity(pos)) instanceof TileEntitySkull) {
            TileEntitySkull tileentityskull = (TileEntitySkull)tileentity;
            ItemStack itemstack = new ItemStack(Items.skull, 1, this.getDamageValue(worldIn, pos));
            if (tileentityskull.getSkullType() == 3 && tileentityskull.getPlayerProfile() != null) {
                itemstack.setTagCompound(new NBTTagCompound());
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                NBTUtil.writeGameProfile(nbttagcompound, tileentityskull.getPlayerProfile());
                itemstack.getTagCompound().setTag("SkullOwner", nbttagcompound);
            }
            BlockSkull.spawnAsEntity(worldIn, pos, itemstack);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.skull;
    }

    public boolean canDispenserPlace(World worldIn, BlockPos pos, ItemStack stack) {
        if (stack.getMetadata() != 1) return false;
        if (pos.getY() < 2) return false;
        if (worldIn.getDifficulty() == EnumDifficulty.PEACEFUL) return false;
        if (worldIn.isRemote) return false;
        if (this.getWitherBasePattern().match(worldIn, pos) == null) return false;
        return true;
    }

    public void checkWitherSpawn(World worldIn, BlockPos pos, TileEntitySkull te) {
        if (te.getSkullType() != 1) return;
        if (pos.getY() < 2) return;
        if (worldIn.getDifficulty() == EnumDifficulty.PEACEFUL) return;
        if (worldIn.isRemote) return;
        BlockPattern blockpattern = this.getWitherPattern();
        BlockPattern.PatternHelper blockpattern$patternhelper = blockpattern.match(worldIn, pos);
        if (blockpattern$patternhelper == null) return;
        for (int i = 0; i < 3; ++i) {
            BlockWorldState blockworldstate = blockpattern$patternhelper.translateOffset(i, 0, 0);
            worldIn.setBlockState(blockworldstate.getPos(), blockworldstate.getBlockState().withProperty(NODROP, true), 2);
        }
        for (int j = 0; j < blockpattern.getPalmLength(); ++j) {
            for (int k = 0; k < blockpattern.getThumbLength(); ++k) {
                BlockWorldState blockworldstate1 = blockpattern$patternhelper.translateOffset(j, k, 0);
                worldIn.setBlockState(blockworldstate1.getPos(), Blocks.air.getDefaultState(), 2);
            }
        }
        BlockPos blockpos = blockpattern$patternhelper.translateOffset(1, 0, 0).getPos();
        EntityWither entitywither = new EntityWither(worldIn);
        BlockPos blockpos1 = blockpattern$patternhelper.translateOffset(1, 2, 0).getPos();
        entitywither.setLocationAndAngles((double)blockpos1.getX() + 0.5, (double)blockpos1.getY() + 0.55, (double)blockpos1.getZ() + 0.5, blockpattern$patternhelper.getFinger().getAxis() == EnumFacing.Axis.X ? 0.0f : 90.0f, 0.0f);
        entitywither.renderYawOffset = blockpattern$patternhelper.getFinger().getAxis() == EnumFacing.Axis.X ? 0.0f : 90.0f;
        entitywither.func_82206_m();
        for (EntityPlayer entityplayer : worldIn.getEntitiesWithinAABB(EntityPlayer.class, entitywither.getEntityBoundingBox().expand(50.0, 50.0, 50.0))) {
            entityplayer.triggerAchievement(AchievementList.spawnWither);
        }
        worldIn.spawnEntityInWorld(entitywither);
        for (int l = 0; l < 120; ++l) {
            worldIn.spawnParticle(EnumParticleTypes.SNOWBALL, (double)blockpos.getX() + worldIn.rand.nextDouble(), (double)(blockpos.getY() - 2) + worldIn.rand.nextDouble() * 3.9, (double)blockpos.getZ() + worldIn.rand.nextDouble(), 0.0, 0.0, 0.0, new int[0]);
        }
        int i1 = 0;
        while (i1 < blockpattern.getPalmLength()) {
            for (int j1 = 0; j1 < blockpattern.getThumbLength(); ++j1) {
                BlockWorldState blockworldstate2 = blockpattern$patternhelper.translateOffset(i1, j1, 0);
                worldIn.notifyNeighborsRespectDebug(blockworldstate2.getPos(), Blocks.air);
            }
            ++i1;
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        boolean bl;
        IBlockState iBlockState = this.getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7));
        if ((meta & 8) > 0) {
            bl = true;
            return iBlockState.withProperty(NODROP, bl);
        }
        bl = false;
        return iBlockState.withProperty(NODROP, bl);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        if (state.getValue(NODROP) == false) return i |= state.getValue(FACING).getIndex();
        i |= 8;
        return i |= state.getValue(FACING).getIndex();
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING, NODROP);
    }

    protected BlockPattern getWitherBasePattern() {
        if (this.witherBasePattern != null) return this.witherBasePattern;
        this.witherBasePattern = FactoryBlockPattern.start().aisle("   ", "###", "~#~").where('#', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.soul_sand))).where('~', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.air))).build();
        return this.witherBasePattern;
    }

    protected BlockPattern getWitherPattern() {
        if (this.witherPattern != null) return this.witherPattern;
        this.witherPattern = FactoryBlockPattern.start().aisle("^^^", "###", "~#~").where('#', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.soul_sand))).where('^', IS_WITHER_SKELETON).where('~', BlockWorldState.hasState(BlockStateHelper.forBlock(Blocks.air))).build();
        return this.witherPattern;
    }
}

