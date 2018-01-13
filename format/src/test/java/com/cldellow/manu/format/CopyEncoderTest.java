package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CopyEncoderTest {

    @Test
    public void isVariableLength() {
        assertTrue(new CopyEncoder().isVariableLength());
    }


    @Test
    public void getLength() {
        assertEquals(-1L, new CopyEncoder().getLength());
    }

    @Test
    public void encode() throws Exception {
        FieldEncoder encoder = new CopyEncoder();

        int[] bytes = new int[new Random().nextInt(16384)];
        IntWrapper len = new IntWrapper(0);
        EncoderTools.roundtrip(bytes, encoder, len);
    }
}