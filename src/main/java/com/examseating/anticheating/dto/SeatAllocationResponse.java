package com.examseating.anticheating.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SeatAllocationResponse {
    private String hallId;
    private SeatDTO[][] seats;
    private Map<String, Object> riskReport;
    private boolean success;
    private String message;
}