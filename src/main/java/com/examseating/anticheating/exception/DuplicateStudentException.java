package com.examseating.anticheating.exception;

public class DuplicateStudentException extends CSVParsingException {
    private final String duplicateRollNo;
    
    public DuplicateStudentException(String duplicateRollNo) {
        super("Duplicate roll number found: " + duplicateRollNo);
        this.duplicateRollNo = duplicateRollNo;
    }
    
    public String getDuplicateRollNo() {
        return duplicateRollNo;
    }
}