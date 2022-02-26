/*
 * Decompiled with CFR 0.152.
 */
package com.viaversion.viaversion.velocity.util;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.slf4j.Logger;

public class LoggerWrapper
extends java.util.logging.Logger {
    private final Logger base;

    public LoggerWrapper(Logger logger) {
        super("logger", null);
        this.base = logger;
    }

    @Override
    public void log(LogRecord record) {
        this.log(record.getLevel(), record.getMessage());
    }

    @Override
    public void log(Level level, String msg) {
        if (level == Level.FINE) {
            this.base.debug(msg);
            return;
        }
        if (level == Level.WARNING) {
            this.base.warn(msg);
            return;
        }
        if (level == Level.SEVERE) {
            this.base.error(msg);
            return;
        }
        if (level == Level.INFO) {
            this.base.info(msg);
            return;
        }
        this.base.trace(msg);
    }

    @Override
    public void log(Level level, String msg, Object param1) {
        if (level == Level.FINE) {
            this.base.debug(msg, param1);
            return;
        }
        if (level == Level.WARNING) {
            this.base.warn(msg, param1);
            return;
        }
        if (level == Level.SEVERE) {
            this.base.error(msg, param1);
            return;
        }
        if (level == Level.INFO) {
            this.base.info(msg, param1);
            return;
        }
        this.base.trace(msg, param1);
    }

    @Override
    public void log(Level level, String msg, Object[] params) {
        this.log(level, MessageFormat.format(msg, params));
    }

    @Override
    public void log(Level level, String msg, Throwable params) {
        if (level == Level.FINE) {
            this.base.debug(msg, params);
            return;
        }
        if (level == Level.WARNING) {
            this.base.warn(msg, params);
            return;
        }
        if (level == Level.SEVERE) {
            this.base.error(msg, params);
            return;
        }
        if (level == Level.INFO) {
            this.base.info(msg, params);
            return;
        }
        this.base.trace(msg, params);
    }
}

