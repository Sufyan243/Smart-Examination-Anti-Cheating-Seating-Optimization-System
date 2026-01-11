package com.examseating.anticheating.exception;

public class EmptyCSVException extends CSVParsingException {
    
    public EmptyCSVException() {
        super("CSV file is empty or contains no data");
    }
    
    public EmptyCSVException(String message) {
        super(message);
    }
}