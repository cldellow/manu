package com.cldellow.manu.format;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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

    @Test
    public void truncateMinute() {
        assertEquals(
                new DateTime(2012, 1, 2, 12, 34, 0),
                Interval.MINUTE.truncate(new DateTime(2012, 1, 2, 12, 34, 56, 123)));
    }

    @Test
    public void truncateHour() {
        assertEquals(
                new DateTime(2012, 1, 2, 12, 0, 0),
                Interval.HOUR.truncate(new DateTime(2012, 1, 2, 12, 34, 56, 123)));
    }

    @Test
    public void truncateDay() {
        assertEquals(
                new DateTime(2012, 1, 2, 0, 0, 0),
                Interval.DAY.truncate(new DateTime(2012, 1, 2, 12, 34, 56, 123)));
    }

    @Test
    public void truncateWeek() {
        assertEquals(
                new DateTime(2011, 12, 29, 0, 0, 0, 0),
                Interval.WEEK.truncate(new DateTime(2012, 1, 2, 12, 34, 56, 123))
        );

    }

    @Test
    public void truncateMonth() {
        assertEquals(
                new DateTime(2012, 1, 1, 0, 0, 0),
                Interval.MONTH.truncate(new DateTime(2012, 1, 2, 12, 34, 56, 123)));
    }

    @Test
    public void truncateQuarter() {
        assertEquals(
                new DateTime(2012, 1, 1, 0, 0, 0),
                Interval.QUARTER.truncate(new DateTime(2012, 1, 2, 12, 34, 56, 123)));

        assertEquals(
                new DateTime(2012, 10, 1, 0, 0, 0),
                Interval.QUARTER.truncate(new DateTime(2012, 12, 2, 12, 34, 56, 123)));
    }


    @Test
    public void truncateYear() {
        assertEquals(
                new DateTime(2012, 1, 1, 0, 0, 0),
                Interval.YEAR.truncate(new DateTime(2012, 1, 2, 12, 34, 56, 123)));
    }

    @Test
    public void difference() {
        DateTime s1 = new DateTime(2012, 1, 2, 3, 4, 5);
        DateTime s2 = new DateTime(2015, 3, 4, 1, 2, 3);

        assertEquals(1665958, Interval.MINUTE.difference(s1, s2));
        assertEquals(27766, Interval.HOUR.difference(s1, s2));
        assertEquals(1157, Interval.DAY.difference(s1, s2));
        assertEquals(165, Interval.WEEK.difference(s1, s2));
        assertEquals(38, Interval.MONTH.difference(s1, s2));
        assertEquals(12, Interval.QUARTER.difference(s1, s2));
        assertEquals(3, Interval.YEAR.difference(s1, s2));

        assertEquals(0, Interval.MINUTE.difference(s1, s1));
        assertEquals(0, Interval.HOUR.difference(s1, s1));
        assertEquals(0, Interval.DAY.difference(s1, s1));
        assertEquals(0, Interval.WEEK.difference(s1, s1));
        assertEquals(0, Interval.MONTH.difference(s1, s1));
        assertEquals(0, Interval.QUARTER.difference(s1, s1));
        assertEquals(0, Interval.YEAR.difference(s1, s1));

        assertEquals(1, Interval.MINUTE.difference(s1, s1.plusMinutes(1)));
        assertEquals(1, Interval.HOUR.difference(s1, s1.plusHours(1)));
        assertEquals(1, Interval.DAY.difference(s1, s1.plusDays(1)));
        assertEquals(1, Interval.WEEK.difference(s1, s1.plusWeeks(1)));
        assertEquals(1, Interval.MONTH.difference(s1, s1.plusMonths(1)));
        assertEquals(1, Interval.QUARTER.difference(s1, s1.plusMonths(3)));
        assertEquals(1, Interval.YEAR.difference(s1, s1.plusYears(1)));
    }
}