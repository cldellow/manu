package com.cldellow.manu.serve;

import org.junit.Test;

import static org.junit.Assert.*;

public class ServerArgsTest {
    @Test
    public void defaults() throws Exception {
        ServerArgs sa = new ServerArgs(new String[]{});
        assertEquals(6268, sa.port);
        assertEquals("./datadir", sa.datadir);
    }

    @Test
    public void longForm() throws Exception {
        ServerArgs sa = new ServerArgs(new String[]{"--datadir", "foo", "--port", "123"});
        assertEquals(123, sa.port);
        assertEquals("foo", sa.datadir);
    }

    @Test
    public void shortForm() throws Exception {
        ServerArgs sa = new ServerArgs(new String[]{"-d", "foo", "-p", "123"});
        assertEquals(123, sa.port);
        assertEquals("foo", sa.datadir);
    }

    @Test(expected = IllegalArgumentException.class)
    public void incompletePort() throws Exception {
        ServerArgs sa = new ServerArgs(new String[]{"-p"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void incompleteDatadir() throws Exception {
        ServerArgs sa = new ServerArgs(new String[]{"-d"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknown() throws Exception {
        ServerArgs sa = new ServerArgs(new String[]{"xyz"});
    }

}