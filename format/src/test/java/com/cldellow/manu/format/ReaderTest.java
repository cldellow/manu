package com.cldellow.manu.format;

import com.cldellow.manu.common.Common;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.Size;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.cldellow.manu.format.Common.nonNullIterator;
import static junit.framework.TestCase.*;

@RunWith(JUnitQuickcheck.class)
public class ReaderTest {
    private String dbLoc = "/tmp/manu-test.data";

    @After
    public void cleanup() throws Exception {
        File f = new File(dbLoc);
        if (f.exists())
            f.delete();
    }

    private Iterator<Record> getIterator() throws Exception {
        Long epochMs = System.currentTimeMillis();
        int numDatapoints = 3;
        Interval interval = Interval.DAY;
        int recordOffset = 123;
        int numRecords = 2;
        String[] fieldNames = {"int"};
        FieldType[] fieldTypes = {FieldType.INT};
        FieldEncoder[] encoders = {new CopyEncoder()};
        int[] datapoints = {1, 1, 1};
        int[] datapoints2 = {123, 124, 125};

        Record[] records = {
                new SimpleRecord(0, new FieldEncoder[]{new AverageEncoder()}, new int[][]{datapoints}),
                new SimpleRecord(1, new FieldEncoder[]{new PFOREncoder()}, new int[][]{datapoints2})
        };

        Writer.write(
                dbLoc,
                epochMs,
                interval,
                fieldNames,
                fieldTypes,
                Arrays.asList(records).iterator());
        ManuReader r = new ManuReader(dbLoc);
        return r.getRecords();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorNextOnEmpty() throws Exception {
        Iterator<Record> records = getIterator();
        records.next();
        records.next();
        records.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() throws Exception {
        Iterator<Record> records = getIterator();
        records.remove();
    }

    @Test
    public void testMixed() throws Exception {
        Iterator<Record> records = getIterator();
        Record r = records.next();
        int[] data = r.getValues(0);
        assertEquals(3, data.length);
        assertEquals(1, data[0]);

        r = records.next();
        data = r.getValues(0);
        assertEquals(3, data.length);
        assertEquals(123, data[0]);
    }

    @Test(expected = NotManuException.class)
    public void testNotManu() throws Exception {
        new ManuReader(new Common().getFile("not-a-manu"));
    }

    @Test(expected = NotManuException.class)
    public void testNotManu2() throws Exception {
        new ManuReader(new Common().getFile("not-a-manu2"));
    }

    @Test
    public void testSparseRecordsIterator() throws Exception {
        SimpleRecord[] allRecords = new SimpleRecord[]{
                new SimpleRecord(0, new FieldEncoder[]{new PFOREncoder()}, new int[][]{{1, 2, 3, 4, 5}}),
                new SimpleRecord(1, new FieldEncoder[]{new PFOREncoder()}, new int[][]{{2, 3, 4, 6, 7}}),
                new SimpleRecord(2, new FieldEncoder[]{new PFOREncoder()}, new int[][]{{3, 4, 5, 7, 8}}),
                new SimpleRecord(3, new FieldEncoder[]{new PFOREncoder()}, new int[][]{{4, 5, 6, 8, 9}}),
                new SimpleRecord(4, new FieldEncoder[]{new PFOREncoder()}, new int[][]{{5, 6, 7, 9, 10}})};

        // NB: we skip i=0, as all sparse records w/o explicit numDataPoints is not supported
        for (int i = 1; i < 32; i++) {
            Record[] inRecords = new Record[5];
            for (int j = 0; j < 5; j++)
                if (((i >> j) & 1) != 0)
                    inRecords[j] = allRecords[j];

            Writer.write(dbLoc,
                    0L,
                    Interval.DAY,
                    new String[]{"field"},
                    new FieldType[]{FieldType.INT},
                    nonNullIterator(inRecords));

            ManuReader r = new ManuReader(dbLoc);
            Record[] outRecords = new Record[5]; //r.records.next(), r.records.next(), r.records.next()};

            RecordIterator it = r.getRecords();
            while (it.hasNext()) {
                Record rec = it.next();
                outRecords[rec.getId()] = rec;
            }
            it.close();

            for (int j = 0; j < 5; j++) {
                if (inRecords[j] == null)
                    assertNull(outRecords[j]);
                else {
                    assertNotNull(outRecords[j]);
                    assertEquals(j, outRecords[j].getId());
                    int[] values = outRecords[j].getValues(0);
                    assertNotNull(values);
                    for (int k = 0; k < values.length; k++)
                        assertEquals(inRecords[j].getValues(0)[k], values[k]);
                }
            }

            cleanup();
        }
    }

    @Property(trials = 5)
    public void testVariableSize512(
            int @Size(min = 512, max = 512) [] f1,
            int @Size(min = 512, max = 512) [] f2,
            int @Size(min = 512, max = 512) [] f3,
            int @Size(min = 512, max = 512) [] f4
    ) throws Exception {
        testVariableSize(f1, f2, f3, f4);
    }

    @Property(trials = 5)
    public void testVariableSize20K(
            int @Size(min = 20480, max = 20480) [] f1,
            int @Size(min = 20480, max = 20480) [] f2,
            int @Size(min = 20480, max = 20480) [] f3,
            int @Size(min = 20480, max = 20480) [] f4
    ) throws Exception {
        testVariableSize(f1, f2, f3, f4);
    }

    @Test
    public void testFixedSizeMultiple() throws Exception {
        testVariableSize(new int[]{0}, new int[]{1}, new int[]{2}, new int[]{3});
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetOutOfBounds() throws Exception {
        SimpleRecord r1 = new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder()}, new int[][]{new int[]{1}});

        Writer.write(
                dbLoc,
                0L,
                Interval.DAY,
                new String[]{"field1"},
                new FieldType[]{FieldType.INT},
                Arrays.asList(new Record[]{r1}).iterator());

        Reader reader = new ManuReader(dbLoc);
        reader.get(1024);
    }

    void testVariableSize(int[] f1, int[] f2, int[] f3, int[] f4) throws Exception {
        FieldEncoder encoder = new CopyEncoder();
        if (f1.length == 1)
            encoder = new AverageEncoder();
        int[][] values1 = new int[][]{f1, f2};
        SimpleRecord r1 = new SimpleRecord(1, new FieldEncoder[]{encoder, encoder}, values1);

        int[][] values2 = new int[][]{f3, f4};
        SimpleRecord r2 = new SimpleRecord(2, new FieldEncoder[]{encoder, encoder}, values2);

        Writer.write(
                dbLoc,
                0L,
                Interval.DAY,
                new String[]{"field1", "field2"},
                new FieldType[]{FieldType.INT, FieldType.INT},
                Arrays.asList(new Record[]{r1, r2}).iterator());

        Reader reader = new ManuReader(dbLoc);
        RecordIterator it = reader.getRecords();
        assertTrue(it.hasNext());
        Record r12 = it.next();
        assertEquals(f1.length, reader.getNumDatapoints());

        int[] f12 = r12.getValues(0);
        assertEquals(f1.length, f12.length);
        for (int i = 0; i < f12.length; i++)
            assertEquals(f1[i], f12[i]);

        int[] f22 = r12.getValues(1);
        assertEquals(f2.length, f22.length);
        for (int i = 0; i < f22.length; i++)
            assertEquals(f2[i], f22[i]);

        assertTrue(it.hasNext());
        Record r22 = it.next();
        assertFalse(it.hasNext());

        int[] f32 = r22.getValues(0);
        assertEquals(f3.length, f32.length);
        for (int i = 0; i < f32.length; i++)
            assertEquals(f3[i], f32[i]);

        int[] f42 = r22.getValues(1);
        assertEquals(f4.length, f42.length);
        for (int i = 0; i < f42.length; i++) {
            assertEquals(f4[i], f42[i]);
        }
    }

    @Test
    public void testSparseRecordsGet() throws Exception {
        SimpleRecord[] allRecords = new SimpleRecord[]{
                new SimpleRecord(0, new FieldEncoder[]{new PFOREncoder()}, new int[][]{{1, 2, 3, 4, 5}}),
                new SimpleRecord(1, new FieldEncoder[]{new PFOREncoder()}, new int[][]{{2, 3, 4, 6, 7}}),
                new SimpleRecord(2, new FieldEncoder[]{new PFOREncoder()}, new int[][]{{3, 4, 5, 7, 8}}),
                new SimpleRecord(3, new FieldEncoder[]{new PFOREncoder()}, new int[][]{{4, 5, 6, 8, 9}}),
                new SimpleRecord(4, new FieldEncoder[]{new PFOREncoder()}, new int[][]{{5, 6, 7, 9, 10}})};

        // NB: we skip i=0, as all sparse records is not supported
        for (int i = 1; i < 32; i++) {
            Record[] inRecords = new Record[5];
            for (int j = 0; j < 5; j++)
                if (((i >> j) & 1) != 0)
                    inRecords[j] = allRecords[j];

            Writer.write(dbLoc,
                    0L,
                    Interval.DAY,
                    new String[]{"field"},
                    new FieldType[]{FieldType.INT},
                    nonNullIterator(inRecords));

            ManuReader r = new ManuReader(dbLoc);
            Record[] outRecords = new Record[5]; //r.records.next(), r.records.next(), r.records.next()};

            for (int j = 0; j < 5; j++) {
                try {
                    outRecords[j] = r.get(j);
                } catch (NoSuchElementException nsee) {
                }
            }

            for (int j = 0; j < 5; j++) {
                if (inRecords[j] == null)
                    assertNull(outRecords[j]);
                else {
                    assertNotNull(outRecords[j]);
                    assertEquals(j, outRecords[j].getId());
                    int[] values = outRecords[j].getValues(0);
                    assertNotNull(values);
                    for (int k = 0; k < values.length; k++)
                        assertEquals(inRecords[j].getValues(0)[k], values[k]);
                }
            }

            cleanup();
        }
    }


    @Test
    public void testVarietyOfReads() throws Exception {
        int[] numRecords = {1, 15, 16, 17, 32, 33};
        int[] numFields = {1, 2};

        for (int i = 0; i < numRecords.length; i++)
            for (int j = 0; j < numFields.length; j++) {
                parameterizedTest(numRecords[i], numFields[j]);
            }
    }

    private void parameterizedTest(int numRecords, int numFields) throws Exception {
        Long epochMs = System.currentTimeMillis();
        int numDatapoints = 10;
        Interval interval = Interval.DAY;
        int recordOffset = 123;
        String[] fieldNames = new String[numFields];
        for (int i = 0; i < numFields; i++)
            fieldNames[i] = "field" + i;

        FieldType[] fieldTypes = new FieldType[numFields];
        for (int i = 0; i < numFields; i++)
            fieldTypes[i] = FieldType.INT;

        FieldEncoder[] encoders = new FieldEncoder[numFields];
        for (int i = 0; i < numFields; i++) {
            encoders[i] = new CopyEncoder();
            if (i == 1)
                encoders[i] = new PFOREncoder();
        }


        int[][][] datapoints = new int[numRecords][][];
        for (int i = 0; i < numRecords; i++) {
            datapoints[i] = new int[numFields][];
            for (int j = 0; j < numFields; j++) {
                datapoints[i][j] = new int[numDatapoints];
                for (int k = 0; k < numDatapoints; k++)
                    datapoints[i][j][k] = numRecords + i + j + k;
            }
        }

        Record[] inRecords = new Record[numRecords];
        for (int i = 0; i < numRecords; i++)
            inRecords[i] = new SimpleRecord(recordOffset + i, encoders, datapoints[i]);

        Writer.write(
                dbLoc,
                (short) 16,
                Integer.MIN_VALUE,
                epochMs,
                numDatapoints,
                interval,
                recordOffset,
                fieldNames,
                fieldTypes,
                Arrays.asList(inRecords).iterator());
        ManuReader reader = new ManuReader(dbLoc);
        assertEquals(16, reader.getRowListSize());
        assertEquals((long) epochMs, reader.getFrom().getMillis());
        assertEquals(Integer.MIN_VALUE, reader.getNullValue());
        assertEquals(numDatapoints, reader.getNumDatapoints());
        assertEquals(interval, reader.getInterval());
        assertEquals(recordOffset, reader.getRecordOffset());
        assertEquals(numRecords, reader.getNumRecords());
        assertEquals(numFields, reader.getNumFields());
        for (int i = 0; i < numFields; i++) {
            assertEquals(fieldNames[i], reader.getFieldName(i));
            assertEquals(fieldTypes[i], reader.getFieldType(i));
        }

        RecordIterator records = reader.getRecords();
        int recordIndex = recordOffset;
        while (records.hasNext()) {
            Record r = records.next();
            assertEquals(recordIndex, r.getId());
            for (int fieldIndex = 0; fieldIndex < numFields; fieldIndex++) {
                int[] vals = r.getValues(fieldIndex);
                for (int i = 0; i < vals.length; i++) {
                    assertEquals("recordIndex=" + recordIndex + ", fieldIndex=" + fieldIndex + ", i=" + i, datapoints[recordIndex - recordOffset][fieldIndex][i], vals[i]);
                }
            }
            recordIndex++;
        }
        assertEquals(numRecords, recordIndex - recordOffset);

        for (recordIndex = recordOffset; recordIndex < recordOffset + numRecords; recordIndex++) {
            Record r = reader.get(recordIndex);
            assertEquals(recordIndex, r.getId());
            for (int fieldIndex = 0; fieldIndex < numFields; fieldIndex++) {
                int[] vals = r.getValues(fieldIndex);
                for (int i = 0; i < vals.length; i++) {
                    assertEquals("recordIndex=" + recordIndex + ", fieldIndex=" + fieldIndex + ", i=" + i,
                            datapoints[recordIndex - recordOffset][fieldIndex][i],
                            vals[i]);
                }
            }
        }
        records.close();
    }
}
