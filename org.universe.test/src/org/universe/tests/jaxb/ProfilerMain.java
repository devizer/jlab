package org.universe.tests.jaxb;

import org.junit.runner.JUnitCore;

public class ProfilerMain {

    public static void main(String... args)
    {
        System.out.println("Press a key to start");
        System.console().readLine();
        JUnitCore.runClasses(TestSuite.class);
    }
}
