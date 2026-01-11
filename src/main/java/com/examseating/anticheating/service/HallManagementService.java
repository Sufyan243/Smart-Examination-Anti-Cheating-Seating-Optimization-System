package com.examseating.anticheating.service;

import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.Student;
import com.examseating.anticheating.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class HallManagementService {
    
    private final HallRepository hallRepository;
    
    /**
     * Distributes students across multiple halls using priority queue for optimal capacity utilization
     */
    public Map<String, List<Student>> distributeStudentsAcrossHalls(List<Student> students, List<ExamHall> halls) {
        log.info("Distributing {} students across {} halls", students.size(), halls.size());
        
        if (students.isEmpty()) {
            log.warn("No students to distribute");
            return new HashMap<>();
        }
        
        if (halls.isEmpty()) {
            log.error("No halls available for distribution");
            throw new IllegalArgumentException("No halls available");
        }
        
        // Calculate total capacity
        int totalCapacity = halls.stream().mapToInt(ExamHall::getCapacity).sum();
        if (students.size() > totalCapacity) {
            log.warn("Students ({}) exceed total capacity ({})", students.size(), totalCapacity);
        }
        
        // Create priority queue ordered by available capacity (descending)
        PriorityQueue<HallCapacity> hallQueue = new PriorityQueue<>((h1, h2) -> 
                Integer.compare(h2.availableCapacity, h1.availableCapacity));
        
        // Initialize hall capacities
        Map<String, List<Student>> distribution = new HashMap<>();
        for (ExamHall hall : halls) {
            int availableCapacity = hall.getCapacity() - hall.getOccupiedSeats();
            hallQueue.offer(new HallCapacity(hall.getHallId(), availableCapacity));
            distribution.put(hall.getHallId(), new ArrayList<>());
        }
        
        // Distribute students
        List<Student> overflowStudents = new ArrayList<>();
        
        for (Student student : students) {
            if (hallQueue.isEmpty()) {
                overflowStudents.add(student);
                continue;
            }
            
            HallCapacity hallCap = hallQueue.poll();
            
            if (hallCap.availableCapacity > 0) {
                distribution.get(hallCap.hallId).add(student);
                hallCap.availableCapacity--;
                
                // Re-insert if still has capacity
                if (hallCap.availableCapacity > 0) {
                    hallQueue.offer(hallCap);
                }
                
                log.debug("Assigned student {} to hall {}", student.getRollNo(), hallCap.hallId);
            } else {
                overflowStudents.add(student);
            }
        }
        
        // Handle overflow
        if (!overflowStudents.isEmpty()) {
            log.warn("Could not assign {} students due to capacity constraints", overflowStudents.size());
            distribution.put("OVERFLOW", overflowStudents);
        }
        
        // Log distribution summary
        distribution.forEach((hallId, assignedStudents) -> {
            if (!"OVERFLOW".equals(hallId)) {
                log.info("Hall {}: {} students assigned", hallId, assignedStudents.size());
            }
        });
        
        return distribution;
    }
    
    /**
     * Creates a new exam hall and stores it in repository
     */
    public ExamHall createHall(String hallId, int rows, int cols) {
        log.info("Creating new hall: {} with dimensions {}x{}", hallId, rows, cols);
        
        if (hallRepository.existsById(hallId)) {
            log.error("Hall with ID {} already exists", hallId);
            throw new IllegalArgumentException("Hall with ID " + hallId + " already exists");
        }
        
        if (rows <= 0 || cols <= 0) {
            log.error("Invalid hall dimensions: {}x{}", rows, cols);
            throw new IllegalArgumentException("Hall dimensions must be positive");
        }
        
        ExamHall hall = new ExamHall(hallId, rows, cols);
        ExamHall savedHall = hallRepository.save(hall);
        
        log.info("Successfully created hall {} with capacity {}", hallId, hall.getCapacity());
        return savedHall;
    }
    
    /**
     * Retrieves hall by ID
     */
    public ExamHall getHallById(String hallId) {
        log.debug("Retrieving hall with ID: {}", hallId);
        ExamHall hall = hallRepository.findById(hallId);
        
        if (hall == null) {
            log.warn("Hall not found with ID: {}", hallId);
        }
        
        return hall;
    }
    
    /**
     * Retrieves all halls
     */
    public List<ExamHall> getAllHalls() {
        log.debug("Retrieving all halls");
        List<ExamHall> halls = hallRepository.findAll();
        log.info("Found {} halls", halls.size());
        return halls;
    }
    
    /**
     * Deletes a hall by ID
     */
    public boolean deleteHall(String hallId) {
        log.info("Deleting hall with ID: {}", hallId);
        
        if (!hallRepository.existsById(hallId)) {
            log.warn("Cannot delete hall {}: not found", hallId);
            return false;
        }
        
        hallRepository.delete(hallId);
        log.info("Successfully deleted hall {}", hallId);
        return true;
    }
    
    /**
     * Gets total capacity across all halls
     */
    public int getTotalCapacity() {
        return getAllHalls().stream().mapToInt(ExamHall::getCapacity).sum();
    }
    
    /**
     * Gets total occupied seats across all halls
     */
    public long getTotalOccupiedSeats() {
        return getAllHalls().stream().mapToLong(ExamHall::getOccupiedSeats).sum();
    }
    
    /**
     * Gets hall utilization statistics
     */
    public Map<String, Object> getHallStatistics() {
        List<ExamHall> halls = getAllHalls();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalHalls", halls.size());
        stats.put("totalCapacity", getTotalCapacity());
        stats.put("totalOccupied", getTotalOccupiedSeats());
        stats.put("totalAvailable", getTotalCapacity() - getTotalOccupiedSeats());
        
        if (getTotalCapacity() > 0) {
            stats.put("overallUtilization", (double) getTotalOccupiedSeats() / getTotalCapacity() * 100);
        } else {
            stats.put("overallUtilization", 0.0);
        }
        
        // Individual hall stats
        List<Map<String, Object>> hallStats = new ArrayList<>();
        for (ExamHall hall : halls) {
            Map<String, Object> hallStat = new HashMap<>();
            hallStat.put("hallId", hall.getHallId());
            hallStat.put("capacity", hall.getCapacity());
            hallStat.put("occupied", hall.getOccupiedSeats());
            hallStat.put("available", hall.getCapacity() - hall.getOccupiedSeats());
            hallStat.put("utilization", hall.getCapacity() > 0 ? 
                    (double) hall.getOccupiedSeats() / hall.getCapacity() * 100 : 0.0);
            hallStats.add(hallStat);
        }
        stats.put("hallDetails", hallStats);
        
        return stats;
    }
    
    /**
     * Helper class for priority queue operations
     */
    private static class HallCapacity {
        String hallId;
        int availableCapacity;
        
        HallCapacity(String hallId, int availableCapacity) {
            this.hallId = hallId;
            this.availableCapacity = availableCapacity;
        }
    }
}