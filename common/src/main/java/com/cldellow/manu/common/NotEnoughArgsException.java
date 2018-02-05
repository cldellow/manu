package com.cldellow.manu.common;

/**
 * An exception indicating the user has invoked a program
 * with an insufficient number of arguments.
 */
public class NotEnoughArgsException extends Exception {
    /**
     * Constructs an instance of {@code NotEnoughArgsException}.
     */
    public NotEnoughArgsException() {

    }

    /**
     * Constructs an instance of {@code NotEnoughArgsException}.
     * @param message The message to show the user.
     */
    public NotEnoughArgsException(String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@code NotEnoughArgsException}.
     * @param message The message to show the user.
     * @param cause An underlying cause of the exception.
     */
    public NotEnoughArgsException(String message, Throwable cause) {
        super(message, cause);
    }
}
