/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.convert;

import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.CtMethod;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.BadBytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.Bytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeIterator;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.Descriptor;
import com.viaversion.viaversion.libs.javassist.convert.TransformCall;
import com.viaversion.viaversion.libs.javassist.convert.Transformer;

public class TransformBefore
extends TransformCall {
    protected CtClass[] parameterTypes;
    protected int locals;
    protected int maxLocals;
    protected byte[] saveCode;
    protected byte[] loadCode;

    public TransformBefore(Transformer next, CtMethod origMethod, CtMethod beforeMethod) throws NotFoundException {
        super(next, origMethod, beforeMethod);
        this.methodDescriptor = origMethod.getMethodInfo2().getDescriptor();
        this.parameterTypes = origMethod.getParameterTypes();
        this.locals = 0;
        this.maxLocals = 0;
        this.loadCode = null;
        this.saveCode = null;
    }

    @Override
    public void initialize(ConstPool cp, CodeAttribute attr) {
        super.initialize(cp, attr);
        this.locals = 0;
        this.maxLocals = attr.getMaxLocals();
        this.loadCode = null;
        this.saveCode = null;
    }

    @Override
    protected int match(int c, int pos, CodeIterator iterator, int typedesc, ConstPool cp) throws BadBytecode {
        if (this.newIndex == 0) {
            String desc = Descriptor.ofParameters(this.parameterTypes) + 'V';
            desc = Descriptor.insertParameter(this.classname, desc);
            int nt = cp.addNameAndTypeInfo(this.newMethodname, desc);
            int ci = cp.addClassInfo(this.newClassname);
            this.newIndex = cp.addMethodrefInfo(ci, nt);
            this.constPool = cp;
        }
        if (this.saveCode != null) return this.match2(pos, iterator);
        this.makeCode(this.parameterTypes, cp);
        return this.match2(pos, iterator);
    }

    protected int match2(int pos, CodeIterator iterator) throws BadBytecode {
        iterator.move(pos);
        iterator.insert(this.saveCode);
        iterator.insert(this.loadCode);
        int p = iterator.insertGap(3);
        iterator.writeByte(184, p);
        iterator.write16bit(this.newIndex, p + 1);
        iterator.insert(this.loadCode);
        return iterator.next();
    }

    @Override
    public int extraLocals() {
        return this.locals;
    }

    protected void makeCode(CtClass[] paramTypes, ConstPool cp) {
        Bytecode save = new Bytecode(cp, 0, 0);
        Bytecode load = new Bytecode(cp, 0, 0);
        int var = this.maxLocals;
        int len = paramTypes == null ? 0 : paramTypes.length;
        load.addAload(var);
        this.makeCode2(save, load, 0, len, paramTypes, var + 1);
        save.addAstore(var);
        this.saveCode = save.get();
        this.loadCode = load.get();
    }

    private void makeCode2(Bytecode save, Bytecode load, int i, int n, CtClass[] paramTypes, int var) {
        if (i < n) {
            int size = load.addLoad(var, paramTypes[i]);
            this.makeCode2(save, load, i + 1, n, paramTypes, var + size);
            save.addStore(var, paramTypes[i]);
            return;
        }
        this.locals = var - this.maxLocals;
    }
}

