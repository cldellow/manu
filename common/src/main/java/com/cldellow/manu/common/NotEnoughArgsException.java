package com.cldellow.manu.common;

public class NotEnoughArgsException extends Exception {
    public NotEnoughArgsException() {

    }

    public NotEnoughArgsException(String message, Throwable cause) {
        super(message, cause);
    }
}
