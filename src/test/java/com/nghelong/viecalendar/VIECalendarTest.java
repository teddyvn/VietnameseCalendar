package com.nghelong.viecalendar;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.util.Calendar;
import java.util.Date;

public class VIECalendarTest {
    @Test
    void testGetJulianDayNumber(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(2000,0,1); //2000-01-01
        Date testDate = calendar.getTime();
        Assertions.assertEquals(2451545, VIECalendar.getJulianDayNumber(testDate));
    }
    @Test
    void testGetDate(){
        Triplet<Integer, Integer, Integer> testDate = VIECalendar.getDate(2451545);
        Assertions.assertEquals(2000, testDate.getValue0());
        Assertions.assertEquals(0, testDate.getValue1() - 1);
        Assertions.assertEquals(1, testDate.getValue2());
    }
    @Test
    void testSolar2Lunar(){
        Quartet<Integer,Integer,Integer,Boolean> expectedLunarDate = Quartet.with(2022,8,2,false);
        Assertions.assertEquals(expectedLunarDate,VIECalendar.solar2Lunar(2022,8,28,7));
    }
}
