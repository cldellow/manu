package com.cldellow.manu.format;

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
}