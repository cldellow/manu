package com.cldellow.manu.cli;

import com.cldellow.manu.common.NotEnoughArgsException;
import com.cldellow.manu.format.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;

class Merge {
    private final String[] _args;

    Merge(String[] _args) {
        this._args = _args;
    }

    static final int INTERVAL_MISMATCH = 10;
    static final int FIELD_TYPE_MISMATCH = 11;
    static final int UNKNOWN_FIELD = 12;

    private static void usage() {
        System.out.println("./bin/merge output input-1 ... input-N [--lossy[=fields]]\n" +
                "\n" +
                "The output file contains the union of fields in the inputs, for the minimal time range\n" +
                "that spans all the input files.\n" +
                "\n" +
                "`--lossy` indicates which fields can be made lossy.\n" +
                "\n" +
                "If a datapoint is present in multiple files, the last file on the command line wins.\n" +
                "\n" +
                "The order of fields in the output file is based on the order of discovery in the input files.");
    }

    public int entrypoint() throws Exception {
        try {
            MergeArgs args = new MergeArgs(_args);
            Reader[] readers = new ManuReader[args.inputFiles.length];
            for(int i = 0; i < readers.length; i++)
                readers[i] = new ManuReader(args.inputFiles[i]);
            return handle(readers, args.outputFile, args.lossyFields);
        } catch (NotEnoughArgsException neae) {
            usage();
            return 1;
        }
    }

    public static int handle(Reader[] readers, String outputFile, String[] lossyFieldFilters) throws Exception {
        long minEpochMs = 0;
        int recordOffset = 0;
        int maxRecords = 0;
        long maxEpochMs = 0;
        int nullValue = Integer.MIN_VALUE;
        Interval interval = Interval.YEAR;
        Vector<String> fieldNames = new Vector<>();
        Vector<FieldType> fieldTypes = new Vector<>();
        for (int i = 0; i < readers.length; i++) {
            if (i == 0) {
                nullValue = readers[0].getNullValue();
                minEpochMs = readers[0].getFrom().getMillis();
                maxEpochMs = readers[0].getTo().getMillis();
                interval = readers[0].getInterval();
                recordOffset = readers[0].getRecordOffset();
                maxRecords = readers[0].getRecordOffset() + readers[0].getNumRecords();
            } else {
                if (readers[i].getFrom().getMillis() < minEpochMs)
                    minEpochMs = readers[i].getFrom().getMillis();
                if (readers[i].getTo().getMillis() > maxEpochMs)
                    maxEpochMs = readers[i].getTo().getMillis();
                if (readers[i].getRecordOffset() < recordOffset)
                    recordOffset = readers[i].getRecordOffset();
                if (readers[i].getRecordOffset() + readers[i].getNumRecords() > maxRecords)
                    maxRecords = readers[i].getRecordOffset() + readers[i].getNumRecords();
                if (readers[i].getInterval() != interval) {
                    System.err.println(String.format(
                            "expected all files to have interval %s but %s has interval %s",
                            interval,
                            readers[i].getFileName(),
                            readers[i].getInterval()));
                    return INTERVAL_MISMATCH;
                }
            }

            for (int j = 0; j < readers[i].getNumFields(); j++) {
                boolean found = false;
                for (int k = 0; k < fieldNames.size(); k++) {
                    if (fieldNames.get(k).equals(readers[i].getFieldName(j))) {
                        found = true;

                        if (fieldTypes.get(k) != readers[i].getFieldType(j)) {
                            System.err.println(String.format(
                                    "input files disagree on type for field %s: expected %s but %s says %s",
                                    fieldNames.get(k),
                                    fieldTypes.get(k),
                                    readers[i].getFileName(),
                                    readers[i].getFieldType(k)));
                            return FIELD_TYPE_MISMATCH;
                        }
                    }
                }

                if (!found) {
                    fieldNames.add(readers[i].getFieldName(j));
                    fieldTypes.add(readers[i].getFieldType(j));
                }
            }
        }

        int numDatapoints = interval.difference(new DateTime(minEpochMs, DateTimeZone.UTC), new DateTime(maxEpochMs, DateTimeZone.UTC));
        String[] lossyFields = new String[]{};
        if (lossyFieldFilters != null) {
            if (lossyFieldFilters.length == 0) {
                // All the fields can be lossy.
                lossyFields = fieldNames.toArray(new String[]{});
            } else {
                lossyFields = lossyFieldFilters;
                // Validate that all the lossy fields match known fields.
                for (int i = 0; i < lossyFields.length; i++)
                    if (!fieldNames.contains(lossyFields[i])) {
                        System.err.println(String.format("--lossy for unknown field %s", lossyFields[i]));
                        return UNKNOWN_FIELD;
                    }
            }
        }

        Iterator<Record> recordIterator = new MergeIterator(readers, nullValue, numDatapoints, recordOffset, maxRecords, fieldNames.toArray(new String[] {}), lossyFields);
        Writer.write(
                outputFile,
                (short)1024,
                nullValue,
                minEpochMs,
                numDatapoints,
                interval,
                recordOffset,
                fieldNames.toArray(new String[]{}),
                fieldTypes.toArray(new FieldType[]{}),
                recordIterator
        );
        return 0;
    }
}
