package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

public interface FieldEncoder {
    int getId();

    // TODO: refactor isVariableLength/getLength to be getLength/putLength on a ByteBuffer
    boolean isVariableLength();
    int getLength();

    void encode(int[] data, byte[] encoded, IntWrapper encodedLength);

    void decode(byte[] encoded, int encodedLength, int[] data, IntWrapper dataLength);
}