package com.examseating.anticheating.exception;

public class StudentOverflowException extends RuntimeException {
    public StudentOverflowException(String message) {
        super(message);
    }
    
    public StudentOverflowException(int studentCount, int capacity) {
        super(String.format("Cannot allocate %d students to hall with capacity %d", studentCount, capacity));
    }
}