/*
 * Decompiled with CFR 0.152.
 */
package optfine;

import optfine.Config;

public class ReflectorClass {
    private String targetClassName = null;
    private boolean checked = false;
    private Class targetClass = null;

    public ReflectorClass(String p_i55_1_) {
        this.targetClassName = p_i55_1_;
        Class oclass = this.getTargetClass();
    }

    public ReflectorClass(Class p_i56_1_) {
        this.targetClass = p_i56_1_;
        this.targetClassName = p_i56_1_.getName();
        this.checked = true;
    }

    public Class getTargetClass() {
        if (this.checked) {
            return this.targetClass;
        }
        this.checked = true;
        try {
            this.targetClass = Class.forName(this.targetClassName);
            return this.targetClass;
        }
        catch (ClassNotFoundException var2) {
            Config.log("(Reflector) Class not present: " + this.targetClassName);
            return this.targetClass;
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return this.targetClass;
    }

    public boolean exists() {
        if (this.getTargetClass() == null) return false;
        return true;
    }

    public String getTargetClassName() {
        return this.targetClassName;
    }
}

