/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist;

import com.viaversion.viaversion.libs.javassist.CannotCompileException;
import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.ClassPoolTail;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.Translator;
import com.viaversion.viaversion.libs.javassist.bytecode.ClassFile;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class Loader
extends ClassLoader {
    private HashMap<String, ClassLoader> notDefinedHere;
    private Vector<String> notDefinedPackages;
    private ClassPool source;
    private Translator translator;
    private ProtectionDomain domain;
    public boolean doDelegation = true;

    public Loader() {
        this((ClassPool)null);
    }

    public Loader(ClassPool cp) {
        this.init(cp);
    }

    public Loader(ClassLoader parent, ClassPool cp) {
        super(parent);
        this.init(cp);
    }

    private void init(ClassPool cp) {
        this.notDefinedHere = new HashMap();
        this.notDefinedPackages = new Vector();
        this.source = cp;
        this.translator = null;
        this.domain = null;
        this.delegateLoadingOf("com.viaversion.viaversion.libs.javassist.Loader");
    }

    public void delegateLoadingOf(String classname) {
        if (classname.endsWith(".")) {
            this.notDefinedPackages.addElement(classname);
            return;
        }
        this.notDefinedHere.put(classname, this);
    }

    public void setDomain(ProtectionDomain d) {
        this.domain = d;
    }

    public void setClassPool(ClassPool cp) {
        this.source = cp;
    }

    public void addTranslator(ClassPool cp, Translator t) throws NotFoundException, CannotCompileException {
        this.source = cp;
        this.translator = t;
        t.start(cp);
    }

    public static void main(String[] args) throws Throwable {
        Loader cl = new Loader();
        cl.run(args);
    }

    public void run(String[] args) throws Throwable {
        if (args.length < 1) return;
        this.run(args[0], Arrays.copyOfRange(args, 1, args.length));
    }

    public void run(String classname, String[] args) throws Throwable {
        Class<?> c = this.loadClass(classname);
        try {
            c.getDeclaredMethod("main", String[].class).invoke(null, new Object[]{args});
            return;
        }
        catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassFormatError, ClassNotFoundException {
        String string = name = name.intern();
        synchronized (string) {
            Class<?> c = this.findLoadedClass(name);
            if (c == null) {
                c = this.loadClassByDelegation(name);
            }
            if (c == null) {
                c = this.findClass(name);
            }
            if (c == null) {
                c = this.delegateToParent(name);
            }
            if (!resolve) return c;
            this.resolveClass(c);
            return c;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String pname;
        byte[] classfile;
        block10: {
            try {
                if (this.source != null) {
                    if (this.translator != null) {
                        this.translator.onLoad(this.source, name);
                    }
                    try {
                        classfile = this.source.get(name).toBytecode();
                        break block10;
                    }
                    catch (NotFoundException e) {
                        return null;
                    }
                }
                String jarname = "/" + name.replace('.', '/') + ".class";
                InputStream in = this.getClass().getResourceAsStream(jarname);
                if (in == null) {
                    return null;
                }
                classfile = ClassPoolTail.readStream(in);
            }
            catch (Exception e) {
                throw new ClassNotFoundException("caught an exception while obtaining a class file for " + name, e);
            }
        }
        int i = name.lastIndexOf(46);
        if (i != -1 && this.isDefinedPackage(pname = name.substring(0, i))) {
            try {
                this.definePackage(pname, null, null, null, null, null, null, null);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        if (this.domain != null) return this.defineClass(name, classfile, 0, classfile.length, this.domain);
        return this.defineClass(name, classfile, 0, classfile.length);
    }

    private boolean isDefinedPackage(String name) {
        if (ClassFile.MAJOR_VERSION >= 53) {
            if (this.getDefinedPackage(name) != null) return false;
            return true;
        }
        if (this.getPackage(name) != null) return false;
        return true;
    }

    protected Class<?> loadClassByDelegation(String name) throws ClassNotFoundException {
        Class<?> c = null;
        if (!this.doDelegation) return c;
        if (name.startsWith("java.")) return this.delegateToParent(name);
        if (name.startsWith("javax.")) return this.delegateToParent(name);
        if (name.startsWith("sun.")) return this.delegateToParent(name);
        if (name.startsWith("com.sun.")) return this.delegateToParent(name);
        if (name.startsWith("org.w3c.")) return this.delegateToParent(name);
        if (name.startsWith("org.xml.")) return this.delegateToParent(name);
        if (!this.notDelegated(name)) return c;
        return this.delegateToParent(name);
    }

    private boolean notDelegated(String name) {
        String pack;
        if (this.notDefinedHere.containsKey(name)) {
            return true;
        }
        Iterator<String> iterator = this.notDefinedPackages.iterator();
        do {
            if (!iterator.hasNext()) return false;
        } while (!name.startsWith(pack = iterator.next()));
        return true;
    }

    protected Class<?> delegateToParent(String classname) throws ClassNotFoundException {
        ClassLoader cl = this.getParent();
        if (cl == null) return this.findSystemClass(classname);
        return cl.loadClass(classname);
    }

    public static class Simple
    extends ClassLoader {
        public Simple() {
        }

        public Simple(ClassLoader parent) {
            super(parent);
        }

        public Class<?> invokeDefineClass(CtClass cc) throws IOException, CannotCompileException {
            byte[] code = cc.toBytecode();
            return this.defineClass(cc.getName(), code, 0, code.length);
        }
    }
}

