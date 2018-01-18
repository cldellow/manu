package com.cldellow.manu.serve;

import com.cldellow.manu.common.Common;

import java.util.Map;

public class ManuApp {
    public static void main(String[] _args) throws Exception {
        int rv = entrypoint(_args);
        System.exit(rv);
    }

    public static int entrypoint(String[] _args) throws Exception {
        if(Common.contains(_args, "--help")) {
            usage();
            return 1;
        }

        ServerArgs args = new ServerArgs(_args);
        Map<String, Collection> collections =  Collections.discover(args.datadir);
        if(collections.isEmpty()) {
            System.err.println("no collections found in " + args.datadir);
            return 2;
        }

        Server server = new Server(args.port, collections);
        server.run();
        return 0;
    }

    public static void usage() {
        System.err.println("./serve/bin/serve [--port 6268] [--datadir ./datadir]");
    }
}
