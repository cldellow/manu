package com.cldellow.manu.cli;

public class Read {
    public void main(String[] args) throws Exception {
        new Read(args);
    }

    private Read(String[] _args) {
        ArgHolder args = new ArgHolder(_args);
    }
}
