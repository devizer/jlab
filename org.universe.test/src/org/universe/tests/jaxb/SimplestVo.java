package org.universe.tests.jaxb;

import org.universe.ConsoleTable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TimeZone;

@XmlRootElement(name = "Val")
public class SimplestVo {

    @XmlAttribute
    public String Number = "42";
}


