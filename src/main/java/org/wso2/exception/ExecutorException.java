package org.wso2.exception;

public class ExecutorException extends Exception {

    private static final long serialVersionUID = -6272463911272868928L;

    public ExecutorException(String message) {

        super(message);
    }

    public ExecutorException(String message, Throwable cause) {

        super(message, cause);
    }

}


