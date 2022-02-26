/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.compiler;

import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.CtClass;
import com.viaversion.viaversion.libs.javassist.CtField;
import com.viaversion.viaversion.libs.javassist.Modifier;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.ClassFile;
import com.viaversion.viaversion.libs.javassist.bytecode.Descriptor;
import com.viaversion.viaversion.libs.javassist.bytecode.MethodInfo;
import com.viaversion.viaversion.libs.javassist.compiler.CompileError;
import com.viaversion.viaversion.libs.javassist.compiler.NoFieldException;
import com.viaversion.viaversion.libs.javassist.compiler.TokenId;
import com.viaversion.viaversion.libs.javassist.compiler.ast.ASTList;
import com.viaversion.viaversion.libs.javassist.compiler.ast.ASTree;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Declarator;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Keyword;
import com.viaversion.viaversion.libs.javassist.compiler.ast.Symbol;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MemberResolver
implements TokenId {
    private ClassPool classPool;
    private static final int YES = 0;
    private static final int NO = -1;
    private static final String INVALID = "<invalid>";
    private static Map<ClassPool, Reference<Map<String, String>>> invalidNamesMap = new WeakHashMap<ClassPool, Reference<Map<String, String>>>();
    private Map<String, String> invalidNames = null;

    public MemberResolver(ClassPool cp) {
        this.classPool = cp;
    }

    public ClassPool getClassPool() {
        return this.classPool;
    }

    private static void fatal() throws CompileError {
        throw new CompileError("fatal");
    }

    public Method lookupMethod(CtClass clazz, CtClass currentClass, MethodInfo current, String methodName, int[] argTypes, int[] argDims, String[] argClassNames) throws CompileError {
        int res;
        Method maybe = null;
        if (current != null && clazz == currentClass && current.getName().equals(methodName) && (res = this.compareSignature(current.getDescriptor(), argTypes, argDims, argClassNames)) != -1) {
            Method r = new Method(clazz, current, res);
            if (res == 0) {
                return r;
            }
            maybe = r;
        }
        Method m = this.lookupMethod(clazz, methodName, argTypes, argDims, argClassNames, maybe != null);
        if (m == null) return maybe;
        return m;
    }

    private Method lookupMethod(CtClass clazz, String methodName, int[] argTypes, int[] argDims, String[] argClassNames, boolean onlyExact) throws CompileError {
        Method maybe = null;
        ClassFile cf = clazz.getClassFile2();
        if (cf != null) {
            List<MethodInfo> list = cf.getMethods();
            for (MethodInfo minfo : list) {
                int res;
                if (!minfo.getName().equals(methodName) || (minfo.getAccessFlags() & 0x40) != 0 || (res = this.compareSignature(minfo.getDescriptor(), argTypes, argDims, argClassNames)) == -1) continue;
                Method r2 = new Method(clazz, minfo, res);
                if (res == 0) {
                    return r2;
                }
                if (maybe != null && maybe.notmatch <= res) continue;
                maybe = r2;
            }
        }
        if (onlyExact) {
            maybe = null;
        } else if (maybe != null) {
            return maybe;
        }
        int mod = clazz.getModifiers();
        boolean isIntf = Modifier.isInterface(mod);
        try {
            Method r;
            CtClass pclazz;
            if (!isIntf && (pclazz = clazz.getSuperclass()) != null && (r = this.lookupMethod(pclazz, methodName, argTypes, argDims, argClassNames, onlyExact)) != null) {
                return r;
            }
        }
        catch (NotFoundException pclazz) {
            // empty catch block
        }
        try {
            CtClass[] ifs;
            CtClass[] r = ifs = clazz.getInterfaces();
            int r2 = r.length;
            int n = 0;
            while (true) {
                if (n >= r2) {
                    if (!isIntf) return maybe;
                    CtClass pclazz = clazz.getSuperclass();
                    if (pclazz == null) return maybe;
                    Method r3 = this.lookupMethod(pclazz, methodName, argTypes, argDims, argClassNames, onlyExact);
                    if (r3 == null) return maybe;
                    return r3;
                }
                CtClass intf = r[n];
                Method r4 = this.lookupMethod(intf, methodName, argTypes, argDims, argClassNames, onlyExact);
                if (r4 != null) {
                    return r4;
                }
                ++n;
            }
        }
        catch (NotFoundException notFoundException) {
            // empty catch block
        }
        return maybe;
    }

    private int compareSignature(String desc, int[] argTypes, int[] argDims, String[] argClassNames) throws CompileError {
        int result = 0;
        int i = 1;
        int nArgs = argTypes.length;
        if (nArgs != Descriptor.numOfParameters(desc)) {
            return -1;
        }
        int len = desc.length();
        int n = 0;
        while (i < len) {
            char c;
            if ((c = desc.charAt(i++)) == ')') {
                if (n != nArgs) return -1;
                int n2 = result;
                return n2;
            }
            if (n >= nArgs) {
                return -1;
            }
            int dim = 0;
            while (c == '[') {
                ++dim;
                c = desc.charAt(i++);
            }
            if (argTypes[n] == 412) {
                if (dim == 0 && c != 'L') {
                    return -1;
                }
                if (c == 'L') {
                    i = desc.indexOf(59, i) + 1;
                }
            } else if (argDims[n] != dim) {
                if (dim != 0) return -1;
                if (c != 'L') return -1;
                if (!desc.startsWith("java/lang/Object;", i)) {
                    return -1;
                }
                i = desc.indexOf(59, i) + 1;
                ++result;
                if (i <= 0) {
                    return -1;
                }
            } else if (c == 'L') {
                int j = desc.indexOf(59, i);
                if (j < 0) return -1;
                if (argTypes[n] != 307) {
                    return -1;
                }
                String cname = desc.substring(i, j);
                if (!cname.equals(argClassNames[n])) {
                    CtClass clazz = this.lookupClassByJvmName(argClassNames[n]);
                    try {
                        if (!clazz.subtypeOf(this.lookupClassByJvmName(cname))) return -1;
                        ++result;
                    }
                    catch (NotFoundException e) {
                        ++result;
                    }
                }
                i = j + 1;
            } else {
                int at;
                int t = MemberResolver.descToType(c);
                if (t != (at = argTypes[n])) {
                    if (t != 324) return -1;
                    if (at != 334 && at != 303) {
                        if (at != 306) return -1;
                    }
                    ++result;
                }
            }
            ++n;
        }
        return -1;
    }

    public CtField lookupFieldByJvmName2(String jvmClassName, Symbol fieldSym, ASTree expr) throws NoFieldException {
        String field = fieldSym.get();
        CtClass cc = null;
        try {
            cc = this.lookupClass(MemberResolver.jvmToJavaName(jvmClassName), true);
        }
        catch (CompileError e) {
            throw new NoFieldException(jvmClassName + "/" + field, expr);
        }
        try {
            return cc.getField(field);
        }
        catch (NotFoundException e) {
            jvmClassName = MemberResolver.javaToJvmName(cc.getName());
            throw new NoFieldException(jvmClassName + "$" + field, expr);
        }
    }

    public CtField lookupFieldByJvmName(String jvmClassName, Symbol fieldName) throws CompileError {
        return this.lookupField(MemberResolver.jvmToJavaName(jvmClassName), fieldName);
    }

    public CtField lookupField(String className, Symbol fieldName) throws CompileError {
        CtClass cc = this.lookupClass(className, false);
        try {
            return cc.getField(fieldName.get());
        }
        catch (NotFoundException notFoundException) {
            throw new CompileError("no such field: " + fieldName.get());
        }
    }

    public CtClass lookupClassByName(ASTList name) throws CompileError {
        return this.lookupClass(Declarator.astToClassName(name, '.'), false);
    }

    public CtClass lookupClassByJvmName(String jvmName) throws CompileError {
        return this.lookupClass(MemberResolver.jvmToJavaName(jvmName), false);
    }

    public CtClass lookupClass(Declarator decl) throws CompileError {
        return this.lookupClass(decl.getType(), decl.getArrayDim(), decl.getClassName());
    }

    public CtClass lookupClass(int type, int dim, String classname) throws CompileError {
        String cname = "";
        if (type == 307) {
            CtClass clazz = this.lookupClassByJvmName(classname);
            if (dim <= 0) return clazz;
            cname = clazz.getName();
        } else {
            cname = MemberResolver.getTypeName(type);
        }
        while (dim-- > 0) {
            cname = cname + "[]";
        }
        return this.lookupClass(cname, false);
    }

    static String getTypeName(int type) throws CompileError {
        String cname = "";
        switch (type) {
            case 301: {
                return "boolean";
            }
            case 306: {
                return "char";
            }
            case 303: {
                return "byte";
            }
            case 334: {
                return "short";
            }
            case 324: {
                return "int";
            }
            case 326: {
                return "long";
            }
            case 317: {
                return "float";
            }
            case 312: {
                return "double";
            }
            case 344: {
                return "void";
            }
        }
        MemberResolver.fatal();
        return cname;
    }

    public CtClass lookupClass(String name, boolean notCheckInner) throws CompileError {
        Map<String, String> cache = this.getInvalidNames();
        String found = cache.get(name);
        if (found == INVALID) {
            throw new CompileError("no such class: " + name);
        }
        if (found != null) {
            try {
                return this.classPool.get(found);
            }
            catch (NotFoundException notFoundException) {
                // empty catch block
            }
        }
        CtClass cc = null;
        try {
            cc = this.lookupClass0(name, notCheckInner);
        }
        catch (NotFoundException e) {
            cc = this.searchImports(name);
        }
        cache.put(name, cc.getName());
        return cc;
    }

    public static int getInvalidMapSize() {
        return invalidNamesMap.size();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Map<String, String> getInvalidNames() {
        Map<String, String> ht = this.invalidNames;
        if (ht != null) return ht;
        Class<MemberResolver> clazz = MemberResolver.class;
        synchronized (MemberResolver.class) {
            Reference<Map<String, String>> ref = invalidNamesMap.get(this.classPool);
            if (ref != null) {
                ht = ref.get();
            }
            if (ht == null) {
                ht = new Hashtable<String, String>();
                invalidNamesMap.put(this.classPool, new WeakReference<Map<String, String>>(ht));
            }
            // ** MonitorExit[var2_2] (shouldn't be in output)
            this.invalidNames = ht;
            return ht;
        }
    }

    private CtClass searchImports(String orgName) throws CompileError {
        if (orgName.indexOf(46) < 0) {
            Iterator<String> it = this.classPool.getImportedPackages();
            while (it.hasNext()) {
                String pac = it.next();
                String fqName = pac.replaceAll("\\.$", "") + "." + orgName;
                try {
                    return this.classPool.get(fqName);
                }
                catch (NotFoundException e) {
                    try {
                        if (!pac.endsWith("." + orgName)) continue;
                        return this.classPool.get(pac);
                    }
                    catch (NotFoundException notFoundException) {
                    }
                }
            }
        }
        this.getInvalidNames().put(orgName, INVALID);
        throw new CompileError("no such class: " + orgName);
    }

    private CtClass lookupClass0(String classname, boolean notCheckInner) throws NotFoundException {
        CtClass cc = null;
        do {
            try {
                cc = this.classPool.get(classname);
            }
            catch (NotFoundException e) {
                int i = classname.lastIndexOf(46);
                if (notCheckInner) throw e;
                if (i < 0) {
                    throw e;
                }
                StringBuffer sbuf = new StringBuffer(classname);
                sbuf.setCharAt(i, '$');
                classname = sbuf.toString();
            }
        } while (cc == null);
        return cc;
    }

    public String resolveClassName(ASTList name) throws CompileError {
        if (name != null) return MemberResolver.javaToJvmName(this.lookupClassByName(name).getName());
        return null;
    }

    public String resolveJvmClassName(String jvmName) throws CompileError {
        if (jvmName != null) return MemberResolver.javaToJvmName(this.lookupClassByJvmName(jvmName).getName());
        return null;
    }

    public static CtClass getSuperclass(CtClass c) throws CompileError {
        try {
            CtClass sc = c.getSuperclass();
            if (sc == null) throw new CompileError("cannot find the super class of " + c.getName());
            return sc;
        }
        catch (NotFoundException notFoundException) {
            // empty catch block
        }
        throw new CompileError("cannot find the super class of " + c.getName());
    }

    public static CtClass getSuperInterface(CtClass c, String interfaceName) throws CompileError {
        try {
            CtClass[] intfs = c.getInterfaces();
            int i = 0;
            while (i < intfs.length) {
                if (intfs[i].getName().equals(interfaceName)) {
                    return intfs[i];
                }
                ++i;
            }
            throw new CompileError("cannot find the super interface " + interfaceName + " of " + c.getName());
        }
        catch (NotFoundException notFoundException) {
            // empty catch block
        }
        throw new CompileError("cannot find the super interface " + interfaceName + " of " + c.getName());
    }

    public static String javaToJvmName(String classname) {
        return classname.replace('.', '/');
    }

    public static String jvmToJavaName(String classname) {
        return classname.replace('/', '.');
    }

    public static int descToType(char c) throws CompileError {
        switch (c) {
            case 'Z': {
                return 301;
            }
            case 'C': {
                return 306;
            }
            case 'B': {
                return 303;
            }
            case 'S': {
                return 334;
            }
            case 'I': {
                return 324;
            }
            case 'J': {
                return 326;
            }
            case 'F': {
                return 317;
            }
            case 'D': {
                return 312;
            }
            case 'V': {
                return 344;
            }
            case 'L': 
            case '[': {
                return 307;
            }
        }
        MemberResolver.fatal();
        return 344;
    }

    public static int getModifiers(ASTList mods) {
        int m = 0;
        block12: while (mods != null) {
            Keyword k = (Keyword)mods.head();
            mods = mods.tail();
            switch (k.get()) {
                case 335: {
                    m |= 8;
                    break;
                }
                case 315: {
                    m |= 0x10;
                    break;
                }
                case 338: {
                    m |= 0x20;
                    break;
                }
                case 300: {
                    m |= 0x400;
                    break;
                }
                case 332: {
                    m |= 1;
                    break;
                }
                case 331: {
                    m |= 4;
                    break;
                }
                case 330: {
                    m |= 2;
                    break;
                }
                case 345: {
                    m |= 0x40;
                    break;
                }
                case 342: {
                    m |= 0x80;
                    break;
                }
                case 347: {
                    m |= 0x800;
                    continue block12;
                }
            }
        }
        return m;
    }

    public static class Method {
        public CtClass declaring;
        public MethodInfo info;
        public int notmatch;

        public Method(CtClass c, MethodInfo i, int n) {
            this.declaring = c;
            this.info = i;
            this.notmatch = n;
        }

        public boolean isStatic() {
            int acc = this.info.getAccessFlags();
            if ((acc & 8) == 0) return false;
            return true;
        }
    }
}

