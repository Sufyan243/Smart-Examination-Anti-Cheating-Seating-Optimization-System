package com.examseating.anticheating.exception;

public class CSVParsingException extends RuntimeException {
    private final int lineNumber;
    
    public CSVParsingException(String message) {
        super(message);
        this.lineNumber = -1;
    }
    
    public CSVParsingException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }
    
    public CSVParsingException(String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = -1;
    }
    
    public int getLineNumber() {
        return lineNumber;
    }
}