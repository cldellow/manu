package com.cldellow.manu.cli;

import org.junit.Assert.*;

public class EnsureKeysTest {
    @org.junit.Test
    public void testCtor() {
        // hack because Codecov can't tell that the class is static,
        // and so it's OK if the ctor isn't invoked
        new EnsureKeys();
    }
}