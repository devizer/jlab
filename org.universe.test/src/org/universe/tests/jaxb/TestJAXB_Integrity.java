package org.universe.tests.jaxb;

import org.junit.*;
import org.universe.System6;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 11/3/13
 * Time: 12:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestJAXB_Integrity {


    @Test
    public void OverView() throws Exception {
        MyVo1 ver1 = VoEnv.createVO1();

        byte[] asData = VoEnv.Ver1Converter.ToData(ver1);
        String asString = VoEnv.Ver1Converter.ToXml(ver1);
        String asFormattedString = VoEnv.Ver1Converter.ToFormattedXml(ver1);
        System.out.println(String.format(
                "AsData: %d bytes, AsString: %d bytes, AsFormattedString: %d bytes",
                asData.length,
                asString.getBytes().length,
                asFormattedString.getBytes().length
        ));

        MyVo2 ver2 = VoEnv.Ver2Converter.Parse(VoEnv.Ver1Converter.ToXml(ver1));
        String ls = System6.lineSeparator();
        System.out.println("MyVo1" + ls + VoEnv.Ver1Converter.ToFormattedXml(ver1)
                + ls + "MyVol2" + ls + VoEnv.Ver2Converter.ToFormattedXml(ver2));
    }

    @Test
    public void Can_Deserialize_Ver2_Instance_From_Ver1_Xml() throws Exception {
        MyVo1 ver1 = VoEnv.createVO1();
        MyVo2 ver2 = VoEnv.Ver2Converter.Parse(VoEnv.Ver1Converter.ToXml(ver1));
        Assert.assertEquals(ver1.MyInt, ver2.MyInt);
    }

    @Test
    public void Serialize_And_Parse_Is_Repeatable_Via_Xml() throws Exception {
        MyVo1 ver1 = VoEnv.createVO1();
        MyVo1 copy1 = VoEnv.Ver1Converter.Parse(VoEnv.Ver1Converter.ToXml(ver1));
        MyVo1 copy2 = VoEnv.Ver1Converter.Parse(VoEnv.Ver1Converter.ToXml(copy1));

        Assert.assertEquals(VoEnv.Ver1Converter.ToXml(ver1), VoEnv.Ver1Converter.ToXml(copy1));
        Assert.assertEquals(VoEnv.Ver1Converter.ToXml(copy1), VoEnv.Ver1Converter.ToXml(copy2));

        VoEnv.assertEquals(ver1, copy2);
    }

    @Test
    public void Serialize_And_Parse_Is_Repeatable_Via_Data() throws Exception {
        MyVo1 ver1 = VoEnv.createVO1();
        MyVo1 copy1 = VoEnv.Ver1Converter.Parse(VoEnv.Ver1Converter.ToData(ver1));
        MyVo1 copy2 = VoEnv.Ver1Converter.Parse(VoEnv.Ver1Converter.ToData(copy1));

        Assert.assertArrayEquals(VoEnv.Ver1Converter.ToData(ver1), VoEnv.Ver1Converter.ToData(copy1));
        Assert.assertArrayEquals(VoEnv.Ver1Converter.ToData(copy1), VoEnv.Ver1Converter.ToData(copy2));

        VoEnv.assertEquals(ver1, copy2);
    }

    @Test
    public void Can_Deserialize_Ver2_Instance_From_Ver1_FormattedXml() throws Exception {
        MyVo1 ver1 = VoEnv.createVO1();
        MyVo2 ver2 = VoEnv.Ver2Converter.Parse(VoEnv.Ver1Converter.ToFormattedXml(ver1));
        Assert.assertEquals(ver1.MyInt, ver2.MyInt);
    }

    @Test
    public void Can_Deserialize_Ver2_Instance_From_Ver1_Data() throws Exception {
        MyVo1 ver1 = VoEnv.createVO1();
        MyVo2 ver2 = VoEnv.Ver2Converter.Parse(VoEnv.Ver1Converter.ToData(ver1));
        Assert.assertEquals(ver1.MyInt, ver2.MyInt);
    }


    static AtomicInteger instanceCounter = new AtomicInteger(0);

    static AtomicInteger globalThreadCounter = new AtomicInteger(0);
    static ThreadLocal<Integer> threadCounter = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return globalThreadCounter.incrementAndGet();
        }
;    };

    public TestJAXB_Integrity() {
        instanceCounter.incrementAndGet();
    }

    @Before
    public void Before()
    {
        // System.out.println("@Before" + info());
    }

    @BeforeClass
    public static void BeforeClass()
    {
        // System.out.println("@BeforeClass");
    }

    @After
    public void After()
    {
        // System.out.println("Bye! " + info());
    }

    @AfterClass
    public static void AfterClass()
    {
        Properties p = System.getProperties();
        System.out.println("Bye! Java ver is " + System.getProperty("java.version", "N/A")
                + " by " + System.getProperty("java.vendor", "Unknown vendor")
                + ", OS: " + System.getProperty("os.name", "N/A")
                + " ver " + System.getProperty("os.version", "N/A"));
    }

    String info()
    {
        return ": instance " + instanceCounter.get() + ", thread " + threadCounter.get();
    }

    Object[] ToArrayOfObjects(List list)
    {
        Object[] ret = new Object[list.size()];
        int i = 0;
        for(Object item : list)
            ret[i++] = item;

        return ret;
    }
}

