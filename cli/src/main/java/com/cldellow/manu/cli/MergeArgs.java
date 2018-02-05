package com.cldellow.manu.cli;

import com.cldellow.manu.common.ArgHolder;
import com.cldellow.manu.common.NotEnoughArgsException;

import java.util.Vector;

class MergeArgs {
    public final String outputFile;
    public final String[] inputFiles;
    public final String[] lossyFields;

    MergeArgs(String[] _args) throws NotEnoughArgsException {
        ArgHolder args = new ArgHolder(_args);
        String errMsg = "must provide output file";

        String _outputFile = null;
        Vector<String> _inputFiles = new Vector<>();
        Vector<String> _lossyFields = null;
        try {
            while (args.hasNext()) {
                String next = args.next();
                if (next == "--lossy")
                    _lossyFields = new Vector<>();
                else if (next.startsWith("--lossy=")) {
                    _lossyFields = new Vector<>();
                    String[] fields = next.substring("--lossy=".length()).split(",");
                    for(String field: fields)
                        if(!field.isEmpty())
                        _lossyFields.add(field);
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

        } catch (NotEnoughArgsException nean) {
            throw new NotEnoughArgsException(errMsg, nean);
        }
    }
}
