package org.universe.test.jaxb;

import org.junit.runner.JUnitCore;
import org.universe.test.OnBuildTestSuite;

public class ProfilerMain {

    public static void main(String... args)
    {
        System.out.println("Press a key to start");
        System.console().readLine();
        JUnitCore.runClasses(OnBuildTestSuite.class);
    }
}
