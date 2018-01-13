package com.cldellow.manu.cli;

import com.cldellow.manu.format.Index;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

public class EnsureKeys {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            usage();
            System.exit(1);
        }

        Index i = new Index(args[0], false);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Vector<String> keys = new Vector<String>();
        String key = null;
        while (null != (key = br.readLine())) {
            keys.add(key);
        }

        i.add(keys);
        i.close();
    }

    private static void usage() {
        System.err.println("./ensure-keys index-file");
        System.err.println("  Reads keys from stdin, creates them in index-file if not there.");
    }
}
