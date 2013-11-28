package org.universe.testcfx;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.universe.cxf.MessageInfo;
import org.universe.cxf.TransferInterceptor;

public class ClientSide {

    static class MyClientHook extends TransferInterceptor.MessageHook
    {
        @Override
        public void hook(MessageInfo message) {
            message.putStringHeader("X-Lang", "/<java-US>\\");
        }
    }

    public static void Call()
    {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        TransferInterceptor.addClientHook(factory, new MyClientHook());

        factory.getInInterceptors().add(new LoggingInInterceptor());
        factory.getOutInterceptors().add(new LoggingOutInterceptor());
        factory.setServiceClass(HelloWorld.class);
        factory.setAddress(ServerSide.Address);
        HelloWorld client = (HelloWorld) factory.create();

        String reply = client.sayHi("Vlad");
        System.out.println("Server said: " + reply);

        String reply2 = client.sayHi(43);
        System.out.println("Server said: " + reply2);
        // System.exit(0);
    }

}
