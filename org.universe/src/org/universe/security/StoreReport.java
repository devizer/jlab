package org.universe.security;

/**
* Created with IntelliJ IDEA.
* User: lulu
* Date: 11/14/13
* Time: 2:55 AM
* To change this template use File | Settings | File Templates.
*/
public class StoreReport
{
    SimpleIntegrity.Alg alg;
    byte[] hash;
    byte[] data;

    public StoreReport() {
    }

    public SimpleIntegrity.Alg getAlg() {
        return alg;
    }

    public void setAlg(SimpleIntegrity.Alg alg) {
        this.alg = alg;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }
}
