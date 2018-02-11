package com.cldellow.manu.cli;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldDefTest {
    @Test
    public void defaults() {
        FieldDef d = new FieldDef();
        assertEquals(null, d.getName());
        assertEquals(null, d.getFile());
        assertEquals(FieldKind.INT, d.getFieldKind());
        assertEquals(KeyKind.KEY, d.getKeyKind());
    }

    @Test
    public void setters() {
        FieldDef d = new FieldDef();
        d.setName("name");
        assertEquals("name", d.getName());
        d.setFile("file");
        assertEquals("file", d.getFile());
        d.setFieldKind(FieldKind.LOSSY);
        assertEquals(FieldKind.LOSSY, d.getFieldKind());
        d.setKeyKind(KeyKind.ID);
        assertEquals(KeyKind.ID, d.getKeyKind());
    }
}