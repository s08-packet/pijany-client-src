/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3i;

public enum EnumFacing implements IStringSerializable
{
    DOWN("DOWN", 0, 0, 1, -1, "down", AxisDirection.NEGATIVE, Axis.Y, new Vec3i(0, -1, 0)),
    UP("UP", 1, 1, 0, -1, "up", AxisDirection.POSITIVE, Axis.Y, new Vec3i(0, 1, 0)),
    NORTH("NORTH", 2, 2, 3, 2, "north", AxisDirection.NEGATIVE, Axis.Z, new Vec3i(0, 0, -1)),
    SOUTH("SOUTH", 3, 3, 2, 0, "south", AxisDirection.POSITIVE, Axis.Z, new Vec3i(0, 0, 1)),
    WEST("WEST", 4, 4, 5, 1, "west", AxisDirection.NEGATIVE, Axis.X, new Vec3i(-1, 0, 0)),
    EAST("EAST", 5, 5, 4, 3, "east", AxisDirection.POSITIVE, Axis.X, new Vec3i(1, 0, 0));

    private final int index;
    private final int opposite;
    private final int horizontalIndex;
    private final String name;
    private final Axis axis;
    private final AxisDirection axisDirection;
    private final Vec3i directionVec;
    public static final EnumFacing[] VALUES;
    private static final EnumFacing[] HORIZONTALS;
    private static final Map NAME_LOOKUP;
    private static final EnumFacing[] $VALUES;
    private static final String __OBFID = "CL_00001201";

    private EnumFacing(String p_i13_3_, int p_i13_4_, int p_i13_5_, int p_i13_6_, int p_i13_7_, String p_i13_8_, AxisDirection p_i13_9_, Axis p_i13_10_, Vec3i p_i13_11_) {
        this.index = p_i13_5_;
        this.horizontalIndex = p_i13_7_;
        this.opposite = p_i13_6_;
        this.name = p_i13_8_;
        this.axis = p_i13_10_;
        this.axisDirection = p_i13_9_;
        this.directionVec = p_i13_11_;
    }

    public int getIndex() {
        return this.index;
    }

    public int getHorizontalIndex() {
        return this.horizontalIndex;
    }

    public AxisDirection getAxisDirection() {
        return this.axisDirection;
    }

    public EnumFacing getOpposite() {
        return EnumFacing.getFront(this.opposite);
    }

    public EnumFacing rotateAround(Axis axis) {
        switch (EnumFacing.1.field_179515_a[axis.ordinal()]) {
            case 1: {
                if (this == WEST) return this;
                if (this == EAST) return this;
                return this.rotateX();
            }
            case 2: {
                if (this == UP) return this;
                if (this == DOWN) return this;
                return this.rotateY();
            }
            case 3: {
                if (this == NORTH) return this;
                if (this == SOUTH) return this;
                return this.rotateZ();
            }
        }
        throw new IllegalStateException("Unable to get CW facing for axis " + axis);
    }

    public EnumFacing rotateY() {
        switch (this) {
            case NORTH: {
                return EAST;
            }
            case EAST: {
                return SOUTH;
            }
            case SOUTH: {
                return WEST;
            }
            case WEST: {
                return NORTH;
            }
        }
        throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
    }

    private EnumFacing rotateX() {
        switch (this) {
            case NORTH: {
                return DOWN;
            }
            default: {
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
            }
            case SOUTH: {
                return UP;
            }
            case UP: {
                return NORTH;
            }
            case DOWN: 
        }
        return SOUTH;
    }

    private EnumFacing rotateZ() {
        switch (this) {
            case EAST: {
                return DOWN;
            }
            default: {
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
            }
            case WEST: {
                return UP;
            }
            case UP: {
                return EAST;
            }
            case DOWN: 
        }
        return WEST;
    }

    public EnumFacing rotateYCCW() {
        switch (this) {
            case NORTH: {
                return WEST;
            }
            case EAST: {
                return NORTH;
            }
            case SOUTH: {
                return EAST;
            }
            case WEST: {
                return SOUTH;
            }
        }
        throw new IllegalStateException("Unable to get CCW facing of " + this);
    }

