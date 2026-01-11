package com.examseating.anticheating.service;

import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.RiskLevel;
import com.examseating.anticheating.model.Seat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RiskDetectionService {
    
    private final SeatAllocationService seatAllocationService;
    
    public RiskDetectionService(SeatAllocationService seatAllocationService) {
        this.seatAllocationService = seatAllocationService;
    }
    
    /**
     * Calculates risk for a specific seat based on same-subject neighbors
     */
    public void calculateRiskForSeat(Seat[][] seats, int row, int col) {
        Seat seat = seats[row][col];
        
        if (!seat.isOccupied()) {
            seat.setRiskLevel(RiskLevel.SAFE);
            seat.setRiskScore(0.0);
            return;
        }
        
        List<Seat> neighbors = seatAllocationService.getAdjacentSeats(seats, row, col);
        int conflictCount = 0;
        
        String currentSubject = seat.getStudent().getSubject();
        
        for (Seat neighbor : neighbors) {
            if (neighbor.isOccupied() && 
                neighbor.getStudent().getSubject().equals(currentSubject)) {
                conflictCount++;
            }
        }
        
        // Map conflict count to risk level
        RiskLevel riskLevel = RiskLevel.fromConflictCount(conflictCount);
        
        // Calculate risk score as percentage
        double riskScore = neighbors.isEmpty() ? 0.0 : (double) conflictCount / neighbors.size() * 100;
        
        seat.setRiskLevel(riskLevel);
        seat.setRiskScore(riskScore);
        
        log.debug("Seat ({}, {}) - Subject: {}, Conflicts: {}, Risk: {}, Score: {:.1f}%", 
                row, col, currentSubject, conflictCount, riskLevel, riskScore);
    }
    
    /**
     * Calculates risk for all seats in the hall
     */
    public void calculateRiskForAllSeats(ExamHall hall) {
        log.info("Calculating risk for all seats in hall {}", hall.getHallId());
        
        Seat[][] seats = hall.getSeats();
        
        for (int row = 0; row < hall.getRows(); row++) {
            for (int col = 0; col < hall.getCols(); col++) {
                calculateRiskForSeat(seats, row, col);
            }
        }
        
        log.info("Risk calculation completed for hall {}", hall.getHallId());
    }
    
    public void calculateRisksForHall(ExamHall hall) {
        calculateRiskForAllSeats(hall);
    }
    
    /**
     * Calculates total hall risk as average of all occupied seats
     */
    public double calculateTotalHallRisk(ExamHall hall) {
        Seat[][] seats = hall.getSeats();
        double totalRisk = 0.0;
        int occupiedSeats = 0;
        
        for (int row = 0; row < hall.getRows(); row++) {
            for (int col = 0; col < hall.getCols(); col++) {
                if (seats[row][col].isOccupied()) {
                    totalRisk += seats[row][col].getRiskScore();
                    occupiedSeats++;
                }
            }
        }
        
        double averageRisk = occupiedSeats > 0 ? totalRisk / occupiedSeats : 0.0;
        
        log.info("Total hall risk: {:.2f}% (based on {} occupied seats)", averageRisk, occupiedSeats);
        return averageRisk;
    }
    
    /**
     * Generates comprehensive risk report for the hall
     */
    public Map<String, Object> generateRiskReport(ExamHall hall) {
        log.info("Generating risk report for hall {}", hall.getHallId());
        
        // First calculate risks for all seats
        calculateRiskForAllSeats(hall);
        
        Map<String, Object> report = new HashMap<>();
        Seat[][] seats = hall.getSeats();
        
        // Basic metrics
        int totalSeats = hall.getCapacity();
        long occupiedSeats = hall.getOccupiedSeats();
        double totalRisk = calculateTotalHallRisk(hall);
        
        // Risk level distribution
        int safeCount = 0, mediumCount = 0, highCount = 0;
        int totalConflicts = 0;
        
        for (int row = 0; row < hall.getRows(); row++) {
            for (int col = 0; col < hall.getCols(); col++) {
                Seat seat = seats[row][col];
                if (seat.isOccupied()) {
                    switch (seat.getRiskLevel()) {
                        case SAFE -> safeCount++;
                        case MEDIUM -> mediumCount++;
                        case HIGH -> highCount++;
                    }
                    
                    // Count actual conflicts
                    List<Seat> neighbors = seatAllocationService.getAdjacentSeats(seats, row, col);
                    String currentSubject = seat.getStudent().getSubject();
                    
                    for (Seat neighbor : neighbors) {
                        if (neighbor.isOccupied() && 
                            neighbor.getStudent().getSubject().equals(currentSubject)) {
                            totalConflicts++;
                        }
                    }
                }
            }
        }
        
        // Each conflict is counted twice (once for each seat), so divide by 2
        totalConflicts = totalConflicts / 2;
        
        // Build report
        report.put("hallId", hall.getHallId());
        report.put("totalSeats", totalSeats);
        report.put("occupiedSeats", occupiedSeats);
        report.put("availableSeats", totalSeats - occupiedSeats);
        report.put("occupancyRate", occupiedSeats > 0 ? (double) occupiedSeats / totalSeats * 100 : 0.0);
        
        report.put("totalRiskScore", totalRisk);
        report.put("totalConflicts", totalConflicts);
        
        // Risk distribution
        Map<String, Integer> riskDistribution = new HashMap<>();
        riskDistribution.put("safe", safeCount);
        riskDistribution.put("medium", mediumCount);
        riskDistribution.put("high", highCount);
        report.put("riskDistribution", riskDistribution);
        
        // Risk percentages
        Map<String, Double> riskPercentages = new HashMap<>();
        if (occupiedSeats > 0) {
            riskPercentages.put("safePercentage", (double) safeCount / occupiedSeats * 100);
            riskPercentages.put("mediumPercentage", (double) mediumCount / occupiedSeats * 100);
            riskPercentages.put("highPercentage", (double) highCount / occupiedSeats * 100);
        } else {
            riskPercentages.put("safePercentage", 0.0);
            riskPercentages.put("mediumPercentage", 0.0);
            riskPercentages.put("highPercentage", 0.0);
        }
        report.put("riskPercentages", riskPercentages);
        
        log.info("Risk report generated: Total Risk: {:.2f}%, Conflicts: {}, Safe: {}, Medium: {}, High: {}", 
                totalRisk, totalConflicts, safeCount, mediumCount, highCount);
        
        return report;
    }
    
    /**
     * Counts total conflicts in the hall
     */
    public int countTotalConflicts(ExamHall hall) {
        Seat[][] seats = hall.getSeats();
        int conflicts = 0;
        
        for (int row = 0; row < hall.getRows(); row++) {
            for (int col = 0; col < hall.getCols(); col++) {
                Seat seat = seats[row][col];
                if (seat.isOccupied()) {
                    List<Seat> neighbors = seatAllocationService.getAdjacentSeats(seats, row, col);
                    String currentSubject = seat.getStudent().getSubject();
                    
                    for (Seat neighbor : neighbors) {
                        if (neighbor.isOccupied() && 
                            neighbor.getStudent().getSubject().equals(currentSubject)) {
                            conflicts++;
                        }
                    }
                }
            }
        }
        
        // Each conflict is counted twice, so divide by 2
        return conflicts / 2;
    }
}