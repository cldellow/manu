package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.differential.IntegratedIntegerCODEC;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.Closeable;
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
    public final int nullValue;
    public final long epochMs;
    public final int numDatapoints;
    public final Interval interval;
    public final int recordOffset;
    public final int numRecords;
    public final String[] fieldNames;
    public final FieldType[] fieldTypes;
    public final RecordIterator records;
    public final int numFields;
    public final DateTime from;
    public final DateTime to;

    public final String fileName;
    private final long rowListOffset;

    public Reader(String fileName) throws FileNotFoundException, IOException, NotManuException {
        this.fileName = fileName;
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        FileChannel channel = raf.getChannel();
        MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, raf.length());

        byte[] magicPreamble = new byte[]{'M', 'A', 'N', 'U', 0, (byte) Common.getVersion()};
        for (int i = 0; i < magicPreamble.length; i++)
            if (!buffer.hasRemaining() || buffer.get() != magicPreamble[i])
                throw new NotManuException();

        rowListSize = buffer.getShort();
        nullValue = buffer.getInt();
        epochMs = buffer.getLong();
        numDatapoints = buffer.getInt();
        interval = Interval.valueOf(buffer.get());

        from = new DateTime(epochMs, DateTimeZone.UTC);
        to = interval.add(from, numDatapoints);

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
        channel.close();
        records = new RecordIterator(0, 0);
    }

    public Record get(int id) throws FileNotFoundException, IOException {
        RecordIterator it = new RecordIterator(
                id - recordOffset,
                (id - recordOffset) / rowListSize
        );

        try {
            return it.next();
        } finally {
            it.close();
        }
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

    public class RecordIterator implements Iterator<Record>, Closeable {
        private final RandomAccessFile raf;
        private final FileChannel channel;
        private final MappedByteBuffer buffer;
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
        private byte[] tmp = new byte[4 * (numDatapoints + 256)];

        // This points at the start of the next rowlist. It's used when inferring the
        // field length of the last field of the last record of the current rowlist.
        private int nextRowListStart = 0;

        RecordIterator(int currentRecord, int currentRowList) throws FileNotFoundException, IOException {
            this.currentRecord = currentRecord;
            this.currentRowList = currentRowList;

            raf = new RandomAccessFile(fileName, "r");
            channel = raf.getChannel();
            buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
        }

        public void close() throws IOException {
            channel.close();
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
                if(!buffer.hasRemaining()) {

                    buffer.position((int)rowListOffset);
                    nextRowListStart = buffer.getInt();

                    System.out.println("no row lists remaining, so nextRowListStart = first rowList: " + nextRowListStart);
                } else {
                    nextRowListStart = buffer.getInt();
                    System.out.println("row lists remaining, so nextRowListStart = " + nextRowListStart);
                }
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

            Record record = null;

            boolean isNegative = rowOffsets[currentRecord % rowListSize] < 0;
            boolean matchesPrevious = currentRecord % rowListSize > 0 &&
                    (rowOffsets[currentRecord % rowListSize] == rowOffsets[(currentRecord % rowListSize) - 1]);

            if (!isNegative && !matchesPrevious) {
                int recordStart = rowOffsets[currentRecord % rowListSize];
                int recordEnd = 0;
                if ((currentRecord % rowListSize != rowListSize - 1) && currentRecord != numRecords - 1) {
                    recordEnd = rowOffsets[(currentRecord + 1) % rowListSize];
                } else {
                    System.out.println("computing recordEnd from nextRowListStart=" + nextRowListStart + ", recordStart=" + recordStart);
                    recordEnd = nextRowListStart;
                }
                record = new EncodedRecord(recordOffset + currentRecord, buffer, recordStart, recordEnd, numFields, tmp, rv);
            }
            currentRecord++;

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