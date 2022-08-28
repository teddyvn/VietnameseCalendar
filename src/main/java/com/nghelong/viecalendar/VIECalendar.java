package com.nghelong.viecalendar;
import java.util.Calendar;
import java.util.Date;
import org.javatuples.*;

public class VIECalendar {
    /**
     *  Get Julian day number from the specific date
     *  Base date: 1/1/4713 BC Julian Calendar or 24/11/4714 BC Gregorian Calendar
     * @param date the date
     * @return Julian day number
     */
    public static long getJulianDayNumber(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dateOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int year = calendar.get(Calendar.YEAR);
        int a = (14 - month)/12;
        int y = year + 4800 - a; //the number year from 1/1/4800 BC
        int m = month + 12*a - 3;
        long i = dateOfMonth + (153 * m + 2) / 5 + 365L * y + y / 4;
        long jdn = i - y/100 + y/400 - 32045;
        if (jdn < 2299161) {
            jdn = i - 32083;
        }
        return jdn;
    }
    public static Triplet<Integer,Integer,Integer> getDate(long jdn){
        long a, b, c,d,e,m;
        int day, month, year;
        if (jdn > 2299160) { // After 5/10/1582, Gregorian calendar
            a = jdn + 32044;
            b = (4 * a + 3) / 146097;
            c = a - (b * 146097) / 4;
        } else {
            b = 0;
            c = jdn + 32082;
        }
        d = (4*c+3)/1461;
        e = c - (1461*d)/4;
        m = (5*e+2)/153;
        day = (int) (e - (153*m+2)/5 + 1);
        month = (int) (m + 3 - 12* (m/10));
        year = (int) (b*100 + d - 4800 + m/10);
        return Triplet.with(year,month,day);
    }
    static public int getSoc(int times, int timeZone){
        double t = times/1236.85; //Time in Julian centuries from 1900 January 0.5
        double t2 = t*t;
        double t3 = t2*t;
        double dr = Math.PI/180; //degree to radian
        double jd1 = 2415020.75933 + 29.53058868*times + 0.0001178*t2 - 0.000000155*t3;
        jd1 = jd1 + 0.00033*Math.sin((166.56 + 132.87*t - 0.009173*t2)*dr); // Mean new moon
        double m = 359.2242 + 29.10535608*times - 0.0000333*t2 - 0.00000347*t3; // Sun's mean anomaly
        double mpr = 306.0253 + 385.81691806*times + 0.0107306*t2 + 0.00001236*t3; // Moon's mean anomaly
        double f = 21.2964 + 390.67050646*times - 0.0016528*t2 - 0.00000239*t3; // Moon's argument of latitude
        double c1=(0.1734 - 0.000393*t)*Math.sin(m*dr) + 0.0021*Math.sin(2*dr*m);
        c1 = c1 - 0.4068*Math.sin(mpr*dr) + 0.0161*Math.sin(dr*2*mpr);
        c1 = c1 - 0.0004*Math.sin(dr*3*mpr);
        c1 = c1 + 0.0104*Math.sin(dr*2*f) - 0.0051*Math.sin(dr*(m+mpr));
        c1 = c1 - 0.0074*Math.sin(dr*(m-mpr)) + 0.0004*Math.sin(dr*(2*f+m));
        c1 = c1 - 0.0004*Math.sin(dr*(2*f-m)) - 0.0006*Math.sin(dr*(2*f+mpr));
        c1 = c1 + 0.0010*Math.sin(dr*(2*f-mpr)) + 0.0005*Math.sin(dr*(2*mpr+m));
        double delta;
        if (t < -11) {
            delta= 0.001 + 0.000839*t + 0.0002261*t2 - 0.00000845*t3 - 0.000000081*t*t3;
        } else {
            delta= -0.000278 + 0.000265*t + 0.000262*t2;
        };
        double jdNew = jd1 + c1 - delta;
        return (int)Math.floor(jdNew + 0.5 + timeZone/24.0);
    }
    public static int getSunLong(long jdn, int timezone){
        double t = (jdn - 2451545.5 - timezone/24.0) / 36525; // Time in Julian centuries from 2000-01-01 12:00:00 GMT
        double t2 = t*t;
        double dr = Math.PI/180; // degree to radian
        double m = 357.52910 + 35999.05030*t - 0.0001559*t2 - 0.00000048*t*t2; // mean anomaly, degree
        double l0 = 280.46645 + 36000.76983*t + 0.0003032*t2; // mean longitude, degree
        double dl = (1.914600 - 0.004817*t - 0.000014*t2)*Math.sin(dr*m);
        dl +=  (0.019993 - 0.000101*t)*Math.sin(dr*2*m) + 0.000290*Math.sin(dr*3*m);
        double l = l0 + dl; // true longitude, degree
        l *= dr;
        l = l - Math.PI*2*(Math.floor(l/(Math.PI*2))); // Normalize to (0, 2*PI)
        return (int)Math.floor(l / Math.PI * 6.0);
    }
    public static int getLunarMonth11(int year,int timezone){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, Calendar.DECEMBER, 31);//31/12/year
        long jdnFrom1990 = 2415021;
        long off = VIECalendar.getJulianDayNumber(calendar.getTime()) - jdnFrom1990;
        int k = (int) Math.floor(off / 29.530588853);
        int nm = VIECalendar.getSoc(k, timezone);
        int sunPosition = VIECalendar.getSunLong(nm, timezone); // sun longitude at local midnight
        if (sunPosition >= 9) {
            nm = VIECalendar.getSoc(k - 1, timezone);
        }
        return nm;
    }
    public static int getLeapMonthOffset(int a11, int timezone){
        int k = (int)Math.floor((a11 - 2415021.076998695) / 29.530588853 + 0.5);
        int last = 0;
        int month = 1; // We start with the month following lunar month 11
        int arc = getSunLong(getSoc(k+month,timezone),timezone);
        do {
            last = arc;
            month++;
            arc = getSunLong(getSoc(k+month,timezone),timezone);
        } while ((arc != last) && (month < 14));
        return month-1;
    }

    /**
     * Convert a solar date to a Vietnamese lunar date
     * @param y year (example 2022)
     * @param m month (based 1) (example 8)
     * @param d day of month (based 1) (example 28)
     * @param timezone offset timezone from GTM (0-23). GTM+7: timezone =7, GMT-1: timezone=23
     * @return Quartet with (year,month, day, isLeapYear) of lunar date
     */
    public static Quartet<Integer,Integer,Integer,Boolean> solar2Lunar(int y, int m, int d, int timezone){
        //var k, dayNumber, monthStart, a11, b11, lunarDay, lunarMonth, lunarYear, lunarLeap;
        Calendar calendar = Calendar.getInstance();
        calendar.set(y,m-1,d);
        long jdn = VIECalendar.getJulianDayNumber(calendar.getTime()); //dayNumber
        int k = (int)Math.floor((jdn - 2415021.076998695) / 29.530588853);
         int monthStart = VIECalendar.getSoc(k+1, timezone);

        if (monthStart > jdn) {
            monthStart = VIECalendar.getSoc(k, timezone);
        }
        int a11 = VIECalendar.getLunarMonth11(y, timezone);
        int b11 = a11;
        int ly;
        if (a11 >= monthStart) {
            ly = y;
            a11 = VIECalendar.getLunarMonth11(y-1, timezone);
        } else {
            ly = y+1;
            b11 = VIECalendar.getLunarMonth11(y+1, timezone);
        }
        int ld = (int)(jdn - monthStart + 1);
        int diff = (monthStart - a11)/29;
        boolean isLeapYear = false;
        int lm   = diff + 11;
        if (b11 - a11 > 365) {
           int leapMonthDiff = VIECalendar.getLeapMonthOffset(a11, timezone);
            if (diff >= leapMonthDiff) {
                lm = diff + 10;
                if (diff == leapMonthDiff) {
                    isLeapYear = true;
                }
            }
        }
        if (lm > 12) {
            lm = lm - 12;
        }
        if (lm >= 11 && diff < 4) {
            ly -= 1;
        }
        return Quartet.with(ly,lm,ld,isLeapYear);
    }
}
