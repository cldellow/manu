package com.cldellow.manu.serve;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ManuAppTest {
    @Test
    public void testHelp() throws Exception {
        int rv = ManuApp.entrypoint(new String[]{"--help"});
        assertEquals(1, rv);
    }

    @Test
    public void testNoCollections() throws Exception {
        int rv = ManuApp.entrypoint(new String[]{"--datadir", "/tmp/nonexistent"});
        assertEquals(2, rv);
    }

    @Test
    public void testCtor() {
        new ManuApp();
    }

}