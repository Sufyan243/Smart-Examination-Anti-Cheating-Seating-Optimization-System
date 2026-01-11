package com.examseating.anticheating.controller;

import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.Student;
import com.examseating.anticheating.service.HallManagementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
class ExportControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private HallManagementService hallManagementService;
    
    @Test
    void testExportPDFForExistingHall() throws Exception {
        // Create a test hall
        ExamHall hall = hallManagementService.createHall("PDF_TEST_HALL", 3, 3);
        hall.getSeats()[0][0].setStudent(new Student("S001", "Alice", "Math"));
        
        mockMvc.perform(get("/api/export/pdf/PDF_TEST_HALL"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", 
                        "attachment; filename=\"PDF_TEST_HALL_seating_chart.pdf\""));
    }
    
    @Test
    void testExportPDFForNonExistentHall() throws Exception {
        mockMvc.perform(get("/api/export/pdf/NONEXISTENT_HALL"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testPDFDownloadHeaders() throws Exception {
        // Create a test hall
        hallManagementService.createHall("HEADER_TEST_HALL", 2, 2);
        
        mockMvc.perform(get("/api/export/pdf/HEADER_TEST_HALL"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", 
                        "attachment; filename=\"HEADER_TEST_HALL_seating_chart.pdf\""));
    }
}