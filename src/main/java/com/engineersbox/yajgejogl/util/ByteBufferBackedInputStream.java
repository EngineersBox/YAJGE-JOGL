package com.engineersbox.yajgejogl.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

// Source: https://stackoverflow.com/questions/4332264/wrapping-a-bytebuffer-with-an-inputstream/6603018#6603018
public class ByteBufferBackedInputStream extends InputStream {

    private final ByteBuffer buf;

    public ByteBufferBackedInputStream(final ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int read() throws IOException {
        if (!this.buf.hasRemaining()) {
            return -1;
        }
        return this.buf.get() & 0xFF;
    }

    @Override
    public int read(final byte[] bytes,
                    final int off,
                    int len) throws IOException {
        if (!this.buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, this.buf.remaining());
        this.buf.get(bytes, off, len);
        return len;
    }
}