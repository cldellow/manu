package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

public interface FieldEncoder {
    public int id();
    public void encode(int[] data, int[] output, IntWrapper outLength);
}