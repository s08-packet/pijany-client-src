/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.gson;

import com.viaversion.viaversion.libs.gson.JsonSyntaxException;
import com.viaversion.viaversion.libs.gson.TypeAdapter;
import com.viaversion.viaversion.libs.gson.internal.JavaVersion;
import com.viaversion.viaversion.libs.gson.internal.PreJava9DateFormatProvider;
import com.viaversion.viaversion.libs.gson.internal.bind.util.ISO8601Utils;
import com.viaversion.viaversion.libs.gson.stream.JsonReader;
import com.viaversion.viaversion.libs.gson.stream.JsonToken;
import com.viaversion.viaversion.libs.gson.stream.JsonWriter;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class DefaultDateTypeAdapter
extends TypeAdapter<java.util.Date> {
    private static final String SIMPLE_NAME = "DefaultDateTypeAdapter";
    private final Class<? extends java.util.Date> dateType;
    private final List<DateFormat> dateFormats = new ArrayList<DateFormat>();

    DefaultDateTypeAdapter(Class<? extends java.util.Date> dateType) {
        this.dateType = DefaultDateTypeAdapter.verifyDateType(dateType);
        this.dateFormats.add(DateFormat.getDateTimeInstance(2, 2, Locale.US));
        if (!Locale.getDefault().equals(Locale.US)) {
            this.dateFormats.add(DateFormat.getDateTimeInstance(2, 2));
        }
        if (!JavaVersion.isJava9OrLater()) return;
        this.dateFormats.add(PreJava9DateFormatProvider.getUSDateTimeFormat(2, 2));
    }

    DefaultDateTypeAdapter(Class<? extends java.util.Date> dateType, String datePattern) {
        this.dateType = DefaultDateTypeAdapter.verifyDateType(dateType);
        this.dateFormats.add(new SimpleDateFormat(datePattern, Locale.US));
        if (Locale.getDefault().equals(Locale.US)) return;
        this.dateFormats.add(new SimpleDateFormat(datePattern));
    }

    DefaultDateTypeAdapter(Class<? extends java.util.Date> dateType, int style) {
        this.dateType = DefaultDateTypeAdapter.verifyDateType(dateType);
        this.dateFormats.add(DateFormat.getDateInstance(style, Locale.US));
        if (!Locale.getDefault().equals(Locale.US)) {
            this.dateFormats.add(DateFormat.getDateInstance(style));
        }
        if (!JavaVersion.isJava9OrLater()) return;
        this.dateFormats.add(PreJava9DateFormatProvider.getUSDateFormat(style));
    }

    public DefaultDateTypeAdapter(int dateStyle, int timeStyle) {
        this(java.util.Date.class, dateStyle, timeStyle);
    }

    public DefaultDateTypeAdapter(Class<? extends java.util.Date> dateType, int dateStyle, int timeStyle) {
        this.dateType = DefaultDateTypeAdapter.verifyDateType(dateType);
        this.dateFormats.add(DateFormat.getDateTimeInstance(dateStyle, timeStyle, Locale.US));
        if (!Locale.getDefault().equals(Locale.US)) {
            this.dateFormats.add(DateFormat.getDateTimeInstance(dateStyle, timeStyle));
        }
        if (!JavaVersion.isJava9OrLater()) return;
        this.dateFormats.add(PreJava9DateFormatProvider.getUSDateTimeFormat(dateStyle, timeStyle));
    }

    private static Class<? extends java.util.Date> verifyDateType(Class<? extends java.util.Date> dateType) {
        if (dateType == java.util.Date.class) return dateType;
        if (dateType == Date.class) return dateType;
        if (dateType == Timestamp.class) return dateType;
        throw new IllegalArgumentException("Date type must be one of " + java.util.Date.class + ", " + Timestamp.class + ", or " + Date.class + " but was " + dateType);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(JsonWriter out, java.util.Date value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        List<DateFormat> list = this.dateFormats;
        synchronized (list) {
            String dateFormatAsString = this.dateFormats.get(0).format(value);
            out.value(dateFormatAsString);
            return;
        }
    }

    @Override
    public java.util.Date read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        java.util.Date date = this.deserializeToDate(in.nextString());
        if (this.dateType == java.util.Date.class) {
            return date;
        }
        if (this.dateType == Timestamp.class) {
            return new Timestamp(date.getTime());
        }
        if (this.dateType != Date.class) throw new AssertionError();
        return new Date(date.getTime());
    }

    private java.util.Date deserializeToDate(String s) {
        List<DateFormat> list = this.dateFormats;
        synchronized (list) {
            for (DateFormat dateFormat : this.dateFormats) {
                try {
                    return dateFormat.parse(s);
                }
                catch (ParseException parseException) {
                }
            }
            try {
                return ISO8601Utils.parse(s, new ParsePosition(0));
            }
            catch (ParseException e) {
                throw new JsonSyntaxException(s, e);
            }
        }
    }

    public String toString() {
        DateFormat defaultFormat = this.dateFormats.get(0);
        if (!(defaultFormat instanceof SimpleDateFormat)) return "DefaultDateTypeAdapter(" + defaultFormat.getClass().getSimpleName() + ')';
        return "DefaultDateTypeAdapter(" + ((SimpleDateFormat)defaultFormat).toPattern() + ')';
    }
}

