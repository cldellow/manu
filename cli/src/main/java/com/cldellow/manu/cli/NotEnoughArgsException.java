package com.cldellow.manu.cli;

public class NotEnoughArgsException extends Exception {
    public NotEnoughArgsException() {

    }

    public NotEnoughArgsException(String message, Throwable cause) {
        super(message, cause);
    }
}
