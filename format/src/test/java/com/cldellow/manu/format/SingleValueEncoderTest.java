package com.cldellow.manu.format;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import me.lemire.integercompression.IntWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitQuickcheck.class)
public class SingleValueEncoderTest {
    @Test
    public void testId() {
        assertEquals(3, new SingleValueEncoder().getId());
    }

    @Test
    public void isVariableLength() {
        assertTrue(new SingleValueEncoder().isVariableLength());
    }

    @Test
    public void getLength() {
        assertEquals(-1L, new SingleValueEncoder().getLength());
    }

    @Test
    public void encodeSimple() throws Exception {
        FieldEncoder encoder = new SingleValueEncoder();
        {
            int[] b = new int[]{1};
            IntWrapper len = new IntWrapper(0);
            int[] actual = EncoderTools.roundtrip(b, encoder, len);
            assertEquals(1, actual.length);
            assertEquals(1, EncoderTools.roundtripSize(b, encoder));
            assertEquals(b[0], actual[0]);
        }

        {
            int[] s = new int[]{234};
            IntWrapper len = new IntWrapper(0);
            int[] actual = EncoderTools.roundtrip(s, encoder, len);
            assertEquals(1, actual.length);
            assertEquals(2, EncoderTools.roundtripSize(s, encoder));
            assertEquals(s[0], actual[0]);
        }


        {
            int[] i = new int[]{78123};
            IntWrapper len = new IntWrapper(0);
            int[] actual = EncoderTools.roundtrip(i, encoder, len);
            assertEquals(1, actual.length);
            assertEquals(4, EncoderTools.roundtripSize(i, encoder));
            assertEquals(i[0], actual[0]);

        }
    }

    @Property
    public void encode(int i) throws Exception {
        FieldEncoder encoder = new SingleValueEncoder();
        int[] ints = new int[]{i};
        IntWrapper len = new IntWrapper(0);
        int[] actual = EncoderTools.roundtrip(ints, encoder, len);
        assertEquals(1, actual.length);
        assertEquals(i, actual[0]);
    }

    @Test
    public void eligible() throws Exception {
        assertTrue(SingleValueEncoder.eligible(new int[] { 1}));
        assertFalse(SingleValueEncoder.eligible(new int[] { }));
        assertFalse(SingleValueEncoder.eligible(new int[] { 1,2}));
    }
}