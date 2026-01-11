package com.examseating.anticheating.controller;

import com.examseating.anticheating.dto.ErrorResponse;
import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.service.HallManagementService;
import com.examseating.anticheating.service.PDFExportService;
import com.examseating.anticheating.service.RiskDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/export")
@CrossOrigin
@RequiredArgsConstructor
@Slf4j
public class ExportController {
    
    private final PDFExportService pdfExportService;
    private final HallManagementService hallManagementService;
    private final RiskDetectionService riskDetectionService;
    
    @GetMapping("/pdf/{hallId}")
    public ResponseEntity<?> exportToPDF(@PathVariable String hallId) {
        try {
            ExamHall hall = hallManagementService.getHallById(hallId);
            if (hall == null) {
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> riskReport = riskDetectionService.generateRiskReport(hall);
            byte[] pdfBytes = pdfExportService.generateSeatingChartPDF(hall, riskReport);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", hallId + "_seating_chart.pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (IOException e) {
            log.error("PDF generation failed for hall {}: {}", hallId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse(false, "PDF generation failed", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during PDF export for hall {}: {}", hallId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse(false, "Internal server error", e.getMessage()));
        }
    }
}