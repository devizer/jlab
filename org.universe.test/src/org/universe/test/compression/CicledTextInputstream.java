package org.universe.test.compression;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 11/15/13
 * Time: 9:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class CicledTextInputstream extends InputStream {

    byte[] source;
    int position = 0;
    int len;

    public CicledTextInputstream(String arg) {
        source = arg.getBytes();
        len = source.length;
    }

    @Override
    public int read() throws IOException {
        byte ret =  source[position];
        position = (++position) % len;
        return ret;
    }
}
