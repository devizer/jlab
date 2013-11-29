package org.universe.tests.jaxb;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.universe.tests.Test_LightSystemInfo;
import org.universe.tests.compression.GZip_Perfomance;
import org.universe.tests.security.Test_SimpleIntegrity;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        Test_LightSystemInfo.class,
        GZip_Perfomance.class,
        Test_SimpleIntegrity.class,
        TestJAXB_Integrity.class,
        TestJAXB_Perfomance.class,
        ObjectIO_Perfomance.class
})
public class TestSuite {
}


