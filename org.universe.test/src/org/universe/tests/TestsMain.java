package org.universe.tests;

import org.junit.runner.JUnitCore;
import org.universe.tests.jaxb.TestSuite;

/**
 * Created with IntelliJ IDEA.
 * User: admin
 * Date: 23.11.13
 * Time: 4:47
 * To change this template use File | Settings | File Templates.
 */
public class TestsMain {

    public static void main(String... args)
    {
        JUnitCore.runClasses(TestSuite.class);
    }


}
