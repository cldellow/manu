package com.cldellow.manu.cli;

import com.cldellow.manu.format.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.*;

public class MergeIteratorTest {
    final DateTime d1 = new DateTime(2012, 1, 1, 0, 0, 0, DateTimeZone.UTC);
    final DateTime d2 = d1.plusDays(1);
    final DateTime d3 = d1.plusDays(2);

    @Test
    public void concat() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 1 }});
        SimpleRecord r12 = new SimpleRecord(0, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 2 }});
        SimpleRecord r21 = new SimpleRecord(1, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 11 }});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 12 }});

        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[] { "field"}, new FieldType[] { FieldType.INT }, new Record[] { r11, r12 });
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d2, d3, Interval.DAY, 0, new String[] { "field"}, new FieldType[] { FieldType.INT }, new Record[] { r21, r22 });
        MergeIterator it = new MergeIterator(new Reader[] { reader1, reader2 }, Integer.MIN_VALUE, 2, 0, 2,
                new String[] { "field"}, new String[] {});

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
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 1 }});
        SimpleRecord r12 = new SimpleRecord(0, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 2 }});
        SimpleRecord r21 = new SimpleRecord(1, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 11 }});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 12 }});

        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[] { "field"}, new FieldType[] { FieldType.INT }, new Record[] { r11, r12 });
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d2, d3, Interval.DAY, 0, new String[] { "field"}, new FieldType[] { FieldType.INT }, new Record[] { r21, r22 });
        MergeIterator it = new MergeIterator(new Reader[] { reader2, reader1 }, Integer.MIN_VALUE, 2, 0, 2,
                new String[] { "field"}, new String[] {});

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
    public void precedence1() {
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 1 }});
        SimpleRecord r12 = new SimpleRecord(0, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 2 }});
        SimpleRecord r21 = new SimpleRecord(1, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 11 }});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 12 }});

        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[] { "field"}, new FieldType[] { FieldType.INT }, new Record[] { r11, r12 });
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[] { "field"}, new FieldType[] { FieldType.INT }, new Record[] { r21, r22 });
        MergeIterator it = new MergeIterator(new Reader[] { reader1, reader2 }, Integer.MIN_VALUE, 1, 0, 2,
                new String[] { "field"}, new String[] {});

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
        SimpleRecord r11 = new SimpleRecord(0, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 1 }});
        SimpleRecord r12 = new SimpleRecord(0, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 2 }});
        SimpleRecord r21 = new SimpleRecord(1, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 11 }});
        SimpleRecord r22 = new SimpleRecord(1, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] { 12 }});

        SimpleReader reader1 = new SimpleReader("r1", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[] { "field"}, new FieldType[] { FieldType.INT }, new Record[] { r11, r12 });
        SimpleReader reader2 = new SimpleReader("r2", Integer.MIN_VALUE, d1, d2, Interval.DAY, 0, new String[] { "field"}, new FieldType[] { FieldType.INT }, new Record[] { r21, r22 });
        MergeIterator it = new MergeIterator(new Reader[] { reader2, reader1 }, Integer.MIN_VALUE, 1, 0, 2,
                new String[] { "field"}, new String[] {});

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
}