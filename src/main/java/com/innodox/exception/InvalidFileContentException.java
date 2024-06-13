package com.innodox.exception;

public class InvalidFileContentException extends RuntimeException {
    public InvalidFileContentException(Exception e) {
        super(e);
    }
}
