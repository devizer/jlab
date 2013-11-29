package org.universe.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.universe.test.Test_LightSystemInfo;
import org.universe.test.compression.GZip_Perfomance;
import org.universe.test.jaxb.ObjectIO_Perfomance;
import org.universe.test.jaxb.TestJAXB_Integrity;
import org.universe.test.jaxb.TestJAXB_Perfomance;
import org.universe.test.security.Test_SimpleIntegrity;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        Test_LightSystemInfo.class,
        GZip_Perfomance.class,
        Test_SimpleIntegrity.class,
        TestJAXB_Integrity.class,
        TestJAXB_Perfomance.class,
        ObjectIO_Perfomance.class
})
public class OnBuildTestSuite {
}