    public int getFrontOffsetX() {
        if (this.axis != Axis.X) return 0;
        int n = this.axisDirection.getOffset();
        return n;
    }

    public int getFrontOffsetY() {
        if (this.axis != Axis.Y) return 0;
        int n = this.axisDirection.getOffset();
        return n;
    }

    public int getFrontOffsetZ() {
        if (this.axis != Axis.Z) return 0;
        int n = this.axisDirection.getOffset();
        return n;
    }

    public String getName2() {
        return this.name;
    }

    public Axis getAxis() {
        return this.axis;
    }

    public static EnumFacing byName(String name) {
        if (name == null) {
            return null;
        }
        EnumFacing enumFacing = (EnumFacing)NAME_LOOKUP.get(name.toLowerCase());
        return enumFacing;
    }

    public static EnumFacing getFront(int index) {
        return VALUES[MathHelper.abs_int(index % VALUES.length)];
    }

    public static EnumFacing getHorizontal(int p_176731_0_) {
        return HORIZONTALS[MathHelper.abs_int(p_176731_0_ % HORIZONTALS.length)];
    }

    public static EnumFacing fromAngle(double angle) {
        return EnumFacing.getHorizontal(MathHelper.floor_double(angle / 90.0 + 0.5) & 3);
    }

    public static EnumFacing random(Random rand) {
        return EnumFacing.values()[rand.nextInt(EnumFacing.values().length)];
    }

