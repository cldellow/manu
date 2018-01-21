package com.cldellow.manu.serve;

import com.cldellow.manu.common.Common;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;


import java.io.File;
import java.util.Map;

public class CollectionsTest {
    @Test
    public void testCtor() {
        new Collections();
    }

    @Test
    public void discover() throws Exception{
        String datadir = new Common().getFile("datadir");

        Map<String, Collection> rv = Collections.discover(datadir);
        assertEquals(4, rv.size());
        assertTrue(rv.containsKey("hourly"));
        assertTrue(rv.containsKey("hourly2"));
        assertTrue(rv.containsKey("daily"));
    }

    @Test
    public void discoverEmpty() throws Exception{
        Map<String, Collection> rv = Collections.discover("/tmp/nonexistent");
        assertEquals(0, rv.size());
    }

}