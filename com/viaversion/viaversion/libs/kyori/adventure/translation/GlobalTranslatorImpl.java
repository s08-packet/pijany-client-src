/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.translation;

import com.viaversion.viaversion.libs.kyori.adventure.key.Key;
import com.viaversion.viaversion.libs.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import com.viaversion.viaversion.libs.kyori.adventure.translation.GlobalTranslator;
import com.viaversion.viaversion.libs.kyori.adventure.translation.Translator;
import com.viaversion.viaversion.libs.kyori.examination.ExaminableProperty;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class GlobalTranslatorImpl
implements GlobalTranslator {
    private static final Key NAME = Key.key("adventure", "global");
    static final GlobalTranslatorImpl INSTANCE = new GlobalTranslatorImpl();
    final TranslatableComponentRenderer<Locale> renderer = TranslatableComponentRenderer.usingTranslationSource(this);
    private final Set<Translator> sources = Collections.newSetFromMap(new ConcurrentHashMap());

    private GlobalTranslatorImpl() {
    }

    @Override
    @NotNull
    public Key name() {
        return NAME;
    }

    @Override
    @NotNull
    public Iterable<? extends Translator> sources() {
        return Collections.unmodifiableSet(this.sources);
    }

    @Override
    public boolean addSource(@NotNull Translator source) {
        Objects.requireNonNull(source, "source");
        if (source != this) return this.sources.add(source);
        throw new IllegalArgumentException("GlobalTranslationSource");
    }

    @Override
    public boolean removeSource(@NotNull Translator source) {
        Objects.requireNonNull(source, "source");
        return this.sources.remove(source);
    }

    @Override
    @Nullable
    public MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        Translator source;
        MessageFormat translation;
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(locale, "locale");
        Iterator<Translator> iterator = this.sources.iterator();
        do {
            if (!iterator.hasNext()) return null;
        } while ((translation = (source = iterator.next()).translate(key, locale)) == null);
        return translation;
    }

    @Override
    @NotNull
    public Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(ExaminableProperty.of("sources", this.sources));
    }
}

