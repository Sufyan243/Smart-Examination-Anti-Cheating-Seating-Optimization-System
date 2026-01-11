package com.examseating.anticheating.exception;

public class InvalidCSVStructureException extends CSVParsingException {
    
    public InvalidCSVStructureException(String message) {
        super(message);
    }
    
    public InvalidCSVStructureException(String message, Throwable cause) {
        super(message, cause);
    }
}