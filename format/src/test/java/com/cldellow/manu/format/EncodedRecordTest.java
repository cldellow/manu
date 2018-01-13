package com.cldellow.manu.format;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class EncodedRecordTest {

    @Test
    public void getId() {
        byte[] bytes = new byte[100];
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        Record r = new EncodedRecord(123, buf, 0, 1, null, null);
        assertEquals(123, r.getId());
    }
}