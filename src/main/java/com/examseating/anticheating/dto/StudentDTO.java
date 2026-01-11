package com.examseating.anticheating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {
    @NotBlank(message = "Roll number is required")
    private String rollNo;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Subject is required")
    private String subject;
}