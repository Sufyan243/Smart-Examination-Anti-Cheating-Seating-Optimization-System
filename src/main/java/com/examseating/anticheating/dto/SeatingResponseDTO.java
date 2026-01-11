package com.examseating.anticheating.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatingResponseDTO {
    private String hallId;
    private SeatDTO[][] seats;
    private double totalRiskScore;
    private int conflictCount;
    private int occupiedSeats;
    private int capacity;
}