package com.cldellow.manu.cli;


import org.junit.Test;

import static org.junit.Assert.*;

public class EnsureKeysTest {
    @org.junit.Test
    public void testCtor() {
        // hack because Codecov can't tell that the class is static,
        // and so it's OK if the ctor isn't invoked
        new EnsureKeys();
    }

    @Test
    public void noArgs() throws Exception {
        int rv = EnsureKeys.entrypoint(new String[] {});
        assertEquals(1, rv);
    }

    @Test
    public void help() throws Exception {
        int rv = EnsureKeys.entrypoint(new String[] {"--help"});
        assertEquals(1, rv);
    }
}