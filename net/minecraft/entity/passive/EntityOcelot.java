/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIOcelotAttack;
import net.minecraft.entity.ai.EntityAIOcelotSit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityOcelot
extends EntityTameable {
    private EntityAIAvoidEntity<EntityPlayer> avoidEntity;
    private EntityAITempt aiTempt;

    public EntityOcelot(World worldIn) {
        super(worldIn);
        this.setSize(0.6f, 0.7f);
        ((PathNavigateGround)this.getNavigator()).setAvoidsWater(true);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, this.aiSit);
        this.aiTempt = new EntityAITempt(this, 0.6, Items.fish, true);
        this.tasks.addTask(3, this.aiTempt);
        this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0, 10.0f, 5.0f));
        this.tasks.addTask(6, new EntityAIOcelotSit(this, 0.8));
        this.tasks.addTask(7, new EntityAILeapAtTarget(this, 0.3f));
        this.tasks.addTask(8, new EntityAIOcelotAttack(this));
        this.tasks.addTask(9, new EntityAIMate(this, 0.8));
        this.tasks.addTask(10, new EntityAIWander(this, 0.8));
        this.tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0f));
        this.targetTasks.addTask(1, new EntityAITargetNonTamed<EntityChicken>(this, EntityChicken.class, false, null));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataWatcher.addObject(18, (byte)0);
    }

    @Override
    public void updateAITasks() {
        if (!this.getMoveHelper().isUpdating()) {
            this.setSneaking(false);
            this.setSprinting(false);
            return;
        }
        double d0 = this.getMoveHelper().getSpeed();
        if (d0 == 0.6) {
            this.setSneaking(true);
            this.setSprinting(false);
            return;
        }
        if (d0 == 1.33) {
            this.setSneaking(false);
            this.setSprinting(true);
            return;
        }
        this.setSneaking(false);
        this.setSprinting(false);
    }

    @Override
    protected boolean canDespawn() {
        if (this.isTamed()) return false;
        if (this.ticksExisted <= 2400) return false;
        return true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10.0);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3f);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tagCompound) {
        super.writeEntityToNBT(tagCompound);
        tagCompound.setInteger("CatType", this.getTameSkin());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tagCompund) {
        super.readEntityFromNBT(tagCompund);
        this.setTameSkin(tagCompund.getInteger("CatType"));
    }

    @Override
    protected String getLivingSound() {
        if (!this.isTamed()) {
            return "";
        }
        if (this.isInLove()) {
            return "mob.cat.purr";
        }
        if (this.rand.nextInt(4) != 0) return "mob.cat.meow";
        return "mob.cat.purreow";
    }

    @Override
    protected String getHurtSound() {
        return "mob.cat.hitt";
    }

    @Override
    protected String getDeathSound() {
        return "mob.cat.hitt";
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    protected Item getDropItem() {
        return Items.leather;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        return entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 3.0f);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        this.aiSit.setSitting(false);
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
    }

    @Override
    public boolean interact(EntityPlayer player) {
        ItemStack itemstack = player.inventory.getCurrentItem();
        if (this.isTamed()) {
            if (!this.isOwner(player)) return super.interact(player);
            if (this.worldObj.isRemote) return super.interact(player);
            if (this.isBreedingItem(itemstack)) return super.interact(player);
            this.aiSit.setSitting(!this.isSitting());
            return super.interact(player);
        }
        if (!this.aiTempt.isRunning()) return super.interact(player);
        if (itemstack == null) return super.interact(player);
        if (itemstack.getItem() != Items.fish) return super.interact(player);
        if (!(player.getDistanceSqToEntity(this) < 9.0)) return super.interact(player);
        if (!player.capabilities.isCreativeMode) {
            --itemstack.stackSize;
        }
        if (itemstack.stackSize <= 0) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        }
        if (this.worldObj.isRemote) return true;
        if (this.rand.nextInt(3) == 0) {
            this.setTamed(true);
            this.setTameSkin(1 + this.worldObj.rand.nextInt(3));
            this.setOwnerId(player.getUniqueID().toString());
            this.playTameEffect(true);
            this.aiSit.setSitting(true);
            this.worldObj.setEntityState(this, (byte)7);
            return true;
        }
        this.playTameEffect(false);
        this.worldObj.setEntityState(this, (byte)6);
        return true;
    }

    @Override
    public EntityOcelot createChild(EntityAgeable ageable) {
        EntityOcelot entityocelot = new EntityOcelot(this.worldObj);
        if (!this.isTamed()) return entityocelot;
        entityocelot.setOwnerId(this.getOwnerId());
        entityocelot.setTamed(true);
        entityocelot.setTameSkin(this.getTameSkin());
        return entityocelot;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        if (stack == null) return false;
        if (stack.getItem() != Items.fish) return false;
        return true;
    }

    @Override
    public boolean canMateWith(EntityAnimal otherAnimal) {
        if (otherAnimal == this) {
            return false;
        }
        if (!this.isTamed()) {
            return false;
        }
        if (!(otherAnimal instanceof EntityOcelot)) {
            return false;
        }
        EntityOcelot entityocelot = (EntityOcelot)otherAnimal;
        if (!entityocelot.isTamed()) {
            return false;
        }
        if (!this.isInLove()) return false;
        if (!entityocelot.isInLove()) return false;
        return true;
    }

    public int getTameSkin() {
        return this.dataWatcher.getWatchableObjectByte(18);
    }

    public void setTameSkin(int skinId) {
        this.dataWatcher.updateObject(18, (byte)skinId);
    }

    @Override
    public boolean getCanSpawnHere() {
        if (this.worldObj.rand.nextInt(3) == 0) return false;
        return true;
    }

    @Override
    public boolean isNotColliding() {
        if (!this.worldObj.checkNoEntityCollision(this.getEntityBoundingBox(), this)) return false;
        if (!this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox()).isEmpty()) return false;
        if (this.worldObj.isAnyLiquid(this.getEntityBoundingBox())) return false;
        BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);
        if (blockpos.getY() < this.worldObj.func_181545_F()) {
            return false;
        }
        Block block = this.worldObj.getBlockState(blockpos.down()).getBlock();
        if (block == Blocks.grass) return true;
        if (block.getMaterial() != Material.leaves) return false;
        return true;
    }

    @Override
    public String getName() {
        String string;
        if (this.hasCustomName()) {
            string = this.getCustomNameTag();
            return string;
        }
        if (this.isTamed()) {
            string = StatCollector.translateToLocal("entity.Cat.name");
            return string;
        }
        string = super.getName();
        return string;
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
    }

    @Override
    protected void setupTamedAI() {
        if (this.avoidEntity == null) {
            this.avoidEntity = new EntityAIAvoidEntity<EntityPlayer>(this, EntityPlayer.class, 16.0f, 0.8, 1.33);
        }
        this.tasks.removeTask(this.avoidEntity);
        if (this.isTamed()) return;
        this.tasks.addTask(4, this.avoidEntity);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        if (this.worldObj.rand.nextInt(7) != 0) return livingdata;
        int i = 0;
        while (i < 2) {
            EntityOcelot entityocelot = new EntityOcelot(this.worldObj);
            entityocelot.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, 0.0f);
            entityocelot.setGrowingAge(-24000);
            this.worldObj.spawnEntityInWorld(entityocelot);
            ++i;
        }
        return livingdata;
    }
}

