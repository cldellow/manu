package com.cldellow.manu.cli;

import org.junit.Test;

import java.util.Iterator;
import com.cldellow.manu.common.Common;

import static org.junit.Assert.*;

public class FileParserTest {
    @Test
    public void testNumFields() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("simple.tsv"));
        assertEquals(4, fp.getNumFields());
        fp.close();
    }

    @Test
    public void testParseRows() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("simple.tsv"));
        assertEquals(4, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());

        {
            FileParser.RowIterator.Row row = it.next();
            assertEquals("foo", row.getKey());

            int[] data = row.getInts();
            assertEquals(fp.getNumFields() - 1, data.length);
            assertEquals(1, data[0]);
            assertEquals(2, data[1]);
            assertEquals(3, data[2]);
        }

        assertTrue(it.hasNext());
        {
            FileParser.RowIterator.Row row = it.next();
            assertEquals("bar", row.getKey());

            int[] data = row.getInts();
            assertEquals(fp.getNumFields() - 1, data.length);
            assertEquals(2, data[0]);
            assertEquals(3, data[1]);
            assertEquals(4, data[2]);
        }

        assertTrue(it.hasNext());
        {
            FileParser.RowIterator.Row row = it.next();
            assertEquals("baz", row.getKey());

            int[] data = row.getInts();
            assertEquals(fp.getNumFields() - 1, data.length);
            assertEquals(3, data[0]);
            assertEquals(4, data[1]);
            assertEquals(5, data[2]);
        }
        assertFalse(it.hasNext());

    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLine() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("nonewline.tsv"));
        System.out.println(fp.getNumFields());
        fp.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLine2() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("nonewline2.tsv"));
        assertEquals(2, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());
        FileParser.RowIterator.Row row = it.next();
        assertTrue(it.hasNext());
        row = it.next();

        fp.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLine3() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("non-numeric.tsv"));
        assertEquals(2, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());
        FileParser.RowIterator.Row row = it.next();
        assertTrue(it.hasNext());
        fp.close();
    }

    @Test
    public void testOnlyOneColumn() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("only-one-column.tsv"));
        assertEquals(1, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());
        FileParser.RowIterator.Row row = it.next();
        assertFalse(it.hasNext());
        fp.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseRowsRaggedTooLong() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("ragged.tsv"));
        assertEquals(4, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseRowsRaggedTooLong2() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("ragged2.tsv"));
        assertEquals(4, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testParseRowsRaggedTooLong3() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("ragged3.tsv"));
        assertEquals(5, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertTrue(it.hasNext());
        it.next();
        assertFalse(it.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCannotRemove() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("ragged3.tsv"));
        assertEquals(5, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        it.remove();
    }
}
