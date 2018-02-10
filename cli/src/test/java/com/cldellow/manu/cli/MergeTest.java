package com.cldellow.manu.cli;

import com.cldellow.manu.format.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MergeTest {
    private final DateTime date1 = new DateTime(2012, 1, 1, 0, 0, 0, DateTimeZone.UTC);
    private final DateTime date2 = date1.plusDays(1);
    private final DateTime date3 = date1.plusDays(2);

    private String dbLoc = "/tmp/manu-test.data";

    @After
    public void cleanup() throws Exception {
        File f = new File(dbLoc);
        if (f.exists())
            f.delete();
    }


    @Test
    public void testNoArgs() throws Exception {
        int rv = new Merge(new String[]{}).entrypoint();
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
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.HOUR,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{});

        assertEquals(Merge.INTERVAL_MISMATCH, Merge.handle(Integer.MIN_VALUE, new Reader[]{r1, r2}, "blah", null));
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
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.FIXED2},
                new Record[]{});

        assertEquals(Merge.FIELD_TYPE_MISMATCH, Merge.handle(Integer.MIN_VALUE, new Reader[]{r1, r2}, "blah", null));
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
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{});

        assertEquals(Merge.UNKNOWN_FIELD, Merge.handle(Integer.MIN_VALUE, new Reader[]{r1, r2}, "blah", new String[]{"x"}));
    }

    @Test
    public void mergeEmpty() throws Exception {
        Reader r1 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date2,
                date3,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{});

        Merge.handle(Integer.MIN_VALUE, new Reader[]{r1, r2}, dbLoc, new String[]{"field"});
        Reader r = new ManuReader(dbLoc);
        assertEquals(0, r.getNumRecords());
    }

    @Test
    public void mergeEmptyDescending() throws Exception {
        Reader r1 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date2,
                date3,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{});

        Merge.handle(Integer.MIN_VALUE, new Reader[]{r2, r1}, dbLoc, new String[]{"field"});
        Reader r = new ManuReader(dbLoc);
        assertEquals(0, r.getNumRecords());
    }

    @Test
    public void mergeNonEmpty1() throws Exception {
        Record rec0 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{0}});
        Reader r1 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{rec0});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date2,
                date3,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{});

        Merge.handle(Integer.MIN_VALUE, new Reader[]{r1, r2}, dbLoc, new String[]{"field"});
        Reader r = new ManuReader(dbLoc);
        assertEquals(1, r.getNumRecords());
    }

    @Test
    public void mergeNonEmpty2() throws Exception {
        Record rec0 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{0}});
        Record rec1 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});

        Reader r1 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{rec0});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date2,
                date3,
                Interval.DAY,
                1,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{rec1});

        Merge.handle(Integer.MIN_VALUE, new Reader[]{r1, r2}, dbLoc, new String[]{"field"});
        Reader r = new ManuReader(dbLoc);
        assertEquals(2, r.getNumRecords());
    }

    @Test
    public void mergeNonEmpty2Descending() throws Exception {
        Record rec0 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{0}});
        Record rec1 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});

        Reader r1 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{rec0});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date2,
                date3,
                Interval.DAY,
                1,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{rec1});

        Merge.handle(Integer.MIN_VALUE, new Reader[]{r2, r1}, dbLoc, new String[]{"field"});
        Reader r = new ManuReader(dbLoc);
        assertEquals(2, r.getNumRecords());
        Record rec0rt = r.get(0);
        assertTrue(rec0rt.getEncoder(0) instanceof AverageEncoder);
    }

    @Test
    public void mergeNonEmpty2NotLossy() throws Exception {
        Record rec0 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{0}});
        Record rec1 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});

        Reader r1 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{rec0});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date2,
                date3,
                Interval.DAY,
                1,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{rec1});

        Merge.handle(Integer.MIN_VALUE, new Reader[]{r1, r2}, dbLoc, null);
        Reader r = new ManuReader(dbLoc);
        assertEquals(2, r.getNumRecords());
        Record rec0rt = r.get(0);
        assertTrue(rec0rt.getEncoder(0) instanceof PFOREncoder);
    }

    @Test
    public void mergeNonEmpty2AllLossy() throws Exception {
        Record rec0 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{0}});
        Record rec1 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});

        Reader r1 = new SimpleReader(
                "f1",
                0,
                date1,
                date2,
                Interval.DAY,
                0,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{rec0});
        Reader r2 = new SimpleReader(
                "f1",
                0,
                date2,
                date3,
                Interval.DAY,
                1,
                new String[]{"field"},
                new FieldType[]{FieldType.INT},
                new Record[]{rec1});

        Merge.handle(Integer.MIN_VALUE, new Reader[]{r1, r2}, dbLoc, new String[]{});
        Reader r = new ManuReader(dbLoc);
        assertEquals(2, r.getNumRecords());
        Record rec0rt = r.get(0);
        System.out.println(rec0rt.getEncoder(0));
        assertTrue(rec0rt.getEncoder(0) instanceof AverageEncoder);
    }
}