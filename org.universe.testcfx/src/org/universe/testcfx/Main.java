package org.universe.testcfx;


import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.log4j.PropertyConfigurator;
import org.junit.runner.JUnitCore;
import org.universe.cfx.TransferInterceptor.MessageHook;
import org.universe.cfx.MessageInfo;
import org.universe.cfx.TransferInterceptor;

import java.io.InputStream;
import java.util.Properties;
import java.util.TimeZone;

// http://cxf.apache.org/docs/a-simple-jax-ws-service.html
public class Main {

    public static void main(String[] args) {

        System.setProperty("org.apache.cxf.Logger", "org.apache.cxf.common.logging.Log4jLogger");

        ServerSide.Publish();

        ClientSide.Call();

        JUnitCore.runClasses(TestCxf.class);


    }

    static {
        InputStream xx = TestCxf.class.getResourceAsStream("/META-INF/log4j.properties");
        if (xx != null)
            PropertyConfigurator.configure(xx);
        else
            System.err.println("Log4j config not found");
    }


}

