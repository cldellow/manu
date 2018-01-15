package com.cldellow.manu.format;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;

public class IntervalTest {
    @Test
    public void testRoundTrip() {
        assertEquals(0, Interval.MINUTE.getValue());
        assertEquals(Interval.MINUTE, Interval.valueOf(0));
        assertEquals(1, Interval.HOUR.getValue());
        assertEquals(Interval.HOUR, Interval.valueOf(1));
        assertEquals(2, Interval.DAY.getValue());
        assertEquals(Interval.DAY, Interval.valueOf(2));
        assertEquals(3, Interval.WEEK.getValue());
        assertEquals(Interval.WEEK, Interval.valueOf(3));
        assertEquals(4, Interval.MONTH.getValue());
        assertEquals(Interval.MONTH, Interval.valueOf(4));
        assertEquals(5, Interval.QUARTER.getValue());
        assertEquals(Interval.QUARTER, Interval.valueOf(5));
        assertEquals(6, Interval.YEAR.getValue());
        assertEquals(Interval.YEAR, Interval.valueOf(6));
    }

    @Test
    public void testAdd() {
        DateTime s1 = new DateTime(2008, 1, 1, 0, 0, 0);

        assertEquals(s1.plusMinutes(1), Interval.MINUTE.add(s1, 1));
        assertEquals(s1.plusHours(1), Interval.HOUR.add(s1, 1));
        assertEquals(s1.plusDays(1), Interval.DAY.add(s1, 1));
        assertEquals(s1.plusWeeks(1), Interval.WEEK.add(s1, 1));
        assertEquals(s1.plusMonths(1), Interval.MONTH.add(s1, 1));
        assertEquals(s1.plusMonths(3), Interval.QUARTER.add(s1, 1));
        assertEquals(s1.plusYears(1), Interval.YEAR.add(s1, 1));
    }
}