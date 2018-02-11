package com.cldellow.manu.cli;

import com.cldellow.manu.format.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class MergeIteratorTest {
    final DateTime d1 = new DateTime(2012, 1, 1, 0, 0, 0, DateTimeZone.UTC);
    final DateTime d2 = d1.plusDays(1);
    final DateTime d3 = d1.plusDays(2);

    @Test
    public void concat() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});
        SimpleRecord r12 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{2}});
        SimpleRecord r21 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{11}});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{12}});

        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11, r12});
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d2, d3, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r21, r22});
        MergeIterator it = new MergeIterator(new Reader[]{reader1, reader2}, Integer.MIN_VALUE, 2, 0, 2,
                new String[]{"field"}, new String[]{});

        assertTrue(it.hasNext());
        Record r1 = it.next();
        assertEquals(0, r1.getId());
        int[] r1vals = r1.getValues(0);
        assertEquals(2, r1vals.length);
        assertEquals(1, r1vals[0]);
        assertEquals(11, r1vals[1]);

        assertTrue(it.hasNext());
        Record r2 = it.next();
        assertEquals(1, r2.getId());
        int[] r2vals = r2.getValues(0);
        assertEquals(2, r2vals.length);
        assertEquals(2, r2vals[0]);
        assertEquals(12, r2vals[1]);

        assertFalse(it.hasNext());
    }

    @Test
    public void concatOrderDoesNotMatter() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});
        SimpleRecord r12 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{2}});
        SimpleRecord r21 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{11}});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{12}});

        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11, r12});
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d2, d3, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r21, r22});
        MergeIterator it = new MergeIterator(new Reader[]{reader2, reader1}, Integer.MIN_VALUE, 2, 0, 2,
                new String[]{"field"}, new String[]{});

        assertTrue(it.hasNext());
        Record r1 = it.next();
        assertEquals(0, r1.getId());
        int[] r1vals = r1.getValues(0);
        assertEquals(2, r1vals.length);
        assertEquals(1, r1vals[0]);
        assertEquals(11, r1vals[1]);

        assertTrue(it.hasNext());
        Record r2 = it.next();
        assertEquals(1, r2.getId());
        int[] r2vals = r2.getValues(0);
        assertEquals(2, r2vals.length);
        assertEquals(2, r2vals[0]);
        assertEquals(12, r2vals[1]);

        assertFalse(it.hasNext());
    }

    @Test
    public void concatNulls() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{12}});
        SimpleRecord r24 = new SimpleRecord(3, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{33}});


        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11, null});
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d2, d3, Interval.DAY, 1, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r22, null, r24});
        MergeIterator it = new MergeIterator(new Reader[]{reader1, reader2}, Integer.MIN_VALUE, 2, 0, 4,
                new String[]{"field"}, new String[]{});

        assertTrue(it.hasNext());
        Record r1 = it.next();
        assertEquals(0, r1.getId());
        int[] r1vals = r1.getValues(0);
        assertEquals(2, r1vals.length);
        assertEquals(1, r1vals[0]);
        assertEquals(Integer.MIN_VALUE, r1vals[1]);

        assertTrue(it.hasNext());
        Record r2 = it.next();
        assertEquals(1, r2.getId());
        int[] r2vals = r2.getValues(0);
        assertEquals(2, r2vals.length);
        assertEquals(Integer.MIN_VALUE, r2vals[0]);
        assertEquals(12, r2vals[1]);

        assertTrue(it.hasNext());
        Record r4 = it.next();
        assertEquals(3, r4.getId());
    }


    @Test
    public void concatNullsBigGap() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});
        SimpleRecord r25 = new SimpleRecord(4, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{33}});


        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11});
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d2, d3, Interval.DAY, 4, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r25});
        MergeIterator it = new MergeIterator(new Reader[]{reader1, reader2}, Integer.MIN_VALUE, 2, 0, 5,
                new String[]{"field"}, new String[]{});

        assertTrue(it.hasNext());
        Record r1 = it.next();
        assertEquals(0, r1.getId());
        int[] r1vals = r1.getValues(0);
        assertEquals(2, r1vals.length);
        assertEquals(1, r1vals[0]);
        assertEquals(Integer.MIN_VALUE, r1vals[1]);

        assertTrue(it.hasNext());
        Record r2 = it.next();
        assertEquals(4, r2.getId());
        int[] r2vals = r2.getValues(0);
        assertEquals(2, r2vals.length);
        assertEquals(Integer.MIN_VALUE, r2vals[0]);
        assertEquals(33, r2vals[1]);

        assertFalse(it.hasNext());
    }

    @Test
    public void precedence1() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});
        SimpleRecord r12 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{2}});
        SimpleRecord r21 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{11}});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{12}});

        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11, r12});
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r21, r22});
        MergeIterator it = new MergeIterator(new Reader[]{reader1, reader2}, Integer.MIN_VALUE, 1, 0, 2,
                new String[]{"field"}, new String[]{});

        assertTrue(it.hasNext());
        Record r1 = it.next();
        assertEquals(0, r1.getId());
        int[] r1vals = r1.getValues(0);
        assertEquals(1, r1vals.length);
        assertEquals(11, r1vals[0]);

        assertTrue(it.hasNext());
        Record r2 = it.next();
        assertEquals(1, r2.getId());
        int[] r2vals = r2.getValues(0);
        assertEquals(1, r2vals.length);
        assertEquals(12, r2vals[0]);

        assertFalse(it.hasNext());
    }

    @Test
    public void precedence2() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});
        SimpleRecord r12 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{2}});
        SimpleRecord r21 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{11}});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{12}});

        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11, r12});
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r21, r22});
        MergeIterator it = new MergeIterator(new Reader[]{reader2, reader1}, Integer.MIN_VALUE, 1, 0, 2,
                new String[]{"field"}, new String[]{});

        assertTrue(it.hasNext());
        Record r1 = it.next();
        assertEquals(0, r1.getId());
        int[] r1vals = r1.getValues(0);
        assertEquals(1, r1vals.length);
        assertEquals(1, r1vals[0]);

        assertTrue(it.hasNext());
        Record r2 = it.next();
        assertEquals(1, r2.getId());
        int[] r2vals = r2.getValues(0);
        assertEquals(1, r2vals.length);
        assertEquals(2, r2vals[0]);

        assertFalse(it.hasNext());
    }

    @Test
    public void averageEncoder() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1, 1}});
        SimpleReader r1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11});
        MergeIterator it = new MergeIterator(new Reader[]{r1}, Integer.MIN_VALUE, 2, 0, 1, new String[]{"field"}, new String[]{"field"});
        assertTrue(it.hasNext());
        Record r = it.next();
        assertEquals(r11.getId(), r.getId());
        int[] values = r.getValues(0);
        assertEquals(2, values.length);
        assertEquals(1, values[0]);
        assertEquals(1, values[1]);
        assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void iterateOffEnd() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1, 1}});
        SimpleReader r1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11});
        MergeIterator it = new MergeIterator(new Reader[]{r1}, Integer.MIN_VALUE, 2, 0, 1, new String[]{"field"}, new String[]{"field"});
        assertTrue(it.hasNext());
        Record r = it.next();
        assertFalse(it.hasNext());
        it.next();
    }

    @Test
    public void maybeAverageEncoder() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{12345, 1234567}});
        SimpleReader r1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11});
        MergeIterator it = new MergeIterator(new Reader[]{r1}, Integer.MIN_VALUE, 2, 0, 1, new String[]{"field"}, new String[]{"field"});
        assert (it.hasNext());
        Record r = it.next();
        assertEquals(r11.getId(), r.getId());
        int[] values = r.getValues(0);
        assertEquals(2, values.length);
        assertEquals(12345, values[0]);
        assertEquals(1234567, values[1]);
        assertFalse(it.hasNext());
    }

    @Test
    public void mergeTwoFields() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});
        SimpleRecord r12 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{2}});
        SimpleRecord r21 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{11}});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{12}});

        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field1"}, new FieldType[]{FieldType.INT}, new Record[]{r11, r12});
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field2"}, new FieldType[]{FieldType.INT}, new Record[]{r21, r22});
        MergeIterator it = new MergeIterator(new Reader[]{reader1, reader2}, Integer.MIN_VALUE, 1, 0, 2,
                new String[]{"field1", "field2"}, new String[]{});

        assertTrue(it.hasNext());
        Record r1 = it.next();
        assertEquals(0, r1.getId());
        int[] r1f1vals = r1.getValues(0);
        assertEquals(1, r1f1vals.length);
        assertEquals(1, r1f1vals[0]);
        int[] r1f2vals = r1.getValues(1);
        assertEquals(1, r1f2vals.length);
        assertEquals(11, r1f2vals[0]);

        assertTrue(it.hasNext());
        Record r2 = it.next();
        assertEquals(1, r2.getId());
        int[] r2f1vals = r2.getValues(0);
        assertEquals(1, r2f1vals.length);
        assertEquals(2, r2f1vals[0]);
        int[] r2f2vals = r2.getValues(1);
        assertEquals(1, r2f2vals.length);
        assertEquals(12, r2f2vals[0]);

        assertFalse(it.hasNext());
    }

    @Test
    public void mergeTwoFieldsOrderDeclaration() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});
        SimpleRecord r12 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{2}});
        SimpleRecord r21 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{11}});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{12}});

        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field1"}, new FieldType[]{FieldType.INT}, new Record[]{r11, r12});
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field2"}, new FieldType[]{FieldType.INT}, new Record[]{r21, r22});
        MergeIterator it = new MergeIterator(new Reader[]{reader1, reader2}, Integer.MIN_VALUE, 1, 0, 2,
                new String[]{"field2", "field1"}, new String[]{});

        assertTrue(it.hasNext());
        Record r1 = it.next();
        assertEquals(0, r1.getId());
        int[] r1f1vals = r1.getValues(0);
        assertEquals(1, r1f1vals.length);
        assertEquals(11, r1f1vals[0]);
        int[] r1f2vals = r1.getValues(1);
        assertEquals(1, r1f2vals.length);
        assertEquals(1, r1f2vals[0]);

        assertTrue(it.hasNext());
        Record r2 = it.next();
        assertEquals(1, r2.getId());
        int[] r2f1vals = r2.getValues(0);
        assertEquals(1, r2f1vals.length);
        assertEquals(12, r2f1vals[0]);
        int[] r2f2vals = r2.getValues(1);
        assertEquals(1, r2f2vals.length);
        assertEquals(2, r2f2vals[0]);

        assertFalse(it.hasNext());
    }

    @Test(expected = RuntimeException.class)
    public void flakyHasNext() {
        Record r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});
        SimpleReader r1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11}) {
            @Override
            public Record get(int i) throws Exception {
                Exception e = new Exception();
                StackTraceElement[] stes = e.getStackTrace();
                for (StackTraceElement ste : stes) {
                    if (ste.getMethodName() == "hasNext")
                        throw new IllegalAccessException("simulating unexpected exception in hasNext");
                }
                return super.get(i);
            }
        };
        MergeIterator it = new MergeIterator(new Reader[]{r1}, Integer.MIN_VALUE, 2, 0, 1, new String[]{"field"}, new String[]{"field"});
        assertTrue(it.hasNext());
        Record r = it.next();
        assertFalse(it.hasNext());
    }

    @Test(expected = RuntimeException.class)
    public void flakyNext() {
        Record r11 = new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});
        SimpleReader r1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{r11}) {
            @Override
            public Record get(int i) throws Exception {
                Exception e = new Exception();
                StackTraceElement[] stes = e.getStackTrace();
                for (StackTraceElement ste : stes) {
                    if (ste.getMethodName() == "next")
                        throw new IllegalAccessException("simulating unexpected exception in next");
                }
                return super.get(i);
            }
        };
        MergeIterator it = new MergeIterator(new Reader[]{r1}, Integer.MIN_VALUE, 2, 0, 1, new String[]{"field"}, new String[]{"field"});
        assertTrue(it.hasNext());
        Record r = it.next();
        assertFalse(it.hasNext());
    }
}