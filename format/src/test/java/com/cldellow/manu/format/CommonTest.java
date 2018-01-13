package com.cldellow.manu.format;

import org.junit.Test;

import static org.junit.Assert.*;

public class CommonTest {

    @Test(expected=IllegalArgumentException.class)
    public void testGetEncoder() {
        Common.getEncoder(123);
    }

    @Test
    public void testGetKnownEncoders() {
        assertTrue(Common.getEncoder(0) instanceof CopyEncoder);
        assertTrue(Common.getEncoder(1) instanceof PFOREncoder);
        assertTrue(Common.getEncoder(2) instanceof AverageEncoder);
    }

    @Test
    public void testCtor() {
        new Common();
    }
}