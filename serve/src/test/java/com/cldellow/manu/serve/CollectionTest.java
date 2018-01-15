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
}