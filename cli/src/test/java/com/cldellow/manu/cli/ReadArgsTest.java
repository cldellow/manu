package com.cldellow.manu.cli;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReadArgsTest {
    @Test
    public void testArgsSimple() throws Exception {
        ReadArgs ra = new ReadArgs(new String[] { "index", "input"});
        assertEquals("index", ra.indexFile);
        assertEquals("input", ra.inputFile);
        assertEquals(KeyKind.KEY, ra.keyKind);
        assertEquals(0, ra.names.size());
        assertEquals(0, ra.patterns.size());
        assertEquals(0, ra.fields.size());
        assertEquals(0, ra.ids.size());
    }

    @Test
    public void testArgsComplex() throws Exception {
        ReadArgs ra = new ReadArgs(new String[] { "index", "input", "--id", "-i", "123", "-n", "name", "-r", "regex", "field1"});
        assertEquals("index", ra.indexFile);
        assertEquals("input", ra.inputFile);
        assertEquals(KeyKind.ID, ra.keyKind);
        assertEquals(1, ra.names.size());
        assertEquals("name", ra.names.get(0));
        assertEquals(1, ra.patterns.size());
        assertEquals("regex", ra.patterns.get(0).toString());
        assertEquals(1, ra.fields.size());
        assertEquals("field1", ra.fields.get(0));
        assertEquals(1, ra.ids.size());
        assertEquals(new Integer(123), ra.ids.get(0));
    }

    @Test
    public void testArgsLongComplex() throws Exception {
        ReadArgs ra = new ReadArgs(new String[] { "index", "input", "--id", "--key-id", "123", "--key-name", "name", "--key-regex", "regex", "field1"});
        assertEquals("index", ra.indexFile);
        assertEquals("input", ra.inputFile);
        assertEquals(KeyKind.ID, ra.keyKind);
        assertEquals(1, ra.names.size());
        assertEquals("name", ra.names.get(0));
        assertEquals(1, ra.patterns.size());
        assertEquals("regex", ra.patterns.get(0).toString());
        assertEquals(1, ra.fields.size());
        assertEquals("field1", ra.fields.get(0));
        assertEquals(1, ra.ids.size());
        assertEquals(new Integer(123), ra.ids.get(0));

    }

    @Test
    public void testArgsName() throws Exception {
        ReadArgs ra = new ReadArgs(new String[] { "keys", "pvs.out", "-n", "name"});
        assertEquals("keys", ra.indexFile);
        assertEquals("pvs.out", ra.inputFile);
        assertEquals(KeyKind.KEY, ra.keyKind);
        assertEquals(1, ra.names.size());
        assertEquals("name", ra.names.get(0));
        assertEquals(0, ra.patterns.size());
        assertEquals(0, ra.fields.size());
    }


    @Test(expected = NotEnoughArgsException.class)
    public void testArgsIncomplete() throws Exception {
        ReadArgs ra = new ReadArgs(new String[] { "index", "input", "--key-name"});
    }
}
