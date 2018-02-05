package com.cldellow.manu.format;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleRecordTest {

    @Test
    public void getId() {
        FieldEncoder[] encoders = {new CopyEncoder()};
        int[][] datapoints = {{0, 1, 2}};
        Record r = new SimpleRecord(1, encoders, datapoints);
        assertEquals(1, r.getId());
    }

    @Test
    public void getValues() {
        FieldEncoder[] encoders = {new CopyEncoder()};
        int[][] datapoints = {{0, 1, 2}};
        Record r = new SimpleRecord(1, encoders, datapoints);
        assertEquals(0, r.getEncoder(0).getId());
    }

    @Test
    public void getEncoder() {
        FieldEncoder[] encoders = {new CopyEncoder()};
        int[][] datapoints = {{0, 1, 2}};
        Record r = new SimpleRecord(1, encoders, datapoints);
        int[] dp = r.getValues(0);
        assertEquals(0, dp[0]);
        assertEquals(1, dp[1]);
        assertEquals(2, dp[2]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void noFields() {
        new SimpleRecord(1, new FieldEncoder[]{}, new int[][]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void moreFieldsThanDataPoints() {
        new SimpleRecord(1, new FieldEncoder[]{new CopyEncoder(), new CopyEncoder()}, new int[][]{new int[] {0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeId() {
        new SimpleRecord(-1, new FieldEncoder[] { new CopyEncoder() }, new int[][] { new int[] {0}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void raggedData() {
        new SimpleRecord(1, new FieldEncoder[] { new CopyEncoder(), new CopyEncoder() }, new int[][] { new int[] {0}, new int[] {1,2}});
    }
}