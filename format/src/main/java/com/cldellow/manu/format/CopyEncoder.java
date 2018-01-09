package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

// A dumb encoder - just copies src to dst; useful
// for validating the rest of the system.
public class CopyEncoder implements FieldEncoder {
    public int id() { return 0; }

    public void encode(int[] data, int[] output, IntWrapper outLength) {
        System.arraycopy(data, 0, output, 0, data.length);
        outLength.set(data.length);
    }
}
