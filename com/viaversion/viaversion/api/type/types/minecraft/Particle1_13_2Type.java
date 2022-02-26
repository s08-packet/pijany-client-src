/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package com.viaversion.viaversion.api.type.types.minecraft;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.Particle;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;

@Deprecated
public class Particle1_13_2Type
extends Type<Particle> {
    public Particle1_13_2Type() {
        super("Particle", Particle.class);
    }

    @Override
    public void write(ByteBuf buffer, Particle object) throws Exception {
        Type.VAR_INT.writePrimitive(buffer, object.getId());
        Iterator<Particle.ParticleData> iterator = object.getArguments().iterator();
        while (iterator.hasNext()) {
            Particle.ParticleData data = iterator.next();
            data.getType().write(buffer, data.getValue());
        }
    }

    @Override
    public Particle read(ByteBuf buffer) throws Exception {
        int type = Type.VAR_INT.readPrimitive(buffer);
        Particle particle = new Particle(type);
        switch (type) {
            case 3: 
            case 20: {
                particle.getArguments().add(new Particle.ParticleData(Type.VAR_INT, Type.VAR_INT.readPrimitive(buffer)));
                return particle;
            }
            case 11: {
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Float.valueOf(Type.FLOAT.readPrimitive(buffer))));
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Float.valueOf(Type.FLOAT.readPrimitive(buffer))));
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Float.valueOf(Type.FLOAT.readPrimitive(buffer))));
                particle.getArguments().add(new Particle.ParticleData(Type.FLOAT, Float.valueOf(Type.FLOAT.readPrimitive(buffer))));
                return particle;
            }
            case 27: {
                particle.getArguments().add(new Particle.ParticleData(Type.FLAT_VAR_INT_ITEM, Type.FLAT_VAR_INT_ITEM.read(buffer)));
                return particle;
            }
        }
        return particle;
    }
}

