package com.cldellow.manu.format;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import me.lemire.integercompression.IntWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitQuickcheck.class)
public class CopyEncoderTest {
    @Test
    public void testId() {
        assertEquals(0, new CopyEncoder().getId());
    }

    @Test
    public void isVariableLength() {
        assertTrue(new CopyEncoder().isVariableLength());
    }


    @Test
    public void getLength() {
        assertEquals(-1L, new CopyEncoder().getLength());
    }

    @Property
    public void encode(int[] ints) throws Exception {
        FieldEncoder encoder = new CopyEncoder();
        IntWrapper len = new IntWrapper(0);
        int[] actual = EncoderTools.roundtrip(ints, encoder, len);
        assertEquals(ints.length, actual.length);
        for(int i = 0; i < ints.length; i++)
            assertEquals(ints[i], actual[i]);
    }
}