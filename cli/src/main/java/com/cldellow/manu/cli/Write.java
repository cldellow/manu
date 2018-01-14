package com.cldellow.manu.cli;

import com.cldellow.manu.format.*;
import me.lemire.integercompression.IntCompressor;
import me.lemire.integercompression.IntWrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class Write {
    public static void main(String[] _args) throws Exception {
        new Write(_args);
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

    private int numRows;
    private int fields[][][];
    private FieldEncoder pfor = new PFOREncoder();
    private FieldEncoder lossy = new AverageEncoder();
    private Vector<FieldDef> defs;
    private IntCompressor ic = new IntCompressor();

    private Write(String[] _args) throws Exception {
        // TODO: use a proper argparse library
        try {
            ArgHolder args = new ArgHolder(_args);

            String indexFile = args.next();
            String outputFile = args.next();
            long epochMs = Parsers.epochMs(args.next());
            Interval interval = Parsers.interval(args.next());

            defs = Parsers.fieldDefs(args);
            if (defs.isEmpty())
                throw new NotEnoughArgsException();

            Index index = new Index(indexFile, true);
            numRows = index.getNumRows();
            fields = new int[numRows][][];
            String[] fieldNames = new String[defs.size()];
            FieldType[] fieldTypes = new FieldType[defs.size()];

            int numDatapoints = 0;
            for (int def = 0; def < defs.size(); def++) {
                int currentRow = 0;
                fieldNames[def] = defs.get(def).getName();
                switch(defs.get(def).getFieldKind()) {
                    case INT:
                    case LOSSY:
                        fieldTypes[def] = FieldType.INT;
                        break;
                    case FIXED1:
                        fieldTypes[def] = FieldType.FIXED1;
                        break;
                    case FIXED2:
                        fieldTypes[def] = FieldType.FIXED2;
                        break;
                }

                FileParser fp = new FileParser(defs.get(def).getFile());
                if(fp.getNumFields() <= 1)
                    throw new IllegalArgumentException(String.format(
                            "%s: not enough fields, expected at least 2, got %d (are you using tabs?)",
                            defs.get(def).getFile(), fp.getNumFields()));

                if(numDatapoints != 0 && numDatapoints != fp.getNumFields() -1)
                    throw new IllegalArgumentException(String.format(
                            "%s: inconsistent number of fields, expected %d from previous fields, but this file has %d",
                            defs.get(def).getFile(), numDatapoints, fp.getNumFields() - 1));

                numDatapoints = fp.getNumFields() - 1;

                Iterator<FileParser.RowIterator.Row> it = fp.getIterator();
                while(it.hasNext()) {
                    fields[currentRow] = new int[defs.size()][];
                    FileParser.RowIterator.Row row = it.next();
                    int id = 0;
                    if(defs.get(def).getKeyKind() == KeyKind.ID)
                        id = Integer.parseInt(row.getKey());
                    else {
                        id = index.get(row.getKey());
                        if(id == -1)
                            throw new IllegalArgumentException(String.format(
                                    "%s: row %d cites unknown key %s",
                                    defs.get(def).getFile(), currentRow, row.getKey()));
                    }


                    fields[currentRow][def] = ic.compress(row.getInts());
                    currentRow++;
                }
            }

            Writer.write(
                    outputFile,
                    (short)1024,
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
                    ((double)(new File(outputFile).length()) / (numRows * defs.size() * numDatapoints))));



        } catch (NotEnoughArgsException nae) {
            usage();
            System.exit(1);
        }

    }

    class RecordIterator implements Iterator<Record> {
        int index = 0;
        private FieldEncoder[] encoders = new FieldEncoder[defs.size()];

        public boolean hasNext() {
            return index < numRows;
        }

        public Record next() {
            int[][] newFields = new int[defs.size()][];
            for(int i = 0; i < defs.size(); i++) {
                encoders[i] = pfor;
                if(defs.get(i).getFieldKind() == FieldKind.LOSSY)
                    encoders[i] = lossy;

                newFields[i] = ic.uncompress(fields[index][i]);
            }

            Record r = new SimpleRecord(index, encoders, newFields);
            index++;
            return r;
        }
    }

}
