package org.universe.jaxb;

import org.universe.jcl.ReliableThreadLocal;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// Target Date and List<Date> stored without time zone and time of day
// After unmarshalling, Time is zero, time zone is current.
public class DayOnlyAsStringAdapter extends XmlAdapter<String,Date>
{
    // per thread
    final static ReliableThreadLocal<DateFormat> Format = new ReliableThreadLocal<DateFormat>(){
        @Override
        protected DateFormat initialValue() throws Exception {
            return new SimpleDateFormat("yyyy'-'MM'-'dd");
        }
    };


    @Override
    public Date unmarshal(String v) throws Exception {
        return Format.get().parse(v);
    }

    @Override
    public String marshal(Date v) throws Exception {
        return Format.get().format(v);
    }
}
