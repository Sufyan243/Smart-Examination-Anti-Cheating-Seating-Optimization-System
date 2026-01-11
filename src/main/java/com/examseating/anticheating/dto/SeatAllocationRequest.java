package com.examseating.anticheating.dto;

import lombok.Data;

import java.util.List;

@Data
public class SeatAllocationRequest {
    private List<StudentDTO> students;
    private String hallId;
    private int rows;
    private int cols;
    private boolean useOptimization = true;
}