package com.cldellow.manu.format;

import me.lemire.integercompression.FastPFOR128;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.SkippableComposition;
import me.lemire.integercompression.VariableByte;

public class PFOREncoder implements FieldEncoder {
    private IntWrapper outPos = new IntWrapper(0);
    private SkippableComposition compressor = new SkippableComposition(new FastPFOR128(), new VariableByte());

    public int id() {
        return 1;
    }

    public boolean isVariableLength() {
        return true;
    }

    public int getLength() {
        return -1;
    }

    public void encode(int[] data, int[] encoded, IntWrapper encodedLength) {
        encodedLength.set(0);

        compressor.headlessCompress(data, new IntWrapper(0), data.length, encoded, encodedLength);
    }

    public void decode(int[] encoded, int encodedLength, int[] data, IntWrapper dataLength) {
        compressor.headlessUncompress(encoded, new IntWrapper(0), encodedLength, data, dataLength, data.length);
    }
}