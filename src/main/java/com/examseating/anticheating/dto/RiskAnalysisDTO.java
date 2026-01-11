package com.examseating.anticheating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskAnalysisDTO {
    private String hallId;
    private double totalRiskScore;
    private int conflictCount;
    private int safeSeats;
    private int mediumRiskSeats;
    private int highRiskSeats;
    private int occupiedSeats;
    private int capacity;
}