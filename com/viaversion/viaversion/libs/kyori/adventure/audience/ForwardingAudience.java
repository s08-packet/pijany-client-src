/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.kyori.adventure.audience;

import com.viaversion.viaversion.libs.kyori.adventure.audience.Audience;
import com.viaversion.viaversion.libs.kyori.adventure.audience.MessageType;
import com.viaversion.viaversion.libs.kyori.adventure.bossbar.BossBar;
import com.viaversion.viaversion.libs.kyori.adventure.identity.Identified;
import com.viaversion.viaversion.libs.kyori.adventure.identity.Identity;
import com.viaversion.viaversion.libs.kyori.adventure.inventory.Book;
import com.viaversion.viaversion.libs.kyori.adventure.pointer.Pointer;
import com.viaversion.viaversion.libs.kyori.adventure.pointer.Pointers;
import com.viaversion.viaversion.libs.kyori.adventure.sound.Sound;
import com.viaversion.viaversion.libs.kyori.adventure.sound.SoundStop;
import com.viaversion.viaversion.libs.kyori.adventure.text.Component;
import com.viaversion.viaversion.libs.kyori.adventure.title.TitlePart;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@FunctionalInterface
public interface ForwardingAudience
extends Audience {
    @ApiStatus.OverrideOnly
    @NotNull
    public Iterable<? extends Audience> audiences();

    @Override
    @NotNull
    default public Pointers pointers() {
        return Pointers.empty();
    }

    @Override
    @NotNull
    default public Audience filterAudience(@NotNull Predicate<? super Audience> filter) {
        Audience audience;
        @Nullable ArrayList<Audience> audiences = null;
        for (Audience audience2 : this.audiences()) {
            Audience filtered;
            if (!filter.test(audience2) || (filtered = audience2.filterAudience(filter)) == Audience.empty()) continue;
            if (audiences == null) {
                audiences = new ArrayList<Audience>();
            }
            audiences.add(filtered);
        }
        if (audiences != null) {
            audience = Audience.audience(audiences);
            return audience;
        }
        audience = Audience.empty();
        return audience;
    }

    @Override
    default public void forEachAudience(@NotNull Consumer<? super Audience> action) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.forEachAudience(action);
        }
    }

    @Override
    default public void sendMessage(@NotNull Identified source, @NotNull Component message, @NotNull MessageType type) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.sendMessage(source, message, type);
        }
    }

    @Override
    default public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.sendMessage(source, message, type);
        }
    }

    @Override
    default public void sendActionBar(@NotNull Component message) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.sendActionBar(message);
        }
    }

    @Override
    default public void sendPlayerListHeader(@NotNull Component header) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.sendPlayerListHeader(header);
        }
    }

    @Override
    default public void sendPlayerListFooter(@NotNull Component footer) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.sendPlayerListFooter(footer);
        }
    }

    @Override
    default public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.sendPlayerListHeaderAndFooter(header, footer);
        }
    }

    @Override
    default public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.sendTitlePart(part, value);
        }
    }

    @Override
    default public void clearTitle() {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.clearTitle();
        }
    }

    @Override
    default public void resetTitle() {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.resetTitle();
        }
    }

    @Override
    default public void showBossBar(@NotNull BossBar bar) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.showBossBar(bar);
        }
    }

    @Override
    default public void hideBossBar(@NotNull BossBar bar) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.hideBossBar(bar);
        }
    }

    @Override
    default public void playSound(@NotNull Sound sound) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.playSound(sound);
        }
    }

    @Override
    default public void playSound(@NotNull Sound sound, double x, double y, double z) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.playSound(sound, x, y, z);
        }
    }

    @Override
    default public void playSound(@NotNull Sound sound, @NotNull Sound.Emitter emitter) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.playSound(sound, emitter);
        }
    }

    @Override
    default public void stopSound(@NotNull SoundStop stop) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.stopSound(stop);
        }
    }

    @Override
    default public void openBook(@NotNull Book book) {
        Iterator<? extends Audience> iterator = this.audiences().iterator();
        while (iterator.hasNext()) {
            Audience audience = iterator.next();
            audience.openBook(book);
        }
    }

    public static interface Single
    extends ForwardingAudience {
        @ApiStatus.OverrideOnly
        @NotNull
        public Audience audience();

        @Override
        @Deprecated
        @NotNull
        default public Iterable<? extends Audience> audiences() {
            return Collections.singleton(this.audience());
        }

        @Override
        @NotNull
        default public <T> Optional<T> get(@NotNull Pointer<T> pointer) {
            return this.audience().get(pointer);
        }

        @Override
        @Contract(value="_, null -> null; _, !null -> !null")
        @Nullable
        default public <T> T getOrDefault(@NotNull Pointer<T> pointer, @Nullable T defaultValue) {
            return this.audience().getOrDefault(pointer, defaultValue);
        }

        @Override
        default public <T> @UnknownNullability T getOrDefaultFrom(@NotNull Pointer<T> pointer, @NotNull Supplier<? extends T> defaultValue) {
            return this.audience().getOrDefaultFrom(pointer, defaultValue);
        }

        @Override
        @NotNull
        default public Audience filterAudience(@NotNull Predicate<? super Audience> filter) {
            Audience audience;
            Audience audience2 = this.audience();
            if (filter.test(audience2)) {
                audience = this;
                return audience;
            }
            audience = Audience.empty();
            return audience;
        }

        @Override
        default public void forEachAudience(@NotNull Consumer<? super Audience> action) {
            this.audience().forEachAudience(action);
        }

        @Override
        @NotNull
        default public Pointers pointers() {
            return this.audience().pointers();
        }

        @Override
        default public void sendMessage(@NotNull Identified source, @NotNull Component message, @NotNull MessageType type) {
            this.audience().sendMessage(source, message, type);
        }

        @Override
        default public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
            this.audience().sendMessage(source, message, type);
        }

        @Override
        default public void sendActionBar(@NotNull Component message) {
            this.audience().sendActionBar(message);
        }

        @Override
        default public void sendPlayerListHeader(@NotNull Component header) {
            this.audience().sendPlayerListHeader(header);
        }

        @Override
        default public void sendPlayerListFooter(@NotNull Component footer) {
            this.audience().sendPlayerListFooter(footer);
        }

        @Override
        default public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
            this.audience().sendPlayerListHeaderAndFooter(header, footer);
        }

        @Override
        default public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
            this.audience().sendTitlePart(part, value);
        }

        @Override
        default public void clearTitle() {
            this.audience().clearTitle();
        }

        @Override
        default public void resetTitle() {
            this.audience().resetTitle();
        }

        @Override
        default public void showBossBar(@NotNull BossBar bar) {
            this.audience().showBossBar(bar);
        }

        @Override
        default public void hideBossBar(@NotNull BossBar bar) {
            this.audience().hideBossBar(bar);
        }

        @Override
        default public void playSound(@NotNull Sound sound) {
            this.audience().playSound(sound);
        }

        @Override
        default public void playSound(@NotNull Sound sound, double x, double y, double z) {
            this.audience().playSound(sound, x, y, z);
        }

        @Override
        default public void playSound(@NotNull Sound sound, @NotNull Sound.Emitter emitter) {
            this.audience().playSound(sound, emitter);
        }

        @Override
        default public void stopSound(@NotNull SoundStop stop) {
            this.audience().stopSound(stop);
        }

        @Override
        default public void openBook(@NotNull Book book) {
            this.audience().openBook(book);
        }
    }
}

