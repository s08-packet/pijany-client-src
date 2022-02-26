/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.util;

import com.viaversion.viaversion.libs.kyori.adventure.util.Services0;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;
import org.jetbrains.annotations.NotNull;

public final class Services {
    private static final boolean SERVICE_LOAD_FAILURES_ARE_FATAL = Boolean.parseBoolean(System.getProperty(String.join((CharSequence)".", "net", "kyori", "adventure", "serviceLoadFailuresAreFatal"), String.valueOf(true)));

    private Services() {
    }

    @NotNull
    public static <P> Optional<P> service(@NotNull Class<P> type) {
        P instance;
        ServiceLoader<P> loader = Services0.loader(type);
        Iterator<P> it = loader.iterator();
        while (true) {
            if (!it.hasNext()) return Optional.empty();
            try {
                instance = it.next();
            }
            catch (Throwable t) {
                if (!SERVICE_LOAD_FAILURES_ARE_FATAL) continue;
                throw new IllegalStateException("Encountered an exception loading service " + type, t);
            }
            break;
        }
        if (!it.hasNext()) return Optional.of(instance);
        throw new IllegalStateException("Expected to find one service " + type + ", found multiple");
    }
}

