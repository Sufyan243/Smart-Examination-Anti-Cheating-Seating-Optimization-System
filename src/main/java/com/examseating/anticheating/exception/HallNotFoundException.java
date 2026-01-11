package com.examseating.anticheating.exception;

public class HallNotFoundException extends RuntimeException {
    public HallNotFoundException(String message) {
        super(message);
    }
    
    public HallNotFoundException(String hallId, String operation) {
        super(String.format("Hall with ID '%s' not found for operation: %s", hallId, operation));
    }
}