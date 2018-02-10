package com.cldellow.manu.format;

import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.differential.IntegratedIntegerCODEC;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Reads a .manu file -- NOT THREAD-SAFE.
 */
public class ManuReader implements Reader {
    private final short rowListSize;
    private final int nullValue;
    private final int numDatapoints;
    private final Interval interval;
    private final int recordOffset;
    private final int numRecords;
    private final String[] fieldNames;
    private final FieldType[] fieldTypes;
    private final int numFields;
    private final DateTime from;
    private final DateTime to;

    private final String fileName;
    private final long rowListOffset;
    private final RowListCursor getCursor;
    private final byte[] iteratorTmp;

    public ManuReader(String fileName) throws FileNotFoundException, IOException, NotManuException {
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
        long epochMs = buffer.getLong();
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
        getCursor = new RowListCursor(channel, buffer);
        iteratorTmp = new byte[4 * (numDatapoints + 256)];

    }

    public Interval getInterval() { return interval; }
    public int getNullValue() { return nullValue; }
    public int getNumRecords() { return numRecords; }
    public int getNumFields() { return numFields; }
    public int getRecordOffset() { return recordOffset; }
    public int getNumDatapoints() { return numDatapoints; }
    public short getRowListSize() { return rowListSize; }
    public String getFieldName(int i) { return fieldNames[i]; }
    public FieldType getFieldType(int i) { return fieldTypes[i]; }
    public RecordIterator getRecords() {
        try {
            RandomAccessFile raf = new RandomAccessFile(fileName, "r");
            FileChannel channel = raf.getChannel();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, raf.length());
            return new ManuRecordIterator(new RowListCursor(channel, buffer));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    public String getFileName() { return fileName; }
    public DateTime getFrom() { return from; }
    public DateTime getTo() { return to; }

    public void close() {
        getCursor.close();
    }

    public Record get(int id) throws FileNotFoundException, IOException {
        getCursor.position(id);
        // NB: we don't close this iterator, as we don't want to close the underlying
        // cursor. Instead, we rely on our callers calling .close on the reader itself
        ManuRecordIterator it = new ManuRecordIterator(getCursor);

        try {
            Record rv = it.next();
            // Due to sparse records, the record we found may not actually be the record we wanted.
            if(rv.getId() == id)
                return rv;
            return null;
        } catch (NoSuchElementException nsee) {
            if(id < numRecords)
                return null;
            throw nsee;
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
// Caches information about rowlist locations so repeat invocations
// of ManuRecord#get don't have to do a bunch of I/O and decompression.
    class RowListCursor {
        private int currentRecord;
        private int currentRowList;
        private int cachedRowList = -1;
        private int[] rowOffsets = null;
        // we re-use this array; but use rowOffsets as the source of truth for
        // availability of more data.
        private int recordsInThisRowList = 0;
        private final int[] _rowOffsets;
        private final int[] _codedRowOffsets;
        // This points at the start of the next rowlist. It's used when inferring the
        // field length of the last field of the last record of the current rowlist.
        private int nextRowListStart = 0;

        private IntegratedIntegerCODEC codec = Common.getRowListCodec();
        private final FileChannel channel;
        private final ByteBuffer buffer;

        RowListCursor(FileChannel channel, ByteBuffer buffer) {
            _rowOffsets = new int[rowListSize];
            _codedRowOffsets = new int[rowListSize * 4 + 32];
            this.channel = channel;
            this.buffer = buffer;
            position(recordOffset);
        }

        void position(int id) {
            this.currentRecord = id - recordOffset;
            this.currentRowList = (id - recordOffset) / rowListSize;
        }

        void ensureRowList() {
            if (rowOffsets == null || cachedRowList != currentRowList) {
                buffer.position((int) (rowListOffset + 4 * currentRowList));
                int rowListStart = buffer.getInt();
                nextRowListStart = rowListStart;
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

                cachedRowList = currentRowList;
            }
        }

        void close() {
            try {
                channel.close();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    class ManuRecordIterator implements RecordIterator {
        private int[] rv = new int[numDatapoints];
        private boolean advancedToNext;
        private Record nextRecord = null;
        private boolean hasNext = false;
        private final RowListCursor cursor;

        ManuRecordIterator(RowListCursor cursor) throws FileNotFoundException, IOException {
            this.cursor = cursor;

            advancedToNext = false;
        }

        public void close() throws IOException {
            cursor.close();
        }

        private void advanceToNext() {
            if (advancedToNext)
                return;

            hasNext = cursor.currentRecord < numRecords;


            while (hasNext && !advancedToNext) {
                hasNext = cursor.currentRecord < numRecords;

                // Do we need to fetch a new rowlist?
                cursor.ensureRowList();

                Record record = null;

                boolean isNextRowListStart = cursor.rowOffsets[cursor.currentRecord % rowListSize] == cursor.nextRowListStart;
                boolean isNextRecord = cursor.currentRecord + 1 < numRecords &&
                        (cursor.currentRecord + 1) % rowListSize != 0 &&
                        cursor.rowOffsets[cursor.currentRecord % rowListSize] == cursor.rowOffsets[(cursor.currentRecord + 1) % rowListSize];

                if (!isNextRowListStart && !isNextRecord) {
                    int recordStart = cursor.rowOffsets[cursor.currentRecord % rowListSize];
                    int recordEnd = 0;
                    if ((cursor.currentRecord % rowListSize != rowListSize - 1) && cursor.currentRecord != numRecords - 1) {
                        recordEnd = cursor.rowOffsets[(cursor.currentRecord + 1) % rowListSize];
                    } else
                        recordEnd = cursor.nextRowListStart;

                    record = new EncodedRecord(recordOffset + cursor.currentRecord, cursor.buffer, recordStart, recordEnd, numFields, iteratorTmp, rv);
                }
                cursor.currentRecord++;

                if (cursor.currentRecord % rowListSize == 0) {
                    cursor.currentRowList++;
                }

                nextRecord = record;

                if (nextRecord != null)
                    advancedToNext = true;

            }
        }

        public boolean hasNext() {
            advanceToNext();
            return hasNext;
        }

        public Record next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            advancedToNext = false;
            Record rv = nextRecord;
            nextRecord = null;
            return rv;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}