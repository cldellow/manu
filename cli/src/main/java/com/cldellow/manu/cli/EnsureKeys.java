package com.cldellow.manu.cli;

import com.cldellow.manu.common.Common;
import com.cldellow.manu.format.Index;
import com.cldellow.manu.format.IndexAccessMode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

class EnsureKeys {
    String[] args;

    public EnsureKeys(String[] args) {
        this.args = args;
    }

    public int entrypoint() throws Exception {
        if (args.length != 1 || Common.contains(args, "--help")) {
            usage();
            return 1;
        }

        Index i = new Index(args[0], IndexAccessMode.READ_WRITE_SAFE);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Vector<String> keys = new Vector<String>();
        String key = null;
        while (null != (key = br.readLine())) {
            keys.add(key);
        }

        i.add(keys);
        i.close();
        return 0;
    }

    private static void usage() {
        System.err.println("./ensure-keys index-file");
        System.err.println("  Reads keys from stdin, creates them in index-file if not there.");
    }
}
