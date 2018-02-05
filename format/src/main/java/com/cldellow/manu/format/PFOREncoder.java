package com.cldellow.manu.format;

import me.lemire.integercompression.FastPFOR128;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.SkippableComposition;
import me.lemire.integercompression.VariableByte;

import java.nio.ByteBuffer;

/**
 * An encoder which uses {@link me.lemire.integercompression.FastPFOR128} to compress integers.
 */
public class PFOREncoder implements FieldEncoder {
    private int[] tmp = null;
    private SkippableComposition compressor = null;

    public int getId() {
        return 1;
    }

    public boolean isVariableLength() {
        return true;
    }

    public int getLength() {
        return -1;
    }

    private void ensure() {
        if (compressor == null)
            compressor = new SkippableComposition(new FastPFOR128(), new VariableByte());

        if(tmp == null)
            tmp = new int[Common.INT_ARRAY_SIZE];
    }

    public void encode(int[] data, byte[] encoded, IntWrapper encodedLength) {
        ensure();
        encodedLength.set(0);

        compressor.headlessCompress(data, new IntWrapper(0), data.length, tmp, encodedLength);
        ByteBuffer buf = ByteBuffer.wrap(encoded);
        for(int i = 0; i < encodedLength.get(); i++)
            buf.putInt(tmp[i]);

        encodedLength.set(encodedLength.get() * 4);
    }

    public void decode(byte[] encoded, int encodedLength, int[] data, IntWrapper dataLength) {
        ensure();

        ByteBuffer buf = ByteBuffer.wrap(encoded);
        for(int i = 0; i < encodedLength / 4; i++) {
            tmp[i] = buf.getInt();
        }

        compressor.headlessUncompress(tmp, new IntWrapper(0), encodedLength / 4, data, dataLength, data.length);
    }
}