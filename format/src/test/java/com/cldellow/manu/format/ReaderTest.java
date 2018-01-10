package com.cldellow.manu.format;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static junit.framework.TestCase.assertEquals;

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
        int numDatapoints = 10;
        Interval interval = Interval.DAY;
        int recordOffset = 123;
        int numRecords = 2;
        String[] fieldNames = {"int"};
        FieldType[] fieldTypes = {FieldType.INT};
        FieldEncoder[] encoders = {new CopyEncoder()};
        int[] datapoints = {1, 2, 3, 4, 5, 6, 7,8, 9, 10};
        int[] datapoints2 = {2, 3, 4, 5, 6, 7, 8,9, 10, 11};

        Record[] records = {
                new SimpleRecord(0, encoders, new int[][] {datapoints}),
                new SimpleRecord(1, encoders, new int[][] {datapoints2})
        };

        Writer.write(
                dbLoc,
                (short)1024,
                epochMs,
                numDatapoints,
                interval,
                recordOffset,
                numRecords,
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
    public void testVarietyOfReads() throws Exception {
        int[] numRecords = {0, 15, 16, 17, 32, 33};
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
                epochMs,
                numDatapoints,
                interval,
                recordOffset,
                numRecords,
                fieldNames,
                fieldTypes,
                Arrays.asList(inRecords).iterator());

        Reader reader = new Reader(dbLoc);
        assertEquals(16, reader.rowListSize);
        assertEquals((long) epochMs, reader.epochMs);
        assertEquals(numDatapoints, reader.numDatapoints);
        assertEquals(interval, reader.interval);
        assertEquals(recordOffset, reader.recordOffset);
        assertEquals(numRecords, reader.numRecords);
        assertEquals(numFields, reader.numFields);
        for (int i = 0; i < numFields; i++) {
            assertEquals(fieldNames[i], reader.fieldNames[i]);
            assertEquals(fieldTypes[i], reader.fieldTypes[i]);
        }

        Iterator<Record> records = reader.records;
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

        for(recordIndex = recordOffset; recordIndex < recordOffset + numRecords; recordIndex++) {
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
        reader.close();
    }
}