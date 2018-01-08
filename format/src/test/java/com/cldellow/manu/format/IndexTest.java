package com.cldellow.manu.format;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class IndexTest {
    private String dbLoc = "/tmp/manu-test.db";

    @After
    public void cleanup() throws Exception {
      File f = new File(dbLoc);
      if(f.exists())
          f.delete();
    }


    @Test
    public void testEmptyIndexOKWhenWritable() throws Exception {
        Index index = null;
        try {
            index = new Index(dbLoc, false);
        } finally {
            if(index != null) index.close();
        }
    }

    @Test(expected = SQLException.class)
    public void testEmptyIndexNotOKWhenReadOnly() throws Exception {
        Index index = null;
        try {
            index = new Index(dbLoc, true);
        } finally {
            if(index != null) index.close();
        }
    }


    @Test
    public void testNonEmptyIndexOK() throws SQLException {
        Index index = null;

        Connection conn = DriverManager.getConnection("jdbc:sqlite:file:" + dbLoc);
        try {

            try {
                index = new Index(dbLoc, true);
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
            index = new Index(dbLoc, false);
            int id = index.get("this key doesn't exist");
            assertEquals(-1, id);
        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void testAddAndGetKey() throws SQLException {
        Index index = null;
        try {
            index = new Index(dbLoc, false);
            int id = index.add("somekey");
            assertNotEquals(-1, id);
            int id2 = index.get("somekey");
            assertEquals(id, id2);
        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void testAddExistingKey() throws SQLException {
        Index index = null;
        try {
            index = new Index(dbLoc, false);
            int id = index.add("somekey");
            assertNotEquals(-1, id);
            int id2 = index.add("somekey");
            assertEquals(id, id2);
        } finally {
            if (index != null) index.close();
        }
    }

    @Test
    public void testGetExistingById() throws SQLException {
        Index index = null;
        try {
            index = new Index(dbLoc, false);
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
            index = new Index(dbLoc, false);
            String actual = index.get(123);
            assertEquals(null, actual);
        } finally {
            if (index != null) index.close();
        }
    }
}
