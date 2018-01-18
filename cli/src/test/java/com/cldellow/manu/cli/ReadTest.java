package com.cldellow.manu.cli;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReadTest {
    @Test
    public void testNoArgs() throws Exception {
        int rv = new Read(new String[] {}).entrypoint();
        assertEquals(1, rv);
    }

    @Test
    public void testHelp() throws Exception {
        int rv = new Read(new String[] {"--help"}).entrypoint();
        assertEquals(1, rv);
    }

}