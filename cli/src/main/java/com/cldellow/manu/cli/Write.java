package com.cldellow.manu.cli;

import com.cldellow.manu.format.Index;
import com.cldellow.manu.format.Interval;

import java.sql.SQLException;
import java.util.Collection;

public class Write {
    public static void main(String[] _args) throws SQLException {
        // TODO: use a proper argparse library
        try {
            ArgHolder args = new ArgHolder(_args);

            String indexFile = args.next();
            String outputFile = args.next();
            long epochMs = Parsers.epochMs(args.next());
            Interval interval = Parsers.interval(args.next());

            Collection<FieldDef> defs = Parsers.fieldDefs(args);
            if(defs.isEmpty())
                throw new NotEnoughArgsException();

            Index i = new Index(indexFile, true);
        } catch(NotEnoughArgsException nae) {
            usage();
            System.exit(1);
        }
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
}
