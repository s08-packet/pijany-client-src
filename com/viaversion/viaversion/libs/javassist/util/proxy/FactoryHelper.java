/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.util.proxy;

import com.viaversion.viaversion.libs.javassist.CannotCompileException;
import com.viaversion.viaversion.libs.javassist.bytecode.ClassFile;
import com.viaversion.viaversion.libs.javassist.util.proxy.DefineClassHelper;
import com.viaversion.viaversion.libs.javassist.util.proxy.ProxyFactory;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.ProtectionDomain;

public class FactoryHelper {
    public static final Class<?>[] primitiveTypes = new Class[]{Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, Void.TYPE};
    public static final String[] wrapperTypes = new String[]{"java.lang.Boolean", "java.lang.Byte", "java.lang.Character", "java.lang.Short", "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double", "java.lang.Void"};
    public static final String[] wrapperDesc = new String[]{"(Z)V", "(B)V", "(C)V", "(S)V", "(I)V", "(J)V", "(F)V", "(D)V"};
    public static final String[] unwarpMethods = new String[]{"booleanValue", "byteValue", "charValue", "shortValue", "intValue", "longValue", "floatValue", "doubleValue"};
    public static final String[] unwrapDesc = new String[]{"()Z", "()B", "()C", "()S", "()I", "()J", "()F", "()D"};
    public static final int[] dataSize = new int[]{1, 1, 1, 1, 1, 2, 1, 2};

    public static final int typeIndex(Class<?> type) {
        int i = 0;
        while (i < primitiveTypes.length) {
            if (primitiveTypes[i] == type) {
                return i;
            }
            ++i;
        }
        throw new RuntimeException("bad type:" + type.getName());
    }

    public static Class<?> toClass(ClassFile cf, ClassLoader loader) throws CannotCompileException {
        return FactoryHelper.toClass(cf, null, loader, null);
    }

    public static Class<?> toClass(ClassFile cf, ClassLoader loader, ProtectionDomain domain) throws CannotCompileException {
        return FactoryHelper.toClass(cf, null, loader, domain);
    }

    public static Class<?> toClass(ClassFile cf, Class<?> neighbor, ClassLoader loader, ProtectionDomain domain) throws CannotCompileException {
        try {
            byte[] b = FactoryHelper.toBytecode(cf);
            if (!ProxyFactory.onlyPublicMethods) return DefineClassHelper.toClass(cf.getName(), neighbor, loader, domain, b);
            return DefineClassHelper.toPublicClass(cf.getName(), b);
        }
        catch (IOException e) {
            throw new CannotCompileException(e);
        }
    }

    public static Class<?> toClass(ClassFile cf, MethodHandles.Lookup lookup) throws CannotCompileException {
        try {
            byte[] b = FactoryHelper.toBytecode(cf);
            return DefineClassHelper.toClass(lookup, b);
        }
        catch (IOException e) {
            throw new CannotCompileException(e);
        }
    }

    private static byte[] toBytecode(ClassFile cf) throws IOException {
        ByteArrayOutputStream barray = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(barray);){
            cf.write(out);
            return barray.toByteArray();
        }
    }

    public static void writeFile(ClassFile cf, String directoryName) throws CannotCompileException {
        try {
            FactoryHelper.writeFile0(cf, directoryName);
            return;
        }
        catch (IOException e) {
            throw new CannotCompileException(e);
        }
    }

    private static void writeFile0(ClassFile cf, String directoryName) throws CannotCompileException, IOException {
        String dir;
        String classname = cf.getName();
        String filename = directoryName + File.separatorChar + classname.replace('.', File.separatorChar) + ".class";
        int pos = filename.lastIndexOf(File.separatorChar);
        if (pos > 0 && !(dir = filename.substring(0, pos)).equals(".")) {
            new File(dir).mkdirs();
        }
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));){
            cf.write(out);
            return;
        }
    }
}

