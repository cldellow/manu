package com.cldellow.manu.format;

import org.junit.Test;

import static org.junit.Assert.*;

public class FieldTypeTest {
    @Test
    public void testRoundTrip() {
        assertEquals(0, FieldType.INT.getValue());
        assertEquals(FieldType.INT, FieldType.valueOf(0));
        assertEquals(1, FieldType.FIXED1.getValue());
        assertEquals(FieldType.FIXED1, FieldType.valueOf(1));
        assertEquals(2, FieldType.FIXED2.getValue());
        assertEquals(FieldType.FIXED2, FieldType.valueOf(2));

    }
}