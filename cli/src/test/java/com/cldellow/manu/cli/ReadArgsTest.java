package com.cldellow.manu.cli;

import com.cldellow.manu.format.IndexAccessMode;
import org.junit.Test;

import static org.junit.Assert.*;
import com.cldellow.manu.common.NotEnoughArgsException;

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
        assertEquals(IndexAccessMode.READ_ONLY, ra.indexAccessMode);
        assertFalse(ra.filterKeys());
    }

    @Test
    public void testArgsComplex() throws Exception {
        ReadArgs ra = new ReadArgs(new String[] { "index", "input", "--id", "-i", "123", "-n", "name", "-r", "regex", "field1", "--write"});
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
        assertEquals(IndexAccessMode.READ_WRITE_SAFE, ra.indexAccessMode);
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

    @Test
    public void testFilterKeys() throws Exception {
        ReadArgs ra = new ReadArgs(new String[] {"a", "b", "-n", "name"});
        assertTrue(ra.filterKeys());

        ra = new ReadArgs(new String[] {"a", "b", "-i", "0"});
        assertTrue(ra.filterKeys());

        ra = new ReadArgs(new String[] {"a", "b", "-r", "name"});
        assertTrue(ra.filterKeys());
    }

    @Test
    public void testPrintFields() throws Exception {
        ReadArgs ra = new ReadArgs(new String[] {"a", "b"});
        boolean[] rv = ra.printFields(new String[] {"field1", "field2"});
        assertEquals(2, rv.length);
        assertTrue(rv[0]);
        assertTrue(rv[1]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintFieldsUnknown() throws Exception {
        ReadArgs ra = new ReadArgs(new String[] {"a", "b", "field3"});
        boolean[] rv = ra.printFields(new String[] {"field1", "field2"});
    }

    @Test
    public void testPrintFieldsSome() throws Exception {
        ReadArgs ra = new ReadArgs(new String[] {"a", "b", "field1"});
        boolean[] rv = ra.printFields(new String[] {"field1", "field2"});
        assertEquals(2, rv.length);
        assertTrue(rv[0]);
        assertFalse(rv[1]);
    }
}
