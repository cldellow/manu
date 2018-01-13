package com.cldellow.manu.cli;

public class ArgHolder {
    private String[] args;
    private int i = 0;

    public ArgHolder(String[] args) {
        this.args = args;
    }

    public boolean hasNext() {
        return i < args.length;
    }

    public String next() throws NotEnoughArgsException {
        if (!hasNext())
            throw new NotEnoughArgsException();
        i++;
        return args[i - 1];
    }
}
