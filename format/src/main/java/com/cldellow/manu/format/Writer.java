package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.differential.*;

import java.io.*;
import java.util.Iterator;

public class Writer {
    private static final int ROW_LIST_SIZE = 8192;

    // TODO: switch to NIO once we have a baseline implementation of writing/reading
    public static void write(
            String file,
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
        final int[] rowListPositions = new int[(numRecords + ROW_LIST_SIZE - 1) / ROW_LIST_SIZE];
        final int[] recordPositions = new int[numRecords];
        int currentRecord = 0;
        final int[] tmpArray = new int[131072];
        try {
            writePreamble(dos, epochMs, numDatapoints, interval, recordOffset, numRecords, fieldNames, fieldTypes);

            // Stash rowListPosition so we can fix it up later.
            rowListPointerPosition = dos.size();
            dos.writeInt(0); // write a dummy value so the space is allocated

            while(records.hasNext()) {
                if(currentRecord == numRecords)
                    throwRecordNumberException(currentRecord, numRecords);
                recordPositions[currentRecord] = dos.size();
                currentRecord++;
                writeRecord(dos, records.next(), numFields, tmpArray);
            }

            IntegratedIntegerCODEC codec =  new
                    IntegratedComposition(
                    new IntegratedBinaryPacking(),
                    new IntegratedVariableByte());
            for(int i = 0; i < numRecords; i += ROW_LIST_SIZE) {
                IntWrapper inputPos = new IntWrapper(i);
                IntWrapper outputPos = new IntWrapper(0);
                int howMany = Math.min(numRecords - i, ROW_LIST_SIZE);
                codec.compress(recordPositions, inputPos, howMany, tmpArray, outputPos);
                rowListPositions[i / ROW_LIST_SIZE] = dos.size();
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

    private static void writeRecord(DataOutputStream dos, Record r, int numFields, int[] tmpArray) throws IOException {
        final IntWrapper outPos = new IntWrapper(0);

        for(int field = 0; field < numFields; field++) {
            FieldEncoder fe = r.getEncoder(field);
            outPos.set(0);
            fe.encode(r.getValues(field), tmpArray, outPos);
            // TODO: add variable length size encoding
            dos.writeByte(fe.id());
            dos.writeInt(outPos.get());
            for(int i = 0; i < outPos.get(); i++) {
                dos.writeInt(tmpArray[i]);
            }
        }
    }
    private static void writePreamble(
                DataOutputStream dos,
                long epochMs,
                int numDatapoints,
                Interval interval,
                int recordOffset,
                int numRecords,
                String[] fieldNames,
                FieldType[] fieldTypes) throws IOException {
        dos.writeLong(epochMs);
        dos.writeInt(numDatapoints);
        dos.writeByte(interval.getValue());
        dos.writeInt(recordOffset);
        dos.writeInt(numRecords);
        dos.writeByte(fieldNames.length);
        for(FieldType fieldType : fieldTypes) {
            dos.writeByte(fieldType.getValue());
        }

        for(String name : fieldNames) {
            dos.writeUTF(name);
        }
    }

    private static void throwRecordNumberException(int currentRecord, int numRecords) {
        throw new IllegalArgumentException(String.format(
                "numRecords (%d) != currentRecord (%d)", numRecords, currentRecord));
    }
}
