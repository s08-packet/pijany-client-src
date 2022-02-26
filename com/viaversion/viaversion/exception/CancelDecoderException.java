/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.handler.codec.DecoderException
 */
package com.viaversion.viaversion.exception;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.exception.CancelCodecException;
import io.netty.handler.codec.DecoderException;

public class CancelDecoderException
extends DecoderException
implements CancelCodecException {
    public static final CancelDecoderException CACHED = new CancelDecoderException("This packet is supposed to be cancelled; If you have debug enabled, you can ignore these"){

        public Throwable fillInStackTrace() {
            return this;
        }
    };

    public CancelDecoderException() {
    }

    public CancelDecoderException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelDecoderException(String message) {
        super(message);
    }

    public CancelDecoderException(Throwable cause) {
        super(cause);
    }

    public static CancelDecoderException generate(Throwable cause) {
        CancelDecoderException cancelDecoderException;
        if (Via.getManager().isDebug()) {
            cancelDecoderException = new CancelDecoderException(cause);
            return cancelDecoderException;
        }
        cancelDecoderException = CACHED;
        return cancelDecoderException;
    }
}

