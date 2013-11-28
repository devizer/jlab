package org.universe.tests.jaxb;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.universe.ConsoleTable;
import org.universe.System6;
import org.universe.jaxb.BinarySerializer;
import org.universe.jcl.Stress;
import org.universe.tests.TestEnv;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

public class ObjectIO_Perfomance {

    @Test
    public void Is_Integrity_Correct() throws IOException, ClassNotFoundException {
        MyVo1 vo = VoEnv.createVO1();
        byte[] data1 = BinarySerializer.Serialize(vo);
        System.out.println("MyVo1 as Written Object: " + data1.length + " bytes");

        MyVo1 copy2 = BinarySerializer.Parse(data1);
        VoEnv.assertEquals(vo, copy2);

        byte[] data3 = BinarySerializer.Serialize(vo);
        Assert.assertArrayEquals(data1, data3);

        MyVo1 copy4 = BinarySerializer.Parse(data3);
        VoEnv.assertEquals(vo, copy4);
    }

    @Test
    public void _1_Read_MyVo() throws Exception {
        final MyVo1 vo = VoEnv.createVO1();
        final byte[] data = BinarySerializer.Serialize(vo);
        System.out.println("MyVo1 as Written Object: " + data.length + " bytes");
        VoEnv.assertEquals(vo, (MyVo1) BinarySerializer.Parse(data));

        String fileName = new File(System.getProperty("java.io.tmpdir"), "MyVo1").getAbsolutePath();
        IOUtils.write(data, new FileOutputStream(fileName));

        Stress.LoopBody iteration = new Stress.LoopBody() {
            @Override
            public void run() throws IOException, ClassNotFoundException {
                MyVo1 copy = BinarySerializer.Parse(data);
                VoEnv.assertEquals(vo, copy);
            }
        };

        measure(iteration, "READ MyVo1 from byte[] using ObjectInputStream", 2000);
    }

    @Test
    public void _2_Write_MyVo() throws InterruptedException {
        final MyVo1 vo = VoEnv.createVO1();

        Stress.LoopBody iteration =  new Stress.LoopBody() {
            @Override
            public void run() throws IOException {
                byte[] data = BinarySerializer.Serialize(vo);
                Assert.assertTrue("data is non empty", data != null && data.length > 0);
            }
        };

        measure(iteration, "WRITE MyVo1 to byte[] using ObjectOutputStream", 2000);
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



}
