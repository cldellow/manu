package com.cldellow.manu.format;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;

import static org.fusesource.leveldbjni.JniDBFactory.*;

public class Index {
    DB db;
    private final byte[] NUM_ROW_KEYS = new byte[] { 0 };

    public Index(String file, boolean readOnly) throws IOException {
        String maybeRo = "";
        Options options = new Options();
        options.createIfMissing(true);
        db = factory.open(new File(file), options);
    }

    public void close() throws IOException {
        if (db != null) {
            db.close();
            db = null;
        }
    }

    private int toInt(byte[] bytes) {
        if (bytes == null)
            return -1;
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return buf.getInt();
    }

    private byte[] toBytes(int i) {
        byte[] bytes = new byte[4];
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.putInt(i);
        return bytes;
    }

    private byte[] getStringKey(String key) {
        return bytes("_" + key);
    }

    private byte[] getIntKey(int id) {
        byte[] rv = new byte[5];
        ByteBuffer buf = ByteBuffer.wrap(rv);
        buf.put((byte) '-');
        buf.putInt(id);
        return rv;
    }

    public int get(String key) throws SQLException {
        return toInt(db.get(getStringKey(key)));
    }

    public String get(int id) throws SQLException {
        byte[] bytes = db.get(getIntKey(id));
        if (bytes == null)
            return null;

        return asString(bytes);
    }

    public String[] get(int id, int howMany) throws SQLException {
        if (howMany < 1)
            throw new IllegalArgumentException("howMany must be >= 1");

        String[] rv = new String[howMany];
        for (int i = 0; i < rv.length; i++)
            rv[i] = null;

        //TODO: optimize
        for (int k = id; k < id + howMany; k++)
            rv[k - id] = get(k);
        return rv;
    }

    public HashMap<String, Integer> get(Collection<String> keys) throws SQLException {
        if (keys.isEmpty())
            return new HashMap<String, Integer>();

        HashMap<String, Integer> rv = new HashMap<>();
        for(String key : keys) {
            int id = get(key);
            if(id != -1)
                rv.put(key, id);
        }
        return rv;
    }

    public HashMap<String, Integer> add(Collection<String> keys) throws SQLException {
        HashMap<String, Integer> rv = new HashMap<>();
        for(String key: keys) {

            int id = get(key);
            if(id == -1) {
                rv.put(key, add(key));
            } else {
                rv.put(key, id);
            }
        }
        return rv;
    }

    public int add(String key) throws SQLException {
        int id = get(key);
        if(id != -1)
            return id;

        id = getNumRows();
        byte[] idValue = toBytes(id );
        db.put(NUM_ROW_KEYS, toBytes(id+1));
        db.put(getStringKey(key), idValue);
        db.put(getIntKey(id), bytes(key));
        return id;
    }

    public int getNumRows() throws SQLException {
        byte[] rv = db.get(NUM_ROW_KEYS);
        int numRows = toInt(db.get(NUM_ROW_KEYS));
        if(numRows == -1)
            numRows = 0;

        return numRows;
    }

    public static void delete(String filename) {
        deleteDir(new File(filename));
    }

    private static void deleteDir(File file) {
        if(!file.exists())
            return;

        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
}
