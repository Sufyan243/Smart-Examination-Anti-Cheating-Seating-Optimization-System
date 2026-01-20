package com.examseating.anticheating.service;

import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.RiskLevel;
import com.examseating.anticheating.model.Seat;
import com.examseating.anticheating.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeatAllocationService {
    
    public ExamHall allocateSeats(List<Student> students, ExamHall hall) {
        log.info("Starting seat allocation for {} students in hall {}", students.size(), hall.getHallId());
        
        if (students.isEmpty()) {
            log.warn("No students to allocate");
            clearHall(hall);
            return hall;
        }
        
        clearHall(hall);
        
        if (students.size() > hall.getCapacity()) {
            log.warn("Students ({}) exceed hall capacity ({})", students.size(), hall.getCapacity());
        }
        
        // Group students by subject and sort by frequency (descending)
        Map<String, List<Student>> subjectGroups = students.stream()
                .collect(Collectors.groupingBy(Student::getSubject));
        
        // Initialize two-queue system
        Queue<Student> primaryQueue = new LinkedList<>();
        subjectGroups.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .forEach(entry -> primaryQueue.addAll(entry.getValue()));
        
        Queue<Student> retryQueue = new LinkedList<>();
        Seat[][] seats = hall.getSeats();
        int optimalPlacements = 0;
        int fallbackPlacements = 0;
        
        // First pass: Optimal placement
        for (int row = 0; row < hall.getRows() && !primaryQueue.isEmpty(); row++) {
            for (int col = 0; col < hall.getCols() && !primaryQueue.isEmpty(); col++) {
                if (seats[row][col].isOccupied()) continue;
                
                Student student = primaryQueue.poll();
                if (student == null) break;
                
                if (isValidPlacement(seats, row, col, student)) {
                    seats[row][col].setStudent(student);
                    seats[row][col].setRiskLevel(RiskLevel.SAFE);
                    seats[row][col].setRiskScore(0.0);
                    optimalPlacements++;
                    log.debug("Assigned {} to seat ({}, {}) - optimal placement", student.getRollNo(), row, col);
                } else {
                    retryQueue.offer(student);
                    log.debug("Student {} deferred to retry queue", student.getRollNo());
                }
            }
        }
        
        // Second pass: Fallback placement with adjacency checks
        if (!retryQueue.isEmpty()) {
            log.info("Processing retry queue with {} students", retryQueue.size());
            
            while (!retryQueue.isEmpty()) {
                Student student = retryQueue.poll();
                boolean placed = false;
                
                for (int row = 0; row < hall.getRows() && !placed; row++) {
                    for (int col = 0; col < hall.getCols() && !placed; col++) {
                        if (!seats[row][col].isOccupied() && isValidPlacement(seats, row, col, student)) {
                            seats[row][col].setStudent(student);
                            seats[row][col].setRiskLevel(RiskLevel.SAFE);
                            seats[row][col].setRiskScore(0.0);
                            fallbackPlacements++;
                            placed = true;
                            log.debug("Assigned {} to seat ({}, {}) - fallback placement", student.getRollNo(), row, col);
                        }
                    }
                }
                
                if (!placed) {
                    log.debug("Student {} could not be placed without conflicts", student.getRollNo());
                }
            }
        }
        
        int totalAssigned = optimalPlacements + fallbackPlacements;
        int unallocated = students.size() - totalAssigned;
        
        log.info("Allocation complete: {} total students", students.size());
        log.info("Optimal placements: {} ({}%)", optimalPlacements, 
                totalAssigned > 0 ? (optimalPlacements * 100 / totalAssigned) : 0);
        log.info("Fallback placements: {} ({}%)", fallbackPlacements,
                totalAssigned > 0 ? (fallbackPlacements * 100 / totalAssigned) : 0);
        
        if (unallocated > 0) {
            log.warn("Unallocated students: {}", unallocated);
        }
        
        log.info("Final allocation rate: {}%", (totalAssigned * 100 / students.size()));
        return hall;
    }
    
    /**
     * Generates random seating for baseline comparison
     */
    public ExamHall generateRandomSeating(List<Student> students, ExamHall hall) {
        log.info("Generating random seating for {} students", students.size());
        
        // Clear existing allocations
        clearHall(hall);
        
        List<Student> shuffledStudents = new ArrayList<>(students);
        Collections.shuffle(shuffledStudents);
        
        Seat[][] seats = hall.getSeats();
        int studentIndex = 0;
        
        // Fill seats row by row with shuffled students
        for (int row = 0; row < hall.getRows() && studentIndex < shuffledStudents.size(); row++) {
            for (int col = 0; col < hall.getCols() && studentIndex < shuffledStudents.size(); col++) {
                seats[row][col].setStudent(shuffledStudents.get(studentIndex++));
                seats[row][col].setRiskLevel(RiskLevel.SAFE); // Will be recalculated
                seats[row][col].setRiskScore(0.0);
            }
        }
        
        log.info("Random seating completed. Assigned {} students", studentIndex);
        return hall;
    }
    
    /**
     * Checks if placing a student at given position is valid (no same-subject neighbors)
     */
    public boolean isValidPlacement(Seat[][] seats, int row, int col, Student student) {
        List<Seat> neighbors = getAdjacentSeats(seats, row, col);
        
        for (Seat neighbor : neighbors) {
            if (neighbor.isOccupied() && 
                neighbor.getStudent().getSubject().equals(student.getSubject())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns list of 4-directional adjacent seats (up, down, left, right)
     */
    public List<Seat> getAdjacentSeats(Seat[][] seats, int row, int col) {
        List<Seat> neighbors = new ArrayList<>();
        int rows = seats.length;
        int cols = seats[0].length;
        
        // Directions: up, down, left, right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols) {
                neighbors.add(seats[newRow][newCol]);
            }
        }
        
        return neighbors;
    }
    
    /**
     * Clears all seat assignments in the hall
     */
    public void clearHall(ExamHall hall) {
        Seat[][] seats = hall.getSeats();
        for (int row = 0; row < hall.getRows(); row++) {
            for (int col = 0; col < hall.getCols(); col++) {
                seats[row][col].setStudent(null);
                seats[row][col].setRiskLevel(RiskLevel.SAFE);
                seats[row][col].setRiskScore(0.0);
            }
        }
        log.info("Cleared all seat assignments in hall {}", hall.getHallId());
    }
}