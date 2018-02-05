package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

import java.nio.ByteBuffer;

/**
 * An encoder which encodes a single integer in a space-efficient manner.
 *
 * Values between -128 and 127 take 1 byte. Between -32,768 and 32,767 take 2 bytes.
 * All other values take 4 bytes.
 */
public class SingleValueEncoder implements FieldEncoder {
    public int getId() {
        return 3;
    }

    public boolean isVariableLength() {
        return true;
    }

    public static boolean eligible(int[] data) { return data.length == 1; }

    public int getLength() {
        return -1;
    }

    public void encode(int[] data, byte[] encoded, IntWrapper encodedLength) {
        if(!eligible(data))
            throw new IllegalArgumentException("SingleValueEncoder only works on exactly 1 value");

        int value = data[0];
        ByteBuffer buffer = ByteBuffer.wrap(encoded);
        if(value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
            buffer.put((byte)value);
        else if(value >= Short.MIN_VALUE && value <= Short.MAX_VALUE)
            buffer.putShort((short)value);
        else
            buffer.putInt(value);

        encodedLength.set(buffer.position());
    }

    public void decode(byte[] encoded, int encodedLength, int[] data, IntWrapper dataLength) {
        ByteBuffer buffer = ByteBuffer.wrap(encoded);

        if(encodedLength == 1)
            data[0] = buffer.get();
        else if(encodedLength == 2)
            data[0] = buffer.getShort();
        else
            data[0] = buffer.getInt();

        dataLength.set(1);
    }
}
