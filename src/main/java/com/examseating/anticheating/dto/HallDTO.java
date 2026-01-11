package com.examseating.anticheating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HallDTO {
    @NotBlank(message = "Hall ID is required")
    private String hallId;
    
    @Min(value = 1, message = "Rows must be at least 1")
    @Max(value = 20, message = "Rows cannot exceed 20")
    private int rows;
    
    @Min(value = 1, message = "Columns must be at least 1")
    @Max(value = 20, message = "Columns cannot exceed 20")
    private int cols;
    
    private int capacity;
    private int occupiedSeats;
}