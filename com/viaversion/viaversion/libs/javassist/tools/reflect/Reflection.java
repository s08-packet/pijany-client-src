/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.tools.reflect;

import com.viaversion.viaversion.libs.javassist.CannotCompileException;
import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.CodeConverter;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.CtField;
import com.viaversion.viaversion.libs.javassist.CtMethod;
import com.viaversion.viaversion.libs.javassist.CtNewMethod;
import com.viaversion.viaversion.libs.javassist.Modifier;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.Translator;
import com.viaversion.viaversion.libs.javassist.bytecode.BadBytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.ClassFile;
import com.viaversion.viaversion.libs.javassist.bytecode.MethodInfo;
import com.viaversion.viaversion.libs.javassist.tools.reflect.CannotReflectException;
import java.util.Iterator;

public class Reflection
implements Translator {
    static final String classobjectField = "_classobject";
    static final String classobjectAccessor = "_getClass";
    static final String metaobjectField = "_metaobject";
    static final String metaobjectGetter = "_getMetaobject";
    static final String metaobjectSetter = "_setMetaobject";
    static final String readPrefix = "_r_";
    static final String writePrefix = "_w_";
    static final String metaobjectClassName = "com.viaversion.viaversion.libs.javassist.tools.reflect.Metaobject";
    static final String classMetaobjectClassName = "com.viaversion.viaversion.libs.javassist.tools.reflect.ClassMetaobject";
    protected CtMethod trapMethod;
    protected CtMethod trapStaticMethod;
    protected CtMethod trapRead;
    protected CtMethod trapWrite;
    protected CtClass[] readParam;
    protected ClassPool classPool = null;
    protected CodeConverter converter = new CodeConverter();

    private boolean isExcluded(String name) {
        if (name.startsWith("_m_")) return true;
        if (name.equals(classobjectAccessor)) return true;
        if (name.equals(metaobjectSetter)) return true;
        if (name.equals(metaobjectGetter)) return true;
        if (name.startsWith(readPrefix)) return true;
        if (name.startsWith(writePrefix)) return true;
        return false;
    }

    @Override
    public void start(ClassPool pool) throws NotFoundException {
        this.classPool = pool;
        String msg = "com.viaversion.viaversion.libs.javassist.tools.reflect.Sample is not found or broken.";
        try {
            CtClass c = this.classPool.get("com.viaversion.viaversion.libs.javassist.tools.reflect.Sample");
            this.rebuildClassFile(c.getClassFile());
            this.trapMethod = c.getDeclaredMethod("trap");
            this.trapStaticMethod = c.getDeclaredMethod("trapStatic");
            this.trapRead = c.getDeclaredMethod("trapRead");
            this.trapWrite = c.getDeclaredMethod("trapWrite");
            this.readParam = new CtClass[]{this.classPool.get("java.lang.Object")};
            return;
        }
        catch (NotFoundException e) {
            throw new RuntimeException("com.viaversion.viaversion.libs.javassist.tools.reflect.Sample is not found or broken.");
        }
        catch (BadBytecode e) {
            throw new RuntimeException("com.viaversion.viaversion.libs.javassist.tools.reflect.Sample is not found or broken.");
        }
    }

    @Override
    public void onLoad(ClassPool pool, String classname) throws CannotCompileException, NotFoundException {
        CtClass clazz = pool.get(classname);
        clazz.instrument(this.converter);
    }

    public boolean makeReflective(String classname, String metaobject, String metaclass) throws CannotCompileException, NotFoundException {
        return this.makeReflective(this.classPool.get(classname), this.classPool.get(metaobject), this.classPool.get(metaclass));
    }

    public boolean makeReflective(Class<?> clazz, Class<?> metaobject, Class<?> metaclass) throws CannotCompileException, NotFoundException {
        return this.makeReflective(clazz.getName(), metaobject.getName(), metaclass.getName());
    }

    public boolean makeReflective(CtClass clazz, CtClass metaobject, CtClass metaclass) throws CannotCompileException, CannotReflectException, NotFoundException {
        if (clazz.isInterface()) {
            throw new CannotReflectException("Cannot reflect an interface: " + clazz.getName());
        }
        if (clazz.subclassOf(this.classPool.get(classMetaobjectClassName))) {
            throw new CannotReflectException("Cannot reflect a subclass of ClassMetaobject: " + clazz.getName());
        }
        if (clazz.subclassOf(this.classPool.get(metaobjectClassName))) {
            throw new CannotReflectException("Cannot reflect a subclass of Metaobject: " + clazz.getName());
        }
        this.registerReflectiveClass(clazz);
        return this.modifyClassfile(clazz, metaobject, metaclass);
    }

    private void registerReflectiveClass(CtClass clazz) {
        CtField[] fs = clazz.getDeclaredFields();
        int i = 0;
        while (i < fs.length) {
            CtField f = fs[i];
            int mod = f.getModifiers();
            if ((mod & 1) != 0 && (mod & 0x10) == 0) {
                String name = f.getName();
                this.converter.replaceFieldRead(f, clazz, readPrefix + name);
                this.converter.replaceFieldWrite(f, clazz, writePrefix + name);
            }
            ++i;
        }
    }

    private boolean modifyClassfile(CtClass clazz, CtClass metaobject, CtClass metaclass) throws CannotCompileException, NotFoundException {
        CtField f;
        boolean addMeta;
        if (clazz.getAttribute("Reflective") != null) {
            return false;
        }
        clazz.setAttribute("Reflective", new byte[0]);
        CtClass mlevel = this.classPool.get("com.viaversion.viaversion.libs.javassist.tools.reflect.Metalevel");
        boolean bl = addMeta = !clazz.subtypeOf(mlevel);
        if (addMeta) {
            clazz.addInterface(mlevel);
        }
        this.processMethods(clazz, addMeta);
        this.processFields(clazz);
        if (addMeta) {
            f = new CtField(this.classPool.get(metaobjectClassName), metaobjectField, clazz);
            f.setModifiers(4);
            clazz.addField(f, CtField.Initializer.byNewWithParams(metaobject));
            clazz.addMethod(CtNewMethod.getter(metaobjectGetter, f));
            clazz.addMethod(CtNewMethod.setter(metaobjectSetter, f));
        }
        f = new CtField(this.classPool.get(classMetaobjectClassName), classobjectField, clazz);
        f.setModifiers(10);
        clazz.addField(f, CtField.Initializer.byNew(metaclass, new String[]{clazz.getName()}));
        clazz.addMethod(CtNewMethod.getter(classobjectAccessor, f));
        return true;
    }

    private void processMethods(CtClass clazz, boolean dontSearch) throws CannotCompileException, NotFoundException {
        CtMethod[] ms = clazz.getMethods();
        int i = 0;
        while (i < ms.length) {
            CtMethod m = ms[i];
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) && !Modifier.isAbstract(mod)) {
                this.processMethods0(mod, clazz, m, i, dontSearch);
            }
            ++i;
        }
    }

    private void processMethods0(int mod, CtClass clazz, CtMethod m, int identifier, boolean dontSearch) throws CannotCompileException, NotFoundException {
        CtMethod m2;
        String name = m.getName();
        if (this.isExcluded(name)) {
            return;
        }
        if (m.getDeclaringClass() == clazz) {
            if (Modifier.isNative(mod)) {
                return;
            }
            m2 = m;
            if (Modifier.isFinal(mod)) {
                m2.setModifiers(mod &= 0xFFFFFFEF);
            }
        } else {
            if (Modifier.isFinal(mod)) {
                return;
            }
            m2 = CtNewMethod.delegator(this.findOriginal(m, dontSearch), clazz);
            m2.setModifiers(mod &= 0xFFFFFEFF);
            clazz.addMethod(m2);
        }
        m2.setName("_m_" + identifier + "_" + name);
        CtMethod body = Modifier.isStatic(mod) ? this.trapStaticMethod : this.trapMethod;
        CtMethod wmethod = CtNewMethod.wrapped(m.getReturnType(), name, m.getParameterTypes(), m.getExceptionTypes(), body, CtMethod.ConstParameter.integer(identifier), clazz);
        wmethod.setModifiers(mod);
        clazz.addMethod(wmethod);
    }

    private CtMethod findOriginal(CtMethod m, boolean dontSearch) throws NotFoundException {
        if (dontSearch) {
            return m;
        }
        String name = m.getName();
        CtMethod[] ms = m.getDeclaringClass().getDeclaredMethods();
        int i = 0;
        while (i < ms.length) {
            String orgName = ms[i].getName();
            if (orgName.endsWith(name) && orgName.startsWith("_m_") && ms[i].getSignature().equals(m.getSignature())) {
                return ms[i];
            }
            ++i;
        }
        return m;
    }

    private void processFields(CtClass clazz) throws CannotCompileException, NotFoundException {
        CtField[] fs = clazz.getDeclaredFields();
        int i = 0;
        while (i < fs.length) {
            CtField f = fs[i];
            int mod = f.getModifiers();
            if ((mod & 1) != 0 && (mod & 0x10) == 0) {
                String name = f.getName();
                CtClass ftype = f.getType();
                CtMethod wmethod = CtNewMethod.wrapped(ftype, readPrefix + name, this.readParam, null, this.trapRead, CtMethod.ConstParameter.string(name), clazz);
                wmethod.setModifiers(mod |= 8);
                clazz.addMethod(wmethod);
                CtClass[] writeParam = new CtClass[]{this.classPool.get("java.lang.Object"), ftype};
                wmethod = CtNewMethod.wrapped(CtClass.voidType, writePrefix + name, writeParam, null, this.trapWrite, CtMethod.ConstParameter.string(name), clazz);
                wmethod.setModifiers(mod);
                clazz.addMethod(wmethod);
            }
            ++i;
        }
    }

    public void rebuildClassFile(ClassFile cf) throws BadBytecode {
        if (ClassFile.MAJOR_VERSION < 50) {
            return;
        }
        Iterator<MethodInfo> iterator = cf.getMethods().iterator();
        while (iterator.hasNext()) {
            MethodInfo mi = iterator.next();
            mi.rebuildStackMap(this.classPool);
        }
    }
}

