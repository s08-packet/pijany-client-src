/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CommentStore {
    private final Map<String, List<String>> headers = Maps.newConcurrentMap();
    private final char pathSeperator;
    private final int indents;
    private List<String> mainHeader = Lists.newArrayList();

    public CommentStore(char pathSeperator, int indents) {
        this.pathSeperator = pathSeperator;
        this.indents = indents;
    }

    public void mainHeader(String ... header) {
        this.mainHeader = Arrays.asList(header);
    }

    public List<String> mainHeader() {
        return this.mainHeader;
    }

    public void header(String key, String ... header) {
        this.headers.put(key, Arrays.asList(header));
    }

    public List<String> header(String key) {
        return this.headers.get(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void storeComments(InputStream inputStream) throws IOException {
        String contents;
        try (InputStreamReader reader = new InputStreamReader(inputStream);){
            contents = CharStreams.toString(reader);
        }
        StringBuilder memoryData = new StringBuilder();
        String pathSeparator = Character.toString(this.pathSeperator);
        int currentIndents = 0;
        String key = "";
        ArrayList<String> headers = Lists.newArrayList();
        String[] stringArray = contents.split("\n");
        int n = stringArray.length;
        int n2 = 0;
        while (n2 < n) {
            String line = stringArray[n2];
            if (!line.isEmpty()) {
                String subline;
                int indent = this.getSuccessiveCharCount(line, ' ');
                String string = subline = indent > 0 ? line.substring(indent) : line;
                if (subline.startsWith("#")) {
                    if (subline.startsWith("#>")) {
                        String txt = subline.startsWith("#> ") ? subline.substring(3) : subline.substring(2);
                        this.mainHeader.add(txt);
                    } else {
                        String txt = subline.startsWith("# ") ? subline.substring(2) : subline.substring(1);
                        headers.add(txt);
                    }
                } else {
                    int indents = indent / this.indents;
                    if (indents <= currentIndents) {
                        String[] array = key.split(Pattern.quote(pathSeparator));
                        int backspace = currentIndents - indents + 1;
                        key = this.join(array, this.pathSeperator, 0, array.length - backspace);
                    }
                    String separator = key.length() > 0 ? pathSeparator : "";
                    String lineKey = line.contains(":") ? line.split(Pattern.quote(":"))[0] : line;
                    key = key + separator + lineKey.substring(indent);
                    currentIndents = indents;
                    memoryData.append(line).append('\n');
                    if (!headers.isEmpty()) {
                        this.headers.put(key, headers);
                        headers = Lists.newArrayList();
                    }
                }
            }
            ++n2;
        }
    }

    public void writeComments(String yaml, File output) throws IOException {
        int indentLength = this.indents;
        String pathSeparator = Character.toString(this.pathSeperator);
        StringBuilder fileData = new StringBuilder();
        int currentIndents = 0;
        String key = "";
        for (String h : this.mainHeader) {
            fileData.append("#> ").append(h).append('\n');
        }
        String[] stringArray = yaml.split("\n");
        int n = stringArray.length;
        int n2 = 0;
        while (true) {
            if (n2 >= n) {
                Files.write(fileData.toString(), output, StandardCharsets.UTF_8);
                return;
            }
            String line = stringArray[n2];
            if (line.isEmpty() || line.trim().charAt(0) == '-') {
                fileData.append(line).append('\n');
            } else {
                String indentText;
                int indent = this.getSuccessiveCharCount(line, ' ');
                int indents = indent / indentLength;
                String string = indentText = indent > 0 ? line.substring(0, indent) : "";
                if (indents <= currentIndents) {
                    String[] array = key.split(Pattern.quote(pathSeparator));
                    int backspace = currentIndents - indents + 1;
                    key = this.join(array, this.pathSeperator, 0, array.length - backspace);
                }
                String separator = !key.isEmpty() ? pathSeparator : "";
                String lineKey = line.contains(":") ? line.split(Pattern.quote(":"))[0] : line;
                key = key + separator + lineKey.substring(indent);
                currentIndents = indents;
                List<String> header = this.headers.get(key);
                String headerText = header != null ? this.addHeaderTags(header, indentText) : "";
                fileData.append(headerText).append(line).append('\n');
            }
            ++n2;
        }
    }

    private String addHeaderTags(List<String> header, String indent) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = header.iterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            builder.append(indent).append("# ").append(line).append('\n');
        }
        return builder.toString();
    }

    private String join(String[] array, char joinChar, int start, int length) {
        Object[] copy = new String[length - start];
        System.arraycopy(array, start, copy, 0, length - start);
        return Joiner.on(joinChar).join(copy);
    }

    private int getSuccessiveCharCount(String text, char key) {
        int count = 0;
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) != key) return count;
            ++count;
            ++i;
        }
        return count;
    }
}

