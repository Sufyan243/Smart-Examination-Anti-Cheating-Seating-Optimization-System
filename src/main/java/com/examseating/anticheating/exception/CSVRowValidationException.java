package com.examseating.anticheating.exception;

public class CSVRowValidationException extends CSVParsingException {
    private final String fieldName;
    
    public CSVRowValidationException(String message, int lineNumber) {
        super(message, lineNumber);
        this.fieldName = null;
    }
    
    public CSVRowValidationException(String message, int lineNumber, String fieldName) {
        super(message, lineNumber);
        this.fieldName = fieldName;
    }
    
    public String getFieldName() {
        return fieldName;
    }
}