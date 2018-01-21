package com.cldellow.manu.format;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import me.lemire.integercompression.IntWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitQuickcheck.class)
public class PFOREncoderTest {

    @Test
    public void id() {
        assertEquals(1, new PFOREncoder().getId());
    }


    @Test
    public void isVariableLength() {
        assertTrue(new PFOREncoder().isVariableLength());
    }

    @Test
    public void getLength() {
        assertEquals(-1L, new PFOREncoder().getLength());
    }

    @Test
    public void encodeIsSmallerUsually() throws Exception {
        FieldEncoder encoder = new PFOREncoder();

        int[] bytes = new int[new Random().nextInt(16384)];
        EncoderTools.roundtripSize(bytes, encoder);

        int[] compressible = new int[256];
        int rv = EncoderTools.roundtripSize(compressible, encoder);
        assertTrue(rv < compressible.length);
    }

    @Property
    public void encode(int[] ints) throws Exception {
        FieldEncoder encoder = new PFOREncoder();
        IntWrapper len = new IntWrapper(0);
        int[] actual = EncoderTools.roundtrip(ints, encoder, len);
        assertEquals(ints.length, actual.length);
        for(int i = 0; i < ints.length; i++)
            assertEquals(ints[i], actual[i]);
    }

}