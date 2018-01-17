package com.cldellow.manu.format;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.HashMap;
import java.util.Map;

public enum Interval {
    MINUTE(0),
    HOUR(1),
    DAY(2),
    WEEK(3),
    MONTH(4),
    QUARTER(5),
    YEAR(6);

    private static Map map = new HashMap();

    static {
        for (Interval interval : Interval.values()) {
            map.put(interval.value, interval);
        }
    }

    private int value;

    private Interval(int value) {
        this.value = value;
    }

    public static Interval valueOf(int interval) {
        return (Interval) map.get(interval);
    }

    public int getValue() {
        return value;
    }

    public DateTime add(DateTime from, int n) {
        if (this == MINUTE)
            return from.plusMinutes(n);
        if (this == HOUR)
            return from.plusHours(n);
        if (this == DAY)
            return from.plusDays(n);
        if (this == WEEK)
            return from.plusWeeks(n);
        if (this == MONTH)
            return from.plusMonths(n);
        if (this == QUARTER)
            return from.plusMonths(n * 3);

        return from.plusYears(n);
    }

    public int difference(DateTime from, DateTime to) {
        if(this==WEEK)
            throw new UnsupportedOperationException();

        from = truncate(from);
        to = truncate(to);

        PeriodType pt = PeriodType.minutes();
        if (this == HOUR)
            pt = PeriodType.hours();
        else if (this == DAY)
            pt = PeriodType.days();
        else if (this == MONTH || this == QUARTER)
            pt = PeriodType.months();
        else if (this == YEAR)
            pt = PeriodType.years();

        Period p = new Period(from, to, pt);
        if (this == MINUTE)
            return p.getMinutes();
        else if (this == HOUR)
            return p.getHours();
        else if (this == DAY)
            return p.getDays();
        else if (this == MONTH)
            return p.getMonths();
        else if (this == QUARTER)
            return p.getMonths() / 3;

        return p.getYears();
    }

    public DateTime truncate(DateTime dt) {
        DateTime rv = dt.withSecondOfMinute(0).withMillisOfSecond(0);

        if (this == MINUTE) {
            return rv;
        }

        rv = rv.withMinuteOfHour(0);
        if (this == HOUR)
            return rv;

        rv = rv.withHourOfDay(0);
        if (this == DAY)
            return rv;

        if (this == WEEK)
            throw new UnsupportedOperationException();

        rv = rv.withDayOfMonth(1);

        if (this == MONTH)
            return rv;

        rv = rv.withMonthOfYear(((rv.getMonthOfYear() - 1) / 3) * 3 + 1);
        if (this == QUARTER)
            return rv;

        return rv.withMonthOfYear(1);
    }
}