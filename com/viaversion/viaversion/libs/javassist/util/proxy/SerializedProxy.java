/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.util.proxy;

import com.viaversion.viaversion.libs.javassist.util.proxy.MethodHandler;
import com.viaversion.viaversion.libs.javassist.util.proxy.Proxy;
import com.viaversion.viaversion.libs.javassist.util.proxy.ProxyFactory;
import com.viaversion.viaversion.libs.javassist.util.proxy.ProxyObject;
import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

class SerializedProxy
implements Serializable {
    private static final long serialVersionUID = 1L;
    private String superClass;
    private String[] interfaces;
    private byte[] filterSignature;
    private MethodHandler handler;

    SerializedProxy(Class<?> proxy, byte[] sig, MethodHandler h) {
        this.filterSignature = sig;
        this.handler = h;
        this.superClass = proxy.getSuperclass().getName();
        Class<?>[] infs = proxy.getInterfaces();
        int n = infs.length;
        this.interfaces = new String[n - 1];
        String setterInf = ProxyObject.class.getName();
        String setterInf2 = Proxy.class.getName();
        int i = 0;
        while (i < n) {
            String name = infs[i].getName();
            if (!name.equals(setterInf) && !name.equals(setterInf2)) {
                this.interfaces[i] = name;
            }
            ++i;
        }
    }

    protected Class<?> loadClass(final String className) throws ClassNotFoundException {
        try {
            return (Class)AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>(){

                @Override
                public Class<?> run() throws Exception {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    return Class.forName(className, true, cl);
                }
            });
        }
        catch (PrivilegedActionException pae) {
            throw new RuntimeException("cannot load the class: " + className, pae.getException());
        }
    }

    Object readResolve() throws ObjectStreamException {
        try {
            int n = this.interfaces.length;
            Class[] infs = new Class[n];
            int i = 0;
            while (true) {
                if (i >= n) {
                    ProxyFactory f = new ProxyFactory();
                    f.setSuperclass(this.loadClass(this.superClass));
                    f.setInterfaces(infs);
                    Proxy proxy = (Proxy)f.createClass(this.filterSignature).getConstructor(new Class[0]).newInstance(new Object[0]);
                    proxy.setHandler(this.handler);
                    return proxy;
                }
                infs[i] = this.loadClass(this.interfaces[i]);
                ++i;
            }
        }
        catch (NoSuchMethodException e) {
            throw new InvalidClassException(e.getMessage());
        }
        catch (InvocationTargetException e) {
            throw new InvalidClassException(e.getMessage());
        }
        catch (ClassNotFoundException e) {
            throw new InvalidClassException(e.getMessage());
        }
        catch (InstantiationException e2) {
            throw new InvalidObjectException(e2.getMessage());
        }
        catch (IllegalAccessException e3) {
            throw new InvalidClassException(e3.getMessage());
        }
    }
}

