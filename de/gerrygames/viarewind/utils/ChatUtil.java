/*
 * Decompiled with CFR 0.152.
 */
package de.gerrygames.viarewind.utils;

import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.kyori.adventure.text.Component;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import com.viaversion.viaversion.libs.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import com.viaversion.viaversion.protocols.protocol1_13to1_12_2.ChatRewriter;
import de.gerrygames.viarewind.ViaRewind;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class ChatUtil {
    private static final Pattern UNUSED_COLOR_PATTERN = Pattern.compile("(?>(?>\u00a7[0-fk-or])*(\u00a7r|\\Z))|(?>(?>\u00a7[0-f])*(\u00a7[0-f]))");

    public static String jsonToLegacy(String json) {
        if (json == null) return "";
        if (json.equals("null")) return "";
        if (json.isEmpty()) {
            return "";
        }
        try {
            String legacy = LegacyComponentSerializer.legacySection().serialize((Component)ChatRewriter.HOVER_GSON_SERIALIZER.deserialize(json));
            while (legacy.startsWith("\u00a7f")) {
                legacy = legacy.substring(2);
            }
            return legacy;
        }
        catch (Exception ex) {
            ViaRewind.getPlatform().getLogger().log(Level.WARNING, "Could not convert component to legacy text: " + json, ex);
            return "";
        }
    }

    public static String jsonToLegacy(JsonElement component) {
        if (component.isJsonNull()) return "";
        if (component.isJsonArray()) {
            if (component.getAsJsonArray().isEmpty()) return "";
        }
        if (component.isJsonObject() && component.getAsJsonObject().size() == 0) {
            return "";
        }
        if (!component.isJsonPrimitive()) return ChatUtil.jsonToLegacy(component.toString());
        return component.getAsString();
    }

    public static String legacyToJson(String legacy) {
        if (legacy != null) return (String)GsonComponentSerializer.gson().serialize(LegacyComponentSerializer.legacySection().deserialize(legacy));
        return "";
    }

    public static String removeUnusedColor(String legacy, char last) {
        if (legacy == null) {
            return null;
        }
        legacy = UNUSED_COLOR_PATTERN.matcher(legacy).replaceAll("$1$2");
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < legacy.length()) {
            char current = legacy.charAt(i);
            if (current != '\u00a7' || i == legacy.length() - 1) {
                builder.append(current);
            } else if ((current = legacy.charAt(++i)) != last) {
                builder.append('\u00a7').append(current);
                last = current;
            }
            ++i;
        }
        return builder.toString();
    }
}

