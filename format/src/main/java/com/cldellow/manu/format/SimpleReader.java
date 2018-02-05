package com.cldellow.manu.format;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * A mock implementation of {@link Reader}, useful for testing.
 */
public class SimpleReader implements Reader {
    private final String fileName;
    private final int nullValue;
    private final DateTime from;
    private final DateTime to;
    private final Interval interval;
    private final int recordOffset;
    private final String[] fieldNames;
    private final FieldType[] fieldTypes;
    private final Record[] records;
    private final int numDatapoints;

    public SimpleReader(
            String fileName,
            int nullValue,
            DateTime from,
            DateTime to,
            Interval interval,
            int recordOffset,
            String[] fieldNames,
            FieldType[] fieldTypes,
            Record[] records) {
        if(fileName == null)
            throw new IllegalArgumentException("fileName cannot be null");

        if(from == null)
            throw new IllegalArgumentException("from cannot be null");

        if(to == null)
            throw new IllegalArgumentException("to cannot be null");

        if(interval == null)
            throw new IllegalArgumentException("interval cannot be null");

        if(fieldNames == null)
            throw new IllegalArgumentException("fieldNames cannot be null");

        if(fieldTypes == null)
            throw new IllegalArgumentException("fieldTypes cannot be null");

        if(records == null)
            throw new IllegalArgumentException("records cannot be null");

        if(fieldNames.length == 0)
            throw new IllegalArgumentException("must have at least one field");

        if(fieldNames.length != fieldTypes.length)
            throw new IllegalArgumentException(String.format(
                    "number of fieldNames (%d) must match number of fieldTypes (%d)",
                    fieldNames.length,
                    fieldTypes.length));

        for(int i = 0; i < fieldNames.length; i++) {
            if(fieldNames[i] == null)
                throw new IllegalArgumentException("fieldName " + i + " is null");

            if(fieldTypes[i] == null)
                throw new IllegalArgumentException("fieldType " + i + " is null");
        }
        if (from.isAfter(to))
            throw new IllegalArgumentException(String.format(
                    "from (%s) cannot be after to (%s)",
                    from,
                    to));
        if (recordOffset < 0)
            throw new IllegalArgumentException(String.format(
                    "recordOffset %d cannot be negative",
                    recordOffset));

        this.fileName = fileName;
        this.nullValue = nullValue;
        this.from = from;
        this.to = to;
        this.interval = interval;
        this.recordOffset = recordOffset;
        this.fieldNames = fieldNames;
        this.fieldTypes = fieldTypes;
        this.records = records;
        this.numDatapoints = interval.difference(from, to);
    }

    public int getNullValue() {
        return nullValue;
    }

    public Interval getInterval() {
        return interval;
    }

    public String getFieldName(int i) {
        return fieldNames[i];
    }

    public FieldType getFieldType(int i) {
        return fieldTypes[i];
    }

    public int getRecordOffset() {
        return recordOffset;
    }

    public int getNumRecords() {
        return records.length;
    }

    public int getNumFields() {
        return fieldNames.length;
    }

    public RecordIterator getRecords() {
        return new SimpleRecordIterator();
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }

    public int getNumDatapoints() {
        return numDatapoints;
    }

    public String getFileName() {
        return fileName;
    }

    public Record get(int i) throws Exception {
        if (i < recordOffset || i >= (recordOffset + getNumRecords()))
            throw new NoSuchElementException();

        return records[i - recordOffset];
    }

    class SimpleRecordIterator implements RecordIterator {
        private int pos = 0;
        private boolean ensuredNext = false;
        public void close() throws IOException {

        }

        private void ensureNext() {
            if(pos >= records.length)
                return;

            if(ensuredNext)
                return;

            do {
                if(records[pos] != null)
                    ensuredNext = true;
                else
                    pos++;
            } while(!ensuredNext && pos < records.length);
        }

        public boolean hasNext() {
            ensureNext();
            return pos < records.length;
        }

        public Record next() {
            if (!hasNext())
                throw new NoSuchElementException();

            ensuredNext= false;
            return records[pos++];
        }
    }
}