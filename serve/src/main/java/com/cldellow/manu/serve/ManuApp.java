package com.cldellow.manu.serve;

public class ManuApp {
    public static void main(String[] args) throws Exception {

        Server server = new Server(6268);
        server.run();
    }
}
