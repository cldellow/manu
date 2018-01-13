package com.cldellow.manu.cli;

import com.cldellow.manu.format.Interval;
import org.junit.Test;

import java.security.Key;

import static org.junit.Assert.*;

public class ParsersTest {
    @Test
    public void ctor() {
        new Parsers();
    }

    @Test
    public void interval() {
        assertEquals(Interval.MINUTE, Parsers.interval("minute"));
        assertEquals(Interval.HOUR, Parsers.interval("hour"));
        assertEquals(Interval.DAY, Parsers.interval("day"));
        assertEquals(Interval.WEEK, Parsers.interval("week"));
        assertEquals(Interval.MONTH, Parsers.interval("month"));
        assertEquals(Interval.QUARTER, Parsers.interval("quarter"));
        assertEquals(Interval.YEAR, Parsers.interval("year"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownInterval() {
        Parsers.interval("blah");
    }

    @Test
    public void epochMs() {
        assertEquals(0L, Parsers.epochMs("1970-01-01T00:00Z"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEpochMs() {
        Parsers.epochMs("197adsfdsa0-01-01T00:00Z");
    }

    @Test
    public void keyKind() {
        assertEquals(KeyKind.KEY, Parsers.keyKind("--key"));
        assertEquals(KeyKind.ID, Parsers.keyKind("--id"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownKeyKind() {
        Parsers.keyKind("blah");
    }

    @Test
    public void fieldKind() {
        assertEquals(FieldKind.INT, Parsers.fieldKind("--int"));
        assertEquals(FieldKind.FIXED1, Parsers.fieldKind("--fixed1"));
        assertEquals(FieldKind.FIXED2, Parsers.fieldKind("--fixed2"));
        assertEquals(FieldKind.LOSSY, Parsers.fieldKind("--lossy"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownFieldKind() {
        Parsers.fieldKind("blah");
    }

}