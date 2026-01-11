package com.examseating.anticheating.service;

import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Demonstration service showcasing the complete DSA workflow
 * for exam seating optimization and anti-cheating risk detection
 */
@Service
@Slf4j
public class SeatingOptimizationDemoService {
    
    @Autowired
    private SeatAllocationService seatAllocationService;
    
    @Autowired
    private RiskDetectionService riskDetectionService;
    
    @Autowired
    private HallManagementService hallManagementService;
    
    /**
     * Demonstrates complete optimization workflow with before/after comparison
     */
    public Map<String, Object> demonstrateOptimization(List<Student> students, String hallId, int rows, int cols) {
        log.info("Starting seating optimization demonstration for {} students", students.size());
        
        // Step 1: Create exam hall
        ExamHall hall = hallManagementService.createHall(hallId, rows, cols);
        log.info("Created hall {} with capacity {}", hallId, hall.getCapacity());
        
        // Step 2: Generate random baseline seating
        ExamHall randomHall = new ExamHall(hallId + "_RANDOM", rows, cols);
        seatAllocationService.generateRandomSeating(students, randomHall);
        Map<String, Object> beforeReport = riskDetectionService.generateRiskReport(randomHall);
        
        log.info("Random seating - Risk: {:.2f}%, Conflicts: {}", 
                beforeReport.get("totalRiskScore"), beforeReport.get("totalConflicts"));
        
        // Step 3: Apply optimized seating allocation
        seatAllocationService.allocateSeats(students, hall);
        Map<String, Object> afterReport = riskDetectionService.generateRiskReport(hall);
        
        log.info("Optimized seating - Risk: {:.2f}%, Conflicts: {}", 
                afterReport.get("totalRiskScore"), afterReport.get("totalConflicts"));
        
        // Step 4: Calculate improvement metrics
        double riskImprovement = (Double) beforeReport.get("totalRiskScore") - 
                                (Double) afterReport.get("totalRiskScore");
        int conflictReduction = (Integer) beforeReport.get("totalConflicts") - 
                               (Integer) afterReport.get("totalConflicts");
        
        // Step 5: Compile demonstration results
        Map<String, Object> demoResults = Map.of(
            "hallId", hallId,
            "studentsCount", students.size(),
            "hallCapacity", hall.getCapacity(),
            "beforeOptimization", beforeReport,
            "afterOptimization", afterReport,
            "improvements", Map.of(
                "riskReduction", riskImprovement,
                "conflictReduction", conflictReduction,
                "improvementPercentage", riskImprovement > 0 ? 
                    (riskImprovement / (Double) beforeReport.get("totalRiskScore")) * 100 : 0.0
            )
        );
        
        log.info("Optimization complete - Risk reduced by {:.2f}%, Conflicts reduced by {}", 
                riskImprovement, conflictReduction);
        
        return demoResults;
    }
    
    /**
     * Demonstrates multi-hall distribution scenario
     */
    public Map<String, Object> demonstrateMultiHallDistribution(List<Student> students, 
                                                               List<Map<String, Integer>> hallConfigs) {
        log.info("Starting multi-hall distribution demonstration");
        
        // Create multiple halls
        List<ExamHall> halls = hallConfigs.stream()
            .map(config -> hallManagementService.createHall(
                "HALL_" + config.get("id"), 
                config.get("rows"), 
                config.get("cols")))
            .toList();
        
        // Distribute students across halls
        Map<String, List<Student>> distribution = 
            hallManagementService.distributeStudentsAcrossHalls(students, halls);
        
        // Apply optimization to each hall and collect results
        Map<String, Map<String, Object>> hallResults = distribution.entrySet().stream()
            .filter(entry -> !"OVERFLOW".equals(entry.getKey()))
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    ExamHall hall = hallManagementService.getHallById(entry.getKey());
                    seatAllocationService.allocateSeats(entry.getValue(), hall);
                    return riskDetectionService.generateRiskReport(hall);
                }
            ));
        
        // Compile multi-hall results
        Map<String, Object> multiHallResults = Map.of(
            "totalStudents", students.size(),
            "totalHalls", halls.size(),
            "distribution", distribution,
            "hallResults", hallResults,
            "overallStatistics", hallManagementService.getHallStatistics()
        );
        
        log.info("Multi-hall distribution complete - {} students across {} halls", 
                students.size(), halls.size());
        
        return multiHallResults;
    }
    
    /**
     * Prints a visual representation of the seating arrangement
     */
    public void printSeatingArrangement(ExamHall hall) {
        log.info("Seating arrangement for hall {}:", hall.getHallId());
        
        System.out.println("\n=== SEATING ARRANGEMENT ===");
        System.out.println("Hall: " + hall.getHallId() + " (" + hall.getRows() + "x" + hall.getCols() + ")");
        System.out.println();
        
        for (int row = 0; row < hall.getRows(); row++) {
            for (int col = 0; col < hall.getCols(); col++) {
                if (hall.getSeats()[row][col].isOccupied()) {
                    Student student = hall.getSeats()[row][col].getStudent();
                    String riskIcon = switch (hall.getSeats()[row][col].getRiskLevel()) {
                        case SAFE -> "🟩";
                        case MEDIUM -> "🟨";
                        case HIGH -> "🟥";
                    };
                    System.out.printf("%s%-8s ", riskIcon, student.getSubject().substring(0, Math.min(3, student.getSubject().length())));
                } else {
                    System.out.print("⬜Empty    ");
                }
            }
            System.out.println();
        }
        
        System.out.println("\nLegend: 🟩 Safe | 🟨 Medium Risk | 🟥 High Risk | ⬜ Empty");
        System.out.println("========================\n");
    }
}