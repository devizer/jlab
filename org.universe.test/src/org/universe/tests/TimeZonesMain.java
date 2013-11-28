package org.universe.tests;

import org.universe.ConsoleTable;
import org.universe.DateCalc;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TimeZonesMain
{
    public static void main(String... args)
    {
        ConsoleTable t = new ConsoleTable("HH:mm", "Delta", "Daylight?", "ID", "Name", "Long Name");
        String[] ids = TimeZone.getAvailableIDs();
        ArrayList<TimeZone> zones = new ArrayList<TimeZone>();
        for(String id : ids)
            zones.add(TimeZone.getTimeZone(id));

        Collections.sort(zones, new TimeZonesMain.Comparer());
        for(TimeZone tz : zones)
            t
                    .put(FormatOffset(tz.getRawOffset()))
                    .put(String.valueOf(tz.getRawOffset()))
                    .put(tz.useDaylightTime())
                    .put(tz.getID())
                    .put(tz.getDisplayName(true, TimeZone.SHORT, Locale.getDefault()))
                    .put(tz.getDisplayName(true, TimeZone.LONG, Locale.getDefault()))
                    .newRow();

        System.out.println("Time Zones");
        System.out.println(t);
    }

    static String FormatOffset(int offset)
    {
        if (offset == 0) return "0";
        return  (offset > 0 ? "+" : offset < 0 ? "-" : " ")
                + DateCalc.MillisecToString(offset, "HH:mm");
    }

    static class Comparer implements Comparator<TimeZone> {
        @Override
        public int compare(TimeZone o1, TimeZone o2) {
            return o1.getRawOffset() - o2.getRawOffset();
        }
    }
}

