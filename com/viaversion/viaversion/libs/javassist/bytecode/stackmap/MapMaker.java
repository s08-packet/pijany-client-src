/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode.stackmap;

import com.viaversion.viaversion.libs.javassist.ClassPool;
import com.viaversion.viaversion.libs.javassist.NotFoundException;
import com.viaversion.viaversion.libs.javassist.bytecode.BadBytecode;
import com.viaversion.viaversion.libs.javassist.bytecode.ByteArray;
import com.viaversion.viaversion.libs.javassist.bytecode.CodeAttribute;
import com.viaversion.viaversion.libs.javassist.bytecode.ConstPool;
import com.viaversion.viaversion.libs.javassist.bytecode.MethodInfo;
import com.viaversion.viaversion.libs.javassist.bytecode.StackMap;
import com.viaversion.viaversion.libs.javassist.bytecode.StackMapTable;
import com.viaversion.viaversion.libs.javassist.bytecode.stackmap.BasicBlock;
import com.viaversion.viaversion.libs.javassist.bytecode.stackmap.Tracer;
import com.viaversion.viaversion.libs.javassist.bytecode.stackmap.TypeData;
import com.viaversion.viaversion.libs.javassist.bytecode.stackmap.TypedBlock;
import java.util.ArrayList;

public class MapMaker
extends Tracer {
    public static StackMapTable make(ClassPool classes, MethodInfo minfo) throws BadBytecode {
        TypedBlock[] blocks;
        CodeAttribute ca = minfo.getCodeAttribute();
        if (ca == null) {
            return null;
        }
        try {
            blocks = TypedBlock.makeBlocks(minfo, ca, true);
        }
        catch (BasicBlock.JsrBytecode e) {
            return null;
        }
        if (blocks == null) {
            return null;
        }
        MapMaker mm = new MapMaker(classes, minfo, ca);
        try {
            mm.make(blocks, ca.getCode());
            return mm.toStackMap(blocks);
        }
        catch (BadBytecode bb) {
            throw new BadBytecode(minfo, (Throwable)bb);
        }
    }

    public static StackMap make2(ClassPool classes, MethodInfo minfo) throws BadBytecode {
        TypedBlock[] blocks;
        CodeAttribute ca = minfo.getCodeAttribute();
        if (ca == null) {
            return null;
        }
        try {
            blocks = TypedBlock.makeBlocks(minfo, ca, true);
        }
        catch (BasicBlock.JsrBytecode e) {
            return null;
        }
        if (blocks == null) {
            return null;
        }
        MapMaker mm = new MapMaker(classes, minfo, ca);
        try {
            mm.make(blocks, ca.getCode());
            return mm.toStackMap2(minfo.getConstPool(), blocks);
        }
        catch (BadBytecode bb) {
            throw new BadBytecode(minfo, (Throwable)bb);
        }
    }

    public MapMaker(ClassPool classes, MethodInfo minfo, CodeAttribute ca) {
        super(classes, minfo.getConstPool(), ca.getMaxStack(), ca.getMaxLocals(), TypedBlock.getRetType(minfo.getDescriptor()));
    }

    protected MapMaker(MapMaker old) {
        super(old);
    }

    void make(TypedBlock[] blocks, byte[] code) throws BadBytecode {
        this.make(code, blocks[0]);
        this.findDeadCatchers(code, blocks);
        try {
            this.fixTypes(code, blocks);
            return;
        }
        catch (NotFoundException e) {
            throw new BadBytecode("failed to resolve types", (Throwable)e);
        }
    }

    private void make(byte[] code, TypedBlock tb) throws BadBytecode {
        int pos;
        MapMaker.copyTypeData(tb.stackTop, tb.stackTypes, this.stackTypes);
        this.stackTop = tb.stackTop;
        MapMaker.copyTypeData(tb.localsTypes.length, tb.localsTypes, this.localsTypes);
        this.traceException(code, tb.toCatch);
        int end = pos + tb.length;
        for (pos = tb.position; pos < end; pos += this.doOpcode(pos, code)) {
            this.traceException(code, tb.toCatch);
        }
        if (tb.exit == null) return;
        int i = 0;
        while (i < tb.exit.length) {
            TypedBlock e = (TypedBlock)tb.exit[i];
            if (e.alreadySet()) {
                this.mergeMap(e, true);
            } else {
                this.recordStackMap(e);
                MapMaker maker = new MapMaker(this);
                maker.make(code, e);
            }
            ++i;
        }
    }

    private void traceException(byte[] code, BasicBlock.Catch handler) throws BadBytecode {
        while (handler != null) {
            TypedBlock tb = (TypedBlock)handler.body;
            if (tb.alreadySet()) {
                this.mergeMap(tb, false);
                if (tb.stackTop < 1) {
                    throw new BadBytecode("bad catch clause: " + handler.typeIndex);
                }
                tb.stackTypes[0] = this.merge(this.toExceptionType(handler.typeIndex), tb.stackTypes[0]);
            } else {
                this.recordStackMap(tb, handler.typeIndex);
                MapMaker maker = new MapMaker(this);
                maker.make(code, tb);
            }
            handler = handler.next;
        }
    }

    private void mergeMap(TypedBlock dest, boolean mergeStack) throws BadBytecode {
        int i;
        int n = this.localsTypes.length;
        for (i = 0; i < n; ++i) {
            dest.localsTypes[i] = this.merge(MapMaker.validateTypeData(this.localsTypes, n, i), dest.localsTypes[i]);
        }
        if (!mergeStack) return;
        n = this.stackTop;
        i = 0;
        while (i < n) {
            dest.stackTypes[i] = this.merge(this.stackTypes[i], dest.stackTypes[i]);
            ++i;
        }
    }

    private TypeData merge(TypeData src, TypeData target) throws BadBytecode {
        if (src == target) {
            return target;
        }
        if (target instanceof TypeData.ClassName) return target;
        if (target instanceof TypeData.BasicType) {
            return target;
        }
        if (!(target instanceof TypeData.AbsTypeVar)) throw new RuntimeException("fatal: this should never happen");
        ((TypeData.AbsTypeVar)target).merge(src);
        return target;
    }

    private void recordStackMap(TypedBlock target) throws BadBytecode {
        TypeData[] tStackTypes = TypeData.make(this.stackTypes.length);
        int st = this.stackTop;
        MapMaker.recordTypeData(st, this.stackTypes, tStackTypes);
        this.recordStackMap0(target, st, tStackTypes);
    }

    private void recordStackMap(TypedBlock target, int exceptionType) throws BadBytecode {
        TypeData[] tStackTypes = TypeData.make(this.stackTypes.length);
        tStackTypes[0] = this.toExceptionType(exceptionType).join();
        this.recordStackMap0(target, 1, tStackTypes);
    }

    private TypeData.ClassName toExceptionType(int exceptionType) {
        String type;
        if (exceptionType == 0) {
            type = "java.lang.Throwable";
            return new TypeData.ClassName(type);
        }
        type = this.cpool.getClassInfo(exceptionType);
        return new TypeData.ClassName(type);
    }

    private void recordStackMap0(TypedBlock target, int st, TypeData[] tStackTypes) throws BadBytecode {
        int n = this.localsTypes.length;
        TypeData[] tLocalsTypes = TypeData.make(n);
        int k = MapMaker.recordTypeData(n, this.localsTypes, tLocalsTypes);
        target.setStackMap(st, tStackTypes, k, tLocalsTypes);
    }

    protected static int recordTypeData(int n, TypeData[] srcTypes, TypeData[] destTypes) {
        int k = -1;
        int i = 0;
        while (i < n) {
            TypeData t = MapMaker.validateTypeData(srcTypes, n, i);
            destTypes[i] = t.join();
            if (t != TOP) {
                k = i + 1;
            }
            ++i;
        }
        return k + 1;
    }

    protected static void copyTypeData(int n, TypeData[] srcTypes, TypeData[] destTypes) {
        int i = 0;
        while (i < n) {
            destTypes[i] = srcTypes[i];
            ++i;
        }
    }

    private static TypeData validateTypeData(TypeData[] data, int length, int index) {
        TypeData td = data[index];
        if (!td.is2WordType()) return td;
        if (index + 1 >= length) return td;
        if (data[index + 1] == TOP) return td;
        return TOP;
    }

    private void findDeadCatchers(byte[] code, TypedBlock[] blocks) throws BadBytecode {
        int len = blocks.length;
        int i = 0;
        while (i < len) {
            TypedBlock block = blocks[i];
            if (!block.alreadySet()) {
                TypedBlock tb;
                this.fixDeadcode(code, block);
                BasicBlock.Catch handler = block.toCatch;
                if (handler != null && !(tb = (TypedBlock)handler.body).alreadySet()) {
                    this.recordStackMap(tb, handler.typeIndex);
                    this.fixDeadcode(code, tb);
                    tb.incoming = 1;
                }
            }
            ++i;
        }
    }

    private void fixDeadcode(byte[] code, TypedBlock block) throws BadBytecode {
        int pos = block.position;
        int len = block.length - 3;
        if (len < 0) {
            if (len == -1) {
                code[pos] = 0;
            }
            code[pos + block.length - 1] = -65;
            block.incoming = 1;
            this.recordStackMap(block, 0);
            return;
        }
        block.incoming = 0;
        int k = 0;
        while (true) {
            if (k >= len) {
                code[pos + len] = -89;
                ByteArray.write16bit(-len, code, pos + len + 1);
                return;
            }
            code[pos + k] = 0;
            ++k;
        }
    }

    private void fixTypes(byte[] code, TypedBlock[] blocks) throws NotFoundException, BadBytecode {
        ArrayList<TypeData> preOrder = new ArrayList<TypeData>();
        int len = blocks.length;
        int index = 0;
        int i = 0;
        while (i < len) {
            TypedBlock block = blocks[i];
            if (block.alreadySet()) {
                int j;
                int n = block.localsTypes.length;
                for (j = 0; j < n; ++j) {
                    index = block.localsTypes[j].dfs(preOrder, index, this.classPool);
                }
                n = block.stackTop;
                for (j = 0; j < n; ++j) {
                    index = block.stackTypes[j].dfs(preOrder, index, this.classPool);
                }
            }
            ++i;
        }
    }

    public StackMapTable toStackMap(TypedBlock[] blocks) {
        StackMapTable.Writer writer = new StackMapTable.Writer(32);
        int n = blocks.length;
        TypedBlock prev = blocks[0];
        int offsetDelta = prev.length;
        if (prev.incoming > 0) {
            writer.sameFrame(0);
            --offsetDelta;
        }
        int i = 1;
        while (i < n) {
            TypedBlock bb = blocks[i];
            if (this.isTarget(bb, blocks[i - 1])) {
                bb.resetNumLocals();
                int diffL = MapMaker.stackMapDiff(prev.numLocals, prev.localsTypes, bb.numLocals, bb.localsTypes);
                this.toStackMapBody(writer, bb, diffL, offsetDelta, prev);
                offsetDelta = bb.length - 1;
                prev = bb;
            } else if (bb.incoming == 0) {
                writer.sameFrame(offsetDelta);
                offsetDelta = bb.length - 1;
            } else {
                offsetDelta += bb.length;
            }
            ++i;
        }
        return writer.toStackMapTable(this.cpool);
    }

    private boolean isTarget(TypedBlock cur, TypedBlock prev) {
        int in = cur.incoming;
        if (in > 1) {
            return true;
        }
        if (in >= 1) return prev.stop;
        return false;
    }

    private void toStackMapBody(StackMapTable.Writer writer, TypedBlock bb, int diffL, int offsetDelta, TypedBlock prev) {
        int stackTop = bb.stackTop;
        if (stackTop == 0) {
            if (diffL == 0) {
                writer.sameFrame(offsetDelta);
                return;
            }
            if (0 > diffL && diffL >= -3) {
                writer.chopFrame(offsetDelta, -diffL);
                return;
            }
            if (0 < diffL && diffL <= 3) {
                int[] data = new int[diffL];
                int[] tags = this.fillStackMap(bb.numLocals - prev.numLocals, prev.numLocals, data, bb.localsTypes);
                writer.appendFrame(offsetDelta, tags, data);
                return;
            }
        } else {
            TypeData td;
            if (stackTop == 1 && diffL == 0) {
                TypeData td2 = bb.stackTypes[0];
                writer.sameLocals(offsetDelta, td2.getTypeTag(), td2.getTypeData(this.cpool));
                return;
            }
            if (stackTop == 2 && diffL == 0 && (td = bb.stackTypes[0]).is2WordType()) {
                writer.sameLocals(offsetDelta, td.getTypeTag(), td.getTypeData(this.cpool));
                return;
            }
        }
        int[] sdata = new int[stackTop];
        int[] stags = this.fillStackMap(stackTop, 0, sdata, bb.stackTypes);
        int[] ldata = new int[bb.numLocals];
        int[] ltags = this.fillStackMap(bb.numLocals, 0, ldata, bb.localsTypes);
        writer.fullFrame(offsetDelta, ltags, ldata, stags, sdata);
    }

    private int[] fillStackMap(int num, int offset, int[] data, TypeData[] types) {
        int realNum = MapMaker.diffSize(types, offset, offset + num);
        ConstPool cp = this.cpool;
        int[] tags = new int[realNum];
        int j = 0;
        int i = 0;
        while (i < num) {
            TypeData td = types[offset + i];
            tags[j] = td.getTypeTag();
            data[j] = td.getTypeData(cp);
            if (td.is2WordType()) {
                ++i;
            }
            ++j;
            ++i;
        }
        return tags;
    }

    private static int stackMapDiff(int oldTdLen, TypeData[] oldTd, int newTdLen, TypeData[] newTd) {
        int diff = newTdLen - oldTdLen;
        int len = diff > 0 ? oldTdLen : newTdLen;
        if (!MapMaker.stackMapEq(oldTd, newTd, len)) return -100;
        if (diff <= 0) return -MapMaker.diffSize(oldTd, len, oldTdLen);
        return MapMaker.diffSize(newTd, len, newTdLen);
    }

    private static boolean stackMapEq(TypeData[] oldTd, TypeData[] newTd, int len) {
        int i = 0;
        while (i < len) {
            if (!oldTd[i].eq(newTd[i])) {
                return false;
            }
            ++i;
        }
        return true;
    }

    private static int diffSize(TypeData[] types, int offset, int len) {
        int num = 0;
        while (offset < len) {
            TypeData td = types[offset++];
            ++num;
            if (!td.is2WordType()) continue;
            ++offset;
        }
        return num;
    }

    public StackMap toStackMap2(ConstPool cp, TypedBlock[] blocks) {
        int i;
        StackMap.Writer writer = new StackMap.Writer();
        int n = blocks.length;
        boolean[] effective = new boolean[n];
        TypedBlock prev = blocks[0];
        effective[0] = prev.incoming > 0;
        int num = effective[0] ? 1 : 0;
        for (i = 1; i < n; ++i) {
            TypedBlock bb = blocks[i];
            effective[i] = this.isTarget(bb, blocks[i - 1]);
            if (!effective[i]) continue;
            bb.resetNumLocals();
            prev = bb;
            ++num;
        }
        if (num == 0) {
            return null;
        }
        writer.write16bit(num);
        i = 0;
        while (i < n) {
            if (effective[i]) {
                this.writeStackFrame(writer, cp, blocks[i].position, blocks[i]);
            }
            ++i;
        }
        return writer.toStackMap(cp);
    }

    private void writeStackFrame(StackMap.Writer writer, ConstPool cp, int offset, TypedBlock tb) {
        writer.write16bit(offset);
        this.writeVerifyTypeInfo(writer, cp, tb.localsTypes, tb.numLocals);
        this.writeVerifyTypeInfo(writer, cp, tb.stackTypes, tb.stackTop);
    }

    private void writeVerifyTypeInfo(StackMap.Writer writer, ConstPool cp, TypeData[] types, int num) {
        TypeData td;
        int i;
        int numDWord = 0;
        for (i = 0; i < num; ++i) {
            td = types[i];
            if (td == null || !td.is2WordType()) continue;
            ++numDWord;
            ++i;
        }
        writer.write16bit(num - numDWord);
        i = 0;
        while (i < num) {
            td = types[i];
            writer.writeVerifyTypeInfo(td.getTypeTag(), td.getTypeData(cp));
            if (td.is2WordType()) {
                ++i;
            }
            ++i;
        }
    }
}

