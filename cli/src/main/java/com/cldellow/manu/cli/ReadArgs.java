package com.cldellow.manu.cli;

import java.util.Vector;
import java.util.regex.Pattern;

import com.cldellow.manu.common.ArgHolder;
import com.cldellow.manu.common.NotEnoughArgsException;
import com.cldellow.manu.format.IndexAccessMode;

class ReadArgs {
    public String indexFile;
    public String inputFile;
    public KeyKind keyKind = KeyKind.KEY;
    public final Vector<String> names = new Vector<String>();
    public final Vector<Pattern> patterns = new Vector<Pattern>();
    public final Vector<String> fields = new Vector<String>();
    public final Vector<Integer> ids = new Vector<Integer>();
    public final IndexAccessMode indexAccessMode;

    ReadArgs(String[] _args) throws NotEnoughArgsException {
        ArgHolder args = new ArgHolder(_args);
        String errMsg = "must provide index file";

        IndexAccessMode mode = IndexAccessMode.READ_ONLY;
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

                if(next.equals("--write")) {
                    mode = IndexAccessMode.READ_WRITE_SAFE;
                    continue;
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
            indexAccessMode = mode;
        } catch (NotEnoughArgsException nean) {
            throw new NotEnoughArgsException(errMsg, nean);
        }
    }

    public boolean filterKeys() {
        return !names.isEmpty() || !patterns.isEmpty() || !ids.isEmpty();
    }

    public boolean[] printFields(String[] fieldNames) {
        boolean[] rv = new boolean[fieldNames.length];
        for (int i = 0; i < rv.length; i++) {
            rv[i] = fields.isEmpty();
        }

        for(int i = 0; i < fields.size(); i++) {
            boolean knownField = false;
            for (int j = 0; j < fieldNames.length; j++) {
                if (fields.get(i).equals(fieldNames[j])) {
                    knownField = true;
                    rv[j] = true;
                    break;
                }
            }

            if(!knownField)
                throw new IllegalArgumentException("unknown field: " + fields.get(i));
        }

        return rv;
    }
}
