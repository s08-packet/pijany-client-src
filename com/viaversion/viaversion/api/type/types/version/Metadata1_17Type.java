/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.api.type.types.version;

import com.viaversion.viaversion.api.minecraft.metadata.MetaType;
import com.viaversion.viaversion.api.type.types.minecraft.ModernMetaType;
import com.viaversion.viaversion.api.type.types.version.Types1_17;

@Deprecated
public class Metadata1_17Type
extends ModernMetaType {
    @Override
    protected MetaType getType(int index) {
        return Types1_17.META_TYPES.byId(index);
    }
}

