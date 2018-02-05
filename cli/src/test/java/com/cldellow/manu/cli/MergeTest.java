package com.cldellow.manu.cli;

import com.cldellow.manu.format.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.*;

public class MergeTest {
    private final DateTime date1 = new DateTime(2012, 1, 1, 0, 0, 0, DateTimeZone.UTC);
    private final DateTime date2 = date1.plusDays(1);
    private final DateTime date3 = date1.plusDays(2);

    @Test
    public void testNoArgs() throws Exception {
        int rv = new Merge(new String[] {}).entrypoint();
        assertEquals(1, rv);
    }

    @Test
    public void mismatchedIntervals() throws Exception {
        Reader r1 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[] { "field" },
                new FieldType[] { FieldType.INT },
                new Record[] {});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.HOUR,
                0,
                new String[] { "field" },
                new FieldType[] { FieldType.INT },
                new Record[] {});

        assertEquals(Merge.INTERVAL_MISMATCH, Merge.handle(new Reader[] { r1, r2}, "blah", null));
    }

    @Test
    public void mismatchedFieldType() throws Exception {
        Reader r1 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[] { "field" },
                new FieldType[] { FieldType.INT },
                new Record[] {});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[] { "field" },
                new FieldType[] { FieldType.FIXED2 },
                new Record[] {});

        assertEquals(Merge.FIELD_TYPE_MISMATCH, Merge.handle(new Reader[] { r1, r2}, "blah", null));
    }

    @Test
    public void unknownLossyField() throws Exception {
        Reader r1 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[] { "field" },
                new FieldType[] { FieldType.INT },
                new Record[] {});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[] { "field" },
                new FieldType[] { FieldType.INT },
                new Record[] {});

        assertEquals(Merge.UNKNOWN_FIELD, Merge.handle(new Reader[] { r1, r2}, "blah", new String[] { "x"}));
    }
}