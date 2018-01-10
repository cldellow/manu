package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.differential.IntegratedIntegerCODEC;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Reader {
    public final short rowListSize;
    public final long epochMs;
    public final int numDatapoints;
    public final Interval interval;
    public final int recordOffset;
    public final int numRecords;
    public final String[] fieldNames;
    public final FieldType[] fieldTypes;
    public final Iterator<Record> records;
    public final int numFields;

    private final long rowListOffset;
    private final FileChannel channel;
    private final MappedByteBuffer buffer;

    public Reader(String fileName) throws FileNotFoundException, IOException {
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        channel = raf.getChannel();
        buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
        rowListSize = buffer.getShort();
        epochMs = buffer.getLong();
        numDatapoints = buffer.getInt();
        interval = Interval.valueOf(buffer.get());
        recordOffset = buffer.getInt();
        numRecords = buffer.getInt();
        numFields = buffer.get();
        fieldTypes = new FieldType[numFields];
        fieldNames = new String[numFields];
        for (int i = 0; i < numFields; i++) {
            fieldTypes[i] = FieldType.valueOf(buffer.get());
        }
        for (int i = 0; i < numFields; i++) {
            int len = buffer.get();
            byte[] utf = new byte[len];
            buffer.get(utf, 0, len);
            fieldNames[i] = new String(utf, "UTF-8");
        }
        rowListOffset = buffer.getInt();
        records = new RecordIterator(0, 0);
    }

    public Record get(int id) {
        return new RecordIterator(id - recordOffset, (id - recordOffset) / rowListSize).next();
    }

    public void close() throws IOException {
        channel.close();
    }


    /*
    private String getSummary() {
        String fieldTypesStr = "";
        String fieldNamesStr = "";
        for(int i = 0; i < numFields; i++) {
            fieldTypesStr += fieldTypes[i];
            fieldNamesStr += fieldNames[i];
            if(i != numFields - 1) {
                fieldTypesStr += ", ";
                fieldNamesStr += ", ";
            }
        }
        return "epochMs=" + epochMs + ", numDatapoints=" + numDatapoints + ", interval=" + interval + "\n" +
                "recordOffset=" + recordOffset + ", numRecords=" + numRecords + ", numFields=" + numFields + "\n" +
                "fieldTypes=" + fieldTypesStr + ", fieldNames=" + fieldNamesStr + ", rowListOffset=" + rowListOffset;
    }
    */

    class RecordIterator implements Iterator<Record> {
        private IntegratedIntegerCODEC codec = Common.getRowListCodec();
        private int currentRecord;
        private int currentRowList;
        private int[] rowOffsets = null;

        // we re-use this array; but use rowOffsets as the source of truth for
        // availability of more data.
        private int recordsInThisRowList = 0;
        private int[] _rowOffsets = new int[rowListSize];
        private int[] _codedRowOffsets = new int[rowListSize * 4 + 32];
        private int[] rv = new int[numDatapoints];
        private int[] tmp = new int[numDatapoints + 256];

        RecordIterator(int currentRecord, int currentRowList) {
            this.currentRecord = currentRecord;
            this.currentRowList = currentRowList;
        }

        public boolean hasNext() {
            return currentRecord < numRecords;
        }

        public Record next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            // Do we need to fetch a new rowlist?
            if (rowOffsets == null) {
                buffer.position((int) (rowListOffset + 4 * currentRowList));
                int rowListStart = buffer.getInt();
                buffer.position(rowListStart);

                short len = buffer.getShort();
                IntBuffer intBuffer = buffer.asIntBuffer();
                intBuffer.get(_codedRowOffsets, 0, len);
                IntWrapper outLen = new IntWrapper(0);
                codec.uncompress(_codedRowOffsets, new IntWrapper(0), len, _rowOffsets, outLen);
                if (outLen.get() + (currentRecord - currentRecord % rowListSize) > numRecords)
                    throw new IllegalArgumentException("more records found than expected");

                if (outLen.get() != rowListSize && outLen.get() + (currentRecord - currentRecord % rowListSize) != numRecords)
                    throw new IllegalArgumentException(
                            String.format("incomplete rowlist found; had %d records, expected %d", outLen.get(), rowListSize));

                recordsInThisRowList = outLen.get();
                rowOffsets = _rowOffsets;

                for (int i = 1; i < recordsInThisRowList; i++) {
                    rowOffsets[i] += rowOffsets[i - 1];
                }
            }

            Record record = new EncodedRecord(recordOffset + currentRecord, buffer, rowOffsets[currentRecord % rowListSize], numFields, tmp, rv);
            currentRecord += 1;

            if (currentRecord % rowListSize == 0) {
                currentRowList++;
                rowOffsets = null;
            }

            return record;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}