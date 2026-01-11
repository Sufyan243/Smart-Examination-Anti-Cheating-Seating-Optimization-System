package com.examseating.anticheating.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Seat {
    private int row;
    private int col;
    private Student student;  // null if seat is empty
    private RiskLevel riskLevel;
    private double riskScore;  // 0-100 percentage
    
    public boolean isOccupied() {
        return student != null;
    }
    
    public boolean isEmpty() {
        return student == null;
    }
}