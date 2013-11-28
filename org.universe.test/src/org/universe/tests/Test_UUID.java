package org.universe.tests;

import org.junit.Test;
import org.universe.System6;

import java.util.UUID;

public class Test_UUID {

    @Test
    public void Test1()
    {
        UUID uuid = new UUID(32, 32);
        String chars = System6.UuidTo26InsensitiveChars(uuid);
        System.out.println("UUID=" + uuid);
        System.out.println("Chars: " + chars);
        System.out.println("Length: " + chars.length());
    }
}
