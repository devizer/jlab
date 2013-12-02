package org.universe.jaxb;

import org.universe.jcl.apparency.ThreadSafe;

import java.io.*;


@ThreadSafe
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
