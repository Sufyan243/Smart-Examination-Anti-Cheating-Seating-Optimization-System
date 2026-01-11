package com.examseating.anticheating.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExamHall {
    private String hallId;
    private int rows;
    private int cols;
    private Seat[][] seats;
    
    public ExamHall(String hallId, int rows, int cols) {
        this.hallId = hallId;
        this.rows = rows;
        this.cols = cols;
        this.seats = new Seat[rows][cols];
        initializeSeats();
    }
    
    private void initializeSeats() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                seats[i][j] = new Seat(i, j, null, RiskLevel.SAFE, 0.0);
            }
        }
    }
    
    public int getCapacity() {
        return rows * cols;
    }
    
    public int getOccupiedSeats() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (seats[i][j].isOccupied()) count++;
            }
        }
        return count;
    }
    
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
}