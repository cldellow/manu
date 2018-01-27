package com.cldellow.manu.cli;

import com.cldellow.manu.common.Common;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Random;

import static org.junit.Assert.*;

@RunWith(JUnitQuickcheck.class)
public class FileParserTest {
    @Test
    public void testNumFields() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("simple.tsv"), Integer.MIN_VALUE);
        assertEquals(4, fp.getNumFields());
        fp.close();
    }

    @Test
    public void testParseRows() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("simple.tsv"), Integer.MIN_VALUE);
        assertEquals(4, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());

        {
            FileParser.RowIterator.Row row = it.next();
            assertEquals("foo", row.getKey());

            int[] data = row.getInts();
            assertEquals(fp.getNumFields() - 1, data.length);
            assertEquals(1, data[0]);
            assertEquals(-2, data[1]);
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

    @Test
    public void testNullField() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("key-and-tab.tsv"), Integer.MIN_VALUE);
        assertEquals(2, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());
        FileParser.RowIterator.Row row = it.next();
        assertNotNull(row);
        assertEquals("a", row.getKey());
        assertEquals(1, row.getInts().length);
        assertEquals(Integer.MIN_VALUE, row.getInts()[0]);
        fp.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLine() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("nonewline.tsv"), Integer.MIN_VALUE);
        fp.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidLine2() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("nonewline2.tsv"), Integer.MIN_VALUE);
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
        FileParser fp = new FileParser(new Common().getFile("non-numeric.tsv"), Integer.MIN_VALUE);
        assertEquals(2, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());
        FileParser.RowIterator.Row row = it.next();
        assertTrue(it.hasNext());
        fp.close();
    }

    @Test
    public void testOnlyOneColumn() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("only-one-column.tsv"), Integer.MIN_VALUE);
        assertEquals(1, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        assertTrue(it.hasNext());
        FileParser.RowIterator.Row row = it.next();
        assertFalse(it.hasNext());
        fp.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseRowsRaggedTooLong() throws Exception {
        FileParser fp = new FileParser(new Common().getFile("ragged.tsv"), Integer.MIN_VALUE);
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
        FileParser fp = new FileParser(new Common().getFile("ragged2.tsv"), Integer.MIN_VALUE);
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
        FileParser fp = new FileParser(new Common().getFile("ragged3.tsv"), Integer.MIN_VALUE);
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
        FileParser fp = new FileParser(new Common().getFile("ragged3.tsv"), Integer.MIN_VALUE);
        assertEquals(5, fp.getNumFields());
        Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
        it.remove();
    }


    @Property(trials = 25)
    public void testRandomInts() throws Exception {
        String tmpFile = "/tmp/fileparser-randomints";


        Random r = new Random();
        int nullValue = r.nextInt(128) - 128;
        int rows = r.nextInt(100) + 1;
        int cols = r.nextInt(100) + 1;
        int[][] ints = new int[rows][];
        for (int i = 0; i < rows; i++) {
            ints[i] = new int[cols];
            for (int j = 0; j < cols; j++)
                ints[i][j] = r.nextInt(128) - 128;
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));
            for (int i = 0; i < ints.length; i++) {
                writer.append("" + i);
                for (int j = 0; j < ints[i].length; j++) {
                    writer.append('\t');
                    if (ints[i][j] != nullValue)
                        writer.append("" + ints[i][j]);
                }
                writer.append("\n");
            }
            writer.close();
            FileParser fp = new FileParser(tmpFile, nullValue);
            assertEquals(cols + 1, fp.getNumFields());
            Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
            for (int i = 0; i < ints.length; i++) {
                assertTrue(it.hasNext());
                FileParser.RowIterator.Row row = it.next();
                int[] values = row.getInts();
                assertEquals(i, Integer.parseInt(row.getKey()));
                assertEquals(cols, values.length);

                for (int j = 0; j < cols; j++)
                    assertEquals(ints[i][j], values[j]);

            }
            assertFalse(it.hasNext());

        } finally {
            File f = new File(tmpFile);
            if (f.exists())
                f.delete();
        }
    }
}
