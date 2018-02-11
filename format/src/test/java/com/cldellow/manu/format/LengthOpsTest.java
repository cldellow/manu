package com.cldellow.manu.format;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

@RunWith(JUnitQuickcheck.class)
public class LengthOpsTest {
    @Test
    public void ctor() {
        new LengthOps();
    }

    @Test
    public void decodeId() {
        byte id = 12;
        assertEquals(id, LengthOps.decodeId(LengthOps.encode(id, 1)));
    }

    @Property
    public void encodeTooBigId(byte id) {
        assumeThat((int) id, anyOf(lessThan(0), greaterThan(63)));

        boolean threw = false;

        try {
            LengthOps.encode(id, 0);
        } catch (Exception e) {
            threw = true;
        }

        assertTrue(threw);
    }

    @Property
    public void encodeTooSmallLen(@InRange(max = "-1") int len) {
        boolean threw = false;

        try {
            LengthOps.encode((byte) 0, len);
        } catch (Exception e) {
            threw = true;
        }

        assertTrue(threw);
    }


    @Property
    public void decodeIdAnyLen(@InRange(min = "0", max = "63") int id, @InRange(min = "0") int len) {
        byte encoded = LengthOps.encode((byte) id, len);
        assertEquals(id, LengthOps.decodeId(encoded));

        // verify the len can fit in the number of bytes allocated
        int lengthSize = LengthOps.decodeLengthSize(encoded);
        int newLen = len;

        assertThat(lengthSize, anyOf(is(1), is(2), is(4)));
        if (lengthSize == 1)
            newLen = (byte) newLen;
        if (lengthSize == 2)
            newLen = (short) newLen;

        assertEquals(len, newLen);
    }
}