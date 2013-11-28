package org.universe.security;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class SimpleIntegrity {

    final private static List<AlgInfo> Algs = Arrays.asList(
            new AlgInfo(Alg.md2,    33, "MD2",     16),
            new AlgInfo(Alg.md5,    34, "MD5",     16),
            new AlgInfo(Alg.sha,    35, "SHA-1",   20),
            new AlgInfo(Alg.sha256, 36, "SHA-256", 32),
            new AlgInfo(Alg.sha384, 37, "SHA-384", 48),
            new AlgInfo(Alg.sha512, 38, "SHA-512", 64)
    );

    static String magic = "SI";

    public enum Alg
    {
        md2,
        md5,
        sha,
        sha256,
        sha384,
        sha512
    }

    static class AlgInfo
    {
        Alg alg;
        String key;
        int length;
        byte id;

        AlgInfo(Alg alg, int id, String key, int length) {
            this.alg = alg;
            this.id = (byte) id;
            this.key = key;
            this.length = length;
        }

        MessageDigest get()  {
            try {
                return MessageDigest.getInstance(key);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Algorithm '" + key + "' is not supported", e);
            }
        }
    }


    public static ParseReport Parse(byte[] source)
    {
        int minLen = 16 + 4 + 1 + 2;
        if (source.length < minLen)
            throw new IllegalArgumentException("Corrupted source");

        byte[] footer = Arrays.copyOfRange(source, source.length - 7, source.length);
        ByteArrayInputStream memf = new ByteArrayInputStream(footer);
        DataInputStream rdrFooter = new DataInputStream(memf);
        int size;
        byte id;
        try
        {
            size = rdrFooter.readInt();
            char ch1 = (char)(int)rdrFooter.readByte();
            char ch2 = (char)(int)rdrFooter.readByte();
            id = rdrFooter.readByte();
            if (ch1 != magic.charAt(0) || ch2 != magic.charAt(1))
                throw new IllegalArgumentException("Corrupted source");
        }
        catch(IOException ex)
        {
            throw new IllegalStateException("Cant read hashed footer. Check free memory", ex);
        }

        AlgInfo info = getAlg(id);
        int expectedLength = size + info.length + 4 + 1 + 2;
        if (source.length != expectedLength)
            throw new IllegalArgumentException("Invalid source");

        ByteArrayInputStream mem = new ByteArrayInputStream(source);
        DataInputStream rdr = new DataInputStream(mem);

        byte[] origin;
        byte[] expectedDigest;
        try
        {
            origin = new byte[size];
            if (mem.read(origin) != size)
                throw new IllegalArgumentException("Corrupted source");

            expectedDigest = new byte[info.length];
            rdr.read(expectedDigest);
        }
        catch(IOException ex)
        {
            throw new IllegalStateException("Cant read hashed footer. Check free memory", ex);
        }

        MessageDigest md = info.get();
        md.update(origin, 0, size);
        byte[] digest = md.digest();
        for(int i=0; i<expectedDigest.length; i++)
            if (expectedDigest[i] != digest[i])
                throw new IllegalArgumentException("Corrupted source");


        ParseReport ret = new ParseReport();
        ret.setData(origin);
        ret.setCorrect(true);
        ret.setAlg(info.alg);
        return ret;
    }

    public static StoreReport Store(byte[] source, Alg alg) {

        AlgInfo info = getAlg(alg);
        MessageDigest a = info.get();
        a.update(source);
        byte[] digest = a.digest();

        int totalLen = source.length + info.length + 4 + 1 + 2;
        ByteArrayOutputStream mem = new ByteArrayOutputStream(totalLen);
        DataOutputStream wr = new DataOutputStream(mem);
        try
        {
            wr.write(source);
            wr.write(digest);
            wr.writeInt(source.length);
            wr.write(magic.charAt(0));
            wr.write(magic.charAt(1));
            wr.write(info.id);
        }
        catch(IOException ex)
        {
            throw new IllegalStateException("Cant create hashed memory with size " + totalLen + ". Check free memory", ex);
        }

        StoreReport ret = new StoreReport();
        ret.setAlg(alg);
        ret.setData(mem.toByteArray());
        ret.setHash(digest);
        return ret;
    }

    public static StoreReport Store(byte[] source)
    {
        return Store(source, Alg.md5);
    }

    static AlgInfo getAlg(Alg alg) {
        for(AlgInfo a: Algs)
            if (a.alg == alg)
                return a;

        throw new IllegalArgumentException("Algorithm " + alg + " is not supported");
    }

    static AlgInfo getAlg(byte id) {
        for(AlgInfo a: Algs)
            if (a.id == id)
                return a;

        throw new IllegalArgumentException("Algorithm " + id + " is not supported");
    }

}
