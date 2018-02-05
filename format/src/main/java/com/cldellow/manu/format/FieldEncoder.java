package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

/**
 * An interface to provide a custom integer compression implementation.
 */
public interface FieldEncoder {
    /**
     * Returns the unique identifier for this encoder.
     * @return The unique identifier for this encoder.
     */
    int getId();

    // TODO: refactor isVariableLength/getLength to be getLength/putLength on a ByteBuffer

    /**
     * Returns whether this encoder takes a fixed or variable number of bytes to store its output.
     * @return Whether this encoder takes a fixed or variable number of bytes to store its output.
     */
    boolean isVariableLength();

    /**
     * If {@link FieldEncoder#isVariableLength()} is {@literal true}, returns the number of bytes
     * this encoder uses to store its output.
     * @return The number of bytes required for this encoder's output.
     */
    int getLength();

    /**
     * Compress a series of ints.
     *
     * @param data The ints to be compressed.
     * @param encoded The array in which to write the compressed output. Must be sufficiently large.
     * @param encodedLength The length, in bytes, of the compressed output in {@code encoded}.
     */
    void encode(int[] data, byte[] encoded, IntWrapper encodedLength);

    /**
     * Decompress a series of ints.
     *
     * @param encoded The ints to be decompressed.
     * @param encodedLength The length, in bytes, of the compressed input in {@code encoded}.
     * @param data The array in which to write the decompressed output. Must be sufficiently large.
     * @param dataLength The length, in bytes, of the decompressed output in {@code data}.
     */
    void decode(byte[] encoded, int encodedLength, int[] data, IntWrapper dataLength);
}