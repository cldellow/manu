package com.cldellow.manu.format;

import com.cldellow.manu.common.IntVector;
import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.differential.IntegratedIntegerCODEC;

import java.io.*;
import java.util.Iterator;

public class Writer {
    private static final int OFFSET_NUM_DATAPOINTS = 20;
    private static final int OFFSET_NUM_RECORDS = 29;

    // TODO: switch to NIO once we have a baseline implementation of writing/reading
    public static void write(
            String file,
            long epochMs,
            Interval interval,
            String[] fieldNames,
            FieldType[] fieldTypes,
            Iterator<Record> records) throws FileNotFoundException, IOException, Exception {
        write(file, (short) 1024, Integer.MIN_VALUE, epochMs, -1, interval, 0, fieldNames, fieldTypes, records);
    }

    public static void write(
            String file,
            short rowListSize,
            int nullValue,
            long epochMs,
            int numDatapoints,
            Interval interval,
            int recordOffset,
            String[] fieldNames,
            FieldType[] fieldTypes,
            Iterator<Record> records) throws FileNotFoundException, IOException, Exception {
        if (fieldNames.length != fieldTypes.length)
            throw new IllegalArgumentException(
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
        final IntVector rowListPositions = new IntVector();
        final IntVector recordPositions = new IntVector();
        int currentRecord = 0;
        final int[] intArray = new int[Common.INT_ARRAY_SIZE];
        final byte[] byteArray = new byte[intArray.length * 4];


        try {
            writePreamble(dos, rowListSize, nullValue, epochMs, interval, recordOffset, fieldNames, fieldTypes);

            // Stash rowListPosition so we can fix it up later.
            rowListPointerPosition = dos.size();
            dos.writeInt(0); // write a dummy value so the space is allocated

            while (records.hasNext()) {
                recordPositions.add(dos.size());
                Record record = records.next();
                if (record != null) {
                    if (numDatapoints == -1)
                        numDatapoints = record.getValues(0).length;
                    writeRecord(currentRecord, numFields, numDatapoints, dos, record, byteArray);
                } else {
                    // If it's the first record in this rowlist, use negative to mark null,
                    // (so we can invert it to get the start of data).
                    // Otherwise, use the previous record's position.
                    if (currentRecord % rowListSize == 0)
                        recordPositions.set(currentRecord, recordPositions.get(currentRecord) * -1);
                    else
                        recordPositions.set(currentRecord, recordPositions.get(currentRecord - 1));
                }
                currentRecord++;

                if (currentRecord % rowListSize == 0)
                    writeRowList(dos, rowListSize, currentRecord, recordPositions, rowListPositions, intArray);

            }

            if (currentRecord % rowListSize != 0)
                writeRowList(dos, rowListSize, currentRecord, recordPositions, rowListPositions, intArray);

            rowListOffset = dos.size();
            for (int i = 0; i < rowListPositions.getSize(); i++)
                dos.writeInt(rowListPositions.get(i));
        } finally {
            dos.flush();
            dos.close();
        }

        if (numDatapoints == -1)
            throw new IllegalArgumentException("unable to infer numDataPoints");
        // Seek and fixup the rowlistposition
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(rowListPointerPosition);
        raf.writeInt(rowListOffset);
        raf.seek(OFFSET_NUM_DATAPOINTS);
        raf.writeInt(numDatapoints);
        raf.seek(OFFSET_NUM_RECORDS);
        raf.writeInt(currentRecord);
        raf.close();
    }

    // writeRowList(dos, rowListSize,currentRecord, recordPositions, rowListPositions);
    private static void writeRowList(
            DataOutputStream dos,
            int rowListSize,
            int currentRecord,
            IntVector recordPositions,
            IntVector rowListPositions,
            int[] intArray) throws Exception {
        // NB: currentRecord points past the current record; ie it's the # of records
        IntegratedIntegerCODEC codec = Common.getRowListCodec();
        int firstRecord = currentRecord - rowListSize;
        if (currentRecord % rowListSize != 0)
            firstRecord = currentRecord / rowListSize * rowListSize;

        IntWrapper inputPos = new IntWrapper(firstRecord);
        IntWrapper outputPos = new IntWrapper(0);
        int howMany = Math.min(currentRecord - firstRecord, rowListSize);
        int start = recordPositions.get(firstRecord);
        for (int k = firstRecord + 1; k < firstRecord + howMany; k++) {
            recordPositions.set(k, recordPositions.get(k) - start);
            start += recordPositions.get(k);
        }
        codec.compress(recordPositions.getArray(), inputPos, howMany, intArray, outputPos);
        rowListPositions.add(dos.size());
        dos.writeShort(outputPos.get());
        for (int j = 0; j < outputPos.get(); j++)
            dos.writeInt(intArray[j]);
    }

    private static void writeRecord(int currentRecord, int numFields, int numDatapoints, DataOutputStream dos, Record r, byte[] byteArray) throws IOException {
        final IntWrapper outPos = new IntWrapper(0);

        for (int field = 0; field < numFields; field++) {
            FieldEncoder fe = r.getEncoder(field);
            outPos.set(0);
            int[] values = r.getValues(field);

            if (values.length != numDatapoints)
                throw new IllegalArgumentException(String.format(
                        "record %d, field %d has %d values; expected %d",
                        currentRecord, field, values.length, numDatapoints));
            fe.encode(values, byteArray, outPos);

            // If the encoder has a fixed length, don't write a size field.
            // Otherwise, write a size field using a byte, short or int
            // as appropriate.
            int length = 0;
            if (fe.isVariableLength())
                length = outPos.get();

            byte newId = LengthOps.encode((byte) fe.getId(), length);
            dos.writeByte(newId);
            if (fe.isVariableLength()) {
                int lengthSize = LengthOps.lengthSize(length);
                if (lengthSize == 0)
                    dos.writeByte(length);
                else if (lengthSize == 1)
                    dos.writeShort(length);
                else
                    dos.writeInt(length);
            }
            for (int i = 0; i < outPos.get(); i++) {
                dos.writeByte(byteArray[i]);
            }
        }
    }

    private static void writePreamble(
            DataOutputStream dos,
            short rowListSize,
            int nullValue,
            long epochMs,
            Interval interval,
            int recordOffset,
            String[] fieldNames,
            FieldType[] fieldTypes) throws IOException {
        dos.writeByte('M');
        dos.writeByte('A');
        dos.writeByte('N');
        dos.writeByte('U');
        dos.writeShort(Common.getVersion());
        dos.writeShort(rowListSize);
        dos.writeInt(nullValue);
        dos.writeLong(epochMs);
        dos.writeInt(0); // placeholder for numDatapoints
        dos.writeByte(interval.getValue());
        dos.writeInt(recordOffset);
        dos.writeInt(0); // placeholder for numRecords
        dos.writeByte((byte) fieldNames.length);
        for (FieldType fieldType : fieldTypes) {
            dos.writeByte(fieldType.getValue());
        }

        for (String name : fieldNames) {
            byte[] utf = name.getBytes("UTF-8");
            dos.writeByte(utf.length);
            for (int i = 0; i < utf.length; i++)
                dos.writeByte(utf[i]);

        }
    }

    private static void throwRecordNumberException(int currentRecord, int numRecords) {
        throw new IllegalArgumentException(String.format(
                "numRecords (%d) != currentRecord (%d)", numRecords, currentRecord));
    }
}
