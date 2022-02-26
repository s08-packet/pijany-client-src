/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode;

import com.viaversion.viaversion.libs.javassist.CannotCompileException;
import com.viaversion.viaversion.libs.javassist.bytecode.AttributeInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.BadBytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.ByteArray;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class StackMap
extends AttributeInfo {
    public static final String tag = "StackMap";
    public static final int TOP = 0;
    public static final int INTEGER = 1;
    public static final int FLOAT = 2;
    public static final int DOUBLE = 3;
    public static final int LONG = 4;
    public static final int NULL = 5;
    public static final int THIS = 6;
    public static final int OBJECT = 7;
    public static final int UNINIT = 8;

    StackMap(ConstPool cp, byte[] newInfo) {
        super(cp, tag, newInfo);
    }

    StackMap(ConstPool cp, int name_id, DataInputStream in) throws IOException {
        super(cp, name_id, in);
    }

    public int numOfEntries() {
        return ByteArray.readU16bit(this.info, 0);
    }

    @Override
    public AttributeInfo copy(ConstPool newCp, Map<String, String> classnames) {
        Copier copier = new Copier(this, newCp, classnames);
        copier.visit();
        return copier.getStackMap();
    }

    public void insertLocal(int index, int tag, int classInfo) throws BadBytecode {
        byte[] data = new InsertLocal(this, index, tag, classInfo).doit();
        this.set(data);
    }

    void shiftPc(int where, int gapSize, boolean exclusive) throws BadBytecode {
        new Shifter(this, where, gapSize, exclusive).visit();
    }

    void shiftForSwitch(int where, int gapSize) throws BadBytecode {
        new SwitchShifter(this, where, gapSize).visit();
    }

    public void removeNew(int where) throws CannotCompileException {
        byte[] data = new NewRemover(this, where).doit();
        this.set(data);
    }

    public void print(PrintWriter out) {
        new Printer(this, out).print();
    }

    public static class Writer {
        private ByteArrayOutputStream output = new ByteArrayOutputStream();

        public byte[] toByteArray() {
            return this.output.toByteArray();
        }

        public StackMap toStackMap(ConstPool cp) {
            return new StackMap(cp, this.output.toByteArray());
        }

        public void writeVerifyTypeInfo(int tag, int data) {
            this.output.write(tag);
            if (tag != 7) {
                if (tag != 8) return;
            }
            this.write16bit(data);
        }

        public void write16bit(int value) {
            this.output.write(value >>> 8 & 0xFF);
            this.output.write(value & 0xFF);
        }
    }

    static class Printer
    extends Walker {
        private PrintWriter writer;

        public Printer(StackMap map, PrintWriter out) {
            super(map);
            this.writer = out;
        }

        public void print() {
            int num = ByteArray.readU16bit(this.info, 0);
            this.writer.println(num + " entries");
            this.visit();
        }

        @Override
        public int locals(int pos, int offset, int num) {
            this.writer.println("  * offset " + offset);
            return super.locals(pos, offset, num);
        }
    }

    static class NewRemover
    extends SimpleCopy {
        int posOfNew;

        NewRemover(StackMap map, int where) {
            super(map);
            this.posOfNew = where;
        }

        @Override
        public int stack(int pos, int offset, int num) {
            return this.stackTypeInfoArray(pos, offset, num);
        }

        private int stackTypeInfoArray(int pos, int offset, int num) {
            int offsetOfNew;
            byte tag;
            int k;
            int p = pos;
            int count = 0;
            for (k = 0; k < num; ++k) {
                tag = this.info[p];
                if (tag == 7) {
                    p += 3;
                    continue;
                }
                if (tag == 8) {
                    offsetOfNew = ByteArray.readU16bit(this.info, p + 1);
                    if (offsetOfNew == this.posOfNew) {
                        ++count;
                    }
                    p += 3;
                    continue;
                }
                ++p;
            }
            this.writer.write16bit(num - count);
            k = 0;
            while (k < num) {
                tag = this.info[pos];
                if (tag == 7) {
                    int clazz = ByteArray.readU16bit(this.info, pos + 1);
                    this.objectVariable(pos, clazz);
                    pos += 3;
                } else if (tag == 8) {
                    offsetOfNew = ByteArray.readU16bit(this.info, pos + 1);
                    if (offsetOfNew != this.posOfNew) {
                        this.uninitialized(pos, offsetOfNew);
                    }
                    pos += 3;
                } else {
                    this.typeInfo(pos, tag);
                    ++pos;
                }
                ++k;
            }
            return pos;
        }
    }

    static class SwitchShifter
    extends Walker {
        private int where;
        private int gap;

        public SwitchShifter(StackMap smt, int where, int gap) {
            super(smt);
            this.where = where;
            this.gap = gap;
        }

        @Override
        public int locals(int pos, int offset, int num) {
            if (this.where == pos + offset) {
                ByteArray.write16bit(offset - this.gap, this.info, pos - 4);
                return super.locals(pos, offset, num);
            }
            if (this.where != pos) return super.locals(pos, offset, num);
            ByteArray.write16bit(offset + this.gap, this.info, pos - 4);
            return super.locals(pos, offset, num);
        }
    }

    static class Shifter
    extends Walker {
        private int where;
        private int gap;
        private boolean exclusive;

        public Shifter(StackMap smt, int where, int gap, boolean exclusive) {
            super(smt);
            this.where = where;
            this.gap = gap;
            this.exclusive = exclusive;
        }

        @Override
        public int locals(int pos, int offset, int num) {
            if (this.exclusive) {
                if (this.where > offset) return super.locals(pos, offset, num);
            } else if (this.where >= offset) return super.locals(pos, offset, num);
            ByteArray.write16bit(offset + this.gap, this.info, pos - 4);
            return super.locals(pos, offset, num);
        }

        @Override
        public void uninitialized(int pos, int offset) {
            if (this.where > offset) return;
            ByteArray.write16bit(offset + this.gap, this.info, pos + 1);
        }
    }

    static class InsertLocal
    extends SimpleCopy {
        private int varIndex;
        private int varTag;
        private int varData;

        InsertLocal(StackMap map, int varIndex, int varTag, int varData) {
            super(map);
            this.varIndex = varIndex;
            this.varTag = varTag;
            this.varData = varData;
        }

        @Override
        public int typeInfoArray(int pos, int offset, int num, boolean isLocals) {
            if (!isLocals) return super.typeInfoArray(pos, offset, num, isLocals);
            if (num < this.varIndex) {
                return super.typeInfoArray(pos, offset, num, isLocals);
            }
            this.writer.write16bit(num + 1);
            int k = 0;
            while (true) {
                if (k >= num) {
                    if (num != this.varIndex) return pos;
                    this.writeVarTypeInfo();
                    return pos;
                }
                if (k == this.varIndex) {
                    this.writeVarTypeInfo();
                }
                pos = this.typeInfoArray2(k, pos);
                ++k;
            }
        }

        private void writeVarTypeInfo() {
            if (this.varTag == 7) {
                this.writer.writeVerifyTypeInfo(7, this.varData);
                return;
            }
            if (this.varTag == 8) {
                this.writer.writeVerifyTypeInfo(8, this.varData);
                return;
            }
            this.writer.writeVerifyTypeInfo(this.varTag, 0);
        }
    }

    static class SimpleCopy
    extends Walker {
        Writer writer = new Writer();

        SimpleCopy(StackMap map) {
            super(map);
        }

        byte[] doit() {
            this.visit();
            return this.writer.toByteArray();
        }

        @Override
        public void visit() {
            int num = ByteArray.readU16bit(this.info, 0);
            this.writer.write16bit(num);
            super.visit();
        }

        @Override
        public int locals(int pos, int offset, int num) {
            this.writer.write16bit(offset);
            return super.locals(pos, offset, num);
        }

        @Override
        public int typeInfoArray(int pos, int offset, int num, boolean isLocals) {
            this.writer.write16bit(num);
            return super.typeInfoArray(pos, offset, num, isLocals);
        }

        @Override
        public void typeInfo(int pos, byte tag) {
            this.writer.writeVerifyTypeInfo(tag, 0);
        }

        @Override
        public void objectVariable(int pos, int clazz) {
            this.writer.writeVerifyTypeInfo(7, clazz);
        }

        @Override
        public void uninitialized(int pos, int offset) {
            this.writer.writeVerifyTypeInfo(8, offset);
        }
    }

    static class Copier
    extends Walker {
        byte[] dest;
        ConstPool srcCp;
        ConstPool destCp;
        Map<String, String> classnames;

        Copier(StackMap map, ConstPool newCp, Map<String, String> classnames) {
            super(map);
            this.srcCp = map.getConstPool();
            this.dest = new byte[this.info.length];
            this.destCp = newCp;
            this.classnames = classnames;
        }

        @Override
        public void visit() {
            int num = ByteArray.readU16bit(this.info, 0);
            ByteArray.write16bit(num, this.dest, 0);
            super.visit();
        }

        @Override
        public int locals(int pos, int offset, int num) {
            ByteArray.write16bit(offset, this.dest, pos - 4);
            return super.locals(pos, offset, num);
        }

        @Override
        public int typeInfoArray(int pos, int offset, int num, boolean isLocals) {
            ByteArray.write16bit(num, this.dest, pos - 2);
            return super.typeInfoArray(pos, offset, num, isLocals);
        }

        @Override
        public void typeInfo(int pos, byte tag) {
            this.dest[pos] = tag;
        }

        @Override
        public void objectVariable(int pos, int clazz) {
            this.dest[pos] = 7;
            int newClazz = this.srcCp.copy(clazz, this.destCp, this.classnames);
            ByteArray.write16bit(newClazz, this.dest, pos + 1);
        }

        @Override
        public void uninitialized(int pos, int offset) {
            this.dest[pos] = 8;
            ByteArray.write16bit(offset, this.dest, pos + 1);
        }

        public StackMap getStackMap() {
            return new StackMap(this.destCp, this.dest);
        }
    }

    public static class Walker {
        byte[] info;

        public Walker(StackMap sm) {
            this.info = sm.get();
        }

        public void visit() {
            int num = ByteArray.readU16bit(this.info, 0);
            int pos = 2;
            int i = 0;
            while (i < num) {
                int offset = ByteArray.readU16bit(this.info, pos);
                int numLoc = ByteArray.readU16bit(this.info, pos + 2);
                pos = this.locals(pos + 4, offset, numLoc);
                int numStack = ByteArray.readU16bit(this.info, pos);
                pos = this.stack(pos + 2, offset, numStack);
                ++i;
            }
        }

        public int locals(int pos, int offset, int num) {
            return this.typeInfoArray(pos, offset, num, true);
        }

        public int stack(int pos, int offset, int num) {
            return this.typeInfoArray(pos, offset, num, false);
        }

        public int typeInfoArray(int pos, int offset, int num, boolean isLocals) {
            int k = 0;
            while (k < num) {
                pos = this.typeInfoArray2(k, pos);
                ++k;
            }
            return pos;
        }

        int typeInfoArray2(int k, int pos) {
            byte tag = this.info[pos];
            if (tag == 7) {
                int clazz = ByteArray.readU16bit(this.info, pos + 1);
                this.objectVariable(pos, clazz);
                return pos += 3;
            }
            if (tag == 8) {
                int offsetOfNew = ByteArray.readU16bit(this.info, pos + 1);
                this.uninitialized(pos, offsetOfNew);
                return pos += 3;
            }
            this.typeInfo(pos, tag);
            ++pos;
            return pos;
        }

        public void typeInfo(int pos, byte tag) {
        }

        public void objectVariable(int pos, int clazz) {
        }

        public void uninitialized(int pos, int offset) {
        }
    }
}