    public static EnumFacing getFacingFromVector(float p_176737_0_, float p_176737_1_, float p_176737_2_) {
        EnumFacing enumfacing = NORTH;
        float f = Float.MIN_VALUE;
        EnumFacing[] enumFacingArray = EnumFacing.values();
        int n = enumFacingArray.length;
        int n2 = 0;
        while (n2 < n) {
            EnumFacing enumfacing1 = enumFacingArray[n2];
            float f1 = p_176737_0_ * (float)enumfacing1.directionVec.getX() + p_176737_1_ * (float)enumfacing1.directionVec.getY() + p_176737_2_ * (float)enumfacing1.directionVec.getZ();
            if (f1 > f) {
                f = f1;
                enumfacing = enumfacing1;
            }
            ++n2;
        }
        return enumfacing;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static EnumFacing func_181076_a(AxisDirection p_181076_0_, Axis p_181076_1_) {
        EnumFacing[] enumFacingArray = EnumFacing.values();
        int n = enumFacingArray.length;
        int n2 = 0;
        while (n2 < n) {
            EnumFacing enumfacing = enumFacingArray[n2];
            if (enumfacing.getAxisDirection() == p_181076_0_ && enumfacing.getAxis() == p_181076_1_) {
                return enumfacing;
            }
            ++n2;
        }
        throw new IllegalArgumentException("No such direction: " + (Object)((Object)p_181076_0_) + " " + p_181076_1_);
    }

    public Vec3i getDirectionVec() {
        return this.directionVec;
    }

    static {
        VALUES = new EnumFacing[6];
        HORIZONTALS = new EnumFacing[4];
        NAME_LOOKUP = Maps.newHashMap();
        $VALUES = new EnumFacing[]{DOWN, UP, NORTH, SOUTH, WEST, EAST};
        EnumFacing[] enumFacingArray = EnumFacing.values();
        int n = enumFacingArray.length;
        int n2 = 0;
        while (n2 < n) {
            EnumFacing enumfacing;
            EnumFacing.VALUES[enumfacing.index] = enumfacing = enumFacingArray[n2];
            if (enumfacing.getAxis().isHorizontal()) {
                EnumFacing.HORIZONTALS[enumfacing.horizontalIndex] = enumfacing;
            }
            NAME_LOOKUP.put(enumfacing.getName2().toLowerCase(), enumfacing);
            ++n2;
        }
    }

    public static enum Plane implements Predicate,
    Iterable
    {
        HORIZONTAL("HORIZONTAL", 0),
        VERTICAL("VERTICAL", 1);

        private static final Plane[] $VALUES;
        private static final String __OBFID = "CL_00002319";

        private Plane(String p_i12_3_, int p_i12_4_) {
        }

        public EnumFacing[] facings() {
            switch (this) {
                case HORIZONTAL: {
                    return new EnumFacing[]{NORTH, EAST, SOUTH, WEST};
                }
                case VERTICAL: {
                    return new EnumFacing[]{UP, DOWN};
                }
            }
            throw new Error("Someone's been tampering with the universe!");
        }

        public EnumFacing random(Random rand) {
            EnumFacing[] aenumfacing = this.facings();
            return aenumfacing[rand.nextInt(aenumfacing.length)];
        }

        public boolean apply(EnumFacing p_apply_1_) {
            if (p_apply_1_ == null) return false;
            if (p_apply_1_.getAxis().getPlane() != this) return false;
            return true;
        }

        public Iterator iterator() {
            return Iterators.forArray(this.facings());
        }

        public boolean apply(Object p_apply_1_) {
            return this.apply((EnumFacing)p_apply_1_);
        }

        static {
            $VALUES = new Plane[]{HORIZONTAL, VERTICAL};
        }
    }

    public static enum AxisDirection {
        POSITIVE("POSITIVE", 0, 1, "Towards positive"),
        NEGATIVE("NEGATIVE", 1, -1, "Towards negative");

        private final int offset;
        private final String description;
        private static final AxisDirection[] $VALUES;
        private static final String __OBFID = "CL_00002320";

        private AxisDirection(String p_i11_3_, int p_i11_4_, int p_i11_5_, String p_i11_6_) {
            this.offset = p_i11_5_;
            this.description = p_i11_6_;
        }

        public int getOffset() {
            return this.offset;
        }

        public String toString() {
            return this.description;
        }

        static {
            $VALUES = new AxisDirection[]{POSITIVE, NEGATIVE};
        }
    }

    public static enum Axis implements Predicate,
    IStringSerializable
    {
        X("X", 0, "x", Plane.HORIZONTAL),
        Y("Y", 1, "y", Plane.VERTICAL),
        Z("Z", 2, "z", Plane.HORIZONTAL);

        private static final Map NAME_LOOKUP;
        private final String name;
        private final Plane plane;
        private static final Axis[] $VALUES;
        private static final String __OBFID = "CL_00002321";

        private Axis(String p_i10_3_, int p_i10_4_, String p_i10_5_, Plane p_i10_6_) {
            this.name = p_i10_5_;
            this.plane = p_i10_6_;
        }

        public static Axis byName(String name) {
            if (name == null) {
                return null;
            }
            Axis axis = (Axis)NAME_LOOKUP.get(name.toLowerCase());
            return axis;
        }

        public String getName2() {
            return this.name;
        }

        public boolean isVertical() {
            if (this.plane != Plane.VERTICAL) return false;
            return true;
        }

        public boolean isHorizontal() {
            if (this.plane != Plane.HORIZONTAL) return false;
            return true;
        }

        public String toString() {
            return this.name;
        }

        public boolean apply(EnumFacing p_apply_1_) {
            if (p_apply_1_ == null) return false;
            if (p_apply_1_.getAxis() != this) return false;
            return true;
        }

        public Plane getPlane() {
            return this.plane;
        }

        @Override
        public String getName() {
            return this.name;
        }

        public boolean apply(Object p_apply_1_) {
            return this.apply((EnumFacing)p_apply_1_);
        }

        static {
            NAME_LOOKUP = Maps.newHashMap();
            $VALUES = new Axis[]{X, Y, Z};
            Axis[] axisArray = Axis.values();
            int n = axisArray.length;
            int n2 = 0;
            while (n2 < n) {
                Axis enumfacing$axis = axisArray[n2];
                NAME_LOOKUP.put(enumfacing$axis.getName2().toLowerCase(), enumfacing$axis);
                ++n2;
            }
        }
    }
}

