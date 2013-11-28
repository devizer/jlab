package org.universe;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateCalc {

    private Date date;

    public static DateCalc Zero()
    {
        return new DateCalc(new Date(0));
    }

    public DateCalc(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public DateCalc TrimTimeOfDay()
    {
        Calendar c = Calendar.getInstance();
        // c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setTime(date);
        c.set(Calendar.AM_PM, Calendar.AM);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        date = c.getTime();
        return this;
    }

    public static DateCalc Create(int year, int month, int day)
    {
        Calendar c = Calendar.getInstance();
        // c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.set(Calendar.AM_PM, Calendar.AM);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.YEAR, year);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.MONTH, month);
        DateCalc ret = new DateCalc(c.getTime());
        return ret;
    }

    public DateCalc AddDays(int days)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        date = c.getTime();
        return this;
    }

    public DateCalc AddWeeks(int weeks)
    {
        return AddDays(weeks * 7);
    }

    public DateCalc FirstDayOfMonth()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, 1);
        date = c.getTime();
        return this;
    }

    public DateCalc FirstDayOfWeek()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        // 1: sunday, 2: monday, 7: Saturday
        int fwd = c.getFirstDayOfWeek();
        int cwd = c.get(Calendar.DAY_OF_WEEK);
        int delta = 0;
        while(cwd != fwd)
        {
            cwd = (cwd - 1 + 7) % 7 + 1;
            delta--;
        }

        c.add(Calendar.DATE, delta);
        date = c.getTime();
        return this;
    }

    public DateCalc AddMilliSeconds(int milliSeconds)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MILLISECOND, milliSeconds);
        date = c.getTime();
        return this;
    }

    public String Format(String format)
    {
        final SimpleDateFormat f = new SimpleDateFormat(format);
        // f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(date);
    }

    public static String MillisecToString(int millisec, String format)
    {
        // String pre =(millisec > 0 ? "+" : millisec < 0 ? "-" : " ");
        final SimpleDateFormat f = new SimpleDateFormat(format);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date(Math.abs(millisec)));
    }


    // HH:mm:ss.SSS, where HH and mm are optional
    public static String NanosecToString(long nanosec)
    {
        return NanosecToString(nanosec, 3);
    }

    public static String NanosecToString(long nanosec, int scale)
    {
        long v3 = nanosec % 60000000000L;
        DecimalFormat decimalFormat;
        if (scale == 0)
            decimalFormat = new DecimalFormat("00");
        else
        {
            StringBuilder fmt = new StringBuilder();
            for(int i=0; i<scale; i++) fmt.append('0');
            decimalFormat = new DecimalFormat("00." + fmt);
        }

        String s3 = decimalFormat.format(v3 / 1000000000d);
        long r3 = nanosec / 60000000000L;
        long v2 = r3 % 60;
        String s2 = v2 == 0 ? "" : v2 < 10 ? ("0" + v2 + ":") : (String.valueOf(v2) + ":");
        long v1 = r3 / 60;
        String s1 = v1 == 0 ? "" : v1 + ":";
        return s1 + s2 + s3;
    }


}
