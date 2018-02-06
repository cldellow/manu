package com.cldellow.manu.serve;

import com.cldellow.manu.common.Common;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ManuApp {
    public static void main(String[] _args) throws Exception {
        int rv = entrypoint(_args);
        System.exit(rv);
    }

    static int entrypoint(String[] _args) throws Exception {
        CountDownLatch done = new CountDownLatch(1);
        int rv = 0;

        if (Common.contains(_args, "--help")) {
            usage();
            rv = 1;
            done.countDown();
        } else {

            ServerArgs args = new ServerArgs(_args);
            Map<String, Collection> collections = Collections.discover(args.datadir);
            if (collections.isEmpty()) {
                System.err.println("no collections found in " + args.datadir);
                rv = 2;
                done.countDown();
            } else {
                Server server = new Server(args.port, collections);
                server.run();
            }
        }
        done.await();
        return rv;
    }

    static void usage() {
        System.err.println("./serve/bin/serve [--port 6268] [--datadir ./datadir]");
    }
}
