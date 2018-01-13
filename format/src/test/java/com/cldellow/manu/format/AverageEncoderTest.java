package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;
import org.junit.Test;

import java.beans.Encoder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AverageEncoderTest {

    @Test
    public void bigNumbersNotEligible() {
        assertFalse(AverageEncoder.eligible(new int[] { 1, 1, 2, 3, 4, 5, 6, 100}));
    }

    @Test
    public void uniformNumbersOk() {
        assertTrue(AverageEncoder.eligible(new int[] { 3, 2, 1, 3, 4, 5, 0, 23}));
    }

    @Test
    public void negativeNumbersNotOk() {
        assertFalse(AverageEncoder.eligible(new int[] { -1, 2, 1, 3, 4, 5, 0, 23}));
    }


    @Test
    public void isVariableLength() {
        assertFalse(new AverageEncoder().isVariableLength());
    }

    @Test
    public void getLength() {
        assertEquals(-1L, new AverageEncoder().getLength());
    }

    private void roundtrip(int[] data) throws Exception {
        IntWrapper length = new IntWrapper(0);
        int[] newData = EncoderTools.roundtrip(data, new AverageEncoder(), length);

        int[] numPoints = new int[2];
        int[] sum = new int[2];

        for(int i = 0; i< data.length; i++) {
            if(data[i] > 0) {
                numPoints[0]++;
                sum[0] += data[i];
            }

            if(newData[i] > 0) {
                numPoints[1]++;
                sum[1] += newData[i];
            }
        }

        assertEquals((long)numPoints[0], numPoints[1]);
        assertEquals((long)sum[0], sum[1]);
    }

    @Test
    public void encodeDense() throws Exception {
        roundtrip(new int[] {1, 2, 3, 1, 2, 3});
    }

    @Test
    public void encodeSparse() throws Exception {
        roundtrip(new int[] {0, 0, 0, 1, 2, 1, 0});
    }
}