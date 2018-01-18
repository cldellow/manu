package com.cldellow.manu.cli;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CliAppTest {
    @Test
    public void noArgs() throws Exception {
        int rv = new CliApp(new String[]{}).entrypoint();
        assertEquals(1, rv);
    }

    @Test
    public void help() throws Exception {
        int rv = new CliApp(new String[]{"--help"}).entrypoint();
        assertEquals(1, rv);
    }

}