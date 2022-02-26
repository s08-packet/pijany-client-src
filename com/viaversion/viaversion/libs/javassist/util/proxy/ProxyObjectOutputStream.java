/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.util.proxy;

import com.viaversion.viaversion.libs.javassist.util.proxy.Proxy;
import com.viaversion.viaversion.libs.javassist.util.proxy.ProxyFactory;
import com.viaversion.viaversion.libs.javassist.util.proxy.ProxyObject;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

public class ProxyObjectOutputStream
extends ObjectOutputStream {
    public ProxyObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        Class<?> cl = desc.forClass();
        if (!ProxyFactory.isProxyClass(cl)) {
            this.writeBoolean(false);
            super.writeClassDescriptor(desc);
            return;
        }
        this.writeBoolean(true);
        Class<?> superClass = cl.getSuperclass();
        Class<?>[] interfaces = cl.getInterfaces();
        byte[] signature = ProxyFactory.getFilterSignature(cl);
        String name = superClass.getName();
        this.writeObject(name);
        this.writeInt(interfaces.length - 1);
        int i = 0;
        while (true) {
            if (i >= interfaces.length) {
                this.writeInt(signature.length);
                this.write(signature);
                return;
            }
            Class<?> interfaze = interfaces[i];
            if (interfaze != ProxyObject.class && interfaze != Proxy.class) {
                name = interfaces[i].getName();
                this.writeObject(name);
            }
            ++i;
        }
    }
}

