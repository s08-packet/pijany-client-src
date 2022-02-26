/*
 * Decompiled with CFR 0.152.
 */
package drunkclient.beta.API.events.world;

import drunkclient.beta.API.Event;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;

public class EventCollide
extends Event {
    private AxisAlignedBB axisalignedbb;
    private Block block;
    private Entity collidingEntity;
    private int x;
    private int y;
    private int z;

    public EventCollide(Entity collidingEntity, int x, int y, int z, AxisAlignedBB axisalignedbb, Block block) {
        this.collidingEntity = collidingEntity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.axisalignedbb = axisalignedbb;
        this.block = block;
    }

    public AxisAlignedBB getBoundingBox() {
        return this.axisalignedbb;
    }

    public Entity getCollidingEntity() {
        return this.collidingEntity;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public Block getBlock() {
        return this.block;
    }

    public void setBoundingBox(AxisAlignedBB object) {
        this.axisalignedbb = object;
    }
}

