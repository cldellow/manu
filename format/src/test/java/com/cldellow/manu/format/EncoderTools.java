package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

public class EncoderTools {
    public static int[] roundtrip(int[] data, FieldEncoder encoder, IntWrapper encodedLength) throws Exception {
        int[] encoded = new int[data.length * 2];
        encoder.encode(data, encoded, encodedLength);

        int[] decoded = new int[data.length];
        IntWrapper decodedLength = new IntWrapper(0);
        encoder.decode(encoded, encodedLength.get(), decoded, decodedLength);

        if (decodedLength.get() != data.length)
            throw new Exception(String.format("array did not roundtrip: old length=%d, new length=%d", data.length, decodedLength.get()));

        return decoded;
    }

    public static int roundtripSize(int[] data, FieldEncoder encoder) throws Exception {
        IntWrapper rv = new IntWrapper(0);
        int[] decoded = roundtrip(data, encoder, rv);

        for (int i = 0; i < data.length; i++)
            if (data[i] != decoded[i])
                throw new Exception(String.format("array did not roundtrip: differs at index %d", i));

        return rv.get();
    }
}
