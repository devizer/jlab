package org.universe.test.jaxb;

import org.universe.jaxb.DayOnlyAsStringAdapter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: lulu
 * Date: 11/3/13
 * Time: 1:21 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement(name = MyVo1.RootName)
public class MyVo1 implements Serializable {

    final static String RootName = "MyValue";

    public int MyInt;
    public String MyString;
    public BigDecimal MyDecimal;
    public Date MyDate;

    @XmlJavaTypeAdapter(DayOnlyAsStringAdapter.class)
    public Date MyDay;

    @XmlJavaTypeAdapter(DayOnlyAsStringAdapter.class)
    @XmlElementWrapper(name = "MySpecialDays")
    @XmlElement(name = "Day")
    public List<Date> MySpecialDays;

    @XmlJavaTypeAdapter(DayOnlyAsStringAdapter.class)
    @XmlList
    public List<Date> MyWorstDays;

    @XmlElementWrapper(name = "Files")
    @XmlElements({
        @XmlElement(name = "File", type = MyFile.class),
        @XmlElement(name = "SecuredFile", type = MySecuredFile.class)})
    public List<MyFile> Files;

    @XmlAttribute
    public int MyInt2;
    @XmlAttribute
    public String MyString2;
    @XmlAttribute
    public BigDecimal MyDecimal2;

    @XmlRootElement(name = "File")
    public static class MyFile implements Serializable
    {
        // @XmlValue
        public byte[] Content;

        @XmlAttribute
        public String Name;
    }

    @XmlRootElement(name = "SecuredFile")
    public static class MySecuredFile extends MyFile implements Serializable
    {
        @XmlElementWrapper(name = "Roles")
        @XmlElement(name = "Role")
        public List<String> Roles;
    }

}
