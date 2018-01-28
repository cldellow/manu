package com.cldellow.manu.format;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

public class Index {
    private Connection conn = null;

    public Index(String file, boolean readOnly) throws SQLException {
        String maybeRo = "";
        if (readOnly)
            maybeRo = "?mode=ro";

        conn = DriverManager.getConnection("jdbc:sqlite:file:" + file + maybeRo);

        if (!readOnly)
            ensureSchema();
    }

    public void close() throws SQLException {
        if (conn != null) {
            conn.close();
            conn = null;
        }
    }

    public int get(String key) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT rowid FROM keys WHERE key = ?");
        try {
            statement.setString(1, key);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return rs.getInt(1) - 1;
            return -1;
        } finally {
            statement.close();
        }
    }

    public String get(int id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT key FROM keys WHERE rowid = ?");
        try {
            statement.setInt(1, id + 1);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return rs.getString(1);
            return null;
        } finally {
            statement.close();
        }
    }

    public HashMap<String, Integer> get(Collection<String> keys) throws SQLException {
        if(keys.isEmpty())
            return new HashMap<String, Integer>();

        StringBuilder qs = new StringBuilder();
        HashMap<String, Integer> rv = new HashMap<>();
        for(int i = 0; i < keys.size(); i++) {
            if(i > 0)
                qs.append(',');
            qs.append('?');
        }

        PreparedStatement statement = conn.prepareStatement("SELECT key, rowid FROM keys WHERE key in (" + qs.toString() + ")");
        try {
            int i = 1;
            for(String key: keys)
                statement.setString(i++, key);
            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                String key = rs.getString(1);
                int id = rs.getInt(2) - 1;
                rv.put(key, id);
            }
        } finally {
            statement.close();
        }

        return rv;
    }

    public HashMap<String, Integer> add(Collection<String> keys) throws SQLException {
        conn.setAutoCommit(false);
        PreparedStatement statement = conn.prepareStatement("INSERT OR IGNORE INTO keys VALUES (?)");
        try {
            for (String key : keys) {
                statement.setString(1, key);
                statement.executeUpdate();
            }
        } finally {
            statement.close();
            conn.setAutoCommit(true);

        }

        return get(keys);
    }

    public int add(String key) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("INSERT OR IGNORE INTO keys VALUES (?)");

        try {
            statement.setString(1, key);
            statement.executeUpdate();
            return get(key);
        } finally {
            statement.close();
        }
    }

    public int getNumRows() throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT MAX(rowid) FROM keys");

        try {
            ResultSet rs = statement.executeQuery();
            rs.next();
            return rs.getInt(1);
        } finally {
            statement.close();
        }
    }

    private void ensureSchema() throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS keys(key TEXT PRIMARY KEY);");
        statement.close();
    }
}
