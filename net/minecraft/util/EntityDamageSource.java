/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

public class EntityDamageSource
extends DamageSource {
    protected Entity damageSourceEntity;
    private boolean isThornsDamage = false;

    public EntityDamageSource(String p_i1567_1_, Entity damageSourceEntityIn) {
        super(p_i1567_1_);
        this.damageSourceEntity = damageSourceEntityIn;
    }

    public EntityDamageSource setIsThornsDamage() {
        this.isThornsDamage = true;
        return this;
    }

    public boolean getIsThornsDamage() {
        return this.isThornsDamage;
    }

    @Override
    public Entity getEntity() {
        return this.damageSourceEntity;
    }

    @Override
    public IChatComponent getDeathMessage(EntityLivingBase p_151519_1_) {
        ChatComponentTranslation chatComponentTranslation;
        ItemStack itemstack = this.damageSourceEntity instanceof EntityLivingBase ? ((EntityLivingBase)this.damageSourceEntity).getHeldItem() : null;
        String s = "death.attack." + this.damageType;
        String s1 = s + ".item";
        if (itemstack != null && itemstack.hasDisplayName() && StatCollector.canTranslate(s1)) {
            chatComponentTranslation = new ChatComponentTranslation(s1, p_151519_1_.getDisplayName(), this.damageSourceEntity.getDisplayName(), itemstack.getChatComponent());
            return chatComponentTranslation;
        }
        chatComponentTranslation = new ChatComponentTranslation(s, p_151519_1_.getDisplayName(), this.damageSourceEntity.getDisplayName());
        return chatComponentTranslation;
    }

    @Override
    public boolean isDifficultyScaled() {
        if (this.damageSourceEntity == null) return false;
        if (!(this.damageSourceEntity instanceof EntityLivingBase)) return false;
        if (this.damageSourceEntity instanceof EntityPlayer) return false;
        return true;
    }
}

