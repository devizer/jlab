package org.universe.testcfx;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.log4j.PropertyConfigurator;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.universe.ConsoleTable;
import org.universe.DateCalc;
import org.universe.System6;
import org.universe.cxf.MessageInfo;
import org.universe.cxf.TransferInterceptor;
import org.universe.jcl.Stress;
import org.universe.tests.TestEnv;
import org.universe.tests.jaxb.MyVo1;
import org.universe.tests.jaxb.VoEnv;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCxf {

    Logger log = org.slf4j.LoggerFactory.getLogger(TestCxf.class);

    static {
        InputStream xx = TestCxf.class.getResourceAsStream("/META-INF/log4j.properties");
        if (xx != null)
            PropertyConfigurator.configure(xx);
    }


    int Port = 9000;
    String Address = "http://localhost:9000/helloWorld";
    private JaxWsServerFactoryBean svrFactory;
    static int HeaderSize = 100;

    static class MyServerHook extends TransferInterceptor.MessageHook
    {
        @Override
        public void hook(MessageInfo message) {
            MyContextOnServer.setLang(message.getStringHeader("X-MySecret"));
        }
    }

    static class MyClientHook extends TransferInterceptor.MessageHook
    {
        @Override
        public void hook(MessageInfo message) {
            message.putStringHeader("X-MySecret", newString(HeaderSize));
        }
    }

    @BeforeClass
    public static void beforeClass() throws JAXBException {
        // System.out.println(VoEnv.Ver1Converter.ToFormattedXml(VoEnv.createVO1()));
    }

    @Before
    public void before() throws IOException {

        Port = FindAvailablePort();
        Address = String.format("http://localhost:%d/helloWorld", Port);
        System.out.println("New Test's Address: " + Address);

        // Create Server 'host'
        HelloWorldImpl implementor = new HelloWorldImpl();
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        System.setProperties(ServerSide.applyNoLimits(System.getProperties()));
        factory.setServiceClass(HelloWorld.class);
        factory.setAddress(Address);
        factory.setServiceBean(implementor);
        TransferInterceptor.addServerHook(factory, new MyServerHook());
        // factory.getInInterceptors().add(new LoggingInInterceptor());
        // factory.getOutInterceptors().add(new LoggingOutInterceptor());
        factory.create();
        svrFactory = factory;
    }

    final static int FindAvailablePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    @After public void after()
    {
        svrFactory.destroy();
    }

    JaxWsProxyFactoryBean createClientFactory()
    {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        TransferInterceptor.addClientHook(factory, new MyClientHook());
        factory.setServiceClass(HelloWorld.class);
        factory.setAddress(Address);
        // factory.getInInterceptors().add(new LoggingInInterceptor());
        // factory.getOutInterceptors().add(new LoggingOutInterceptor());
        return factory;
    }

    @Test
    public void Is_Header_Recieved()
    {
        JaxWsProxyFactoryBean factory = createClientFactory();
        HelloWorld client = (HelloWorld) factory.create();
        String res = client.returnCopyOfSecretHeader();
        Assert.assertEquals(newString(HeaderSize), res);
        System.out.println("Server returned " + res);
    }

    @Test
    public void What_Sizes_Are_Supported()
    {
        System.out.println("Looking formward about max supported header size");
        JaxWsProxyFactoryBean factory = createClientFactory();
        HelloWorld client = (HelloWorld) factory.create();
        int currentSize = 1;
        int supportedSize = 0;
        while(currentSize < 1000000)
        {
            HeaderSize = currentSize;
            final String expected = newString(HeaderSize);
            try
            {
                String actual = client.returnCopyOfSecretHeader();
                Assert.assertEquals(expected, actual);
                supportedSize = currentSize;
            }
            catch(/*org.apache.cxf.interceptor.Fault*/Exception ex)
            {
                break;
            }
            currentSize += 128;
        }

        HeaderSize = 100;
        System.out.println("SUPPORTED header size: " + supportedSize);
        Assert.assertTrue("Max Sopported Header Size > 1K", supportedSize > 1024);
    }

    @Test
    public void _1_Configure_Client_Factory() throws Exception
    {
        Stress.LoopBody run = new Stress.LoopBody() {
            @Override
            public void run() {
                JaxWsProxyFactoryBean f = createClientFactory();
            }
        };

        String caption = "Measure: Create Client Factory";
        measure(run, caption);
    }

    @Test
    public void _2_Create_Proxy_and_Say_Hi() throws Exception
    {
        HeaderSize = 10;
        final String expected = newString(HeaderSize);
        final JaxWsProxyFactoryBean f = createClientFactory();
        Stress.LoopBody run = new Stress.LoopBody() {
            @Override
            public void run() {
                HelloWorld client = (HelloWorld) f.create();
                String actual = client.returnCopyOfSecretHeader();
                Assert.assertEquals(expected, actual);
            }
        };

        String caption = "Measure: Create Proxy and Say Hi";
        measure(run, caption);
    }

    ThreadLocal<HelloWorld> ThreadLocalProxy = new ThreadLocal<HelloWorld>()
    {
        @Override
        protected HelloWorld initialValue() {
            HeaderSize = 10;
            final JaxWsProxyFactoryBean f = createClientFactory();
            return (HelloWorld) f.create();
        }
    };

    @Test
    public void _3_Say_Hi_Using_ThreadLocal_Proxy() throws Exception
    {
        HeaderSize = 10;
        final String expected = newString(HeaderSize);

        Stress.ThreadLocalInitializer init = new Stress.ThreadLocalInitializer() {
            @Override
            public void exec() throws Exception {
                ThreadLocalProxy.get().toString();
            }
        };

        Stress.LoopBody run = new Stress.LoopBody() {
            @Override
            public void run() {
                String actual = ThreadLocalProxy.get().returnCopyOfSecretHeader();
                Assert.assertEquals(expected, actual);
            }
        };

        String caption = "Measure: Say Hi using ThreadLocal proxy";
        measure(init, run, caption);
    }

    @Test
    public void _4_Say_Hi_Using_Single_Proxy() throws Exception
    {
        HeaderSize = 10;
        final String expected = newString(HeaderSize);
        final HelloWorld client = (HelloWorld) createClientFactory().create();
        Stress.LoopBody run = new Stress.LoopBody() {
            @Override
            public void run() {
                String actual = client.returnCopyOfSecretHeader();
                Assert.assertEquals(actual, expected);
            }
        };

        String caption = "Measure: Say Hi using Single Proxy";
        measure(run, caption);
    }

    @Test
    public void _Z1_Publish_100500_Hosts() throws Exception
    {
        HeaderSize = 10;
        final AtomicLong count = new AtomicLong();
        final List<JaxWsServerFactoryBean> list = Collections.synchronizedList(new ArrayList<JaxWsServerFactoryBean>());
        final int port = FindAvailablePort();
        Stress.LoopBody run = new Stress.LoopBody() {
            @Override
            public void run() {

                HelloWorldImpl implementor = new HelloWorldImpl();
                JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
                System.setProperties(ServerSide.applyNoLimits(System.getProperties()));
                factory.setServiceClass(HelloWorld.class);
                String addr = String.format("http://localhost:%d/helloworld_" + count.incrementAndGet(), port);
                factory.setAddress(addr);
                factory.setServiceBean(implementor);
                TransferInterceptor.addServerHook(factory, new MyServerHook());
                factory.create();
                synchronized (list) {
                    list.add(factory);
                }

            }
        };

        String caption = "Measure: Create 100500 Hosts";
        measure(run, caption, 5000);

        Assert.assertTrue("At least 10 hosts successfully created", list.size() >= 10);
        System.out.println("Total hosts was published: " + list.size());

        long startAt = System.nanoTime();
        for(JaxWsServerFactoryBean xx:list)
            xx.destroy();

        System.out.println(String.format(
                "Destroyed %d hosts in %s secs",
                list.size(), DateCalc.NanosecToString(System.nanoTime() - startAt)
        ));
    }

    private void measure(Stress.LoopBody run, String caption) throws InterruptedException {
        measure(run, caption, 3000);
    }

    private void measure(Stress.ThreadLocalInitializer init, Stress.LoopBody run, String caption) throws InterruptedException {
        measure(init, run, caption, 3000);
    }

    private void measure(Stress.LoopBody run, String caption, int duration) throws InterruptedException {
        measure(null, run, caption, duration);
    }

    private void measure(Stress.ThreadLocalInitializer init, Stress.LoopBody run, String caption, int duration) throws InterruptedException {
        System.out.print(caption + ": ");
        Stress.Heat(run);
        List<Integer> threads = Arrays.asList(1, 2, 4, 8, 12, 16);
        ConsoleTable report = Stress.CreateReport();
        for(int t : threads)
        {
            System.out.print(t + " ");
            Stress res = Stress.Measure(TestEnv.scaleDuration(duration), 1, t, init, run);
            res.WriteToReport(report);
            Assert.assertTrue("Success counter must be above zero", res.SuccessCount > 0);
        }

        System.out.println(System6.lineSeparator() + System6.lineSeparator() + caption + System6.lineSeparator() + report);
    }

    static String newString(int length)
    {
        StringBuilder ret = new StringBuilder();
        for(int i=0; i<length; i++) ret.append("!");
        return ret.toString();
    }

    @Test
    public void _9_MyVo_Using_Single_Proxy() throws Exception
    {
        HeaderSize = 10;
        final MyVo1 expected = VoEnv.createVO1();
        final HelloWorld client = (HelloWorld) createClientFactory().create();
        Stress.LoopBody run = new Stress.LoopBody() {
            @Override
            public void run() {
                MyVo1 actual = client.getMyVo1();
                VoEnv.assertEquals(expected, actual);
            }
        };

        String caption = "Measure: MyVo1 Marshal+Unmarshal using Single Proxy";
        measure(run, caption);
    }

    @Test
    public void _8_MyVo_Using_ThreadLocal_Proxy() throws Exception
    {
        HeaderSize = 10;
        final MyVo1 expected = VoEnv.createVO1();

        Stress.ThreadLocalInitializer init = new Stress.ThreadLocalInitializer() {
            @Override
            public void exec() throws Exception {
                ThreadLocalProxy.get().getMyVo1();
            }
        };

        Stress.LoopBody run = new Stress.LoopBody() {
            @Override
            public void run() {
                MyVo1 actual = ThreadLocalProxy.get().getMyVo1();
                VoEnv.assertEquals(expected, actual);
            }
        };

        String caption = "Measure: MyVo1 Marshal+Unmarshal using ThreadLocal Proxy";
        measure(init, run, caption);
    }



}
