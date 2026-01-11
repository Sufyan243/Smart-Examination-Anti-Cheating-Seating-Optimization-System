package com.examseating.anticheating.controller;

import com.examseating.anticheating.dto.HallDTO;
import com.examseating.anticheating.exception.HallNotFoundException;
import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.service.HallManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/halls")
@CrossOrigin
@RequiredArgsConstructor
public class HallController {
    
    private final HallManagementService hallManagementService;
    
    @GetMapping
    public ResponseEntity<List<HallDTO>> getAllHalls() {
        List<ExamHall> halls = hallManagementService.getAllHalls();
        List<HallDTO> hallDTOs = halls.stream()
                .map(this::convertToHallDTO)
                .toList();
        return ResponseEntity.ok(hallDTOs);
    }
    
    @GetMapping("/{hallId}")
    public ResponseEntity<HallDTO> getHall(@PathVariable String hallId) {
        ExamHall hall = hallManagementService.getHallById(hallId);
        if (hall == null) {
            throw new HallNotFoundException(hallId, "hall retrieval");
        }
        return ResponseEntity.ok(convertToHallDTO(hall));
    }
    
    @PostMapping
    public ResponseEntity<HallDTO> createHall(@Valid @RequestBody HallDTO hallDTO) {
        ExamHall hall = hallManagementService.createHall(hallDTO.getHallId(), hallDTO.getRows(), hallDTO.getCols());
        return ResponseEntity.ok(convertToHallDTO(hall));
    }
    
    // Legacy endpoint for backward compatibility
    @PostMapping(params = {"hallId", "rows", "cols"})
    public ResponseEntity<ExamHall> createHallLegacy(@RequestParam String hallId, 
                                              @RequestParam int rows, 
                                              @RequestParam int cols) {
        try {
            ExamHall hall = hallManagementService.createHall(hallId, rows, cols);
            return ResponseEntity.ok(hall);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{hallId}")
    public ResponseEntity<Void> deleteHall(@PathVariable String hallId) {
        boolean deleted = hallManagementService.deleteHall(hallId);
        if (!deleted) {
            throw new HallNotFoundException(hallId, "hall deletion");
        }
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(hallManagementService.getHallStatistics());
    }
    
    private HallDTO convertToHallDTO(ExamHall hall) {
        HallDTO dto = new HallDTO();
        dto.setHallId(hall.getHallId());
        dto.setRows(hall.getRows());
        dto.setCols(hall.getCols());
        dto.setCapacity(hall.getCapacity());
        dto.setOccupiedSeats(hall.getOccupiedSeats());
        return dto;
    }
}