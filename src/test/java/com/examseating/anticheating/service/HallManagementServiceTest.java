package com.examseating.anticheating.service;

import com.examseating.anticheating.fixtures.TestDataFixtures;
import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.Student;
import com.examseating.anticheating.repository.HallRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HallManagementServiceTest {
    
    @Mock
    private HallRepository hallRepository;
    
    @InjectMocks
    private HallManagementService hallManagementService;
    
    @Test
    void testDistributeStudentsAcrossHalls_BalancedDistribution() {
        // Given
        List<Student> students = TestDataFixtures.createStudents(100, 3);
        List<ExamHall> halls = Arrays.asList(
            new ExamHall("HALL-A", 10, 10),
            new ExamHall("HALL-B", 10, 10)
        );
        
        // When
        Map<String, List<Student>> distribution = 
            hallManagementService.distributeStudentsAcrossHalls(students, halls);
        
        // Then
        assertEquals(2, distribution.size());
        assertTrue(Math.abs(
            distribution.get("HALL-A").size() - 
            distribution.get("HALL-B").size()
        ) <= 1);  // Balanced within 1 student
        
        // Verify all students are distributed
        int totalDistributed = distribution.values().stream()
            .mapToInt(List::size).sum();
        assertEquals(100, totalDistributed);
    }
    
    @Test
    void testDistributeStudentsAcrossHalls_Overflow() {
        // Given: More students than total capacity
        List<Student> students = TestDataFixtures.createStudents(150, 3);
        List<ExamHall> halls = Arrays.asList(
            new ExamHall("HALL-A", 5, 5),  // 25 seats
            new ExamHall("HALL-B", 5, 5)   // 25 seats
        );
        
        // When
        Map<String, List<Student>> distribution = 
            hallManagementService.distributeStudentsAcrossHalls(students, halls);
        
        // Then
        assertTrue(distribution.containsKey("OVERFLOW"));
        assertEquals(100, distribution.get("OVERFLOW").size());
        
        // Verify halls are filled to capacity
        assertEquals(25, distribution.get("HALL-A").size());
        assertEquals(25, distribution.get("HALL-B").size());
    }
    
    @Test
    void testDistributeStudentsAcrossHalls_EmptyStudents() {
        // Given
        List<Student> students = Arrays.asList();
        List<ExamHall> halls = Arrays.asList(new ExamHall("HALL-A", 5, 5));
        
        // When
        Map<String, List<Student>> distribution = 
            hallManagementService.distributeStudentsAcrossHalls(students, halls);
        
        // Then
        assertTrue(distribution.isEmpty());
    }
    
    @Test
    void testDistributeStudentsAcrossHalls_NoHalls() {
        // Given
        List<Student> students = TestDataFixtures.createStudents(10, 2);
        List<ExamHall> halls = Arrays.asList();
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            hallManagementService.distributeStudentsAcrossHalls(students, halls));
    }
    
    @Test
    void testCreateHall_Success() {
        // Given
        when(hallRepository.existsById("NEW_HALL")).thenReturn(false);
        when(hallRepository.save(any(ExamHall.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        ExamHall result = hallManagementService.createHall("NEW_HALL", 10, 8);
        
        // Then
        assertNotNull(result);
        assertEquals("NEW_HALL", result.getHallId());
        assertEquals(10, result.getRows());
        assertEquals(8, result.getCols());
        assertEquals(80, result.getCapacity());
        
        verify(hallRepository).save(any(ExamHall.class));
    }
    
    @Test
    void testCreateHall_AlreadyExists() {
        // Given
        when(hallRepository.existsById("EXISTING_HALL")).thenReturn(true);
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            hallManagementService.createHall("EXISTING_HALL", 5, 6));
    }
    
    @Test
    void testCreateHall_InvalidDimensions() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> 
            hallManagementService.createHall("INVALID_HALL", 0, 5));
        
        assertThrows(IllegalArgumentException.class, () -> 
            hallManagementService.createHall("INVALID_HALL", 5, -1));
    }
    
    @Test
    void testGetHallById_Found() {
        // Given
        ExamHall expectedHall = new ExamHall("TEST_HALL", 4, 5);
        when(hallRepository.findById("TEST_HALL")).thenReturn(expectedHall);
        
        // When
        ExamHall result = hallManagementService.getHallById("TEST_HALL");
        
        // Then
        assertNotNull(result);
        assertEquals("TEST_HALL", result.getHallId());
        verify(hallRepository).findById("TEST_HALL");
    }
    
    @Test
    void testGetHallById_NotFound() {
        // Given
        when(hallRepository.findById("NONEXISTENT")).thenReturn(null);
        
        // When
        ExamHall result = hallManagementService.getHallById("NONEXISTENT");
        
        // Then
        assertNull(result);
        verify(hallRepository).findById("NONEXISTENT");
    }
    
    @Test
    void testGetAllHalls() {
        // Given
        List<ExamHall> expectedHalls = Arrays.asList(
            new ExamHall("HALL-A", 5, 5),
            new ExamHall("HALL-B", 6, 6)
        );
        when(hallRepository.findAll()).thenReturn(expectedHalls);
        
        // When
        List<ExamHall> result = hallManagementService.getAllHalls();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(hallRepository).findAll();
    }
    
    @Test
    void testDeleteHall_Success() {
        // Given
        when(hallRepository.existsById("DELETE_HALL")).thenReturn(true);
        
        // When
        boolean result = hallManagementService.deleteHall("DELETE_HALL");
        
        // Then
        assertTrue(result);
        verify(hallRepository).delete("DELETE_HALL");
    }
    
    @Test
    void testDeleteHall_NotFound() {
        // Given
        when(hallRepository.existsById("NONEXISTENT")).thenReturn(false);
        
        // When
        boolean result = hallManagementService.deleteHall("NONEXISTENT");
        
        // Then
        assertFalse(result);
        verify(hallRepository, never()).delete(any());
    }
    
    @Test
    void testGetTotalCapacity() {
        // Given
        List<ExamHall> halls = Arrays.asList(
            new ExamHall("HALL-A", 5, 5),  // 25
            new ExamHall("HALL-B", 6, 6)   // 36
        );
        when(hallRepository.findAll()).thenReturn(halls);
        
        // When
        int totalCapacity = hallManagementService.getTotalCapacity();
        
        // Then
        assertEquals(61, totalCapacity);
    }
    
    @Test
    void testGetHallStatistics() {
        // Given
        List<ExamHall> halls = Arrays.asList(
            new ExamHall("HALL-A", 2, 2),
            new ExamHall("HALL-B", 3, 3)
        );
        when(hallRepository.findAll()).thenReturn(halls);
        
        // When
        Map<String, Object> stats = hallManagementService.getHallStatistics();
        
        // Then
        assertNotNull(stats);
        assertEquals(2, stats.get("totalHalls"));
        assertEquals(13, stats.get("totalCapacity")); // 4 + 9
        assertTrue(stats.containsKey("overallUtilization"));
        assertTrue(stats.containsKey("hallDetails"));
    }
}