/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.javassist.bytecode.annotation;

import com.viaversion.viaversion.libs.javassist.bytecode.annotation.AnnotationMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.ArrayMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.BooleanMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.ByteMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.CharMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.ClassMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.DoubleMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.EnumMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.FloatMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.IntegerMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.LongMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.ShortMemberValue;
import com.viaversion.viaversion.libs.javassist.bytecode.annotation.StringMemberValue;

public interface MemberValueVisitor {
    public void visitAnnotationMemberValue(AnnotationMemberValue var1);

    public void visitArrayMemberValue(ArrayMemberValue var1);

    public void visitBooleanMemberValue(BooleanMemberValue var1);

    public void visitByteMemberValue(ByteMemberValue var1);

    public void visitCharMemberValue(CharMemberValue var1);

    public void visitDoubleMemberValue(DoubleMemberValue var1);

    public void visitEnumMemberValue(EnumMemberValue var1);

    public void visitFloatMemberValue(FloatMemberValue var1);

    public void visitIntegerMemberValue(IntegerMemberValue var1);

    public void visitLongMemberValue(LongMemberValue var1);

    public void visitShortMemberValue(ShortMemberValue var1);

    public void visitStringMemberValue(StringMemberValue var1);

    public void visitClassMemberValue(ClassMemberValue var1);
}

