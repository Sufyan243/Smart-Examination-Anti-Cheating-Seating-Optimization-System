package com.examseating.anticheating.controller;

import com.examseating.anticheating.dto.SeatAllocationRequest;
import com.examseating.anticheating.dto.StudentDTO;
import com.examseating.anticheating.fixtures.TestDataFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
class SeatingControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testAllocateSeats_Success() throws Exception {
        // Given
        SeatAllocationRequest request = new SeatAllocationRequest();
        request.setHallId("TEST_HALL");
        request.setRows(5);
        request.setCols(5);
        request.setUseOptimization(true);
        
        List<StudentDTO> studentDtos = TestDataFixtures.createStudents(10, 3).stream()
            .map(student -> {
                StudentDTO dto = new StudentDTO();
                dto.setRollNo(student.getRollNo());
                dto.setName(student.getName());
                dto.setSubject(student.getSubject());
                return dto;
            })
            .collect(Collectors.toList());
        
        request.setStudents(studentDtos);
        
        // When/Then
        mockMvc.perform(post("/api/seating/allocate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.hallId").value("TEST_HALL"))
                .andExpect(jsonPath("$.riskReport").exists());
    }
    
    @Test
    void testUploadValidCSV() throws Exception {
        // Given
        String csvContent = TestDataFixtures.getValidCSVContent();
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes());
        
        // When/Then
        mockMvc.perform(multipart("/api/seating/upload-csv")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].rollNo").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].subject").exists());
    }
    
    @Test
    void testUploadInvalidCSV() throws Exception {
        // Given
        String csvContent = "RollNo,Name\n2021001,Alice\n2021002,Bob";
        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.csv", "text/csv", csvContent.getBytes());
        
        // When/Then
        mockMvc.perform(multipart("/api/seating/upload-csv")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void testUploadDuplicateCSV() throws Exception {
        // Given
        String csvContent = TestDataFixtures.getCSVWithDuplicates();
        MockMultipartFile file = new MockMultipartFile(
                "file", "duplicates.csv", "text/csv", csvContent.getBytes());
        
        // When/Then
        mockMvc.perform(multipart("/api/seating/upload-csv")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Duplicate student found"));
    }
    
    @Test
    void testDownloadSampleCSV() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/seating/sample-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"sample-students.csv\""))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));
    }
    
    @Test
    void testUploadNonCSVFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "not a csv".getBytes());
        
        // When/Then
        mockMvc.perform(multipart("/api/seating/upload-csv")
                .file(file))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testAllocateSeats_EmptyStudents() throws Exception {
        // Given
        SeatAllocationRequest request = new SeatAllocationRequest();
        request.setHallId("EMPTY_HALL");
        request.setRows(3);
        request.setCols(3);
        request.setUseOptimization(true);
        request.setStudents(List.of());
        
        // When/Then
        mockMvc.perform(post("/api/seating/allocate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.riskReport.occupiedSeats").value(0));
    }
}