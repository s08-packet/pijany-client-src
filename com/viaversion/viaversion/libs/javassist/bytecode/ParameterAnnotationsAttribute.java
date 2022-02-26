/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode;

import com.viaversion.viaversion.libs.javassist.bytecode.AnnotationsAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.AttributeInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.Annotation;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.AnnotationsWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ParameterAnnotationsAttribute
extends AttributeInfo {
    public static final String visibleTag = "RuntimeVisibleParameterAnnotations";
    public static final String invisibleTag = "RuntimeInvisibleParameterAnnotations";

    public ParameterAnnotationsAttribute(ConstPool cp, String attrname, byte[] info) {
        super(cp, attrname, info);
    }

    public ParameterAnnotationsAttribute(ConstPool cp, String attrname) {
        this(cp, attrname, new byte[]{0});
    }

    ParameterAnnotationsAttribute(ConstPool cp, int n, DataInputStream in) throws IOException {
        super(cp, n, in);
    }

    public int numParameters() {
        return this.info[0] & 0xFF;
    }

    @Override
    public AttributeInfo copy(ConstPool newCp, Map<String, String> classnames) {
        AnnotationsAttribute.Copier copier = new AnnotationsAttribute.Copier(this.info, this.constPool, newCp, classnames);
        try {
            copier.parameters();
            return new ParameterAnnotationsAttribute(newCp, this.getName(), copier.close());
        }
        catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    public Annotation[][] getAnnotations() {
        try {
            return new AnnotationsAttribute.Parser(this.info, this.constPool).parseParameters();
        }
        catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    public void setAnnotations(Annotation[][] params) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        AnnotationsWriter writer = new AnnotationsWriter(output, this.constPool);
        try {
            writer.numParameters(params.length);
            for (Annotation[] anno : params) {
                writer.numAnnotations(anno.length);
                for (int j = 0; j < anno.length; ++j) {
                    anno[j].write(writer);
                }
            }
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.set(output.toByteArray());
    }

    @Override
    void renameClass(String oldname, String newname) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(oldname, newname);
        this.renameClass(map);
    }

    @Override
    void renameClass(Map<String, String> classnames) {
        AnnotationsAttribute.Renamer renamer = new AnnotationsAttribute.Renamer(this.info, this.getConstPool(), classnames);
        try {
            renamer.parameters();
            return;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void getRefClasses(Map<String, String> classnames) {
        this.renameClass(classnames);
    }

    public String toString() {
        Annotation[][] aa = this.getAnnotations();
        StringBuilder sbuf = new StringBuilder();
        Annotation[][] annotationArray = aa;
        int n = annotationArray.length;
        int n2 = 0;
        while (n2 < n) {
            Annotation[] a;
            for (Annotation i : a = annotationArray[n2]) {
                sbuf.append(i.toString()).append(" ");
            }
            sbuf.append(", ");
            ++n2;
        }
        return sbuf.toString().replaceAll(" (?=,)|, $", "");
    }
}

