echo Cross Platform Test Runner
set TEST_LINGERING=0.01
TEST_LINGERING=0.01
export TEST_LINGERING
java -cp ./* org.junit.runner.JUnitCore org.universe.test.OnBuildTestSuite org.universe.cxf.test.TestCxf org.universe.rabbitstress.TestMQ
java -cp ./*: org.junit.runner.JUnitCore org.universe.test.OnBuildTestSuite org.universe.cxf.test.TestCxf org.universe.rabbitstress.TestMQ
