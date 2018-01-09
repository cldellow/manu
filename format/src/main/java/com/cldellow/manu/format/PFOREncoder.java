package com.cldellow.manu.format;

import me.lemire.integercompression.FastPFOR;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.SkippableComposition;
import me.lemire.integercompression.VariableByte;

public class PFOREncoder implements FieldEncoder {
    private int[] tmp = new int[16384];
    private IntWrapper outPos = new IntWrapper(0);
    private SkippableComposition compressor = new SkippableComposition(new FastPFOR(), new VariableByte());

    public int id() {
        return 1;
    }

    public void encode(int[] data, int[] output, IntWrapper outLength) {
        compressor.headlessCompress(data, new IntWrapper(0), data.length, tmp, outLength);
    }
}
