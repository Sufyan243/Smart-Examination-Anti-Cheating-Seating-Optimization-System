package com.examseating.anticheating.exception;

public class InvalidRollNumberFormatException extends RuntimeException {
    private final String rollNumber;
    private final String reason;

    public InvalidRollNumberFormatException(String rollNumber, String reason) {
        super(String.format("Invalid roll number format '%s': %s", rollNumber, reason));
        this.rollNumber = rollNumber;
        this.reason = reason;
    }

    public InvalidRollNumberFormatException(String rollNumber, String reason, Throwable cause) {
        super(String.format("Invalid roll number format '%s': %s", rollNumber, reason), cause);
        this.rollNumber = rollNumber;
        this.reason = reason;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getReason() {
        return reason;
    }
}