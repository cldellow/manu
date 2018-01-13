package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

public interface FieldEncoder {
    public int id();

    // TODO: refactor isVariableLength/getLength to be getLength/putLength on a ByteBuffer
    public boolean isVariableLength();
    public int getLength();

    public void encode(int[] data, int[] encoded, IntWrapper encodedLength);

    public void decode(int[] encoded, int encodedLength, int[] data, IntWrapper dataLength);
}