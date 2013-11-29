package org.universe.tests.security;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.universe.ConsoleTable;
import org.universe.System6;
import org.universe.jcl.Stress;
import org.universe.security.ParseReport;
import org.universe.security.SimpleIntegrity;
import org.universe.security.StoreReport;
import org.universe.tests.TestEnv;

import javax.swing.text.NumberFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Test_SimpleIntegrity {

    @Test
    public void Is_IntegrityCorrect()
    {
        byte[] src = new byte[] {  };
        SimpleIntegrity.Alg[] algs = SimpleIntegrity.Alg.values();
        for(SimpleIntegrity.Alg a : algs)
        {
            StoreReport hashed = SimpleIntegrity.Store(src, a);
            // System.out.println("Source: " + asString(src));
            // System.out.println("Hashed: " + asString(hashed.getData()));
            ParseReport orig = SimpleIntegrity.Parse(hashed.getData());
            Assert.assertArrayEquals(src, orig.getData());
        }
    }

    @Test
    public void test1() throws NoSuchAlgorithmException, InterruptedException {
        List<String> algs = Arrays.asList("MD2", "MD5", "SHA", "SHA-1", "SHA-256", "SHA-384", "SHA-512");
        List<Integer> length = Arrays.asList(2, 100000);

        for(final String alg : algs)
            for(final Integer len: length)
        {

            final byte[] data = new byte[len];
            {
                Stress.LoopBody loopBody = new Stress.LoopBody() {
                    @Override
                    public void run() throws Exception {
                        MessageDigest m = MessageDigest.getInstance(alg);
                        m.update(data);
                        byte[] digest = m.digest();
                    }
                };

                measure(loopBody, alg + " digest(" + len + " byte)", 100);
            }

            {
                final MessageDigest m = MessageDigest.getInstance(alg);
                Stress.LoopBody loopBody = new Stress.LoopBody() {
                    @Override
                    public void run() throws Exception {
                        MessageDigest m2 = (MessageDigest) m.clone();
                        m2.update(data);
                        byte[] digest = m2.digest();
                    }
                };

                measure(loopBody, alg + " CLONED digest(" + len + " bytes)", 100);
            }
        }
    }

    private void measure(Stress.LoopBody run, String caption, int duration) throws InterruptedException {
        System.out.print(caption + ": ");
        Stress.Heat(run);
        List<Integer> threads = Arrays.asList(1, 2, 6);
        ConsoleTable tbl = Stress.CreateReport();
        for(int t : threads)
        {
            System.out.print(t + " ");
            Stress res = Stress.Measure(TestEnv.scaleDuration(duration), 1, t, run);
            res.WriteToReport(tbl);
        }

        System.out.println(System6.lineSeparator() + System6.lineSeparator() + caption + System6.lineSeparator() + tbl);
    }

    String asString(byte[] src)
    {
        StringBuilder ret = new StringBuilder();
        for(byte b : src)
            ret.append(ret.length() == 0 ? "" : " ").append(String.valueOf(b));

        return ret.toString();
    }


}
