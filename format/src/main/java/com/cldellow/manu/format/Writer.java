package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.differential.*;

import java.io.*;
import java.util.Iterator;

public class Writer {
    // TODO: switch to NIO once we have a baseline implementation of writing/reading
    public static void write(
            String file,
            short rowListSize,
            long epochMs,
            int numDatapoints,
            Interval interval,
            int recordOffset,
            int numRecords,
            String[] fieldNames,
            FieldType[] fieldTypes,
            Iterator<Record> records) throws FileNotFoundException, IOException, Exception {
        if(fieldNames.length != fieldTypes.length)
            throw new  IllegalArgumentException(
                    String.format(
                            "Number of field names (%d) does not match number of field types (%d).",
                            fieldNames.length,
                            fieldTypes.length));

        final FileOutputStream fos = new FileOutputStream(file);
        final BufferedOutputStream bos = new BufferedOutputStream(fos);
        final DataOutputStream dos = new DataOutputStream(bos);

        final int numFields = fieldNames.length;
        int rowListPointerPosition = -1;
        int rowListOffset = -1;
        final int[] rowListPositions = new int[(numRecords + rowListSize - 1) / rowListSize];
        final int[] recordPositions = new int[numRecords];
        int currentRecord = 0;
        final int[] tmpArray = new int[131072];
        try {
            writePreamble(dos, rowListSize, epochMs, numDatapoints, interval, recordOffset, numRecords, fieldNames, fieldTypes);

            // Stash rowListPosition so we can fix it up later.
            rowListPointerPosition = dos.size();
            dos.writeInt(0); // write a dummy value so the space is allocated

            while(records.hasNext()) {
                if(currentRecord == numRecords)
                    throwRecordNumberException(currentRecord, numRecords);
                recordPositions[currentRecord] = dos.size();
                writeRecord(currentRecord, numFields, numDatapoints, dos, records.next(), tmpArray);
                currentRecord++;
            }

            IntegratedIntegerCODEC codec =  Common.getRowListCodec();
            for(int i = 0; i < numRecords; i += rowListSize) {
                IntWrapper inputPos = new IntWrapper(i);
                IntWrapper outputPos = new IntWrapper(0);
                int howMany = Math.min(numRecords - i, rowListSize);
                int start = recordPositions[i];
                for(int k = i+1; k < i + howMany; k++) {
                    recordPositions[k] -= start;
                    start += recordPositions[k];
                }
                codec.compress(recordPositions, inputPos, howMany, tmpArray, outputPos);
                rowListPositions[i / rowListSize] = dos.size();
                dos.writeShort(outputPos.get());
                for(int j = 0; j < outputPos.get(); j++)
                    dos.writeInt(tmpArray[j]);
            }
            rowListOffset = dos.size();
            for(int i = 0; i < rowListPositions.length; i++)
                dos.writeInt(rowListPositions[i]);
        } finally {
            dos.flush();
            dos.close();
        }

        // Seek and fixup the rowlistposition
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(rowListPointerPosition);
        raf.writeInt(rowListOffset);
        raf.close();

        if(numRecords != currentRecord)
            throwRecordNumberException(currentRecord, numRecords);
    }

    private static void writeRecord(int currentRecord, int numFields, int numDatapoints, DataOutputStream dos, Record r, int[] tmpArray) throws IOException {
        final IntWrapper outPos = new IntWrapper(0);

        for(int field = 0; field < numFields; field++) {
            FieldEncoder fe = r.getEncoder(field);
            outPos.set(0);
            int[] values = r.getValues(field);

            if(values.length != numDatapoints)
                throw new IllegalArgumentException(String.format(
                        "record %d, field %d has %d values; expected %d",
                        currentRecord, field, values.length, numDatapoints));
            fe.encode(values, tmpArray, outPos);
            // TODO: add variable length size encoding
            dos.writeByte(fe.getId());
            if(fe.isVariableLength())
                dos.writeInt(outPos.get());
            for(int i = 0; i < outPos.get(); i++) {
                dos.writeInt(tmpArray[i]);
            }
        }
    }
    private static void writePreamble(
                DataOutputStream dos,
                short rowListSize,
                long epochMs,
                int numDatapoints,
                Interval interval,
                int recordOffset,
                int numRecords,
                String[] fieldNames,
                FieldType[] fieldTypes) throws IOException {
        dos.writeByte('M');
        dos.writeByte('A');
        dos.writeByte('N');
        dos.writeByte('U');
        dos.writeShort(Common.getVersion());
        dos.writeShort(rowListSize);
        dos.writeLong(epochMs);
        dos.writeInt(numDatapoints);
        dos.writeByte(interval.getValue());
        dos.writeInt(recordOffset);
        dos.writeInt(numRecords);
        dos.writeByte((byte)fieldNames.length);
        for(FieldType fieldType : fieldTypes) {
            dos.writeByte(fieldType.getValue());
        }

        for(String name : fieldNames) {
            byte[] utf = name.getBytes("UTF-8");
            dos.writeByte(utf.length);
            for(int i = 0; i < utf.length; i++)
                dos.writeByte(utf[i]);

        }
    }

    private static void throwRecordNumberException(int currentRecord, int numRecords) {
        throw new IllegalArgumentException(String.format(
                "numRecords (%d) != currentRecord (%d)", numRecords, currentRecord));
    }
}
