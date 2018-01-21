package com.cldellow.manu.format;

public class LengthOps {
    public static byte encode(byte id, int len) {
        if (id > 63 || id < 0)
            throw new IllegalArgumentException("id " + id + " is out of range (must be 0..63)");

        int bytesNeeded = lengthSize(len);
        return (byte) (id | (bytesNeeded << 6));
    }

    public static int lengthSize(int len) {
        if (len < 0)
            throw new IllegalArgumentException("len " + len + " is out of range (must be >= 0)");

        int bytesNeeded = 0;
        // NB: we map 1..4 to 0..3 for ease of bit shifting
        // Also we only store 1, 2, or 4-byte sizes as they map
        // neatly to byte, short and int.
        if (len > 65535)
            bytesNeeded = 3;
        else if (len > 255)
            bytesNeeded = 1;
        else
            bytesNeeded = 0;
        return bytesNeeded;
    }

    public static byte decodeId(byte encoded) {
        return (byte) (encoded & 0b00111111);
    }

    public static byte decodeLengthSize(byte encoded) {
        return (byte) (((encoded >> 6) & 0b00000011) + 1);
    }

}
