/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.gson;

import com.viaversion.viaversion.libs.gson.FieldNamingStrategy;
import java.lang.reflect.Field;
import java.util.Locale;

public enum FieldNamingPolicy implements FieldNamingStrategy
{
    IDENTITY{

        @Override
        public String translateName(Field f) {
            return f.getName();
        }
    }
    ,
    UPPER_CAMEL_CASE{

        @Override
        public String translateName(Field f) {
            return 2.upperCaseFirstLetter(f.getName());
        }
    }
    ,
    UPPER_CAMEL_CASE_WITH_SPACES{

        @Override
        public String translateName(Field f) {
            return 3.upperCaseFirstLetter(3.separateCamelCase(f.getName(), " "));
        }
    }
    ,
    LOWER_CASE_WITH_UNDERSCORES{

        @Override
        public String translateName(Field f) {
            return 4.separateCamelCase(f.getName(), "_").toLowerCase(Locale.ENGLISH);
        }
    }
    ,
    LOWER_CASE_WITH_DASHES{

        @Override
        public String translateName(Field f) {
            return 5.separateCamelCase(f.getName(), "-").toLowerCase(Locale.ENGLISH);
        }
    }
    ,
    LOWER_CASE_WITH_DOTS{

        @Override
        public String translateName(Field f) {
            return 6.separateCamelCase(f.getName(), ".").toLowerCase(Locale.ENGLISH);
        }
    };


    static String separateCamelCase(String name, String separator) {
        StringBuilder translation = new StringBuilder();
        int i = 0;
        int length = name.length();
        while (i < length) {
            char character = name.charAt(i);
            if (Character.isUpperCase(character) && translation.length() != 0) {
                translation.append(separator);
            }
            translation.append(character);
            ++i;
        }
        return translation.toString();
    }

    static String upperCaseFirstLetter(String name) {
        int firstLetterIndex;
        int limit = name.length() - 1;
        for (firstLetterIndex = 0; !Character.isLetter(name.charAt(firstLetterIndex)) && firstLetterIndex < limit; ++firstLetterIndex) {
        }
        char firstLetter = name.charAt(firstLetterIndex);
        if (Character.isUpperCase(firstLetter)) {
            return name;
        }
        char uppercased = Character.toUpperCase(firstLetter);
        if (firstLetterIndex != 0) return name.substring(0, firstLetterIndex) + uppercased + name.substring(firstLetterIndex + 1);
        return uppercased + name.substring(1);
    }
}

