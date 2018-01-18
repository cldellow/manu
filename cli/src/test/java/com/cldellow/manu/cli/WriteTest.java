package com.cldellow.manu.cli;

import static org.junit.Assert.*;

import com.cldellow.manu.format.Interval;
import org.junit.Test;

public class WriteTest {
    @Test
    public void noArgs() throws Exception {
        int rv = new Write(new String[] {}).entrypoint();
        assertEquals(1, rv);
    }

    @Test
    public void help() throws Exception {
        int rv = new Write(new String[] {"--help"}).entrypoint();
        assertEquals(1, rv);
    }
}