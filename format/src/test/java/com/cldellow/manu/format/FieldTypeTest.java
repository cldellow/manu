package com.cldellow.manu.format;

import org.junit.Test;

import static org.junit.Assert.*;

public class FieldTypeTest {
    @Test
    public void testRoundTrip() {
        assertEquals(0, FieldType.INT.getValue());
        assertEquals(FieldType.INT, FieldType.valueOf(0));
        assertEquals(1, FieldType.FLOAT.getValue());
        assertEquals(FieldType.FLOAT, FieldType.valueOf(1));
    }
}