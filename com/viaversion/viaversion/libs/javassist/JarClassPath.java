/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist;

import com.viaversion.viaversion.libs.javassist.ClassPath;
import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

final class JarClassPath
implements ClassPath {
    Set<String> jarfileEntries;
    String jarfileURL;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    JarClassPath(String pathname) throws NotFoundException {
        JarFile jarfile = null;
        try {
            jarfile = new JarFile(pathname);
            this.jarfileEntries = new HashSet<String>();
            for (JarEntry je : Collections.list(jarfile.entries())) {
                if (!je.getName().endsWith(".class")) continue;
                this.jarfileEntries.add(je.getName());
            }
            this.jarfileURL = new File(pathname).getCanonicalFile().toURI().toURL().toString();
            return;
        }
        catch (IOException iOException) {
            throw new NotFoundException(pathname);
        }
        finally {
            if (null != jarfile) {
                try {
                    jarfile.close();
                }
                catch (IOException iOException) {}
            }
        }
    }

    @Override
    public InputStream openClassfile(String classname) throws NotFoundException {
        URL jarURL = this.find(classname);
        if (null == jarURL) return null;
        try {
            if (ClassPool.cacheOpenedJarFile) {
                return jarURL.openConnection().getInputStream();
            }
            URLConnection con = jarURL.openConnection();
            con.setUseCaches(false);
            return con.getInputStream();
        }
        catch (IOException e) {
            throw new NotFoundException("broken jar file?: " + classname);
        }
    }

    @Override
    public URL find(String classname) {
        String jarname = classname.replace('.', '/') + ".class";
        if (!this.jarfileEntries.contains(jarname)) return null;
        try {
            return new URL(String.format("jar:%s!/%s", this.jarfileURL, jarname));
        }
        catch (MalformedURLException malformedURLException) {
            // empty catch block
        }
        return null;
    }

    public String toString() {
        if (this.jarfileURL == null) {
            return "<null>";
        }
        String string = this.jarfileURL.toString();
        return string;
    }
}

