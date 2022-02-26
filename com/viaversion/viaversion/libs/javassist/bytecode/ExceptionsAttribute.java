/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode;

import com.viaversion.viaversion.libs.javassist.bytecode.AttributeInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.ByteArray;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

public class ExceptionsAttribute
extends AttributeInfo {
    public static final String tag = "Exceptions";

    ExceptionsAttribute(ConstPool cp, int n, DataInputStream in) throws IOException {
        super(cp, n, in);
    }

    private ExceptionsAttribute(ConstPool cp, ExceptionsAttribute src, Map<String, String> classnames) {
        super(cp, tag);
        this.copyFrom(src, classnames);
    }

    public ExceptionsAttribute(ConstPool cp) {
        super(cp, tag);
        byte[] data = new byte[2];
        data[1] = 0;
        data[0] = 0;
        this.info = data;
    }

    @Override
    public AttributeInfo copy(ConstPool newCp, Map<String, String> classnames) {
        return new ExceptionsAttribute(newCp, this, classnames);
    }

    private void copyFrom(ExceptionsAttribute srcAttr, Map<String, String> classnames) {
        ConstPool srcCp = srcAttr.constPool;
        ConstPool destCp = this.constPool;
        byte[] src = srcAttr.info;
        int num = src.length;
        byte[] dest = new byte[num];
        dest[0] = src[0];
        dest[1] = src[1];
        int i = 2;
        while (true) {
            if (i >= num) {
                this.info = dest;
                return;
            }
            int index = ByteArray.readU16bit(src, i);
            ByteArray.write16bit(srcCp.copy(index, destCp, classnames), dest, i);
            i += 2;
        }
    }

    public int[] getExceptionIndexes() {
        byte[] blist = this.info;
        int n = blist.length;
        if (n <= 2) {
            return null;
        }
        int[] elist = new int[n / 2 - 1];
        int k = 0;
        int j = 2;
        while (j < n) {
            elist[k++] = (blist[j] & 0xFF) << 8 | blist[j + 1] & 0xFF;
            j += 2;
        }
        return elist;
    }

    public String[] getExceptions() {
        byte[] blist = this.info;
        int n = blist.length;
        if (n <= 2) {
            return null;
        }
        String[] elist = new String[n / 2 - 1];
        int k = 0;
        int j = 2;
        while (j < n) {
            int index = (blist[j] & 0xFF) << 8 | blist[j + 1] & 0xFF;
            elist[k++] = this.constPool.getClassInfo(index);
            j += 2;
        }
        return elist;
    }

    public void setExceptionIndexes(int[] elist) {
        int n = elist.length;
        byte[] blist = new byte[n * 2 + 2];
        ByteArray.write16bit(n, blist, 0);
        int i = 0;
        while (true) {
            if (i >= n) {
                this.info = blist;
                return;
            }
            ByteArray.write16bit(elist[i], blist, i * 2 + 2);
            ++i;
        }
    }

    public void setExceptions(String[] elist) {
        int n = elist.length;
        byte[] blist = new byte[n * 2 + 2];
        ByteArray.write16bit(n, blist, 0);
        int i = 0;
        while (true) {
            if (i >= n) {
                this.info = blist;
                return;
            }
            ByteArray.write16bit(this.constPool.addClassInfo(elist[i]), blist, i * 2 + 2);
            ++i;
        }
    }

    public int tableLength() {
        return this.info.length / 2 - 1;
    }

    public int getException(int nth) {
        int index = nth * 2 + 2;
        return (this.info[index] & 0xFF) << 8 | this.info[index + 1] & 0xFF;
    }
}

