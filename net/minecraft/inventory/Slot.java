/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class Slot {
    private final int slotIndex;
    public final IInventory inventory;
    public int slotNumber;
    public int xDisplayPosition;
    public int yDisplayPosition;

    public Slot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        this.inventory = inventoryIn;
        this.slotIndex = index;
        this.xDisplayPosition = xPosition;
        this.yDisplayPosition = yPosition;
    }

    public void onSlotChange(ItemStack p_75220_1_, ItemStack p_75220_2_) {
        if (p_75220_1_ == null) return;
        if (p_75220_2_ == null) return;
        if (p_75220_1_.getItem() != p_75220_2_.getItem()) return;
        int i = p_75220_2_.stackSize - p_75220_1_.stackSize;
        if (i <= 0) return;
        this.onCrafting(p_75220_1_, i);
    }

    protected void onCrafting(ItemStack stack, int amount) {
    }

    protected void onCrafting(ItemStack stack) {
    }

    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
        this.onSlotChanged();
    }

    public boolean isItemValid(ItemStack stack) {
        return true;
    }

    public ItemStack getStack() {
        return this.inventory.getStackInSlot(this.slotIndex);
    }

    public boolean getHasStack() {
        if (this.getStack() == null) return false;
        return true;
    }

    public void putStack(ItemStack stack) {
        this.inventory.setInventorySlotContents(this.slotIndex, stack);
        this.onSlotChanged();
    }

    public void onSlotChanged() {
        this.inventory.markDirty();
    }

    public int getSlotStackLimit() {
        return this.inventory.getInventoryStackLimit();
    }

    public int getItemStackLimit(ItemStack stack) {
        return this.getSlotStackLimit();
    }

    public String getSlotTexture() {
        return null;
    }

    public ItemStack decrStackSize(int amount) {
        return this.inventory.decrStackSize(this.slotIndex, amount);
    }

    public boolean isHere(IInventory inv, int slotIn) {
        if (inv != this.inventory) return false;
        if (slotIn != this.slotIndex) return false;
        return true;
    }

    public boolean canTakeStack(EntityPlayer playerIn) {
        return true;
    }

    public boolean canBeHovered() {
        return true;
    }
}

