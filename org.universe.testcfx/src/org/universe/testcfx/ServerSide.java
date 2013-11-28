package org.universe.testcfx;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.universe.cxf.MessageInfo;
import org.universe.cxf.TransferInterceptor;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;

public class ServerSide {

    public static final String Address;

    static {
        // Find available port
        int port = 9000;
        try {
            ServerSocket socket = new ServerSocket(0);
            port = socket.getLocalPort();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Address = String.format("http://localhost:%d/helloWorld", port);
        System.out.println("Address: " + Address);
    }

    static class MyServerHook extends TransferInterceptor.MessageHook
    {
        @Override
        public void hook(MessageInfo message) {
            MyContextOnServer.setLang(message.getStringHeader("X-Lang"));
        }
    }

    static class MyLogHook extends TransferInterceptor.MessageHook
    {
        @Override
        public void hook(MessageInfo message) {
            System.out.println(String.format(
                    "%s::%s %d bytes from %s. Key is %s",
                    message.getAboslutePath(),
                    message.getOperationName(),
                    message.getContentLength(),
                    message.getRemoteAddr(),
                    message.getActionKey()
            ));
        }
    }

    static void Publish()
    {
        HelloWorldImpl implementor = new HelloWorldImpl();
        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        System.setProperties(applyNoLimits(System.getProperties()));
        svrFactory.setServiceClass(HelloWorld.class);
        svrFactory.setAddress(Address);
        svrFactory.setServiceBean(implementor);

        TransferInterceptor.addServerHook(svrFactory, new MyServerHook());
        TransferInterceptor.addServerHook(svrFactory,new MyLogHook());

        svrFactory.getInInterceptors().add(new LoggingInInterceptor());
        svrFactory.getOutInterceptors().add(new LoggingOutInterceptor());
        svrFactory.create();
        System.out.println("Published: " + implementor.getClass());
    }

    public static Properties applyNoLimits(Properties p)
    {
        p.put("com.ctc.wstx.maxAttributesPerElement", new Integer(500));
        p.put("com.ctc.wstx.maxAttributeSize", new Integer(64 * 1024));
        p.put("com.ctc.wstx.maxChildrenPerElement", new Integer(50000));
        p.put("com.ctc.wstx.maxElementCount", new Long(Long.MAX_VALUE));
        p.put("com.ctc.wstx.maxElementDepth", new Integer(100));
        p.put("com.ctc.wstx.maxCharacters", new Long(Long.MAX_VALUE));
        p.put("com.ctc.wstx.maxTextLength", new Long(128 * 1024 * 1024));
        p.put("com.ctc.wstx.inputBufferLength", new Integer(65536));
        p.put("org.apache.cxf.stax.allowInsecureParser", "1");
        return p;
    }
}
