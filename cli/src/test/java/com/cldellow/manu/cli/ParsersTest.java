package com.cldellow.manu.cli;

import com.cldellow.manu.format.Interval;
import com.cldellow.manu.common.ArgHolder;
import org.junit.Test;

import java.security.Key;
import java.util.Collection;

import static org.junit.Assert.*;

public class ParsersTest {
    @Test
    public void ctor() {
        new Parsers();
    }

    @Test
    public void interval() {
        assertEquals(Interval.MINUTE, Parsers.interval("minute"));
        assertEquals(Interval.HOUR, Parsers.interval("hour"));
        assertEquals(Interval.DAY, Parsers.interval("day"));
        assertEquals(Interval.WEEK, Parsers.interval("week"));
        assertEquals(Interval.MONTH, Parsers.interval("month"));
        assertEquals(Interval.QUARTER, Parsers.interval("quarter"));
        assertEquals(Interval.YEAR, Parsers.interval("year"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownInterval() {
        Parsers.interval("blah");
    }

    @Test
    public void epochMs() {
        assertEquals(0L, Parsers.epochMs("1970-01-01T00:00Z"));
        assertEquals(0L, Parsers.epochMs("1970-01-01"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEpochMs() {
        Parsers.epochMs("197adsfdsa0-01-01T00:00Z");
    }

    @Test
    public void keyKind() {
        assertEquals(KeyKind.KEY, Parsers.keyKind("--key"));
        assertEquals(KeyKind.ID, Parsers.keyKind("--id"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownKeyKind() {
        Parsers.keyKind("blah");
    }

    @Test
    public void fieldKind() {
        assertEquals(FieldKind.INT, Parsers.fieldKind("--int"));
        assertEquals(FieldKind.FIXED1, Parsers.fieldKind("--fixed1"));
        assertEquals(FieldKind.FIXED2, Parsers.fieldKind("--fixed2"));
        assertEquals(FieldKind.LOSSY, Parsers.fieldKind("--lossy"));

    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownFieldKind() {
        Parsers.fieldKind("blah");
    }

    @Test
    public void emptyFieldDefs() throws Exception{
        Collection<FieldDef> defs = Parsers.fieldDefs(new ArgHolder(new String[]{}));
        assertTrue(defs.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void incompleteFieldDef() throws Exception{
        Parsers.fieldDefs(new ArgHolder(new String[]{"--key"}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void incompleteFieldDef2() throws Exception{
        Parsers.fieldDefs(new ArgHolder(new String[]{"foo", "--key"}));
    }


    @Test
    public void oneFieldDef() throws Exception{
        Collection<FieldDef> defs = Parsers.fieldDefs(new ArgHolder(new String[]{"pvs", "pvs.file"}));
        assertEquals(1L, defs.size());
        FieldDef[] arr = new FieldDef[0];
        arr = defs.toArray(arr);
        assertEquals("pvs", arr[0].getName());
        assertEquals("pvs.file", arr[0].getFile());
    }

    @Test
    public void oneFieldDefWithFlags() throws Exception{
        Collection<FieldDef> defs = Parsers.fieldDefs(new ArgHolder(new String[]{"--id", "--lossy", "pvs", "pvs.file"}));
        assertEquals(1L, defs.size());
        FieldDef[] arr = new FieldDef[0];
        arr = defs.toArray(arr);
        assertEquals("pvs", arr[0].getName());
        assertEquals("pvs.file", arr[0].getFile());
        assertEquals(KeyKind.ID, arr[0].getKeyKind());
        assertEquals(FieldKind.LOSSY, arr[0].getFieldKind());
    }


    @Test
    public void twoFieldDefs() throws Exception{
        Collection<FieldDef> defs = Parsers.fieldDefs(new ArgHolder(new String[]{"pvs", "pvs.file", "edits", "edits.file"}));
        assertEquals(2L, defs.size());
        FieldDef[] arr = new FieldDef[0];;
        arr = defs.toArray(arr);
        assertEquals("pvs", arr[0].getName());
        assertEquals("pvs.file", arr[0].getFile());
        assertEquals("edits", arr[1].getName());
        assertEquals("edits.file", arr[1].getFile());
    }
}
