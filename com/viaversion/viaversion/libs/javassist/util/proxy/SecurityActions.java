/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.util.proxy;

import com.viaversion.viaversion.libs.javassist.bytecode.ClassFile;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class SecurityActions
extends SecurityManager {
    public static final SecurityActions stack = new SecurityActions();

    SecurityActions() {
    }

    public Class<?> getCallerClass() {
        return this.getClassContext()[2];
    }

    static Method[] getDeclaredMethods(final Class<?> clazz) {
        if (System.getSecurityManager() != null) return AccessController.doPrivileged(new PrivilegedAction<Method[]>(){

            @Override
            public Method[] run() {
                return clazz.getDeclaredMethods();
            }
        });
        return clazz.getDeclaredMethods();
    }

    static Constructor<?>[] getDeclaredConstructors(final Class<?> clazz) {
        if (System.getSecurityManager() != null) return AccessController.doPrivileged(new PrivilegedAction<Constructor<?>[]>(){

            @Override
            public Constructor<?>[] run() {
                return clazz.getDeclaredConstructors();
            }
        });
        return clazz.getDeclaredConstructors();
    }

    static MethodHandle getMethodHandle(final Class<?> clazz, final String name, final Class<?>[] params) throws NoSuchMethodException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<MethodHandle>(){

                @Override
                public MethodHandle run() throws IllegalAccessException, NoSuchMethodException, SecurityException {
                    Method rmet = clazz.getDeclaredMethod(name, params);
                    rmet.setAccessible(true);
                    MethodHandle meth = MethodHandles.lookup().unreflect(rmet);
                    rmet.setAccessible(false);
                    return meth;
                }
            });
        }
        catch (PrivilegedActionException e) {
            if (!(e.getCause() instanceof NoSuchMethodException)) throw new RuntimeException(e.getCause());
            throw (NoSuchMethodException)e.getCause();
        }
    }

    static Method getDeclaredMethod(final Class<?> clazz, final String name, final Class<?>[] types) throws NoSuchMethodException {
        if (System.getSecurityManager() == null) {
            return clazz.getDeclaredMethod(name, types);
        }
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){

                @Override
                public Method run() throws Exception {
                    return clazz.getDeclaredMethod(name, types);
                }
            });
        }
        catch (PrivilegedActionException e) {
            if (!(e.getCause() instanceof NoSuchMethodException)) throw new RuntimeException(e.getCause());
            throw (NoSuchMethodException)e.getCause();
        }
    }

    static Constructor<?> getDeclaredConstructor(final Class<?> clazz, final Class<?>[] types) throws NoSuchMethodException {
        if (System.getSecurityManager() == null) {
            return clazz.getDeclaredConstructor(types);
        }
        try {
            return (Constructor)AccessController.doPrivileged(new PrivilegedExceptionAction<Constructor<?>>(){

                @Override
                public Constructor<?> run() throws Exception {
                    return clazz.getDeclaredConstructor(types);
                }
            });
        }
        catch (PrivilegedActionException e) {
            if (!(e.getCause() instanceof NoSuchMethodException)) throw new RuntimeException(e.getCause());
            throw (NoSuchMethodException)e.getCause();
        }
    }

    static void setAccessible(final AccessibleObject ao, final boolean accessible) {
        if (System.getSecurityManager() == null) {
            ao.setAccessible(accessible);
            return;
        }
        AccessController.doPrivileged(new PrivilegedAction<Void>(){

            @Override
            public Void run() {
                ao.setAccessible(accessible);
                return null;
            }
        });
    }

    static void set(final Field fld, final Object target, final Object value) throws IllegalAccessException {
        if (System.getSecurityManager() == null) {
            fld.set(target, value);
            return;
        }
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>(){

                @Override
                public Void run() throws Exception {
                    fld.set(target, value);
                    return null;
                }
            });
            return;
        }
        catch (PrivilegedActionException e) {
            if (!(e.getCause() instanceof NoSuchMethodException)) throw new RuntimeException(e.getCause());
            throw (IllegalAccessException)e.getCause();
        }
    }

    static TheUnsafe getSunMiscUnsafeAnonymously() throws ClassNotFoundException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<TheUnsafe>(){

                @Override
                public TheUnsafe run() throws ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
                    Class<?> unsafe = Class.forName("sun.misc.Unsafe");
                    Field theUnsafe = unsafe.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    SecurityActions securityActions = stack;
                    Objects.requireNonNull(securityActions);
                    TheUnsafe usf = securityActions.new TheUnsafe(unsafe, theUnsafe.get(null));
                    theUnsafe.setAccessible(false);
                    SecurityActions.disableWarning(usf);
                    return usf;
                }
            });
        }
        catch (PrivilegedActionException e) {
            if (e.getCause() instanceof ClassNotFoundException) {
                throw (ClassNotFoundException)e.getCause();
            }
            if (e.getCause() instanceof NoSuchFieldException) {
                throw new ClassNotFoundException("No such instance.", e.getCause());
            }
            if (e.getCause() instanceof IllegalAccessException) throw new ClassNotFoundException("Security denied access.", e.getCause());
            if (e.getCause() instanceof IllegalAccessException) throw new ClassNotFoundException("Security denied access.", e.getCause());
            if (!(e.getCause() instanceof SecurityException)) throw new RuntimeException(e.getCause());
            throw new ClassNotFoundException("Security denied access.", e.getCause());
        }
    }

    static void disableWarning(TheUnsafe tu) {
        try {
            if (ClassFile.MAJOR_VERSION < 53) {
                return;
            }
            Class<?> cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field logger = cls.getDeclaredField("logger");
            tu.call("putObjectVolatile", cls, tu.call("staticFieldOffset", logger), null);
            return;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    class TheUnsafe {
        final Class<?> unsafe;
        final Object theUnsafe;
        final Map<String, List<Method>> methods = new HashMap<String, List<Method>>();

        TheUnsafe(Class<?> c, Object o) {
            this.unsafe = c;
            this.theUnsafe = o;
            Method[] methodArray = this.unsafe.getDeclaredMethods();
            int n = methodArray.length;
            int n2 = 0;
            while (n2 < n) {
                Method m = methodArray[n2];
                if (!this.methods.containsKey(m.getName())) {
                    this.methods.put(m.getName(), Collections.singletonList(m));
                } else {
                    if (this.methods.get(m.getName()).size() == 1) {
                        this.methods.put(m.getName(), new ArrayList(this.methods.get(m.getName())));
                    }
                    this.methods.get(m.getName()).add(m);
                }
                ++n2;
            }
        }

        private Method getM(String name, Object[] o) {
            return this.methods.get(name).get(0);
        }

        public Object call(String name, Object ... args) {
            try {
                return this.getM(name, args).invoke(this.theUnsafe, args);
            }
            catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }
    }
}

