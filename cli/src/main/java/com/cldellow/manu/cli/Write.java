package com.cldellow.manu.cli;

import com.cldellow.manu.common.ArgHolder;
import com.cldellow.manu.common.Common;
import com.cldellow.manu.common.NotEnoughArgsException;
import com.cldellow.manu.format.*;
import me.lemire.integercompression.IntCompressor;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

public class Write {
    private final String[] _args;
    private final FieldEncoder pfor = new PFOREncoder();
    private final FieldEncoder lossy = new AverageEncoder();
    private final IntCompressor ic = new IntCompressor();
    private int numRows;
    private int fields[][][];
    private Vector<FieldDef> defs;

    Write(String[] _args) throws Exception {
        // TODO: use a proper argparse library
        this._args = _args;
    }

    public static void main(String[] _args) throws Exception {
        int rv = new Write(_args).entrypoint();
        System.exit(rv);
    }

    public static void usage() {
        System.out.println("./bin/write keys.index output-file timestamp interval [[field-kind-1] [key-kind-1] field-name-1 field-source-1], ...]\n" +
                "\n" +
                "field-kind is one of:\n" +
                "\n" +
                "--int, represents an integer (default)\n" +
                "--fixed1, represents a number with 1 decimal point\n" +
                "--fixed2, represents a number with 2 decimal points\n" +
                "--lossy, represents an integer; series that have only small numbers with little variation may be lossily stored\n" +
                "\n" +
                "key-kind is one of:\n" +
                "\n" +
                "--key, the key is a string with an entry in keys.index (default)\n" +
                "--id, the key is an integer for an entry in keys.index");
    }

    public int entrypoint() throws Exception {
        if(Common.contains(_args, "--help")) {
            usage();
            return 1;
        }
        
        try {
            ArgHolder args = new ArgHolder(_args);

            String indexFile = args.next();
            String outputFile = args.next();
            long epochMs = Parsers.epochMs(args.next());
            Interval interval = Parsers.interval(args.next());

            // Parsers.fieldDefs consumes the rest of the args array
            defs = Parsers.fieldDefs(args);
            if (defs.isEmpty())
                throw new NotEnoughArgsException();

            Index index = new Index(indexFile, true);
            numRows = index.getNumRows();
            fields = new int[numRows][][];
            for (int i = 0; i < numRows; i++)
                fields[i] = new int[defs.size()][];

            String[] fieldNames = new String[defs.size()];
            FieldType[] fieldTypes = new FieldType[defs.size()];

            int numDatapoints = 0;

            for (int def = 0; def < defs.size(); def++) {
                int currentRow = 0;
                fieldNames[def] = defs.get(def).getName();
                fieldTypes[def] = defs.get(def).getFieldKind().getFieldType();

                FileParser fp = new FileParser(defs.get(def).getFile());
                if (fp.getNumFields() <= 1)
                    throw new IllegalArgumentException(String.format(
                            "%s: not enough fields, expected at least 2, got %d (are you using tabs?)",
                            defs.get(def).getFile(), fp.getNumFields()));

                if (numDatapoints != 0 && numDatapoints != fp.getNumFields() - 1)
                    throw new IllegalArgumentException(String.format(
                            "%s: inconsistent number of fields, expected %d from previous fields, but this file has %d",
                            defs.get(def).getFile(), numDatapoints, fp.getNumFields() - 1));

                numDatapoints = fp.getNumFields() - 1;

                Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
                while (it.hasNext()) {
                    FileParser.RowIterator.Row row = it.next();
                    int id = 0;
                    if (defs.get(def).getKeyKind() == KeyKind.ID)
                        id = Integer.parseInt(row.getKey());
                    else {
                        id = index.get(row.getKey());
                    }

                    if (id < 0 || id >= numRows)
                        throw new IllegalArgumentException(String.format(
                                "%s: row %d cites unknown key %s",
                                defs.get(def).getFile(), currentRow, row.getKey()));

                    fields[currentRow][def] = ic.compress(row.getInts());
                    currentRow++;
                }
            }

            Writer.write(
                    outputFile,
                    (short) 1024,
                    epochMs,
                    numDatapoints,
                    interval,
                    0,
                    numRows,
                    fieldNames,
                    fieldTypes,
                    new RecordIterator());
            System.out.println(String.format(
                    "records: %d, fields: %d, datapoints: %d. bytes/point: %2.3f",
                    numRows, defs.size(), numDatapoints,
                    ((double) (new File(outputFile).length()) / (numRows * defs.size() * numDatapoints))));


        } catch (NotEnoughArgsException nae) {
            usage();
            return 1;
        }

        return 0;
    }

    class RecordIterator implements Iterator<Record> {
        int index = 0;

        public boolean hasNext() {
            return index < numRows;
        }

        public Record next() {
            FieldEncoder[] encoders = new FieldEncoder[defs.size()];

            int[][] newFields = new int[defs.size()][];
            for (int i = 0; i < defs.size(); i++) {
                newFields[i] = ic.uncompress(fields[index][i]);

                encoders[i] = pfor;
                if (defs.get(i).getFieldKind() == FieldKind.LOSSY && AverageEncoder.eligible(newFields[i]))
                    encoders[i] = lossy;
            }

            Record r = new SimpleRecord(index, encoders, newFields);
            index++;
            return r;
        }
    }

}
