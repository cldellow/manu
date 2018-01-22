package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class EncodedRecord implements Record {
    private final int id;
    private final ByteBuffer buffer;
    private final int numFields;

    private final int[] fieldOffsets;
    private final int[] fieldContentOffsets;
    private final int[] fieldLengths;

    private final int[] encoderIds;
    private final byte[] tmp;
    private final int[] rv;

    public EncodedRecord(int id, ByteBuffer buffer, int recordStart, int recordEnd, int numFields, byte[] tmp, int[] rv) {
        this.id = id;
        this.buffer = buffer;
        this.numFields = numFields;
        this.tmp = tmp;
        this.rv = rv;

        this.fieldOffsets = new int[numFields];
        this.encoderIds = new int[numFields];
        this.fieldContentOffsets = new int[numFields];
        this.fieldLengths = new int[numFields];
        // Determine the offsets of all the fields.
        buffer.position(recordStart);

        for (int i = 0; i < numFields; i++) {
            fieldOffsets[i] = buffer.position();
            byte encoderIdRaw = buffer.get();
            int encoderId = LengthOps.decodeId(encoderIdRaw);
            encoderIds[i] = encoderId;

            // Length is only for the first N-1 fields.
            if(i == numFields -1) {
              fieldLengths[i] = recordEnd - buffer.position();
                System.out.println("computing length; recordStart=" + recordStart + ", recordEnd=" + recordEnd + ", length=" + fieldLengths[i]);

            } else if (ThreadEncoders.get()[encoderId].isVariableLength()) {
                int lengthSize = LengthOps.decodeLengthSize(encoderIdRaw);
                int length;
                if (lengthSize == 1)
                    length = (int)(buffer.get() & 0xFF);
                else if (lengthSize == 2)
                    length = (int)(buffer.getShort() & 0xFFFF);
                  else
                    length = buffer.getInt();
                fieldLengths[i] = length;
            } else {
                fieldLengths[i] = ThreadEncoders.get()[encoderId].getLength();
            }
            fieldContentOffsets[i] = buffer.position();
            buffer.position(buffer.position() + fieldLengths[i]);
        }
    }

    public int getId() {
        return id;
    }

    public int[] getValues(int field) {
        FieldEncoder encoder = getEncoder(field);
        buffer.position(fieldContentOffsets[field]);

        buffer.get(tmp, 0, fieldLengths[field]);

        encoder.decode(tmp, fieldLengths[field], rv, new IntWrapper(0));
        return rv;
    }

    public FieldEncoder getEncoder(int field) {
        return ThreadEncoders.get()[encoderIds[field]];
    }
}