package org.universe.jaxb;

import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 11/14/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class BinarySerializer {

    public static byte[] Serialize(Object graph) throws IOException {
        ByteArrayOutputStream mem = new ByteArrayOutputStream();
        ObjectOutputStream wr = new ObjectOutputStream(mem);
        wr.writeObject(graph);
        wr.close();
        return mem.toByteArray();
    }

    public static <T> T Parse(byte[] data) throws IOException, ClassNotFoundException {
        if (data == null)
            throw new IllegalArgumentException("data is null");

        ByteArrayInputStream mem = new ByteArrayInputStream(data);
        ObjectInputStream rdr = new ObjectInputStream(mem);
        return (T) rdr.readObject();
    }
}
