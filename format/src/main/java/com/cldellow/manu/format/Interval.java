package com.cldellow.manu.format;

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

    private int value;
    private static Map map = new HashMap();

    private Interval(int value) {
        this.value = value;
    }

    static {
        for (Interval interval : Interval.values()) {
            map.put(interval.value, interval);
        }
    }

    public static Interval valueOf(int interval) {
        return (Interval) map.get(interval);
    }

    public int getValue() {
        return value;
    }
}