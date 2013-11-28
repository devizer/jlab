package org.universe;

import java.lang.reflect.Array;
import java.util.Locale;
import java.util.UUID;

public class System6 {

    private static String _lineSeparator;

    static {

        _lineSeparator = System.getProperty("line.separator");
        if (_lineSeparator == null)
        {
            String osFamily = LightSysInfo.getOsFamily();
            if (LightSysInfo.OS_LINUX.equals(osFamily) || LightSysInfo.OS_MACOSX.equals(osFamily))
                _lineSeparator = "\n";
            else if (LightSysInfo.OS_WINDOWS.equals(osFamily))
                _lineSeparator = "\r\n";
            else if ("Mac OS 9 and older (unknown)".equals(osFamily))
                _lineSeparator = "\r";
            else
                _lineSeparator = "\n";
        }
    }

    public static String lineSeparator()
    {
        return _lineSeparator;
    }


    public static String toLocaleTag(Locale locale)
    {
        String c = locale.getCountry();
        return
                locale.getLanguage()
                + (c == null || c.length() == 0 ? "" : ("-" + c));
    }

    final static char[] digits = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9' , 'a' , 'b' ,
            'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
            'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
            'o' , 'p' , 'q' , 'r' , 's' , 't' ,
            'u' , 'v' , 'w' , 'x' , 'y' , 'z',
            '$', '@'
    };

    public static String UuidTo26InsensitiveChars(UUID uuid)
    {
        if (uuid == null) return null;

        StringBuilder ret = new StringBuilder(26);
        // String tmp1 = "";
        long[] longs = new long[] { uuid.getLeastSignificantBits(), uuid.getMostSignificantBits() };
        int buffer = 0;
        int pos = 0;
        for(long l : longs)
        {
            for(int i=0; i<64; i++)
            {
                byte bit = (byte) (int) (l & 1);
                // tmp1 = tmp1 + (bit == 0 ? "0" : "1");
                l = l >> 1;
                buffer |= (bit << pos);
                if (++pos == 5)
                {
                    ret.append(digits[buffer]);
                    buffer = 0;
                    pos = 0;
                }
            }
        }

        if (pos != 0)
            ret.append(digits[buffer]);

        // System.out.println("TMP1=" + tmp1);
        return ret.toString();

    }

}
