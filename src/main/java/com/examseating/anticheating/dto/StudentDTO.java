package com.examseating.anticheating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.examseating.anticheating.model.Student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.util.Map;

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
    
    @Pattern(regexp = "^\\d{4}[FS]$", message = "Batch must be in format YYYY[F/S]")
    private String batch;
    
    @Pattern(regexp = "^BSE$", message = "Department must be BSE")
    private String department;
    
    @Pattern(regexp = "^[A-F]$", message = "Section must be A-F")
    private String section;
    
    @Min(value = 1, message = "Roll number must be at least 1")
    @Max(value = 300, message = "Roll number cannot exceed 300")
    private Integer rollNumber;
    
    public void populateFromFullRollNo() {
        if (rollNo != null) {
            Map<String, Object> parsed = Student.parseRollNumber(rollNo);
            this.batch = (String) parsed.get("batch");
            this.department = (String) parsed.get("department");
            this.rollNumber = (Integer) parsed.get("rollNumber");
            this.section = (String) parsed.get("section");
        }
    }
}