/*
 * Decompiled with CFR 0.152.
 */
package org.yaml.snakeyaml.events;

import org.yaml.snakeyaml.error.Mark;

public abstract class Event {
    private final Mark startMark;
    private final Mark endMark;

    public Event(Mark startMark, Mark endMark) {
        this.startMark = startMark;
        this.endMark = endMark;
    }

    public String toString() {
        return "<" + this.getClass().getName() + "(" + this.getArguments() + ")>";
    }

    public Mark getStartMark() {
        return this.startMark;
    }

    public Mark getEndMark() {
        return this.endMark;
    }

    protected String getArguments() {
        return "";
    }

    public boolean is(ID id) {
        if (this.getEventId() != id) return false;
        return true;
    }

    public abstract ID getEventId();

    public boolean equals(Object obj) {
        if (!(obj instanceof Event)) return false;
        return this.toString().equals(obj.toString());
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public static enum ID {
        Alias,
        DocumentEnd,
        DocumentStart,
        MappingEnd,
        MappingStart,
        Scalar,
        SequenceEnd,
        SequenceStart,
        StreamEnd,
        StreamStart;

    }
}

