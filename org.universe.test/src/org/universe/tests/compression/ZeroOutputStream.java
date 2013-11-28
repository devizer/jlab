package org.universe.tests.compression;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 11/15/13
 * Time: 8:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class ZeroOutputStream extends OutputStream {

    private long length = 0;

    @Override
    public void write(byte[] b) throws IOException {
        length += b.length;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        length += len;
    }

    @Override
    public void write(int b) throws IOException {
        length++;
    }

    public long getLength() {
        return length;
    }
}
