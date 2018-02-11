package com.cldellow.manu.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArgHolderTest {
    @Test
    public void next() throws NotEnoughArgsException {
        ArgHolder arg = new ArgHolder(new String[]{"foo", "bar"});

        assertEquals("foo", arg.next());
        assertTrue(arg.hasNext());
        assertEquals("bar", arg.next());
        assertFalse(arg.hasNext());
    }

    @Test(expected = NotEnoughArgsException.class)
    public void nextWithoutEnough() throws NotEnoughArgsException {
        new ArgHolder(new String[]{}).next();
    }
}
