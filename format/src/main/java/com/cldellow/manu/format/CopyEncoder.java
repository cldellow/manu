package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

// A dumb encoder - just copies src to dst; useful
// for validating the rest of the system.
public class CopyEncoder implements FieldEncoder {
    public int id() { return 0; }

    public void encode(int[] data, int[] encoded, IntWrapper encodedLength) {
        System.arraycopy(data, 0, encoded, 0, data.length);
        encodedLength.set(data.length);
    }

    public void decode(int[] encoded, int encodedLength, int[] data, IntWrapper dataLength) {
        System.arraycopy(encoded, 0, data, 0, encodedLength);
        dataLength.set(encodedLength);
    }
}
