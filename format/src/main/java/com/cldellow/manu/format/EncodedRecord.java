package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class EncodedRecord implements Record {
    private final int id;
    private final ByteBuffer buffer;
    private final int numFields;

    private final FieldEncoder[] fieldEncoders;
    private final int[] fieldOffsets;
    private final int[] fieldContentOffsets;
    private final int[] fieldLengths;

    private final int[] tmp;
    private final int[] rv;

    public EncodedRecord(int id, ByteBuffer buffer, int recordStart, int numFields, int[] tmp, int[] rv) {
        this.id = id;
        this.buffer = buffer;
        this.numFields = numFields;
        this.tmp = tmp;
        this.rv = rv;

        this.fieldOffsets = new int[numFields];
        this.fieldEncoders = new FieldEncoder[numFields];
        this.fieldContentOffsets = new int[numFields];
        this.fieldLengths = new int[numFields];
        // Determine the offsets of all the fields.
        buffer.position(recordStart);

        for(int i = 0; i < numFields; i++) {
            fieldOffsets[i] = buffer.position();
            int encoderId = buffer.get();
            fieldEncoders[i] = Common.getEncoder(encoderId);
            if(fieldEncoders[i].isVariableLength()) {
                int length = buffer.getInt();
                fieldLengths[i] = length;
            } else {
                fieldLengths[i] = fieldEncoders[i].getLength();
            }
            fieldContentOffsets[i] = buffer.position();
            buffer.position(buffer.position() + 4*fieldLengths[i]);
         }
    }

    public int getId() {
        return id;
    }

    public int[] getValues(int field) {
        FieldEncoder encoder = getEncoder(field);
        buffer.position(fieldContentOffsets[field]);
        if(encoder.isVariableLength()) {
            IntBuffer intBuffer = buffer.asIntBuffer();
            intBuffer.get(tmp, 0, fieldLengths[field]);
        } else {
            fieldLengths[field] = encoder.getLength();
        }
        encoder.decode(tmp, fieldLengths[field], rv, new IntWrapper(0));
        return rv;
    }

    public FieldEncoder getEncoder(int field) {
        // TODO: figure out if we should guarantee that these are threadsafe
        // so we can get rid of this defensive clone
        return Common.getEncoder(fieldEncoders[field].getId());
    }
}