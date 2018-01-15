package com.cldellow.manu.serve;

import org.joda.time.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class ManuAppTest {

    @Test
    public void stuf() {
        DateTime dt = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeZone.UTC);
        DateTime dt2 = dt.plusHours(744);
        System.out.println(dt);
        System.out.println(dt2);
        Period p = new Period(dt, dt2, PeriodType.hours());
        System.out.println(p);
    }
}