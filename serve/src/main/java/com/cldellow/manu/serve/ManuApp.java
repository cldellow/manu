package com.cldellow.manu.serve;

public class ManuApp {
    public static void main(String[] _args) throws Exception {
        ServerArgs args = new ServerArgs(_args);
        Server server = new Server(args.port, Collections.discover(args.datadir));
        server.run();
    }
}
