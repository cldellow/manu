package com.cldellow.manu.cli;

public class CliApp {
    public static void main(String[] args) throws Exception {
        int rv = new CliApp(args).entrypoint();
        System.exit(rv);
    }

    final String[] _args;
    CliApp(String[] args) {
        _args = args;
    }

    int entrypoint() throws Exception {
        if(_args.length == 0) {
            usage();
            return 1;
        }

        String[] cmdArgs = new String[_args.length - 1];
        System.arraycopy(_args, 1, cmdArgs, 0, cmdArgs.length);

        switch(_args[0]) {
            case "ensure-keys":
                return new EnsureKeys(cmdArgs).entrypoint();
            case "read":
                return new Read(cmdArgs).entrypoint();
            case "write":
                return new Write(cmdArgs).entrypoint();
            default:
                usage();
                return 1;
        }
    }

    public void usage() {
        System.err.println("manu-cli <command> <args>");
        System.err.println();
        System.err.println("Valid commands:");
        System.err.println("  ensure-keys");
        System.err.println("  read");
        System.err.println("  write");
    }
}
