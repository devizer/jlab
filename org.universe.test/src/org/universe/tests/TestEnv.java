package org.universe.tests;

public class TestEnv {

    public static int scaleDuration(int milliSecs)
    {
        return Math.max(1, (int) (milliSecs * getTestLingering()));
    }

    public static Float getTestLingering()
    {
        try
        {
            return Float.parseFloat(System.getenv("TEST_LINGERING"));
        }
        catch(Throwable ex)
        {
            return 1f;
        }
    }

}
