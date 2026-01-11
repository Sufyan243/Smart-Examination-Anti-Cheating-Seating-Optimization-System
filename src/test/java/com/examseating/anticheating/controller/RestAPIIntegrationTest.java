package com.examseating.anticheating.controller;

import com.examseating.anticheating.dto.*;
import com.examseating.anticheating.service.HallManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
public class RestAPIIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HallManagementService hallManagementService;

    @Test
    public void testCreateHallEndpoint() throws Exception {
        HallDTO hallDTO = new HallDTO("TEST_HALL", 5, 5, 0, 0);

        mockMvc.perform(post("/api/halls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hallDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hallId").value("TEST_HALL"))
                .andExpect(jsonPath("$.rows").value(5))
                .andExpect(jsonPath("$.cols").value(5));
    }

    @Test
    public void testGetAllHallsEndpoint() throws Exception {
        // Create a test hall first
        hallManagementService.createHall("TEST_HALL_2", 3, 3);

        mockMvc.perform(get("/api/halls"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testOptimizeSeatingEndpoint() throws Exception {
        // Create a test hall first
        hallManagementService.createHall("TEST_HALL_3", 4, 4);

        SeatingRequestDTO request = new SeatingRequestDTO();
        request.setHallId("TEST_HALL_3");
        request.setStudentIds(Arrays.asList("S001", "S002", "S003"));

        mockMvc.perform(post("/api/seating/optimize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hallId").value("TEST_HALL_3"))
                .andExpect(jsonPath("$.occupiedSeats").value(3));
    }

    @Test
    public void testHallNotFoundError() throws Exception {
        SeatingRequestDTO request = new SeatingRequestDTO();
        request.setHallId("NON_EXISTENT_HALL");
        request.setStudentIds(Arrays.asList("S001"));

        mockMvc.perform(post("/api/seating/optimize")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Hall not found"));
    }

    @Test
    public void testValidationError() throws Exception {
        HallDTO invalidHall = new HallDTO("", -1, -1, 0, 0); // Invalid data

        mockMvc.perform(post("/api/halls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidHall)))
                .andExpect(status().isBadRequest());
    }
}