package com.examseating.anticheating.exception;

import com.examseating.anticheating.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(HallNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleHallNotFound(HallNotFoundException ex) {
        log.error("Hall not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, "Hall not found", ex.getMessage()));
    }
    
    @ExceptionHandler(InvalidHallConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidHallConfiguration(InvalidHallConfigurationException ex) {
        log.error("Invalid hall configuration: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Invalid hall configuration", ex.getMessage()));
    }
    
    @ExceptionHandler(StudentOverflowException.class)
    public ResponseEntity<ErrorResponse> handleStudentOverflow(StudentOverflowException ex) {
        log.error("Student overflow: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Student overflow", ex.getMessage()));
    }
    
    @ExceptionHandler(DuplicateStudentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateStudent(DuplicateStudentException ex) {
        log.error("Duplicate student: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Duplicate student found", ex.getMessage()));
    }
    
    @ExceptionHandler(CSVParsingException.class)
    public ResponseEntity<ErrorResponse> handleCSVParsing(CSVParsingException ex) {
        log.error("CSV parsing error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "CSV parsing failed", ex.getMessage(), ex.getLineNumber()));
    }
    
    @ExceptionHandler(InvalidCSVStructureException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCSVStructure(InvalidCSVStructureException ex) {
        log.error("Invalid CSV structure: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Invalid CSV structure", ex.getMessage()));
    }
    
    @ExceptionHandler(EmptyCSVException.class)
    public ResponseEntity<ErrorResponse> handleEmptyCSV(EmptyCSVException ex) {
        log.error("Empty CSV: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Empty CSV file", ex.getMessage()));
    }
    
    @ExceptionHandler(CSVRowValidationException.class)
    public ResponseEntity<ErrorResponse> handleCSVRowValidation(CSVRowValidationException ex) {
        log.error("CSV row validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Row validation failed", ex.getMessage(), ex.getLineNumber()));
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.error("File too large: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(false, "File too large", "Maximum file size is 10MB"));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Validation failed", errors.toString()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, "Internal server error", "An unexpected error occurred"));
    }
}