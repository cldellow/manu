package com.cldellow.manu.serve;

import com.cldellow.manu.common.Common;
import com.cldellow.manu.format.Reader;
import org.junit.Test;

import static org.junit.Assert.*;

public class CollectionTest {
    @Test(expected = IllegalArgumentException.class)
    public void noKey() throws Exception {
        new Collection(new Common().getFile("datadir/manu-without-keys"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void noManus() throws Exception {
        Collection.validateReaders("foo", new Reader[]{});
    }

    @Test(expected =  IllegalArgumentException.class)
    public void mismatchedInterval() throws Exception {
        Reader[] readers = new Reader[] {
                new Reader(new Common().getFile("datadir/hourly/200801.manu")),
                new Reader(new Common().getFile("datadir/daily/200801.manu"))
        };

        Collection.validateReaders("foo", readers);
    }

    @Test
    public void valid() throws Exception {
        Collection c = new Collection(new Common().getFile("datadir/hourly"));
        assertEquals(2, c.readers.length);
        assertTrue(c.readers[0].epochMs < c.readers[1].epochMs);
        c.close();
    }

    @Test
    public void validateReadersSorts() throws Exception {
        Reader r1 = new Reader(new Common().getFile("datadir/hourly/200801.manu"));
        Reader r2 = new Reader(new Common().getFile("datadir/hourly/200802.manu"));

        {
            Reader r[] = new Reader[] { r1, r2};
            Collection.validateReaders("d", r);
            assertTrue(r[0].epochMs < r[1].epochMs);
        }

        {
            Reader r[] = new Reader[] { r2, r1};
            Collection.validateReaders("d", r);
            assertTrue(r[0].epochMs < r[1].epochMs);
        }
   }

    @Test(expected = IllegalArgumentException.class)
    public void validateReadersOverlap() throws Exception {
        Reader r1 = new Reader(new Common().getFile("datadir/hourly/200801.manu"));

        Reader r[] = new Reader[] { r1, r1};
            Collection.validateReaders("d", r);
    }
}