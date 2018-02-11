package com.cldellow.manu.serve;

import com.cldellow.manu.common.Common;
import com.cldellow.manu.format.ManuReader;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CollectionTest {
    @Test(expected = IllegalArgumentException.class)
    public void noKey() throws Exception {
        new Collection(new Common().getFile("datadir/manu-without-keys"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void noManus() throws Exception {
        Collection.validateReaders("foo", new ManuReader[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void mismatchedInterval() throws Exception {
        ManuReader[] readers = new ManuReader[]{
                new ManuReader(new Common().getFile("datadir/hourly/200801.manu")),
                new ManuReader(new Common().getFile("datadir/daily/200801.manu"))
        };

        Collection.validateReaders("foo", readers);
    }

    @Test
    public void valid() throws Exception {
        Collection c = new Collection(new Common().getFile("datadir/hourly"));
        assertEquals(2, c.readers.length);
        assertTrue(c.readers[0].getFrom().getMillis() < c.readers[1].getFrom().getMillis());
        c.close();
    }

    @Test
    public void validateReadersSorts() throws Exception {
        ManuReader r1 = new ManuReader(new Common().getFile("datadir/hourly/200801.manu"));
        ManuReader r2 = new ManuReader(new Common().getFile("datadir/hourly/200802.manu"));

        {
            ManuReader r[] = new ManuReader[]{r1, r2};
            Collection.validateReaders("d", r);
            assertTrue(r[0].getFrom().getMillis() < r[1].getFrom().getMillis());
        }

        {
            ManuReader r[] = new ManuReader[]{r2, r1};
            Collection.validateReaders("d", r);
            assertTrue(r[0].getFrom().getMillis() < r[1].getFrom().getMillis());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateReadersOverlap() throws Exception {
        ManuReader r1 = new ManuReader(new Common().getFile("datadir/hourly/200801.manu"));

        ManuReader r[] = new ManuReader[]{r1, r1};
        Collection.validateReaders("d", r);
    }
}