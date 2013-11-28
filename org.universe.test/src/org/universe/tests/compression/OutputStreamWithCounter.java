package org.universe.tests.compression;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamWithCounter extends OutputStream {

    OutputStream stream;
    long length = 0;

    public OutputStreamWithCounter(OutputStream stream) {
        this.stream = stream;
    }

    @Override
    public void write(int b) throws IOException {
        stream.write(b);
        length += b;
    }

    @Override
    public void write(byte[] b) throws IOException {
        stream.write(b);
        length += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
        length += len;
    }

    public long getLength() {
        return length;
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
