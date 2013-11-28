package org.universe.tests.jaxb;

import org.junit.Assert;
import org.universe.DateCalc;
import org.universe.jaxb.JAXBUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class VoEnv {

    public static final JAXBUtils<MyVo1> Ver1Converter = new JAXBUtils<MyVo1>(MyVo1.class);
    static final JAXBUtils<MyVo2> Ver2Converter = new JAXBUtils<MyVo2>(MyVo2.class);

    public static MyVo1 createVO1()
    {
        return new MyVo1() {{
            MyInt = 42;
            MyString = "Hi!";
            MyDecimal = new BigDecimal(100.5);
            MyInt2 = 24;
            MyString2 = "Ih!";
            MyDecimal2 = new BigDecimal(987654321.186).setScale(2, RoundingMode.HALF_UP);
            Date now = DateCalc.Create(2013,12,31).getDate();
            MyDate = AddDays(now, -10);
            MyDay = AddDays(now, -20);
            MySpecialDays = Arrays.asList(AddDays(now, -30), AddDays(now, -40));
            MyWorstDays = Arrays.asList(AddDays(now, -50), AddDays(now, -60), AddDays(now, -70));

            MyFile file = new MyFile();
            file.Name = "MyDocument.rtf";
            file.Content = new byte[] { 1,2,3,4,5 };
            MyFile pict = new MyFile();
            pict.Name = "MyPicture.jpg";
            pict.Content = new byte[] { 5,4,3,2,1 };
            MySecuredFile secrets = new MySecuredFile();
            secrets.Name = "Passwords.txt";
            secrets.Content = new byte[]{9,8,7,6,5,5,4,3,3,32};
            secrets.Roles = Arrays.asList("Admin", "Power User");
            Files = Arrays.asList(file, pict, secrets);
        }};
    }

    static Date AddDays(Date source, int delta)
    {
        return new DateCalc(source).TrimTimeOfDay().AddDays(delta).getDate();
    }

    public static void assertEquals(MyVo1 e, MyVo1 a) {

        Assert.assertEquals(e.MyInt, a.MyInt);
        Assert.assertEquals(e.MyString, a.MyString);
        Assert.assertEquals(e.MyDecimal, a.MyDecimal);

        Assert.assertNotNull(e.Files);
        Assert.assertEquals(e.Files.size(), a.Files.size());
        for(int i=0; i<e.Files.size(); i++)
        {
            Assert.assertEquals(e.Files.get(i).Name, a.Files.get(i).Name);
            Assert.assertEquals(e.Files.get(i).getClass(), a.Files.get(i).getClass());
        }

        Assert.assertNotNull(e.MySpecialDays);
        Assert.assertNotNull(a.MySpecialDays);
        assertArrayEquals(
                e.MySpecialDays.toArray(),
                a.MySpecialDays.toArray());
        assertArrayEquals(
                e.MyWorstDays.toArray(),
                a.MyWorstDays.toArray());
    }



}
