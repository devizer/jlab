package org.universe.test.jaxb;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;

/**
 * Its a 'slightly' modified version of MoVo1
 */
@XmlRootElement(name = MyVo1.RootName)
public class MyVo2 {

    public int MyInt;
    public String MyString;
    public BigDecimal MyDecimal;

    @XmlAttribute
    public int MyInt2;
    @XmlAttribute
    public String MyString2;
    @XmlAttribute
    public BigDecimal MyDecimal2;

    // Added!
    public String NewString;
    public Integer NewInteger;
    @XmlAttribute public String NewString2;
    @XmlAttribute public Integer NewInteger2;

    public int NewInt3;
    @XmlAttribute public int NewInt3A;


}
