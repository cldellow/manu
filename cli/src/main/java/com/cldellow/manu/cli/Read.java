package com.cldellow.manu.cli;

import com.cldellow.manu.format.Index;
import com.cldellow.manu.format.Reader;
import com.cldellow.manu.format.Record;
import com.cldellow.manu.common.NotEnoughArgsException;

import java.util.Iterator;

public class Read {
    ReadArgs args;

    private Read(String[] _args) throws Exception {
        try {
            this.args = new ReadArgs(_args);

            Reader reader = new Reader(args.inputFile);
            Index index = new Index(args.indexFile, true);
            boolean filterKeys = args.filterKeys();
            boolean[] printFields = args.printFields(reader.fieldNames);

            // TODO: consider only doing a full scan for --key-regex,
            // for --key-id and --key-name, convert to ID and lookup by ID
            Iterator<Record> it = reader.records;
            while (it.hasNext()) {
                Record record = it.next();
                boolean printRecord = !filterKeys;
                String key = null;

                if (filterKeys) {
                    for (int i = 0; i < args.ids.size(); i++)
                        if (record.getId() == args.ids.get(i).intValue()) {
                            printRecord = true;
                            break;
                        }

                    if (!args.names.isEmpty() || !args.patterns.isEmpty()) {
                        key = index.get(record.getId());

                        for (int i = 0; i < args.names.size(); i++)
                            if (key.equals(args.names.get(i))) {
                                printRecord = true;
                                break;
                            }

                        for (int i = 0; i < args.patterns.size(); i++)
                            if (args.patterns.get(i).matcher(key).find()) {
                                printRecord = true;
                                break;
                            }
                    }
                }

                if (!printRecord)
                    continue;

                for (int i = 0; i < printFields.length; i++) {
                    if (!printFields[i])
                        continue;

                    if (args.keyKind == KeyKind.ID)
                        System.out.print(record.getId());
                    else {
                        if (key == null)
                            key = index.get(record.getId());

                        System.out.print(index.get(record.getId()));
                    }
                    int[] datapoints = record.getValues(i);
                    for (int j = 0; j < reader.numDatapoints; j++) {
                        System.out.print('\t');
                        System.out.print(datapoints[j]);
                    }
                    System.out.println();
                }


            }
        } catch (NotEnoughArgsException neae) {
            usage();
            System.exit(1);
        }
    }

    private static void usage() {
        System.out.println("./bin/read keys.index input-file [key-kind] [[--key-name needle-1], ...] [[--key-regex needle-1], ...] [[field-name-1], ...]\n" +
                "\n" +
                "key-kind is one of:\n" +
                "\n" +
                "--key, the key is printed as a string (default)\n" +
                "--id, the key is printed as an integer\n" +
                "\n" +
                "--key-name (-n) and --key-regex (-r) filter the results to only keys that are exactly that string or that match a regex with that string.\n" +
                "\n" +
                "If no field names are specified, all fields are printed in the order they are present in the file.");
    }

    public static void main(String[] args) throws Exception {
        new Read(args);
    }
}
