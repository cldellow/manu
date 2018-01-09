package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;

import java.io.*;
import java.util.Iterator;

public class Writer {
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

        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        DataOutputStream dos = new DataOutputStream(bos);

        int rowListPosition = -1;
        int[] recordPositions = new int[numRecords];
        int currentRecord = 0;
        int[] tmpArray = new int[131072];
        IntWrapper outPos = new IntWrapper(0);
        try {
            dos.writeLong(epochMs);
            dos.writeInt(numDatapoints);
            dos.writeByte(interval.getValue());
            dos.writeInt(recordOffset);
            dos.writeInt(numRecords);
            dos.writeByte(fieldNames.length);
            for(FieldType fieldType : fieldTypes) {
                dos.writeByte(fieldType.getValue());
            }

            // Stash rowListPosition so we can fix it up later.
            rowListPosition = dos.size();
            dos.writeInt(0); // write a dummy value so the space is allocated
            for(String name : fieldNames) {
                dos.writeUTF(name);
            }
            while(records.hasNext()) {
                Record r = records.next();
                if(currentRecord == numRecords)
                    throwRecordNumberException(currentRecord, numRecords);
                recordPositions[currentRecord] = dos.size();
                currentRecord++;
                for(int field = 0; field < fieldNames.length; field++) {
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
        } finally {
            dos.flush();
            dos.close();
        }

        if(numRecords != currentRecord)
            throwRecordNumberException(currentRecord, numRecords);
    }

    private static void throwRecordNumberException(int currentRecord, int numRecords) {
        throw new IllegalArgumentException(String.format(
                "numRecords (%d) != currentRecord (%d)", numRecords, currentRecord));
    }
}
