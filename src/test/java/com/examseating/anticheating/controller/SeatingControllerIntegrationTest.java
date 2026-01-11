package com.examseating.anticheating.controller;

import com.examseating.anticheating.dto.SeatAllocationRequest;
import com.examseating.anticheating.dto.StudentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
class SeatingControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testAllocateSeats() throws Exception {
        SeatAllocationRequest request = new SeatAllocationRequest();
        request.setHallId("TEST_HALL");
        request.setRows(3);
        request.setCols(3);
        request.setUseOptimization(true);
        
        StudentDTO student1 = new StudentDTO();
        student1.setRollNo("S001");
        student1.setName("Alice");
        student1.setSubject("Math");
        
        StudentDTO student2 = new StudentDTO();
        student2.setRollNo("S002");
        student2.setName("Bob");
        student2.setSubject("Physics");
        
        request.setStudents(Arrays.asList(student1, student2));
        
        mockMvc.perform(post("/api/seating/allocate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.hallId").value("TEST_HALL"));
    }
    
    @Test
    void testUploadValidCSV() throws Exception {
        String csvContent = "RollNo,Name,Subject\nS001,Alice,Math\nS002,Bob,Physics";
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes());
        
        mockMvc.perform(multipart("/api/seating/upload-csv")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rollNo").value("S001"))
                .andExpect(jsonPath("$[1].rollNo").value("S002"));
    }
    
    @Test
    void testUploadInvalidCSV() throws Exception {
        String csvContent = "RollNo,Name\nS001,Alice\nS002,Bob";
        MockMultipartFile file = new MockMultipartFile(
                "file", "invalid.csv", "text/csv", csvContent.getBytes());
        
        mockMvc.perform(multipart("/api/seating/upload-csv")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid CSV structure"));
    }
    
    @Test
    void testUploadDuplicateCSV() throws Exception {
        String csvContent = "RollNo,Name,Subject\nS001,Alice,Math\nS001,Bob,Physics";
        MockMultipartFile file = new MockMultipartFile(
                "file", "duplicates.csv", "text/csv", csvContent.getBytes());
        
        mockMvc.perform(multipart("/api/seating/upload-csv")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Duplicate student found"));
    }
    
    @Test
    void testDownloadSampleCSV() throws Exception {
        mockMvc.perform(get("/api/seating/sample-csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"sample-students.csv\""))
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));
    }
    
    @Test
    void testUploadLargeFile() throws Exception {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.csv", "text/csv", largeContent);
        
        mockMvc.perform(multipart("/api/seating/upload-csv")
                .file(file))
                .andExpect(status().isPayloadTooLarge());
    }
}