package org.universe.testcfx;


import org.apache.log4j.PropertyConfigurator;
import org.junit.runner.JUnitCore;

import java.io.InputStream;

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

