package com.cldellow.manu.format;

import java.sql.*;

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
                return rs.getInt(1);
            return -1;
        } finally {
            statement.close();
        }
    }

    public String get(int id) throws SQLException {
        PreparedStatement statement = conn.prepareStatement("SELECT key FROM keys WHERE rowid = ?");
        try {
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next())
                return rs.getString(1);
            return null;
        } finally {
            statement.close();
        }
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

    private void ensureSchema() throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS keys(key TEXT PRIMARY KEY);");
        statement.close();
    }
}
