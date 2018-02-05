package com.cldellow.manu.serve;

import com.cldellow.manu.common.ArgHolder;
import com.cldellow.manu.common.NotEnoughArgsException;

class ServerArgs {
    int port = 6268;
    String datadir = "./datadir";

    public ServerArgs(String[] _args) throws IllegalArgumentException, NotEnoughArgsException {
        ArgHolder args = new ArgHolder(_args);
        while(args.hasNext()) {
            String next = args.next();

            String errMsg =  "";
            try {
                switch (next) {
                    case "--port":
                    case "-p":
                        errMsg ="--port must be followed by the port number";
                        port = Integer.parseInt(args.next());
                        break;
                    case "--datadir":
                    case "-d":
                        errMsg = "--datadir must be followed by the data directory";
                        datadir = args.next();
                        break;
                    default:
                        throw new IllegalArgumentException(String.format(
                                "unknown argument: %s", next));
                }
            } catch (NotEnoughArgsException neae) {
                throw new IllegalArgumentException(errMsg, neae);
            }
        }
    }
}
