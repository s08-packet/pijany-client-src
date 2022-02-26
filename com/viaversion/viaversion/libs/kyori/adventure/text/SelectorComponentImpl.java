/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.text;

import com.viaversion.viaversion.libs.kyori.adventure.text.AbstractComponent;
import com.viaversion.viaversion.libs.kyori.adventure.text.AbstractComponentBuilder;
import com.viaversion.viaversion.libs.kyori.adventure.text.Component;
import com.viaversion.viaversion.libs.kyori.adventure.text.ComponentLike;
import com.viaversion.viaversion.libs.kyori.adventure.text.SelectorComponent;
import com.viaversion.viaversion.libs.kyori.adventure.text.format.Style;
import com.viaversion.viaversion.libs.kyori.examination.ExaminableProperty;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SelectorComponentImpl
extends AbstractComponent
implements SelectorComponent {
    private final String pattern;
    @Nullable
    private final Component separator;

    SelectorComponentImpl(@NotNull List<? extends ComponentLike> children, @NotNull Style style, @NotNull String pattern, @Nullable ComponentLike separator) {
        super(children, style);
        this.pattern = pattern;
        this.separator = ComponentLike.unbox(separator);
    }

    @Override
    @NotNull
    public String pattern() {
        return this.pattern;
    }

    @Override
    @NotNull
    public SelectorComponent pattern(@NotNull String pattern) {
        if (!Objects.equals(this.pattern, pattern)) return new SelectorComponentImpl(this.children, this.style, Objects.requireNonNull(pattern, "pattern"), this.separator);
        return this;
    }

    @Override
    @Nullable
    public Component separator() {
        return this.separator;
    }

    @Override
    @NotNull
    public SelectorComponent separator(@Nullable ComponentLike separator) {
        return new SelectorComponentImpl(this.children, this.style, this.pattern, separator);
    }

    @Override
    @NotNull
    public SelectorComponent children(@NotNull List<? extends ComponentLike> children) {
        return new SelectorComponentImpl(children, this.style, this.pattern, this.separator);
    }

    @Override
    @NotNull
    public SelectorComponent style(@NotNull Style style) {
        return new SelectorComponentImpl(this.children, style, this.pattern, this.separator);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SelectorComponent)) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        SelectorComponent that = (SelectorComponent)other;
        if (!Objects.equals(this.pattern, that.pattern())) return false;
        if (!Objects.equals(this.separator, that.separator())) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.pattern.hashCode();
        return 31 * result + Objects.hashCode(this.separator);
    }

    @Override
    @NotNull
    protected Stream<? extends ExaminableProperty> examinablePropertiesWithoutChildren() {
        return Stream.concat(Stream.of(ExaminableProperty.of("pattern", this.pattern), ExaminableProperty.of("separator", this.separator)), super.examinablePropertiesWithoutChildren());
    }

    @Override
    @NotNull
    public SelectorComponent.Builder toBuilder() {
        return new BuilderImpl(this);
    }

    static final class BuilderImpl
    extends AbstractComponentBuilder<SelectorComponent, SelectorComponent.Builder>
    implements SelectorComponent.Builder {
        @Nullable
        private String pattern;
        @Nullable
        private Component separator;

        BuilderImpl() {
        }

        BuilderImpl(@NotNull SelectorComponent component) {
            super(component);
            this.pattern = component.pattern();
        }

        @Override
        @NotNull
        public SelectorComponent.Builder pattern(@NotNull String pattern) {
            this.pattern = pattern;
            return this;
        }

        @Override
        @NotNull
        public SelectorComponent.Builder separator(@Nullable ComponentLike separator) {
            this.separator = ComponentLike.unbox(separator);
            return this;
        }

        @Override
        @NotNull
        public SelectorComponent build() {
            if (this.pattern != null) return new SelectorComponentImpl(this.children, this.buildStyle(), this.pattern, this.separator);
            throw new IllegalStateException("pattern must be set");
        }
    }
}

