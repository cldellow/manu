package com.cldellow.manu.format;

import me.lemire.integercompression.*;

public class PFOREncoder implements FieldEncoder {
    private IntWrapper outPos = new IntWrapper(0);
    private SkippableComposition compressor = new SkippableComposition(new NewPFD(), new VariableByte());

    public int id() {
        return 1;
    }

    public void encode(int[] data, int[] encoded, IntWrapper encodedLength) {
        compressor.headlessCompress(data, new IntWrapper(0), data.length, encoded, encodedLength);
    }

    public void decode(int[] encoded, int encodedLength, int[] data, IntWrapper dataLength) {
        compressor.headlessUncompress(encoded, new IntWrapper(0), encodedLength, data, new IntWrapper(0), data.length);
    }
}
