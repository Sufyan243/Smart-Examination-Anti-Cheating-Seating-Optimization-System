package com.examseating.anticheating.exception;

public class InvalidHallConfigurationException extends RuntimeException {
    public InvalidHallConfigurationException(String message) {
        super(message);
    }
    
    public InvalidHallConfigurationException(int rows, int cols) {
        super(String.format("Invalid hall configuration: rows=%d, cols=%d. Both must be positive integers.", rows, cols));
    }
}