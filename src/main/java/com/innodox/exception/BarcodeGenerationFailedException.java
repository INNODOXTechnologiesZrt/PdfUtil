package com.innodox.exception;

public class BarcodeGenerationFailedException extends RuntimeException {

    public BarcodeGenerationFailedException() {
    }

    public BarcodeGenerationFailedException(String message) {
        super(message);
    }
}
