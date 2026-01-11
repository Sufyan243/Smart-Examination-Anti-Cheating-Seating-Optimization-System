package com.examseating.anticheating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatDTO {
    private int row;
    private int col;
    private StudentDTO student;  // null if empty
    private String riskLevel;    // "SAFE", "MEDIUM", "HIGH"
    private double riskScore;
    private String colorCode;    // "#4CAF50", "#FFC107", "#F44336"
}