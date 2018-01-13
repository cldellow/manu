package com.cldellow.manu.format;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PFOREncoderTest {

    @Test
    public void isVariableLength() {
        assertTrue(new PFOREncoder().isVariableLength());
    }


    @Test
    public void getLength() {
        assertEquals(-1L, new PFOREncoder().getLength());
    }

    @Test
    public void encode() throws Exception {
        FieldEncoder encoder = new PFOREncoder();

        int[] bytes = new int[new Random().nextInt(16384)];
        EncoderTools.roundtripSize(bytes, encoder);

        int[] compressible = new int[256];
        int rv = EncoderTools.roundtripSize(compressible, encoder);
        assertTrue(rv < compressible.length);
    }
}