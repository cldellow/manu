package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

import java.nio.ByteBuffer;

/**
 * An encoder which copies the input exactly as it's represented in memory.
 *
 * This encoder should only be used for integration testing, not actual data storage.
 */
public class CopyEncoder implements FieldEncoder {
    private int[] tmp = null;

    public int getId() { return 0; }

    public boolean isVariableLength() { return true; }
    public int getLength() { return -1; }

    private void ensure() {
        if(tmp == null)
            tmp = new int[Common.INT_ARRAY_SIZE];
    }

    public void encode(int[] data, byte[] encoded, IntWrapper encodedLength) {
        ensure();

        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        for(int i = 0; i < data.length; i++)
            buffer.putInt(data[i]);

        encodedLength.set(data.length * 4);
    }

    public void decode(byte[] encoded, int encodedLength, int[] data, IntWrapper dataLength) {
        ensure();
        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        for(int i = 0; i < encodedLength / 4; i++)
            data[i] = buffer.getInt();

        dataLength.set(encodedLength / 4);
    }
}
