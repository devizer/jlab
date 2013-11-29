package org.universe.test;

import org.junit.Assert;
import org.junit.Test;
import org.universe.System6;

import java.util.UUID;

public class Test_UUID {

    @Test
    public void Test_UuidTo26InsensitiveChars()
    {
        UUID uuid = new UUID(32, 32);
        String chars = System6.UuidTo26InsensitiveChars(uuid);
        Assert.assertNotNull(chars);
        Assert.assertEquals("Length is 26 chars", 26, chars.length());
        System.out.println("Chars of '" + uuid + " are '" + chars + "'");
    }
}
