package com.cldellow.manu.cli;

import com.cldellow.manu.common.ArgHolder;
import com.cldellow.manu.common.NotEnoughArgsException;

import java.util.Vector;

class MergeArgs {
    public final String outputFile;
    public final String[] inputFiles;
    public final String[] lossyFields;
    public final int nullValue;

    MergeArgs(String[] _args) throws NotEnoughArgsException {
        ArgHolder args = new ArgHolder(_args);
        String errMsg = "must provide output file";

        String _outputFile = null;
        Vector<String> _inputFiles = new Vector<>();
        Vector<String> _lossyFields = null;
        int nullValue = Integer.MIN_VALUE;
        try {
            while (args.hasNext()) {
                String next = args.next();
                if (next.equals("--lossy"))
                    _lossyFields = new Vector<>();
                else if (next.startsWith("--lossy=")) {
                    _lossyFields = new Vector<>();
                    String[] fields = next.substring("--lossy=".length()).split(",");
                    for (String field : fields)
                        if (!field.isEmpty())
                            _lossyFields.add(field);
                } else if (next.equals("--null")) {
                    if(!args.hasNext())
                        throw new NotEnoughArgsException("must provide the sentinel value");

                    nullValue = Integer.valueOf(args.next());
                } else if (_outputFile == null)
                    _outputFile = next;
                else
                    _inputFiles.add(next);
            }

            if (_outputFile == null)
                throw new NotEnoughArgsException("must provide output file");

            if (_inputFiles.isEmpty())
                throw new NotEnoughArgsException("must provide input files");
            outputFile = _outputFile;
            inputFiles = _inputFiles.toArray(new String[]{});
            if (_lossyFields != null)
                lossyFields = _lossyFields.toArray(new String[]{});
            else
                lossyFields = null;

            this.nullValue = nullValue;
        } catch (NotEnoughArgsException nean) {
            throw new NotEnoughArgsException(errMsg, nean);
        }
    }
}
