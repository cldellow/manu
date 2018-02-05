package com.cldellow.manu.format;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.HashMap;
import java.util.Map;

/**
 * The interval of time between datapoints in a Manu file.
 */
public enum Interval {
    MINUTE(0),
    HOUR(1),
    DAY(2),
    /**
     * Not fully supported.
     */
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

    /**
     * Return the date {@code n} intervals after {@code from}.
     * .
     *
     * @param from The date from which to start counting.
     * @param n    The number of intervals of this type to add.
     * @return The date {@code n} intervals after {@code from}.
     */
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

    /**
     * Returns the number of intervals between {@code from} and {@code to}.
     *
     * @param from The start date.
     * @param to   The final date.
     * @return The number of intervals between {@code from} and {@code to}.
     */
    public int difference(DateTime from, DateTime to) {
        from = truncate(from);
        to = truncate(to);

        PeriodType pt = PeriodType.minutes();
        if (this == HOUR)
            pt = PeriodType.hours();
        else if (this == DAY)
            pt = PeriodType.days();
        else if (this == WEEK)
            pt = PeriodType.weeks();
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
        else if (this == WEEK)
            return p.getWeeks();
        else if (this == MONTH)
            return p.getMonths();
        else if (this == QUARTER)
            return p.getMonths() / 3;

        return p.getYears();
    }

    /**
     * Truncates {@code date} such that it lies on an interval boundary.
     *
     * @param date The date to be truncated.
     * @return The truncated value.
     */
    public DateTime truncate(DateTime date) {
        DateTime rv = date.withSecondOfMinute(0).withMillisOfSecond(0);

        if (this == MINUTE) {
            return rv;
        }

        rv = rv.withMinuteOfHour(0);
        if (this == HOUR)
            return rv;

        rv = rv.withHourOfDay(0);
        if (this == DAY)
            return rv;

        if (this == WEEK) {
            while (rv.getDayOfWeek() != DateTimeConstants.THURSDAY)
                rv = rv.plusDays(-1);

            return rv;
        }

        rv = rv.withDayOfMonth(1);

        if (this == MONTH)
            return rv;

        rv = rv.withMonthOfYear(((rv.getMonthOfYear() - 1) / 3) * 3 + 1);
        if (this == QUARTER)
            return rv;

        return rv.withMonthOfYear(1);
    }
}