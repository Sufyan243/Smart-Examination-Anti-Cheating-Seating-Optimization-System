package com.examseating.anticheating.service;

import com.examseating.anticheating.fixtures.TestDataFixtures;
import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.RiskLevel;
import com.examseating.anticheating.model.Seat;
import com.examseating.anticheating.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RiskDetectionServiceTest {
    
    private RiskDetectionService riskDetectionService;
    private SeatAllocationService seatAllocationService;
    
    @BeforeEach
    void setUp() {
        seatAllocationService = new SeatAllocationService();
        riskDetectionService = new RiskDetectionService(seatAllocationService);
    }
    
    @Test
    void testCalculateRiskForSeat_NoConflicts() {
        // Given: Seat with no same-subject neighbors
        Seat[][] seats = TestDataFixtures.createSeatsWithNoConflicts();
        
        // When
        riskDetectionService.calculateRiskForSeat(seats, 5, 5);
        
        // Then
        assertEquals(RiskLevel.SAFE, seats[5][5].getRiskLevel());
        assertEquals(0.0, seats[5][5].getRiskScore());
    }
    
    @Test
    void testCalculateRiskForSeat_OneConflict() {
        // Given: Seat with 1 same-subject neighbor
        Seat[][] seats = TestDataFixtures.createSeatsWithOneConflict();
        
        // When
        riskDetectionService.calculateRiskForSeat(seats, 5, 5);
        
        // Then
        assertEquals(RiskLevel.MEDIUM, seats[5][5].getRiskLevel());
        assertEquals(25.0, seats[5][5].getRiskScore(), 0.1);
    }
    
    @Test
    void testCalculateRiskForSeat_MultipleConflicts() {
        // Given: Seat with 2+ same-subject neighbors
        Seat[][] seats = TestDataFixtures.createSeatsWithMultipleConflicts();
        
        // When
        riskDetectionService.calculateRiskForSeat(seats, 5, 5);
        
        // Then
        assertEquals(RiskLevel.HIGH, seats[5][5].getRiskLevel());
        assertTrue(seats[5][5].getRiskScore() >= 50.0);
    }
    
    @Test
    void testCalculateRiskForSeat_CornerSeat() {
        // Given: Corner seat (only 2 neighbors)
        Seat[][] seats = TestDataFixtures.createSeats(10, 10);
        seats[0][0].setStudent(new Student("S001", "Alice", "Math"));
        seats[0][1].setStudent(new Student("S002", "Bob", "Physics"));
        seats[1][0].setStudent(new Student("S003", "Charlie", "Chemistry"));
        
        // When
        riskDetectionService.calculateRiskForSeat(seats, 0, 0);
        
        // Then: Should handle fewer neighbors correctly
        assertNotNull(seats[0][0].getRiskLevel());
        assertEquals(RiskLevel.SAFE, seats[0][0].getRiskLevel());
    }
    
    @Test
    void testCalculateRiskForSeat_EmptySeat() {
        // Given: Empty seat
        Seat[][] seats = TestDataFixtures.createSeats(5, 5);
        
        // When
        riskDetectionService.calculateRiskForSeat(seats, 2, 2);
        
        // Then
        assertEquals(RiskLevel.SAFE, seats[2][2].getRiskLevel());
        assertEquals(0.0, seats[2][2].getRiskScore());
    }
    
    @Test
    void testCalculateRiskForAllSeats() {
        // Given
        ExamHall hall = new ExamHall("TEST_HALL", 3, 3);
        hall.getSeats()[1][1].setStudent(new Student("S001", "Alice", "Math"));
        hall.getSeats()[0][1].setStudent(new Student("S002", "Bob", "Physics"));
        
        // When
        riskDetectionService.calculateRiskForAllSeats(hall);
        
        // Then
        assertNotNull(hall.getSeats()[1][1].getRiskLevel());
        assertNotNull(hall.getSeats()[0][1].getRiskLevel());
    }
    
    @Test
    void testCalculateTotalHallRisk() {
        // Given
        ExamHall hall = TestDataFixtures.createHallWithKnownRisks();
        
        // When
        double totalRisk = riskDetectionService.calculateTotalHallRisk(hall);
        
        // Then
        assertTrue(totalRisk >= 0.0 && totalRisk <= 100.0);
    }
    
    @Test
    void testGenerateRiskReport() {
        // Given
        ExamHall hall = new ExamHall("TEST_HALL", 3, 3);
        hall.getSeats()[0][0].setStudent(new Student("S001", "Alice", "Math"));
        hall.getSeats()[0][1].setStudent(new Student("S002", "Bob", "Math"));
        hall.getSeats()[1][0].setStudent(new Student("S003", "Charlie", "Physics"));
        
        // When
        Map<String, Object> report = riskDetectionService.generateRiskReport(hall);
        
        // Then
        assertNotNull(report);
        assertEquals("TEST_HALL", report.get("hallId"));
        assertEquals(9, report.get("totalSeats"));
        assertEquals(3L, report.get("occupiedSeats"));
        assertTrue(report.containsKey("totalRiskScore"));
        assertTrue(report.containsKey("riskDistribution"));
        assertTrue(report.containsKey("riskPercentages"));
    }
    
    @Test
    void testCountTotalConflicts() {
        // Given: Create a scenario with known conflicts
        ExamHall hall = new ExamHall("TEST_HALL", 3, 3);
        hall.getSeats()[0][0].setStudent(new Student("S001", "Alice", "Math"));
        hall.getSeats()[0][1].setStudent(new Student("S002", "Bob", "Math"));
        hall.getSeats()[1][0].setStudent(new Student("S003", "Charlie", "Physics"));
        
        // When
        int conflicts = riskDetectionService.countTotalConflicts(hall);
        
        // Then
        assertEquals(1, conflicts); // One Math-Math adjacency
    }
    
    @Test
    void testRiskLevelMapping() {
        // Test RiskLevel.fromConflictCount method
        assertEquals(RiskLevel.SAFE, RiskLevel.fromConflictCount(0));
        assertEquals(RiskLevel.MEDIUM, RiskLevel.fromConflictCount(1));
        assertEquals(RiskLevel.HIGH, RiskLevel.fromConflictCount(2));
        assertEquals(RiskLevel.HIGH, RiskLevel.fromConflictCount(3));
    }
}