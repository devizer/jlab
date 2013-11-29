package org.universe.test.compression;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.universe.ConsoleTable;
import org.universe.DateCalc;
import org.universe.System6;
import org.universe.test.TestEnv;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GZip_Perfomance {

    static interface MyWriter
    {
        void write(OutputStream stream) throws Exception;
    }

    @Test
    public void _1_MeashureGzipText() throws Throwable {
        String caption = "Measure GZIP TEXT (builtin) perfomance: ";

        final byte[] source = LoremIpsum.getBytes();
        MyWriter loopBody = new MyWriter() {
            @Override
            public void write(OutputStream stream) throws Exception {
                stream.write(source);
            }
        };

        measure(caption, loopBody);
    }

    @Test
    public void _2_MeashureGzipRandom() throws Exception {
        String caption = "Measure GZIP RANDOM (builtin) perfomance: ";
        final int kbs = 10*1024;
        final byte[] random = new byte[kbs*1024];
        new Random(1).nextBytes(random);

        final AtomicInteger pos = new AtomicInteger();
        MyWriter loopBody = new MyWriter() {
            @Override
            public void write(OutputStream stream) throws Exception {
                int p = pos.get();
                stream.write(random, p*1024, 1024);
                pos.set((p + 1) % kbs);
            }
        };

        measure(caption, loopBody);
    }

    private void measure(String caption, MyWriter loopBody) throws Exception {
        measure(null, loopBody, 1, Arrays.asList(256));
        measure(caption, loopBody, TestEnv.scaleDuration(4000), Arrays.asList(1, 16, 64, 1024, 8192, 128 * 1024));
    }

    private void measure(String caption, MyWriter loopBody, int duration, List<Integer> bufferSizes) throws Exception {
        ConsoleTable tbl = new ConsoleTable("Buffer", "Duration", "Input, KB", "GZipped, Kb", "Ratio", "KB/s");
        if (caption != null) System.out.print(caption);
        DecimalFormat pcFormat = new DecimalFormat("##0.00'%'");
        DecimalFormat kbsecFormat = new DecimalFormat("##,###,##0.0 KB/s");
        DecimalFormat kbFormat = new DecimalFormat("##,###,##0");
        DecimalFormat kbsmallFormat = new DecimalFormat("##,###,##0.000");
        for(int bSize : bufferSizes)
        {
            if (caption != null) System.out.print(bSize + " ");
            ZeroOutputStream zero = new ZeroOutputStream();
            BufferedOutputStream b1 = new BufferedOutputStream(zero, bSize);
            GZIPOutputStream gzip = new GZIPOutputStream(b1, bSize);
            BufferedOutputStream b2 = new BufferedOutputStream(gzip, bSize);
            OutputStreamWithCounter out = new OutputStreamWithCounter(b2);

            long startAt = System.nanoTime();
            long until = startAt + duration * 1000000L;
            do {
                loopBody.write(out);
            }
            while(System.nanoTime() < until);
            out.flush();
            long nanoSec = System.nanoTime() - startAt;
            long gzipSize = zero.getLength();
            long sourceSize = out.getLength();

            DecimalFormat f1 = gzipSize < 9999 ? kbsmallFormat : kbFormat;
            tbl     .put(bSize)
                    .put(DateCalc.NanosecToString(nanoSec))
                    .put(f1.format(sourceSize / 1024d))
                    .put(f1.format(gzipSize / 1024d))
                    .put(pcFormat.format(100d * gzipSize / sourceSize))
                    .put(kbsecFormat.format(sourceSize / 1024d * 1000000000d / nanoSec))
                    .newRow();
        }

        String ls = System6.lineSeparator();
        if (caption != null)
            System.out.println(String.format("%s%s%s",
                ls, tbl.toString(), ls));
    }

    @After public void after()
    {
        System.gc();
    }



    static final String LoremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas in porttitor diam, sed aliquam tellus. Pellentesque facilisis odio eu purus convallis aliquet. Maecenas ut tincidunt tellus. Fusce varius vitae tellus id lobortis. Maecenas sem libero, consequat rutrum consectetur at, molestie bibendum justo. Phasellus vestibulum augue at purus interdum auctor. In augue dolor, interdum ac risus a, pharetra condimentum nisl. Cras adipiscing accumsan tortor vel auctor. Donec eleifend varius commodo. Fusce quis fermentum ante. Nunc eget eleifend nisi, vel dignissim eros.\n" +
            "\n" +
            "Ut adipiscing mi eget nunc dictum, in viverra augue laoreet. Aenean venenatis in nunc eu placerat. Duis ullamcorper vitae massa ut gravida. Praesent elementum, ipsum a ornare convallis, diam ipsum dignissim felis, eu venenatis mauris massa at ante. Morbi commodo quam consequat orci tempor, sed bibendum odio volutpat. Nunc sodales massa erat, eget aliquet augue porta nec. Sed fermentum tortor at nulla mollis venenatis.\n" +
            "\n" +
            "Cras a imperdiet est. Nam ac arcu venenatis, viverra diam et, vestibulum metus. Maecenas malesuada odio eget varius rutrum. Proin cursus ac orci ut accumsan. Mauris vestibulum erat at mi congue, tristique elementum lectus pharetra. Sed rutrum quis massa vel tristique. Fusce molestie purus sed magna ornare commodo. Aenean lacinia lectus vel odio placerat pharetra. Pellentesque eget justo ut nisi faucibus mattis a quis nulla. Etiam pretium nisl non tellus ornare, tincidunt vulputate sem pellentesque.\n" +
            "\n" +
            "Fusce a eros elit. Integer congue ac lacus id consequat. Donec pretium, sem vel bibendum lobortis, mi ante dignissim dolor, vel egestas tortor risus eget velit. Donec suscipit quam vel nisl fermentum, vitae feugiat arcu pharetra. Aliquam ornare enim et tellus fringilla, quis sagittis quam malesuada. Pellentesque accumsan cursus augue in consectetur. Donec non interdum eros. Sed consequat rhoncus pretium. Fusce nec tortor ante. Integer sagittis euismod mauris et sagittis. Sed vitae tristique odio. Interdum et malesuada fames ac ante ipsum primis in faucibus.\n" +
            "\n" +
            "Proin ipsum neque, blandit nec tincidunt a, lacinia ut mauris. Donec pellentesque, justo nec lacinia venenatis, lorem diam volutpat massa, at mollis urna massa quis ante. Phasellus sagittis, ante eu hendrerit facilisis, velit nibh venenatis lacus, ac sodales est orci id urna. Praesent non sem quis ante venenatis venenatis eu quis felis. Etiam dolor dui, aliquet vel lorem rhoncus, congue consectetur neque. Quisque iaculis urna fringilla pharetra lobortis. Cras semper lobortis magna in tincidunt. Sed faucibus, odio et semper volutpat, odio odio ultricies nisi, sit amet pulvinar urna metus non ante. Nullam metus nibh, volutpat interdum felis ut, egestas malesuada nisl. Suspendisse sed lectus mattis, dignissim nibh at, cursus tortor.\n" +
            "\n" +
            "Nulla auctor quam nec turpis scelerisque, a placerat magna pellentesque. Mauris in metus sapien. Cras orci risus, placerat eget ultrices non, commodo a sem. Nunc tristique est nec nunc elementum, id mollis arcu rutrum. Nulla luctus in massa vitae semper. Duis convallis tellus a pretium adipiscing. In at erat sit amet lacus pretium ullamcorper quis aliquam est. Curabitur mollis dui et nisi varius lobortis. Phasellus hendrerit vitae libero sit amet tincidunt. Etiam elementum ultrices odio, in elementum nisi pellentesque a. Suspendisse id porta mauris. Quisque fringilla vitae tellus sed tristique. Phasellus blandit elit leo, id commodo purus eleifend ac. Quisque quis libero magna. Phasellus facilisis rutrum erat, a dignissim nulla elementum ac. Nunc aliquam, diam at bibendum sodales, nulla urna sagittis orci, in sagittis diam arcu faucibus purus.\n" +
            "\n" +
            "Donec ligula nulla, blandit ac odio ac, condimentum dignissim dolor. Donec elementum turpis posuere ipsum malesuada, id sollicitudin dui pharetra. In interdum, enim sed consequat tristique, mi diam adipiscing eros, quis venenatis odio diam non ipsum. Vestibulum condimentum adipiscing odio, in venenatis leo volutpat eget. Mauris id porta leo, sed gravida elit. Maecenas et mauris laoreet, fermentum odio et, faucibus lorem. Integer blandit felis mi, quis sodales sem cursus quis. Vestibulum fringilla, nibh sed congue cursus, nisi turpis convallis augue, at accumsan lectus odio non lacus. Maecenas aliquet fermentum condimentum. Integer condimentum in dolor sed scelerisque. Quisque posuere, nisl id elementum faucibus, ante eros imperdiet tellus, nec suscipit massa tellus vel erat. Morbi aliquam dictum pulvinar. In nisl lectus, fringilla non sapien sed, blandit mollis est.\n" +
            "\n" +
            "Duis hendrerit pretium purus vel mattis. Suspendisse in vulputate ante. Vestibulum a eleifend lacus. Suspendisse iaculis volutpat eros non elementum. Nam facilisis dolor eu arcu fermentum malesuada. Vivamus ornare sed libero vel iaculis. Donec suscipit magna eu libero fringilla sagittis.\n" +
            "\n" +
            "Maecenas eu ipsum risus. Cras tincidunt dapibus neque, ac malesuada est rhoncus non. Vestibulum eu pulvinar massa. Aliquam erat volutpat. Sed et odio eget mi sollicitudin sollicitudin. Suspendisse elementum risus nunc, vel vulputate dui placerat at. Nulla turpis mi, ullamcorper sed ligula a, varius dignissim arcu. Morbi tristique purus at mollis aliquam. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Quisque ultricies volutpat arcu sit amet dictum. Etiam cursus scelerisque lacinia. Curabitur a velit neque.\n" +
            "\n" +
            "Morbi placerat justo vel nunc pretium sagittis. Pellentesque diam magna, malesuada non sem sed, laoreet facilisis ipsum. Aliquam imperdiet, quam quis viverra condimentum, nisi leo tempus risus, ut laoreet nulla leo sed lectus. Quisque tincidunt ipsum pretium purus luctus hendrerit a sed dui. Aliquam in mauris nulla. Sed porttitor neque ante, ac vulputate massa bibendum sit amet. Aliquam felis risus, congue id nibh aliquet, venenatis porttitor sem. Duis consequat, risus in pulvinar ornare, arcu leo consequat elit, at cursus nibh augue sit amet augue. Nullam purus erat, sodales at dignissim non, malesuada vel ante. Cras at leo id erat facilisis fermentum at eu tellus. Fusce nec nisl nec mauris lobortis fringilla eu imperdiet ante. Phasellus purus ligula, pellentesque vitae adipiscing sit amet, varius euismod mi. Suspendisse potenti. Proin posuere vel turpis id consequat. Maecenas non nunc et neque placerat adipiscing auctor non ligula.\n" +
            "\n" +
            "Donec tincidunt ante ac elit sollicitudin, eget vulputate justo pulvinar. Morbi sed iaculis dui. Cras volutpat velit volutpat, varius odio a, mollis purus. Vivamus adipiscing nisl sed arcu pharetra, sit amet aliquam tellus tincidunt. Ut vitae aliquet augue. Integer placerat pharetra nunc id bibendum. Nullam sed tellus urna.\n" +
            "\n" +
            "Sed non leo quis ipsum imperdiet rutrum. Donec vulputate odio metus, nec facilisis nibh porttitor a. Proin metus libero, sollicitudin non laoreet.";
}
