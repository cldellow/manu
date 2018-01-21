package com.cldellow.manu.cli;

import com.cldellow.manu.common.Common;
import com.cldellow.manu.format.Index;
import com.cldellow.manu.format.Reader;
import com.cldellow.manu.format.Record;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static junit.framework.TestCase.*;

public class ReadWriteTest {
    private String keyLoc = "/tmp/manu-test.keys";
    private String dbLoc = "/tmp/manu-test.data";

    @After
    public void cleanup() throws Exception {
        File f = new File(dbLoc);
        if (f.exists())
            f.delete();

        f = new File(keyLoc);
        if (f.exists())
            f.delete();
    }

    @Test
    public void testSparseRecord() throws Exception {
        Index idx = new Index(keyLoc, false);
        idx.add("a");
        idx.add("b");
        idx.close();

        Write w = new Write(new String[] {keyLoc, dbLoc, "0", "hour", "field", new Common().getFile("b-1.tsv")});
        w.entrypoint();

        Reader reader = new Reader(dbLoc);
        Record r = reader.records.next();
        // TODO: rejig this to not emit nulls
        assertNull(r);
        assertTrue(reader.records.hasNext());
        r = reader.records.next();
        assertNotNull(r);
        assertEquals(1, r.getId());

    }
}