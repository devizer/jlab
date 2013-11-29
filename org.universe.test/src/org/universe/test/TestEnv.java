package org.universe.test;

public class TestEnv {

    public enum Scope
    {
        // SKIP USAGE of MYSQL, SQL Server & RabbitMQ Server
        BUILD,

        // Default, SKIP USE of MYSQL, SQL Server & RabbitMQ Server
        DEPLOY,
    }

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

    public static Scope getScope()
    {
        String raw = System.getenv("TEST_SCOPE");
        return "BUILD".equals(raw) ? Scope.BUILD : Scope.DEPLOY;
    }

}
