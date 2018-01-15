package com.cldellow.manu.format;

import org.joda.time.DateTime;

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
}