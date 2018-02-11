package com.cldellow.manu.common;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CommonTest {
    @Test
    public void testContainsYes() {
        assertTrue(Common.contains(new String[]{"foo", "bar"}, "bar"));
    }


    @Test
    public void testContainsNo() {
        assertFalse(Common.contains(new String[]{"foo", "bar"}, "baz"));
    }
}