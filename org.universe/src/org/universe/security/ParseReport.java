package org.universe.security;

/**
* Created with IntelliJ IDEA.
* User: lulu
* Date: 11/14/13
* Time: 2:55 AM
* To change this template use File | Settings | File Templates.
*/
public class ParseReport
{
    byte[] data;
    boolean isCorrect;
    SimpleIntegrity.Alg alg;

    public ParseReport() {
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public SimpleIntegrity.Alg getAlg() {
        return alg;
    }

    public void setAlg(SimpleIntegrity.Alg alg) {
        this.alg = alg;
    }
}
