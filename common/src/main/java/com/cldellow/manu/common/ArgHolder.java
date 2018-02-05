package com.cldellow.manu.common;

/**
 * A tool to help parse command-line arguments.
 */
public class ArgHolder {
    private String[] args;
    private int i = 0;

    /**
     * Constructs the ArgHolder.
     * @param args The {@code String[] args} from your class's {@code main} method.
     */
    public ArgHolder(String[] args) {
        this.args = args;
    }

    /**
     * Returns whether or not there are more arguments to parse.
     * @return Whether or not there are more arguments.
     */
    public boolean hasNext() {
        return i < args.length;
    }

    /**
     * Returns the next argument.
     * @return The next argument.
     * @throws NotEnoughArgsException When called when {@code hasNext} returns {@code false}.
     */
    public String next() throws NotEnoughArgsException {
        if (!hasNext())
            throw new NotEnoughArgsException();
        i++;
        return args[i - 1];
    }
}
