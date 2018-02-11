package com.cldellow.manu.cli;

import com.cldellow.manu.common.NotEnoughArgsException;
import org.junit.Test;

import static org.junit.Assert.*;

public class MergeArgsTest {
    @Test(expected = NotEnoughArgsException.class)
    public void noOutputFile() throws Exception {
        new MergeArgs(new String[]{});
    }

    @Test(expected = NotEnoughArgsException.class)
    public void noInputFile() throws Exception {
        new MergeArgs(new String[]{"output"});
    }

    @Test
    public void simple() throws Exception {
        MergeArgs ma = new MergeArgs(new String[]{"output", "input"});
        assertEquals("output", ma.outputFile);
        assertEquals(1, ma.inputFiles.length);
        assertEquals("input", ma.inputFiles[0]);
        assertNull(ma.lossyFields);
    }

    @Test
    public void simple2() throws Exception {
        MergeArgs ma = new MergeArgs(new String[]{"output", "input", "input2"});
        assertEquals("output", ma.outputFile);
        assertEquals(2, ma.inputFiles.length);
        assertEquals("input", ma.inputFiles[0]);
        assertEquals("input2", ma.inputFiles[1]);
        assertNull(ma.lossyFields);
    }

    @Test
    public void lossyAll() throws Exception {
        MergeArgs ma = new MergeArgs(new String[]{"output", "input", "--lossy"});
        assertEquals("output", ma.outputFile);
        assertEquals(1, ma.inputFiles.length);
        assertEquals("input", ma.inputFiles[0]);
        assertNotNull(ma.lossyFields);
        assertEquals(0, ma.lossyFields.length);
    }

    @Test
    public void lossyAll2() throws Exception {
        MergeArgs ma = new MergeArgs(new String[]{"output", "input", "--lossy="});
        assertEquals("output", ma.outputFile);
        assertEquals(1, ma.inputFiles.length);
        assertEquals("input", ma.inputFiles[0]);
        assertNotNull(ma.lossyFields);
        assertEquals(0, ma.lossyFields.length);
    }

    @Test
    public void lossy1() throws Exception {
        MergeArgs ma = new MergeArgs(new String[]{"output", "input", "--lossy=foo"});
        assertEquals("output", ma.outputFile);
        assertEquals(1, ma.inputFiles.length);
        assertEquals("input", ma.inputFiles[0]);
        assertNotNull(ma.lossyFields);
        assertEquals(1, ma.lossyFields.length);
        assertEquals("foo", ma.lossyFields[0]);
    }

    @Test
    public void lossy2() throws Exception {
        MergeArgs ma = new MergeArgs(new String[]{"output", "input", "--lossy=foo,bar"});
        assertEquals("output", ma.outputFile);
        assertEquals(1, ma.inputFiles.length);
        assertEquals("input", ma.inputFiles[0]);
        assertNotNull(ma.lossyFields);
        assertEquals(2, ma.lossyFields.length);
        assertEquals("foo", ma.lossyFields[0]);
        assertEquals("bar", ma.lossyFields[1]);
    }

    @Test
    public void lossyPosition() throws Exception {
        MergeArgs ma = new MergeArgs(new String[]{"--lossy=foo", "output", "input"});
        assertEquals("output", ma.outputFile);
        assertEquals(1, ma.inputFiles.length);
        assertEquals("input", ma.inputFiles[0]);
        assertNotNull(ma.lossyFields);
        assertEquals(1, ma.lossyFields.length);
        assertEquals("foo", ma.lossyFields[0]);
    }

}