package com.cldellow.manu.format;

import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(JUnitQuickcheck.class)
public class IndexTest {
    private String dbLoc = "/tmp/manu-test.db";

    @After
    public void cleanup() throws Exception {
        File f = new File(dbLoc);
        if (f.exists())
            f.delete();
    }


    @Test
    public void testEmptyIndexOKWhenWritable() throws Exception {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_SAFE);
        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void testDoubleCloseOk() throws Exception {
        Index index = new Index(dbLoc, IndexAccessMode.READ_WRITE_SAFE);
        index.close();
        index.close();
    }


    @Test(expected = SQLException.class)
    public void testEmptyIndexNotOKWhenReadOnly() throws Exception {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_ONLY);
        } finally {
            if (index != null) index.close();
        }
    }


    @Test
    public void testNonEmptyIndexOK() throws SQLException {
        Index index = null;

        Connection conn = DriverManager.getConnection("jdbc:sqlite:file:" + dbLoc);
        try {

            try {
                index = new Index(dbLoc, IndexAccessMode.READ_ONLY);
            } finally {
                if (index != null) index.close();
            }
        } finally {
            conn.close();
        }
    }

    @Test
    public void testNonExistentKey() throws SQLException {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_SAFE);
            int id = index.get("this key doesn't exist");
            assertEquals(-1, id);
        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void testEmptyHasZeroRows() throws SQLException {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_SAFE);
            assertEquals(0, index.getNumRows());
        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void testAddAndGetKey() throws SQLException {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_SAFE);
            int id = index.add("somekey");
            assertNotEquals(-1, id);
            assertEquals(0, id);
            int id2 = index.get("somekey");
            assertEquals(id, id2);
            assertEquals(1L, index.getNumRows());
        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void testAddExistingKey() throws SQLException {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_SAFE);
            int id = index.add("somekey");
            assertNotEquals(-1, id);
            int id2 = index.add("somekey");
            assertEquals(id, id2);
        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void testAddMultiple() throws SQLException {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_SAFE);
            Collection<String> keys = new Vector<String>();
            keys.add("abc");
            keys.add("def");
            index.add(keys);
            int id = index.add("abc");
            assertNotEquals(-1, id);
            int id2 = index.get("abc");
            assertEquals(id, id2);
        } finally {
            if (index != null) index.close();
        }
    }


    @Test
    public void testGetExistingById() throws SQLException {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_SAFE);
            int id = index.add("somekey");
            String actual = index.get(id);
            assertEquals("somekey", actual);
        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void testGetNonExistingById() throws SQLException {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_SAFE);
            String actual = index.get(123);
            assertEquals(null, actual);
        } finally {
            if (index != null) index.close();
        }
    }

    /**
     * This test is surprisingly slow. Maybe re-enable it if we switch backends.
     *
     * @Property public void bunchaKeys(String[] keys) throws Exception {
     * Index index = null;
     * try {
     * index = new Index(dbLoc, false);
     * System.err.println(keys.length);
     * for(String key : keys) {
     * int id = index.add(key);
     * assertEquals(id, index.get(key));
     * }
     * } finally {
     * if (index != null) index.close();
     * }
     * }
     */

    @Test
    public void addBulk() throws Exception {
        Vector<String> toInsert = new Vector<String>();
        toInsert.add("0");
        toInsert.add("1");

        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_UNSAFE);
            HashMap<String, Integer> rv = index.add(toInsert);
            assertEquals(0, rv.get("0").intValue());
            assertEquals(index.get("0"), rv.get("0").intValue());
            assertEquals(1, rv.get("1").intValue());
            assertEquals(index.get("1"), rv.get("1").intValue());

        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void getBulk() throws Exception {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_UNSAFE);
            int foo = index.add("foo");
            Vector<String> keys = new Vector<>();
            keys.add("foo");
            keys.add("bar");
            HashMap<String, Integer> rv = index.get(keys);
            assertEquals(foo, rv.get("foo").intValue());
            assertEquals(1, rv.size());
        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void getIdBulk() throws Exception {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_UNSAFE);
            int foo = index.add("foo");
            int bar = index.add("bar");
            String[] keys = index.get(0, 3);
            assertEquals(3, keys.length);
            assertEquals("foo", keys[0]);
            assertEquals("bar", keys[1]);
            assertEquals(null, keys[2]);
        } finally {
            if (index != null) index.close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getIdBulkInvalid() throws Exception {
        Index index = null;
        try {
            index = new Index(dbLoc, IndexAccessMode.READ_WRITE_UNSAFE);
            index.get(0, 0);
        } finally {
            if (index != null) index.close();
        }
    }
}
