package com.examseating.anticheating.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private boolean success;
    private String message;
    private String errorDetails;
    private Integer lineNumber;
    
    public ErrorResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public ErrorResponse(boolean success, String message, String errorDetails) {
        this.success = success;
        this.message = message;
        this.errorDetails = errorDetails;
    }
    
    public ErrorResponse(boolean success, String message, String errorDetails, Integer lineNumber) {
        this.success = success;
        this.message = message;
        this.errorDetails = errorDetails;
        this.lineNumber = lineNumber;
    }
}