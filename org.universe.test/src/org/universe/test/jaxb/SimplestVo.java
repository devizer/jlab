package org.universe.test.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Val")
public class SimplestVo {

    @XmlAttribute
    public String Number = "42";
}


