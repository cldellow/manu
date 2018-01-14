package com.cldellow.manu.cli;

import java.util.Vector;
import java.util.regex.Pattern;

public class ReadArgs {
    public String indexFile;
    public String inputFile;
    public KeyKind keyKind = KeyKind.KEY;
    public final Vector<String> names = new Vector<String>();
    public final Vector<Pattern> patterns = new Vector<Pattern>();
    public final Vector<String> fields = new Vector<String>();
    public final Vector<Integer> ids = new Vector<Integer>();

    ReadArgs(String[] _args) throws NotEnoughArgsException {
        ArgHolder args = new ArgHolder(_args);
        String errMsg = "must provide index file";

        try {
            indexFile = args.next();
            errMsg = "must provide input file";
            inputFile = args.next();

            while (args.hasNext()) {
                String next = args.next();
                try {
                    keyKind = Parsers.keyKind(next);
                    continue;
                } catch (IllegalArgumentException iae) {
                }

                if (next.equals("--key-name") || next.equals("-n")) {
                    errMsg = "must provide argument for --key-name";
                    names.add(args.next());
                    continue;
                }

                if (next.equals("--key-regex") || next.equals("-r")) {
                    errMsg = "must provide argument for --key-regex";
                    patterns.add(Pattern.compile(args.next()));
                    continue;
                }

                if (next.equals("--key-id") || next.equals("-i")) {
                    errMsg = "must provide numeric argument for --key-id";
                    ids.add(Integer.parseInt(args.next()));
                    continue;
                }

                fields.add(next);
            }
        } catch (NotEnoughArgsException nean) {
            throw new NotEnoughArgsException(errMsg, nean);
        }
    }
}