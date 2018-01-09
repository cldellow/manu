package com.cldellow.manu.format;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.*;

public class WriterTest {
    private String dbLoc = "/tmp/manu-test.data";

    @After
    public void cleanup() throws Exception {
        File f = new File(dbLoc);
        if(f.exists())
            f.delete();
    }

    @Test
    public void testSingleField() throws Exception {
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
                new SimpleRecord(encoders, new int[][] {datapoints}),
                new SimpleRecord(encoders, new int[][] {datapoints2})
        };

        Writer.write(
                dbLoc,
                epochMs,
                numDatapoints,
                interval,
                recordOffset,
                numRecords,
                fieldNames,
                fieldTypes,
                Arrays.asList(records).iterator());
    }

    @Test
    public void testTwoField() throws Exception {
        Long epochMs = System.currentTimeMillis();
        int numDatapoints = 10;
        Interval interval = Interval.DAY;
        int recordOffset = 123;
        int numRecords = 2;
        String[] fieldNames = {"int", "decimal"};
        FieldType[] fieldTypes = {FieldType.INT, FieldType.FIXED1};
        FieldEncoder[] encoders = {new CopyEncoder(), new CopyEncoder()};
        int[] datapoints = {1, 2, 3, 4, 5, 6, 7,8, 9, 10};
        int[] datapoints2 = {2, 3, 4, 5, 6, 7, 8,9, 10, 11};

        Record[] records = {
                new SimpleRecord(encoders, new int[][] {datapoints, datapoints2}),
                new SimpleRecord(encoders, new int[][] {datapoints2, datapoints})
        };

        Writer.write(
                dbLoc,
                epochMs,
                numDatapoints,
                interval,
                recordOffset,
                numRecords,
                fieldNames,
                fieldTypes,
                Arrays.asList(records).iterator());
    }

    @Test
    public void testLarge() throws Exception {
        Long epochMs = System.currentTimeMillis();
        int numDatapoints = 1000;
        Interval interval = Interval.DAY;
        int recordOffset = 0;
        int numRecords = 16385;
        String[] fieldNames = {"int"};
        FieldType[] fieldTypes = {FieldType.INT};
        FieldEncoder[] encoders = {new PFOREncoder()};
        int[] datapoints = new int[numDatapoints];
        Random r = new Random();
        System.out.println(System.currentTimeMillis());
        for(int i = 0; i < numDatapoints; i++)
            datapoints[i] = Math.abs(r.nextInt()) % 30;
        System.out.println(System.currentTimeMillis());
        Record[] records = new Record[numRecords];
        for(int i = 0; i < numRecords; i++)
            records[i] = new SimpleRecord(encoders, new int[][] {datapoints});
        System.out.println(System.currentTimeMillis());
        Writer.write(
                dbLoc,
                epochMs,
                numDatapoints,
                interval,
                recordOffset,
                numRecords,
                fieldNames,
                fieldTypes,
                Arrays.asList(records).iterator());
        System.out.println(System.currentTimeMillis());
        System.out.println(new File(dbLoc).length());
    }


    @Test(expected=IllegalArgumentException.class)
    public void testTooFewRows() throws Exception {
        Long epochMs = System.currentTimeMillis();
        int numDatapoints = 10;
        Interval interval = Interval.DAY;
        int recordOffset = 123;
        int numRecords = 2;
        String[] fieldNames = {"int"};
        FieldType[] fieldTypes = {FieldType.INT};
        FieldEncoder[] encoders = {new CopyEncoder()};
        int[] datapoints = {1, 2, 3, 4, 5, 6, 7,8, 9, 10};

        Record[] records = {
                new SimpleRecord(encoders, new int[][] {datapoints})
        };

        Writer.write(
                dbLoc,
                epochMs,
                numDatapoints,
                interval,
                recordOffset,
                numRecords,
                fieldNames,
                fieldTypes,
                Arrays.asList(records).iterator());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testTooManyRows() throws Exception {
        Long epochMs = System.currentTimeMillis();
        int numDatapoints = 10;
        Interval interval = Interval.DAY;
        int recordOffset = 123;
        int numRecords = 1;
        String[] fieldNames = {"int"};
        FieldType[] fieldTypes = {FieldType.INT};
        FieldEncoder[] encoders = {new CopyEncoder()};
        int[] datapoints = {1, 2, 3, 4, 5, 6, 7,8, 9, 10};

        Record[] records = {
                new SimpleRecord(encoders, new int[][] {datapoints}),
                new SimpleRecord(encoders, new int[][] {datapoints})
        };

        Writer.write(
                dbLoc,
                epochMs,
                numDatapoints,
                interval,
                recordOffset,
                numRecords,
                fieldNames,
                fieldTypes,
                Arrays.asList(records).iterator());
    }

    @Test(expected=IllegalArgumentException.class)
    public void testFieldMismatch() throws Exception {
        Long epochMs = System.currentTimeMillis();
        int numDatapoints = 10;
        Interval interval = Interval.DAY;
        int recordOffset = 123;
        int numRecords = 1;
        String[] fieldNames = {"int"};
        FieldType[] fieldTypes = {FieldType.INT, FieldType.INT};
        FieldEncoder[] encoders = {new CopyEncoder()};
        int[] datapoints = {1, 2, 3, 4, 5, 6, 7,8, 9, 10};

        Record[] records = {
                new SimpleRecord(encoders, new int[][] {datapoints}),
                new SimpleRecord(encoders, new int[][] {datapoints})
        };

        Writer.write(
                dbLoc,
                epochMs,
                numDatapoints,
                interval,
                recordOffset,
                numRecords,
                fieldNames,
                fieldTypes,
                Arrays.asList(records).iterator());
    }
}