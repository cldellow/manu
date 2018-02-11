package com.cldellow.manu.serve;

import com.cldellow.manu.common.Common;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CollectionsTest {
    @Test
    public void testCtor() {
        new Collections();
    }

    @Test
    public void discover() throws Exception {
        String datadir = new Common().getFile("datadir");

        Map<String, Collection> rv = Collections.discover(datadir);
        assertEquals(5, rv.size());
        assertTrue(rv.containsKey("hourly"));
        assertTrue(rv.containsKey("hourly2"));
        assertTrue(rv.containsKey("daily"));
        assertTrue(rv.containsKey("nulls"));
        assertTrue(rv.containsKey("sparse"));
    }

    @Test
    public void discoverEmpty() throws Exception {
        Map<String, Collection> rv = Collections.discover("/tmp/nonexistent");
        assertEquals(0, rv.size());
    }

}