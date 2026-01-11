package com.examseating.anticheating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatingRequestDTO {
    @NotBlank(message = "Hall ID is required")
    private String hallId;
    
    @NotEmpty(message = "Students list cannot be empty")
    private List<StudentDTO> students;  // Full student objects with subjects
}