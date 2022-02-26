/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.player;

import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ReportedException;

public class InventoryPlayer
implements IInventory {
    public ItemStack[] mainInventory = new ItemStack[36];
    public ItemStack[] armorInventory = new ItemStack[4];
    public int currentItem;
    public EntityPlayer player;
    private ItemStack itemStack;
    public boolean inventoryChanged;

    public InventoryPlayer(EntityPlayer playerIn) {
        this.player = playerIn;
    }

    public ItemStack getCurrentItem() {
        if (this.currentItem >= 9) return null;
        if (this.currentItem < 0) return null;
        ItemStack itemStack = this.mainInventory[this.currentItem];
        return itemStack;
    }

    public static int getHotbarSize() {
        return 9;
    }

    private int getInventorySlotContainItem(Item itemIn) {
        int i = 0;
        while (i < this.mainInventory.length) {
            if (this.mainInventory[i] != null && this.mainInventory[i].getItem() == itemIn) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    private int getInventorySlotContainItemAndDamage(Item itemIn, int p_146024_2_) {
        int i = 0;
        while (i < this.mainInventory.length) {
            if (this.mainInventory[i] != null && this.mainInventory[i].getItem() == itemIn && this.mainInventory[i].getMetadata() == p_146024_2_) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    private int storeItemStack(ItemStack itemStackIn) {
        int i = 0;
        while (i < this.mainInventory.length) {
            if (this.mainInventory[i] != null && this.mainInventory[i].getItem() == itemStackIn.getItem() && this.mainInventory[i].isStackable() && this.mainInventory[i].stackSize < this.mainInventory[i].getMaxStackSize() && this.mainInventory[i].stackSize < this.getInventoryStackLimit() && (!this.mainInventory[i].getHasSubtypes() || this.mainInventory[i].getMetadata() == itemStackIn.getMetadata()) && ItemStack.areItemStackTagsEqual(this.mainInventory[i], itemStackIn)) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public int getFirstEmptyStack() {
        int i = 0;
        while (i < this.mainInventory.length) {
            if (this.mainInventory[i] == null) {
                return i;
            }
            ++i;
        }
        return -1;
    }

    public void setCurrentItem(Item itemIn, int p_146030_2_, boolean p_146030_3_, boolean p_146030_4_) {
        int l;
        int k;
        int i;
        ItemStack itemstack = this.getCurrentItem();
        int n = i = p_146030_3_ ? this.getInventorySlotContainItemAndDamage(itemIn, p_146030_2_) : this.getInventorySlotContainItem(itemIn);
        if (i >= 0 && i < 9) {
            this.currentItem = i;
            return;
        }
        if (!p_146030_4_) return;
        if (itemIn == null) return;
        int j = this.getFirstEmptyStack();
        if (j >= 0 && j < 9) {
            this.currentItem = j;
        }
        if (itemstack != null && itemstack.isItemEnchantable()) {
            if (this.getInventorySlotContainItemAndDamage(itemstack.getItem(), itemstack.getItemDamage()) == this.currentItem) return;
        }
        if ((k = this.getInventorySlotContainItemAndDamage(itemIn, p_146030_2_)) >= 0) {
            l = this.mainInventory[k].stackSize;
            this.mainInventory[k] = this.mainInventory[this.currentItem];
        } else {
            l = 1;
        }
        this.mainInventory[this.currentItem] = new ItemStack(itemIn, l, p_146030_2_);
    }

    public void changeCurrentItem(int p_70453_1_) {
        if (p_70453_1_ > 0) {
            p_70453_1_ = 1;
        }
        if (p_70453_1_ < 0) {
            p_70453_1_ = -1;
        }
        this.currentItem -= p_70453_1_;
        while (true) {
            if (this.currentItem >= 0) {
                while (this.currentItem >= 9) {
                    this.currentItem -= 9;
                }
                return;
            }
            this.currentItem += 9;
        }
    }

    public int clearMatchingItems(Item itemIn, int metadataIn, int removeCount, NBTTagCompound itemNBT) {
        int i = 0;
        for (int j = 0; j < this.mainInventory.length; ++j) {
            ItemStack itemstack = this.mainInventory[j];
            if (itemstack == null || itemIn != null && itemstack.getItem() != itemIn || metadataIn > -1 && itemstack.getMetadata() != metadataIn || itemNBT != null && !NBTUtil.func_181123_a(itemNBT, itemstack.getTagCompound(), true)) continue;
            int k = removeCount <= 0 ? itemstack.stackSize : Math.min(removeCount - i, itemstack.stackSize);
            i += k;
            if (removeCount == 0) continue;
            this.mainInventory[j].stackSize -= k;
            if (this.mainInventory[j].stackSize == 0) {
                this.mainInventory[j] = null;
            }
            if (removeCount <= 0 || i < removeCount) continue;
            return i;
        }
        for (int l = 0; l < this.armorInventory.length; ++l) {
            ItemStack itemstack1 = this.armorInventory[l];
            if (itemstack1 == null || itemIn != null && itemstack1.getItem() != itemIn || metadataIn > -1 && itemstack1.getMetadata() != metadataIn || itemNBT != null && !NBTUtil.func_181123_a(itemNBT, itemstack1.getTagCompound(), false)) continue;
            int j1 = removeCount <= 0 ? itemstack1.stackSize : Math.min(removeCount - i, itemstack1.stackSize);
            i += j1;
            if (removeCount == 0) continue;
            this.armorInventory[l].stackSize -= j1;
            if (this.armorInventory[l].stackSize == 0) {
                this.armorInventory[l] = null;
            }
            if (removeCount <= 0 || i < removeCount) continue;
            return i;
        }
        if (this.itemStack == null) return i;
        if (itemIn != null && this.itemStack.getItem() != itemIn) {
            return i;
        }
        if (metadataIn > -1 && this.itemStack.getMetadata() != metadataIn) {
            return i;
        }
        if (itemNBT != null && !NBTUtil.func_181123_a(itemNBT, this.itemStack.getTagCompound(), false)) {
            return i;
        }
        int i1 = removeCount <= 0 ? this.itemStack.stackSize : Math.min(removeCount - i, this.itemStack.stackSize);
        i += i1;
        if (removeCount == 0) return i;
        this.itemStack.stackSize -= i1;
        if (this.itemStack.stackSize == 0) {
            this.itemStack = null;
        }
        if (removeCount <= 0) return i;
        if (i < removeCount) return i;
        return i;
    }

    private int storePartialItemStack(ItemStack itemStackIn) {
        Item item = itemStackIn.getItem();
        int i = itemStackIn.stackSize;
        int j = this.storeItemStack(itemStackIn);
        if (j < 0) {
            j = this.getFirstEmptyStack();
        }
        if (j < 0) {
            return i;
        }
        if (this.mainInventory[j] == null) {
            this.mainInventory[j] = new ItemStack(item, 0, itemStackIn.getMetadata());
            if (itemStackIn.hasTagCompound()) {
                this.mainInventory[j].setTagCompound((NBTTagCompound)itemStackIn.getTagCompound().copy());
            }
        }
        int k = i;
        if (i > this.mainInventory[j].getMaxStackSize() - this.mainInventory[j].stackSize) {
            k = this.mainInventory[j].getMaxStackSize() - this.mainInventory[j].stackSize;
        }
        if (k > this.getInventoryStackLimit() - this.mainInventory[j].stackSize) {
            k = this.getInventoryStackLimit() - this.mainInventory[j].stackSize;
        }
        if (k == 0) {
            return i;
        }
        this.mainInventory[j].stackSize += k;
        this.mainInventory[j].animationsToGo = 5;
        return i -= k;
    }

    public void decrementAnimations() {
        int i = 0;
        while (i < this.mainInventory.length) {
            if (this.mainInventory[i] != null) {
                this.mainInventory[i].updateAnimation(this.player.worldObj, this.player, i, this.currentItem == i);
            }
            ++i;
        }
    }

    public boolean consumeInventoryItem(Item itemIn) {
        int i = this.getInventorySlotContainItem(itemIn);
        if (i < 0) {
            return false;
        }
        if (--this.mainInventory[i].stackSize > 0) return true;
        this.mainInventory[i] = null;
        return true;
    }

    public boolean hasItem(Item itemIn) {
        int i = this.getInventorySlotContainItem(itemIn);
        if (i < 0) return false;
        return true;
    }

    public boolean addItemStackToInventory(final ItemStack itemStackIn) {
        if (itemStackIn == null) return false;
        if (itemStackIn.stackSize == 0) return false;
        if (itemStackIn.getItem() == null) return false;
        try {
            int i;
            if (itemStackIn.isItemDamaged()) {
                int j = this.getFirstEmptyStack();
                if (j >= 0) {
                    this.mainInventory[j] = ItemStack.copyItemStack(itemStackIn);
                    this.mainInventory[j].animationsToGo = 5;
                    itemStackIn.stackSize = 0;
                    return true;
                }
                if (!this.player.capabilities.isCreativeMode) return false;
                itemStackIn.stackSize = 0;
                return true;
            }
            do {
                i = itemStackIn.stackSize;
                itemStackIn.stackSize = this.storePartialItemStack(itemStackIn);
            } while (itemStackIn.stackSize > 0 && itemStackIn.stackSize < i);
            if (itemStackIn.stackSize == i && this.player.capabilities.isCreativeMode) {
                itemStackIn.stackSize = 0;
                return true;
            }
            if (itemStackIn.stackSize >= i) return false;
            return true;
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Adding item to inventory");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Item being added");
            crashreportcategory.addCrashSection("Item ID", Item.getIdFromItem(itemStackIn.getItem()));
            crashreportcategory.addCrashSection("Item data", itemStackIn.getMetadata());
            crashreportcategory.addCrashSectionCallable("Item name", new Callable<String>(){

                @Override
                public String call() throws Exception {
                    return itemStackIn.getDisplayName();
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack[] aitemstack = this.mainInventory;
        if (index >= this.mainInventory.length) {
            aitemstack = this.armorInventory;
            index -= this.mainInventory.length;
        }
        if (aitemstack[index] == null) return null;
        if (aitemstack[index].stackSize <= count) {
            ItemStack itemstack1 = aitemstack[index];
            aitemstack[index] = null;
            return itemstack1;
        }
        ItemStack itemstack = aitemstack[index].splitStack(count);
        if (aitemstack[index].stackSize != 0) return itemstack;
        aitemstack[index] = null;
        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack[] aitemstack = this.mainInventory;
        if (index >= this.mainInventory.length) {
            aitemstack = this.armorInventory;
            index -= this.mainInventory.length;
        }
        if (aitemstack[index] == null) return null;
        ItemStack itemstack = aitemstack[index];
        aitemstack[index] = null;
        return itemstack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        ItemStack[] aitemstack = this.mainInventory;
        if (index >= aitemstack.length) {
            index -= aitemstack.length;
            aitemstack = this.armorInventory;
        }
        aitemstack[index] = stack;
    }

    public float getStrVsBlock(Block blockIn) {
        float f = 1.0f;
        if (this.mainInventory[this.currentItem] == null) return f;
        f *= this.mainInventory[this.currentItem].getStrVsBlock(blockIn);
        return f;
    }

    public NBTTagList writeToNBT(NBTTagList p_70442_1_) {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            if (this.mainInventory[i] == null) continue;
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.mainInventory[i].writeToNBT(nbttagcompound);
            p_70442_1_.appendTag(nbttagcompound);
        }
        int j = 0;
        while (j < this.armorInventory.length) {
            if (this.armorInventory[j] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)(j + 100));
                this.armorInventory[j].writeToNBT(nbttagcompound1);
                p_70442_1_.appendTag(nbttagcompound1);
            }
            ++j;
        }
        return p_70442_1_;
    }

    public void readFromNBT(NBTTagList p_70443_1_) {
        this.mainInventory = new ItemStack[36];
        this.armorInventory = new ItemStack[4];
        int i = 0;
        while (i < p_70443_1_.tagCount()) {
            NBTTagCompound nbttagcompound = p_70443_1_.getCompoundTagAt(i);
            int j = nbttagcompound.getByte("Slot") & 0xFF;
            ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound);
            if (itemstack != null) {
                if (j >= 0 && j < this.mainInventory.length) {
                    this.mainInventory[j] = itemstack;
                }
                if (j >= 100 && j < this.armorInventory.length + 100) {
                    this.armorInventory[j - 100] = itemstack;
                }
            }
            ++i;
        }
    }

    @Override
    public int getSizeInventory() {
        return this.mainInventory.length + 4;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        ItemStack[] aitemstack = this.mainInventory;
        if (index < aitemstack.length) return aitemstack[index];
        index -= aitemstack.length;
        aitemstack = this.armorInventory;
        return aitemstack[index];
    }

    @Override
    public String getName() {
        return "container.inventory";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public IChatComponent getDisplayName() {
        ChatComponentStyle chatComponentStyle;
        if (this.hasCustomName()) {
            chatComponentStyle = new ChatComponentText(this.getName());
            return chatComponentStyle;
        }
        chatComponentStyle = new ChatComponentTranslation(this.getName(), new Object[0]);
        return chatComponentStyle;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    public boolean canHeldItemHarvest(Block blockIn) {
        if (blockIn.getMaterial().isToolNotRequired()) {
            return true;
        }
        ItemStack itemstack = this.getStackInSlot(this.currentItem);
        if (itemstack == null) return false;
        boolean bl = itemstack.canHarvestBlock(blockIn);
        return bl;
    }

    public ItemStack armorItemInSlot(int p_70440_1_) {
        return this.armorInventory[p_70440_1_];
    }

    public int getTotalArmorValue() {
        int i = 0;
        int j = 0;
        while (j < this.armorInventory.length) {
            if (this.armorInventory[j] != null && this.armorInventory[j].getItem() instanceof ItemArmor) {
                int k = ((ItemArmor)this.armorInventory[j].getItem()).damageReduceAmount;
                i += k;
            }
            ++j;
        }
        return i;
    }

    public void damageArmor(float damage) {
        if ((damage /= 4.0f) < 1.0f) {
            damage = 1.0f;
        }
        int i = 0;
        while (i < this.armorInventory.length) {
            if (this.armorInventory[i] != null && this.armorInventory[i].getItem() instanceof ItemArmor) {
                this.armorInventory[i].damageItem((int)damage, this.player);
                if (this.armorInventory[i].stackSize == 0) {
                    this.armorInventory[i] = null;
                }
            }
            ++i;
        }
    }

    public void dropAllItems() {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            if (this.mainInventory[i] == null) continue;
            this.player.dropItem(this.mainInventory[i], true, false);
            this.mainInventory[i] = null;
        }
        int j = 0;
        while (j < this.armorInventory.length) {
            if (this.armorInventory[j] != null) {
                this.player.dropItem(this.armorInventory[j], true, false);
                this.armorInventory[j] = null;
            }
            ++j;
        }
    }

    @Override
    public void markDirty() {
        this.inventoryChanged = true;
    }

    public void setItemStack(ItemStack itemStackIn) {
        this.itemStack = itemStackIn;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (this.player.isDead) {
            return false;
        }
        if (!(player.getDistanceSqToEntity(this.player) <= 64.0)) return false;
        return true;
    }

    public boolean hasItemStack(ItemStack itemStackIn) {
        for (int i = 0; i < this.armorInventory.length; ++i) {
            if (this.armorInventory[i] == null || !this.armorInventory[i].isItemEqual(itemStackIn)) continue;
            return true;
        }
        int j = 0;
        while (j < this.mainInventory.length) {
            if (this.mainInventory[j] != null && this.mainInventory[j].isItemEqual(itemStackIn)) {
                return true;
            }
            ++j;
        }
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    public void copyInventory(InventoryPlayer playerInventory) {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            this.mainInventory[i] = ItemStack.copyItemStack(playerInventory.mainInventory[i]);
        }
        int j = 0;
        while (true) {
            if (j >= this.armorInventory.length) {
                this.currentItem = playerInventory.currentItem;
                return;
            }
            this.armorInventory[j] = ItemStack.copyItemStack(playerInventory.armorInventory[j]);
            ++j;
        }
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.mainInventory.length; ++i) {
            this.mainInventory[i] = null;
        }
        int j = 0;
        while (j < this.armorInventory.length) {
            this.armorInventory[j] = null;
            ++j;
        }
    }
}

