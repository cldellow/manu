package com.cldellow.manu.format;

import com.cldellow.manu.common.Common;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static junit.framework.TestCase.*;

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
        Reader r = new Reader(dbLoc);
        return r.records;
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
        new Reader(new Common().getFile("not-a-manu"));
    }

    @Test(expected = NotManuException.class)
    public void testNotManu2() throws Exception {
        new Reader(new Common().getFile("not-a-manu2"));
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
                    Arrays.asList(inRecords).iterator());

            Reader r = new Reader(dbLoc);
            Record[] outRecords = new Record[5]; //r.records.next(), r.records.next(), r.records.next()};

            for (int j = 0; j < 5; j++) {
                assertTrue(r.records.hasNext());
                outRecords[j] = r.records.next();
            }
            assertFalse(r.records.hasNext());

            for (int j = 0; j < 5; j++) {
                if (inRecords[j] == null)
                    assertNull(outRecords[j]);
                else {
                    assertNotNull(outRecords[j]);
                    assertEquals(j, outRecords[j].getId());
                    int[] values = outRecords[j].getValues(0);
                    assertNotNull(values);
                    for(int k = 0; k < values.length; k++)
                        assertEquals(inRecords[j].getValues(0)[k], values[k]);
                }
            }

            cleanup();
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
                    Arrays.asList(inRecords).iterator());

            Reader r = new Reader(dbLoc);
            Record[] outRecords = new Record[5]; //r.records.next(), r.records.next(), r.records.next()};

            for (int j = 0; j < 5; j++) {
                outRecords[j] = r.get(j);
            }

            for (int j = 0; j < 5; j++) {
                if (inRecords[j] == null)
                    assertNull(outRecords[j]);
                else {
                    assertNotNull(outRecords[j]);
                    assertEquals(j, outRecords[j].getId());
                    int[] values = outRecords[j].getValues(0);
                    assertNotNull(values);
                    for(int k = 0; k < values.length; k++)
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
            for (int j = 0; j < numFields.length; j++)
                parameterizedTest(numRecords[i], numFields[j]);
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
            // TODO: test pfor encoder
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

        Reader reader = new Reader(dbLoc);
        assertEquals(16, reader.rowListSize);
        assertEquals((long) epochMs, reader.epochMs);
        assertEquals(Integer.MIN_VALUE, reader.nullValue);
        assertEquals(numDatapoints, reader.numDatapoints);
        assertEquals(interval, reader.interval);
        assertEquals(recordOffset, reader.recordOffset);
        assertEquals(numRecords, reader.numRecords);
        assertEquals(numFields, reader.numFields);
        for (int i = 0; i < numFields; i++) {
            assertEquals(fieldNames[i], reader.fieldNames[i]);
            assertEquals(fieldTypes[i], reader.fieldTypes[i]);
        }

        Reader.RecordIterator records = reader.records;
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
