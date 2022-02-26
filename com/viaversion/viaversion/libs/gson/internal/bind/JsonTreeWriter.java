/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.libs.gson.internal.bind;

import com.viaversion.viaversion.libs.gson.JsonArray;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonNull;
import com.viaversion.viaversion.libs.gson.JsonObject;
import com.viaversion.viaversion.libs.gson.JsonPrimitive;
import com.viaversion.viaversion.libs.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public final class JsonTreeWriter
extends JsonWriter {
    private static final Writer UNWRITABLE_WRITER = new Writer(){

        @Override
        public void write(char[] buffer, int offset, int counter) {
            throw new AssertionError();
        }

        @Override
        public void flush() throws IOException {
            throw new AssertionError();
        }

        @Override
        public void close() throws IOException {
            throw new AssertionError();
        }
    };
    private static final JsonPrimitive SENTINEL_CLOSED = new JsonPrimitive("closed");
    private final List<JsonElement> stack = new ArrayList<JsonElement>();
    private String pendingName;
    private JsonElement product = JsonNull.INSTANCE;

    public JsonTreeWriter() {
        super(UNWRITABLE_WRITER);
    }

    public JsonElement get() {
        if (this.stack.isEmpty()) return this.product;
        throw new IllegalStateException("Expected one JSON element but was " + this.stack);
    }

    private JsonElement peek() {
        return this.stack.get(this.stack.size() - 1);
    }

    private void put(JsonElement value) {
        if (this.pendingName != null) {
            if (!value.isJsonNull() || this.getSerializeNulls()) {
                JsonObject object = (JsonObject)this.peek();
                object.add(this.pendingName, value);
            }
            this.pendingName = null;
            return;
        }
        if (this.stack.isEmpty()) {
            this.product = value;
            return;
        }
        JsonElement element = this.peek();
        if (!(element instanceof JsonArray)) throw new IllegalStateException();
        ((JsonArray)element).add(value);
    }

    @Override
    public JsonWriter beginArray() throws IOException {
        JsonArray array = new JsonArray();
        this.put(array);
        this.stack.add(array);
        return this;
    }

    @Override
    public JsonWriter endArray() throws IOException {
        if (this.stack.isEmpty()) throw new IllegalStateException();
        if (this.pendingName != null) {
            throw new IllegalStateException();
        }
        JsonElement element = this.peek();
        if (!(element instanceof JsonArray)) throw new IllegalStateException();
        this.stack.remove(this.stack.size() - 1);
        return this;
    }

    @Override
    public JsonWriter beginObject() throws IOException {
        JsonObject object = new JsonObject();
        this.put(object);
        this.stack.add(object);
        return this;
    }

    @Override
    public JsonWriter endObject() throws IOException {
        if (this.stack.isEmpty()) throw new IllegalStateException();
        if (this.pendingName != null) {
            throw new IllegalStateException();
        }
        JsonElement element = this.peek();
        if (!(element instanceof JsonObject)) throw new IllegalStateException();
        this.stack.remove(this.stack.size() - 1);
        return this;
    }

    @Override
    public JsonWriter name(String name) throws IOException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (this.stack.isEmpty()) throw new IllegalStateException();
        if (this.pendingName != null) {
            throw new IllegalStateException();
        }
        JsonElement element = this.peek();
        if (!(element instanceof JsonObject)) throw new IllegalStateException();
        this.pendingName = name;
        return this;
    }

    @Override
    public JsonWriter value(String value) throws IOException {
        if (value == null) {
            return this.nullValue();
        }
        this.put(new JsonPrimitive(value));
        return this;
    }

    @Override
    public JsonWriter nullValue() throws IOException {
        this.put(JsonNull.INSTANCE);
        return this;
    }

    @Override
    public JsonWriter value(boolean value) throws IOException {
        this.put(new JsonPrimitive(value));
        return this;
    }

    @Override
    public JsonWriter value(Boolean value) throws IOException {
        if (value == null) {
            return this.nullValue();
        }
        this.put(new JsonPrimitive(value));
        return this;
    }

    @Override
    public JsonWriter value(double value) throws IOException {
        if (!this.isLenient()) {
            if (Double.isNaN(value)) throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
            if (Double.isInfinite(value)) {
                throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
            }
        }
        this.put(new JsonPrimitive(value));
        return this;
    }

    @Override
    public JsonWriter value(long value) throws IOException {
        this.put(new JsonPrimitive(value));
        return this;
    }

    @Override
    public JsonWriter value(Number value) throws IOException {
        if (value == null) {
            return this.nullValue();
        }
        if (!this.isLenient()) {
            double d = value.doubleValue();
            if (Double.isNaN(d)) throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
            if (Double.isInfinite(d)) {
                throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
            }
        }
        this.put(new JsonPrimitive(value));
        return this;
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        if (!this.stack.isEmpty()) {
            throw new IOException("Incomplete document");
        }
        this.stack.add(SENTINEL_CLOSED);
    }
}

