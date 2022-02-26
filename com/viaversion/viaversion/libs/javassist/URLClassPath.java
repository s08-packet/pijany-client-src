/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist;

import com.viaversion.viaversion.libs.javassist.ClassPath;
import com.viaversion.viaversion.libs.javassist.ClassPoolTail;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class URLClassPath
implements ClassPath {
    protected String hostname;
    protected int port;
    protected String directory;
    protected String packageName;

    public URLClassPath(String host, int port, String directory, String packageName) {
        this.hostname = host;
        this.port = port;
        this.directory = directory;
        this.packageName = packageName;
    }

    public String toString() {
        return this.hostname + ":" + this.port + this.directory;
    }

    @Override
    public InputStream openClassfile(String classname) {
        try {
            URLConnection con = this.openClassfile0(classname);
            if (con == null) return null;
            return con.getInputStream();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return null;
    }

    private URLConnection openClassfile0(String classname) throws IOException {
        if (this.packageName != null) {
            if (!classname.startsWith(this.packageName)) return null;
        }
        String jarname = this.directory + classname.replace('.', '/') + ".class";
        return URLClassPath.fetchClass0(this.hostname, this.port, jarname);
    }

    @Override
    public URL find(String classname) {
        try {
            URLConnection con = this.openClassfile0(classname);
            InputStream is = con.getInputStream();
            if (is == null) return null;
            is.close();
            return con.getURL();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] fetchClass(String host, int port, String directory, String classname) throws IOException {
        URLConnection con = URLClassPath.fetchClass0(host, port, directory + classname.replace('.', '/') + ".class");
        int size = con.getContentLength();
        try (InputStream s = con.getInputStream();){
            int n;
            byte[] b;
            if (size <= 0) {
                b = ClassPoolTail.readStream(s);
                return b;
            }
            b = new byte[size];
            int len = 0;
            do {
                if ((n = s.read(b, len, size - len)) >= 0) continue;
                throw new IOException("the stream was closed: " + classname);
            } while ((len += n) < size);
            return b;
        }
    }

    private static URLConnection fetchClass0(String host, int port, String filename) throws IOException {
        URL url;
        try {
            url = new URL("http", host, port, filename);
        }
        catch (MalformedURLException e) {
            throw new IOException("invalid URL?");
        }
        URLConnection con = url.openConnection();
        con.connect();
        return con;
    }
}

