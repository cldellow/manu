package com.cldellow.manu.cli;

import com.cldellow.manu.common.Common;
import com.cldellow.manu.format.*;
import org.joda.time.DateTime;

import java.util.Iterator;
import java.util.NoSuchElementException;

class MergeIterator implements Iterator<Record> {
    private final Reader[] readers;
    private final int nullValue;
    private final int numDatapoints;
    private final int recordOffset;
    private final int maxRecords;
    private final String[] fieldNames;
    private final String[] lossyFields;
    private final int numFields;
    private final FieldEncoder[] encoders;
    private final FieldEncoder[] templateEncoders;
    private final boolean[] lossyOk;
    private final int[] offsets;
    private int current;
    private boolean ensuredNext;

    MergeIterator(
            Reader[] readers,
            int nullValue,
            int numDatapoints,
            int recordOffset,
            int maxRecords,
            String[] fieldNames,
            String[] lossyFields) {
        this.readers = readers;
        this.nullValue = nullValue;
        this.numDatapoints = numDatapoints;
        this.recordOffset = recordOffset;
        this.maxRecords = maxRecords;
        this.fieldNames = fieldNames;
        this.lossyFields = lossyFields;
        this.numFields = fieldNames.length;

        DateTime startDate = readers[0].getFrom();
        for (int i = 0; i < readers.length; i++) {
            if (readers[i].getFrom().isBefore(startDate))
                startDate = readers[i].getFrom();
        }

        offsets = new int[readers.length];
        for (int i = 0; i < readers.length; i++)
            offsets[i] = readers[i].getInterval().difference(startDate, readers[i].getFrom());

        encoders = new FieldEncoder[fieldNames.length];
        templateEncoders = new FieldEncoder[fieldNames.length];
        lossyOk = new boolean[fieldNames.length];

        for (int i = 0; i < encoders.length; i++) {
            if (numDatapoints == 1)
                encoders[i] = new SingleValueEncoder();
            else {
                lossyOk[i] = Common.contains(lossyFields, fieldNames[i]);
                encoders[i] = new PFOREncoder();
            }
            templateEncoders[i] = encoders[i];
        }

        current = recordOffset;
    }

    private void ensureNext() {
        try {
            while (!ensuredNext && current < maxRecords) {
                for (int readerIndex = 0; readerIndex < readers.length; readerIndex++) {
                    if (current < readers[readerIndex].getRecordOffset() ||
                            current >= readers[readerIndex].getRecordOffset() + readers[readerIndex].getNumRecords())
                        continue;
                    Record r = readers[readerIndex].get(current);
                    if (r == null)
                        continue;

                    ensuredNext = true;
                    break;
                }

                if (ensuredNext)
                    break;

                current++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasNext() {
        ensureNext();
        return current < maxRecords;
    }

    public Record next() {
        if (!hasNext())
            throw new NoSuchElementException();

        ensuredNext = false;
        try {
            int[][] values = new int[fieldNames.length][];

            for (int i = 0; i < numFields; i++) {
                values[i] = new int[numDatapoints];
                for (int j = 0; j < values[i].length; j++)
                    values[i][j] = nullValue;
            }

            for (int readerIndex = 0; readerIndex < readers.length; readerIndex++) {
                if (current < readers[readerIndex].getRecordOffset() ||
                        current >= readers[readerIndex].getRecordOffset() + readers[readerIndex].getNumRecords())
                    continue;
                Record r = readers[readerIndex].get(current);
                if (r == null)
                    continue;

                for (int i = 0; i < readers[readerIndex].getNumFields(); i++) {
                    String fieldName = readers[readerIndex].getFieldName(i);
                    int valuesIndex = -1;
                    for (int j = 0; j < fieldNames.length; j++)
                        if (fieldName.equals(fieldNames[j]))
                            valuesIndex = j;

                    int[] fieldValues = r.getValues(i);
                    for (int j = 0; j < fieldValues.length; j++) {
                        values[valuesIndex][offsets[readerIndex] + j] = fieldValues[j];
                    }
                }
            }

            for (int i = 0; i < encoders.length; i++) {
                encoders[i] = templateEncoders[i];
                if (lossyOk[i] && AverageEncoder.eligible(values[i]))
                    encoders[i] = new AverageEncoder();
            }
            Record rv = new SimpleRecord(current, encoders, values);
            current++;
            return rv;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
