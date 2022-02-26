/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode.annotation;

import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.AnnotationDefaultAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.ClassFile;
import com.viaversion.viaversion.libs.javassist.bytecode.MethodInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.Annotation;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.MemberValue;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AnnotationImpl
implements InvocationHandler {
    private static final String JDK_ANNOTATION_CLASS_NAME = "java.lang.annotation.Annotation";
    private static Method JDK_ANNOTATION_TYPE_METHOD = null;
    private Annotation annotation;
    private ClassPool pool;
    private ClassLoader classLoader;
    private transient Class<?> annotationType;
    private transient int cachedHashCode = Integer.MIN_VALUE;

    public static Object make(ClassLoader cl, Class<?> clazz, ClassPool cp, Annotation anon) throws IllegalArgumentException {
        AnnotationImpl handler = new AnnotationImpl(anon, cp, cl);
        return Proxy.newProxyInstance(cl, new Class[]{clazz}, handler);
    }

    private AnnotationImpl(Annotation a, ClassPool cp, ClassLoader loader) {
        this.annotation = a;
        this.pool = cp;
        this.classLoader = loader;
    }

    public String getTypeName() {
        return this.annotation.getTypeName();
    }

    private Class<?> getAnnotationType() {
        if (this.annotationType != null) return this.annotationType;
        String typeName = this.annotation.getTypeName();
        try {
            this.annotationType = this.classLoader.loadClass(typeName);
            return this.annotationType;
        }
        catch (ClassNotFoundException e) {
            NoClassDefFoundError error = new NoClassDefFoundError("Error loading annotation class: " + typeName);
            error.setStackTrace(e.getStackTrace());
            throw error;
        }
    }

    public Annotation getAnnotation() {
        return this.annotation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MemberValue mv;
        String name = method.getName();
        if (Object.class == method.getDeclaringClass()) {
            if ("equals".equals(name)) {
                Object obj = args[0];
                return this.checkEquals(obj);
            }
            if ("toString".equals(name)) {
                return this.annotation.toString();
            }
            if ("hashCode".equals(name)) {
                return this.hashCode();
            }
        } else if ("annotationType".equals(name) && method.getParameterTypes().length == 0) {
            return this.getAnnotationType();
        }
        if ((mv = this.annotation.getMemberValue(name)) != null) return mv.getValue(this.classLoader, this.pool, method);
        return this.getDefault(name, method);
    }

    private Object getDefault(String name, Method method) throws ClassNotFoundException, RuntimeException {
        String classname = this.annotation.getTypeName();
        if (this.pool == null) throw new RuntimeException("no default value: " + classname + "." + name + "()");
        try {
            CtClass cc = this.pool.get(classname);
            ClassFile cf = cc.getClassFile2();
            MethodInfo minfo = cf.getMethod(name);
            if (minfo == null) throw new RuntimeException("no default value: " + classname + "." + name + "()");
            AnnotationDefaultAttribute ainfo = (AnnotationDefaultAttribute)minfo.getAttribute("AnnotationDefault");
            if (ainfo == null) throw new RuntimeException("no default value: " + classname + "." + name + "()");
            MemberValue mv = ainfo.getDefaultValue();
            return mv.getValue(this.classLoader, this.pool, method);
        }
        catch (NotFoundException e) {
            throw new RuntimeException("cannot find a class file: " + classname);
        }
    }

    public int hashCode() {
        if (this.cachedHashCode != Integer.MIN_VALUE) return this.cachedHashCode;
        int hashCode = 0;
        this.getAnnotationType();
        Method[] methods = this.annotationType.getDeclaredMethods();
        int i = 0;
        while (true) {
            if (i >= methods.length) {
                this.cachedHashCode = hashCode;
                return this.cachedHashCode;
            }
            String name = methods[i].getName();
            int valueHashCode = 0;
            MemberValue mv = this.annotation.getMemberValue(name);
            Object value = null;
            try {
                if (mv != null) {
                    value = mv.getValue(this.classLoader, this.pool, methods[i]);
                }
                if (value == null) {
                    value = this.getDefault(name, methods[i]);
                }
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException("Error retrieving value " + name + " for annotation " + this.annotation.getTypeName(), e);
            }
            if (value != null) {
                valueHashCode = value.getClass().isArray() ? AnnotationImpl.arrayHashCode(value) : value.hashCode();
            }
            hashCode += 127 * name.hashCode() ^ valueHashCode;
            ++i;
        }
    }

    private boolean checkEquals(Object obj) throws Exception {
        InvocationHandler ih;
        if (obj == null) {
            return false;
        }
        if (obj instanceof Proxy && (ih = Proxy.getInvocationHandler(obj)) instanceof AnnotationImpl) {
            AnnotationImpl other = (AnnotationImpl)ih;
            return this.annotation.equals(other.annotation);
        }
        Class otherAnnotationType = (Class)JDK_ANNOTATION_TYPE_METHOD.invoke(obj, new Object[0]);
        if (!this.getAnnotationType().equals(otherAnnotationType)) {
            return false;
        }
        Method[] methods = this.annotationType.getDeclaredMethods();
        int i = 0;
        while (i < methods.length) {
            String name = methods[i].getName();
            MemberValue mv = this.annotation.getMemberValue(name);
            Object value = null;
            Object otherValue = null;
            try {
                if (mv != null) {
                    value = mv.getValue(this.classLoader, this.pool, methods[i]);
                }
                if (value == null) {
                    value = this.getDefault(name, methods[i]);
                }
                otherValue = methods[i].invoke(obj, new Object[0]);
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException("Error retrieving value " + name + " for annotation " + this.annotation.getTypeName(), e);
            }
            if (value == null && otherValue != null) {
                return false;
            }
            if (value != null && !value.equals(otherValue)) {
                return false;
            }
            ++i;
        }
        return true;
    }

    private static int arrayHashCode(Object object) {
        if (object == null) {
            return 0;
        }
        int result = 1;
        Object[] array = (Object[])object;
        int i = 0;
        while (i < array.length) {
            int elementHashCode = 0;
            if (array[i] != null) {
                elementHashCode = array[i].hashCode();
            }
            result = 31 * result + elementHashCode;
            ++i;
        }
        return result;
    }

    static {
        try {
            Class<?> clazz = Class.forName(JDK_ANNOTATION_CLASS_NAME);
            JDK_ANNOTATION_TYPE_METHOD = clazz.getMethod("annotationType", null);
            return;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

