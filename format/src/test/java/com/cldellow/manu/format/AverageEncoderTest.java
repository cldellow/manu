package com.cldellow.manu.format;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.generator.Size;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import me.lemire.integercompression.IntWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.beans.Encoder;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;


@RunWith(JUnitQuickcheck.class)
public class AverageEncoderTest {
    @Test
    public void testId() {
        assertEquals(2, new AverageEncoder().getId());
    }

    @Test
    public void bigNumbersNotEligible() {
        assertFalse(AverageEncoder.eligible(new int[] { 1, 1, 2, 3, 4, 5, 6, 100}));
    }

    @Test
    public void uniformNumbersOk() {
        assertTrue(AverageEncoder.eligible(new int[] { 3, 2, 1, 3, 4, 5, 0, 23}));
    }

    @Test
    public void uniformBigNumbersNotOk() {
        assertFalse(AverageEncoder.eligible(new int[] { 20, 20, 20 }));
    }

    @Test
    public void emptyOk() { assertTrue(AverageEncoder.eligible(new int[] {})); }

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
        assertEquals(4L, new AverageEncoder().getLength());
    }

    private void roundtrip(int[] data, boolean isExact) throws Exception {
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

        if(isExact) {
            for(int i = 0; i< data.length; i++)
                assertEquals(data[i], newData[i]);
        }
    }

    @Test
    public void encodeDense() throws Exception {
        roundtrip(new int[] {1, 2, 3, 1, 2, 3}, false);
    }

    @Test
    public void encodeSparse() throws Exception {
        roundtrip(new int[] {0, 0, 0, 1, 2, 1, 0}, false);
    }

    @Test
    public void encodeLotsOfError() throws Exception {
        roundtrip(new int[] {2, 2, 9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, false);
    }

    @Test
    public void encodeEmpty() throws Exception {
        roundtrip(new int[] {}, false);
    }

    @Test
    public void encodeExact() throws Exception {
        roundtrip(new int[] {1, 1, 1}, true);
    }

    @Property
    public void encodeRandom(@InRange(min="-1", max="20") int[] ints) throws Exception {
        if(AverageEncoder.eligible(ints)) {
            roundtrip(ints, false);
        }
    }
}