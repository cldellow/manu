package com.cldellow.manu.format;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.ValuesOf;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.NoSuchElementException;
import java.util.Vector;

import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class SimpleReaderTest {
    private final DateTime d1 = new DateTime(2012, 1, 1, 0, 0, 0, DateTimeZone.UTC);
    private final DateTime d2 = d1.plusDays(1);

    @Property
    public void reader(
            String fileName,
            int nullValue,
            int epochMs,
            @ValuesOf Interval interval,
            @InRange(min = "1", max = "1000")
                    int numDatapoints,
            @InRange(min = "0")
                    int recordOffset,
            @InRange(min = "1", max = "10")
                    int numFields) {
        DateTime from = interval.truncate(new DateTime(epochMs, DateTimeZone.UTC));
        DateTime to = interval.add(from, numDatapoints);

        String[] fieldNames = new String[numFields];
        FieldType[] fieldTypes = new FieldType[numFields];
        for (int i = 0; i < numFields; i++) {
            fieldNames[i] = "" + i;
            fieldTypes[i] = FieldType.valueOf(i % FieldType.values().length);
            ;
        }
        Reader sr = new SimpleReader(
                fileName,
                nullValue,
                from,
                to,
                interval,
                recordOffset,
                fieldNames,
                fieldTypes,
                new Record[]{}
        );

        assertEquals(fileName, sr.getFileName());
        assertEquals(nullValue, sr.getNullValue());
        assertEquals(from, sr.getFrom());
        assertEquals(to, sr.getTo());
        assertEquals(interval, sr.getInterval());
        assertEquals(recordOffset, sr.getRecordOffset());
        assertEquals(numFields, sr.getNumFields());
        for (int i = 0; i < numFields; i++) {
            assertEquals(fieldNames[i], sr.getFieldName(i));
            assertEquals(fieldTypes[i], sr.getFieldType(i));
        }
    }

    private Reader mkReader() {
        DateTime start = new DateTime(2012, 1, 1, 1, 0, 0, DateTimeZone.UTC);
        Reader sr = new SimpleReader(
                "foo",
                0,
                start,
                start.plusHours(3),
                Interval.HOUR,
                0,
                new String[]{"name"},
                new FieldType[]{FieldType.INT},
                new Record[]{
                        new SimpleRecord(0, new FieldEncoder[]{new CopyEncoder()},
                                new int[][]{new int[]{1, 2, 3}})});
        return sr;
    }

    @Test
    public void recordGets() throws Exception {
        Reader sr = mkReader();
        assertEquals(3, sr.getNumDatapoints());
        Record r = sr.get(0);
        assertNotNull(r);
        assertEquals(0, r.getId());
        int[] values = r.getValues(0);
        assertEquals(3, values.length);
        assertEquals(1, values[0]);
        assertEquals(2, values[1]);
        assertEquals(3, values[2]);
        assertEquals(FieldType.INT, sr.getFieldType(0));

        RecordIterator it = sr.getRecords();
        assertTrue(it.hasNext());
        Record r2 = it.next();
        assertNotNull(r2);
        assertEquals(0, r2.getId());
        assertFalse(it.hasNext());

        it.close();
    }

    @Test(expected = NoSuchElementException.class)
    public void badIdNegative() throws Exception {
        mkReader().get(-1);
    }

    @Test(expected = NoSuchElementException.class)
    public void badIdTooBig() throws Exception {
        mkReader().get(1000);
    }

    @Test(expected = NoSuchElementException.class)
    public void iterateOffEnd() throws Exception {
        Reader r = mkReader();
        RecordIterator it = r.getRecords();
        it.next();
        it.next();
    }

    @Property
    public void iterateWithNulls(@InRange(min = "0", max = "1023") int bits, @InRange(min = "0", max = "10") int numRecords) throws Exception {
        Record[] records = new Record[numRecords];
        Vector<Record> vector = new Vector<>();
        for (int i = 0; i < records.length; i++)
            if (((bits >> i) & 1) != 0) {
                records[i] = new SimpleRecord(i, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{i}});
                vector.add(records[i]);
            }

        Reader sr = new SimpleReader(
                "foo",
                0,
                d1,
                d2,
                Interval.DAY,
                0,
                new String[]{"foo"},
                new FieldType[]{FieldType.INT},
                records);

        for (int i = 0; i < numRecords; i++) {
            Record r = sr.get(i);
            if (records[i] == null)
                assertNull(r);
            else {
                assertNotNull(r);
                assertEquals(records[i].getId(), r.getId());
                assertEquals(1, r.getValues(0).length);
                assertEquals(i, r.getValues(0)[0]);
            }
        }

        RecordIterator it = sr.getRecords();
        int i = 0;
        while (i < vector.size()) {
            assertTrue(it.hasNext());
            Record r = it.next();
            assertNotNull(r);
            assertEquals(vector.get(i).getId(), r.getId());
            i++;
        }
        assertFalse(it.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFilename() {
        new SimpleReader(null, 0, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFrom() {
        new SimpleReader("foo", 0, null, d2, Interval.HOUR, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullTo() {
        new SimpleReader("foo", 0, d1, null, Interval.HOUR, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullInterval() {
        new SimpleReader("foo", 0, d1, d2, null, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFieldNames() {
        new SimpleReader("foo", 0, d1, d2, Interval.DAY, 0, null, new FieldType[]{FieldType.INT}, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFieldTypes() {
        new SimpleReader("foo", 0, d1, d2, Interval.DAY, 0, new String[]{"field"}, null, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullRecords() {
        new SimpleReader("foo", 0, d1, d2, Interval.DAY, 0, new String[]{"field"}, new FieldType[]{FieldType.INT}, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noFieldNames() {
        new SimpleReader("foo", 0, d1, d2, Interval.DAY, 0, new String[]{}, new FieldType[]{}, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void fieldNameTypeMismatch() {
        new SimpleReader("foo", 0, d1, d2, Interval.DAY, 0, new String[]{"foo", "bar"}, new FieldType[]{FieldType.INT}, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromAfterTo() {
        new SimpleReader("foo", 0, d2, d1, Interval.DAY, 0, new String[]{"foo"}, new FieldType[]{FieldType.INT}, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeRecordOffset() {
        new SimpleReader("foo", 0, d1, d2, Interval.DAY, -1, new String[]{"foo"}, new FieldType[]{FieldType.INT}, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFieldName() {
        new SimpleReader("foo", 0, d1, d2, Interval.DAY, 0, new String[]{null}, new FieldType[]{FieldType.INT}, new Record[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFieldType() {
        new SimpleReader("foo", 0, d1, d2, Interval.DAY, 0, new String[]{"foo"}, new FieldType[]{null}, new Record[]{});
    }

}