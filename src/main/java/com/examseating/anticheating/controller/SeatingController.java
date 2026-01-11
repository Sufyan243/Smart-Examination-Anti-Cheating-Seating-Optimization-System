package com.examseating.anticheating.controller;

import com.examseating.anticheating.dto.*;
import com.examseating.anticheating.exception.*;
import com.examseating.anticheating.model.*;
import com.examseating.anticheating.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
@RequiredArgsConstructor
@Tag(name = "Seating Management", description = "APIs for seat allocation, optimization, and risk analysis")
public class SeatingController {
    
    private final SeatAllocationService seatAllocationService;
    private final RiskDetectionService riskDetectionService;
    private final HallManagementService hallManagementService;
    private final CSVService csvService;
    
    @PostMapping("/seating/upload-csv")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file) {
        try {
            if (!csvService.validateCSVFormat(file)) {
                return ResponseEntity.status(415)
                        .body(new ErrorResponse(false, "Unsupported file type. Only CSV files are allowed."));
            }
            
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                return ResponseEntity.status(413)
                        .body(new ErrorResponse(false, "File too large. Maximum size is 10MB."));
            }
            
            List<Student> students = csvService.parseStudentsFromCSV(file);
            List<StudentDTO> studentDtos = students.stream()
                    .map(this::convertToStudentDTO)
                    .toList();
            
            return ResponseEntity.ok(studentDtos);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse(false, "Internal server error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/students/upload")
    public ResponseEntity<?> uploadStudents(@RequestParam("file") MultipartFile file) {
        try {
            if (!csvService.validateCSVFormat(file)) {
                return ResponseEntity.status(415)
                        .body(new ErrorResponse(false, "Unsupported file type. Only CSV files are allowed."));
            }
            
            if (file.getSize() > 10 * 1024 * 1024) { // 10MB
                return ResponseEntity.status(413)
                        .body(new ErrorResponse(false, "File too large. Maximum size is 10MB."));
            }
            
            List<Student> students = csvService.parseStudentsFromCSV(file);
            List<StudentDTO> studentDtos = students.stream()
                    .map(this::convertToStudentDTO)
                    .toList();
            
            return ResponseEntity.ok(studentDtos);
            
        } catch (Exception e) {
            // Let GlobalExceptionHandler handle specific exceptions
            throw e;
        }
    }
    
    @PostMapping("/students/manual")
    public ResponseEntity<String> addStudentsManually(@Valid @RequestBody List<StudentDTO> students) {
        // This would typically save to a temporary store or session
        // For now, just return success
        return ResponseEntity.ok("Students added successfully");
    }
    
    @PostMapping("/halls/create")
    public ResponseEntity<ExamHall> createHall(@Valid @RequestBody HallDTO hallDTO) {
        ExamHall hall = hallManagementService.createHall(hallDTO.getHallId(), hallDTO.getRows(), hallDTO.getCols());
        return ResponseEntity.ok(hall);
    }
    
    @PostMapping("/seating/random")
    public ResponseEntity<SeatingResponseDTO> generateRandomSeating(@Valid @RequestBody SeatingRequestDTO request) {
        ExamHall hall = hallManagementService.getHallById(request.getHallId());
        if (hall == null) {
            throw new HallNotFoundException(request.getHallId(), "random seating generation");
        }
        
        // Convert StudentDTOs to Student objects with real subjects and names
        List<Student> students = request.getStudents().stream()
                .map(dto -> new Student(dto.getRollNo(), dto.getName(), dto.getSubject()))
                .toList();
        
        if (students.size() > hall.getCapacity()) {
            throw new StudentOverflowException(students.size(), hall.getCapacity());
        }
        
        seatAllocationService.generateRandomSeating(students, hall);
        riskDetectionService.calculateRisksForHall(hall);
        
        return ResponseEntity.ok(buildSeatingResponse(hall));
    }
    
    @PostMapping("/seating/optimize")
    @Operation(summary = "Run greedy seat allocation", description = "Allocates seats using greedy optimization algorithm")
    @ApiResponse(responseCode = "200", description = "Seats allocated successfully")
    @ApiResponse(responseCode = "404", description = "Hall not found")
    @ApiResponse(responseCode = "400", description = "Student overflow or validation error")
    public ResponseEntity<SeatingResponseDTO> optimizeSeating(@Valid @RequestBody SeatingRequestDTO request) {
        ExamHall hall = hallManagementService.getHallById(request.getHallId());
        if (hall == null) {
            throw new HallNotFoundException(request.getHallId(), "seating optimization");
        }
        
        // Convert StudentDTOs to Student objects with real subjects and names
        List<Student> students = request.getStudents().stream()
                .map(dto -> new Student(dto.getRollNo(), dto.getName(), dto.getSubject()))
                .toList();
        
        if (students.size() > hall.getCapacity()) {
            throw new StudentOverflowException(students.size(), hall.getCapacity());
        }
        
        seatAllocationService.allocateSeats(students, hall);
        riskDetectionService.calculateRisksForHall(hall);
        
        return ResponseEntity.ok(buildSeatingResponse(hall));
    }
    
    @GetMapping("/seating/risk/{hallId}")
    public ResponseEntity<RiskAnalysisDTO> getRiskAnalysis(@PathVariable String hallId) {
        ExamHall hall = hallManagementService.getHallById(hallId);
        if (hall == null) {
            throw new HallNotFoundException(hallId, "risk analysis");
        }
        
        Map<String, Object> riskReport = riskDetectionService.generateRiskReport(hall);
        RiskAnalysisDTO riskAnalysis = buildRiskAnalysisDTO(hall, riskReport);
        
        return ResponseEntity.ok(riskAnalysis);
    }
    
    @GetMapping("/seating/export/{hallId}")
    public ResponseEntity<byte[]> exportPDF(@PathVariable String hallId) {
        // This endpoint delegates to ExportController for consistency
        // In a real implementation, you might want to consolidate these
        return ResponseEntity.status(302)
                .header("Location", "/api/export/pdf/" + hallId)
                .build();
    }
    


    // Legacy endpoint for backward compatibility
    @PostMapping("/seating/allocate")
    public ResponseEntity<SeatAllocationResponse> allocateSeats(@RequestBody SeatAllocationRequest request) {
        try {
            // Create or get hall
            ExamHall hall = hallManagementService.getHallById(request.getHallId());
            if (hall == null) {
                hall = hallManagementService.createHall(request.getHallId(), request.getRows(), request.getCols());
            }
            
            // Convert DTOs to domain objects
            List<Student> students = request.getStudents().stream()
                    .map(dto -> new Student(dto.getRollNo(), dto.getName(), dto.getSubject()))
                    .toList();
            
            // Allocate seats
            if (request.isUseOptimization()) {
                seatAllocationService.allocateSeats(students, hall);
            } else {
                seatAllocationService.generateRandomSeating(students, hall);
            }
            
            // Generate risk report
            Map<String, Object> riskReport = riskDetectionService.generateRiskReport(hall);
            
            // Convert to response DTO
            SeatAllocationResponse response = new SeatAllocationResponse();
            response.setHallId(hall.getHallId());
            response.setSeats(convertToSeatDTOs(hall.getSeats()));
            response.setRiskReport(riskReport);
            response.setSuccess(true);
            response.setMessage("Seats allocated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            SeatAllocationResponse response = new SeatAllocationResponse();
            response.setSuccess(false);
            response.setMessage("Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // Legacy endpoint moved to /api/students/upload
    
    @GetMapping("/seating/sample-csv")
    public ResponseEntity<byte[]> downloadSampleCSV() {
        byte[] csvContent = csvService.generateSampleCSV();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "sample-students.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvContent);
    }
    
    private SeatingResponseDTO buildSeatingResponse(ExamHall hall) {
        Map<String, Object> riskReport = riskDetectionService.generateRiskReport(hall);
        
        SeatingResponseDTO response = new SeatingResponseDTO();
        response.setHallId(hall.getHallId());
        response.setSeats(convertToSeatDTOs(hall.getSeats()));
        response.setTotalRiskScore((Double) riskReport.get("totalRiskScore"));
        response.setConflictCount((Integer) riskReport.get("totalConflicts"));
        response.setOccupiedSeats((Integer) riskReport.get("occupiedSeats"));
        response.setCapacity((Integer) riskReport.get("totalSeats"));
        
        return response;
    }
    
    private RiskAnalysisDTO buildRiskAnalysisDTO(ExamHall hall, Map<String, Object> riskReport) {
        RiskAnalysisDTO dto = new RiskAnalysisDTO();
        dto.setHallId(hall.getHallId());
        dto.setTotalRiskScore((Double) riskReport.get("totalRiskScore"));
        dto.setConflictCount((Integer) riskReport.get("totalConflicts"));
        dto.setOccupiedSeats((Integer) riskReport.get("occupiedSeats"));
        dto.setCapacity((Integer) riskReport.get("totalSeats"));
        
        // Count risk levels
        int safeSeats = 0, mediumRiskSeats = 0, highRiskSeats = 0;
        for (Seat[] row : hall.getSeats()) {
            for (Seat seat : row) {
                if (seat.isOccupied()) {
                    switch (seat.getRiskLevel()) {
                        case SAFE -> safeSeats++;
                        case MEDIUM -> mediumRiskSeats++;
                        case HIGH -> highRiskSeats++;
                    }
                }
            }
        }
        
        dto.setSafeSeats(safeSeats);
        dto.setMediumRiskSeats(mediumRiskSeats);
        dto.setHighRiskSeats(highRiskSeats);
        
        return dto;
    }
    
    private SeatDTO[][] convertToSeatDTOs(Seat[][] seats) {
        int rows = seats.length;
        int cols = seats[0].length;
        SeatDTO[][] seatDtos = new SeatDTO[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Seat seat = seats[i][j];
                SeatDTO dto = new SeatDTO();
                dto.setRow(seat.getRow());
                dto.setCol(seat.getCol());
                dto.setRiskLevel(seat.getRiskLevel().name());
                dto.setRiskScore(seat.getRiskScore());
                dto.setColorCode(seat.getRiskLevel().getColorCode());
                
                if (seat.isOccupied()) {
                    dto.setStudent(convertToStudentDTO(seat.getStudent()));
                }
                
                seatDtos[i][j] = dto;
            }
        }
        
        return seatDtos;
    }
    
    private StudentDTO convertToStudentDTO(Student student) {
        StudentDTO dto = new StudentDTO();
        dto.setRollNo(student.getRollNo());
        dto.setName(student.getName());
        dto.setSubject(student.getSubject());
        return dto;
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