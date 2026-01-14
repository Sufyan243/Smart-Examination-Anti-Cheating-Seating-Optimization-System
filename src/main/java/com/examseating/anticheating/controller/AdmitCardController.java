package com.examseating.anticheating.controller;

import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.service.AdmitCardService;
import com.examseating.anticheating.service.HallManagementService;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/admit-cards")
@RequiredArgsConstructor
@Slf4j
public class AdmitCardController {

    private final AdmitCardService admitCardService;
    private final HallManagementService hallManagementService;

    @GetMapping("/single")
    public ResponseEntity<byte[]> generateSingleAdmitCard(
            @RequestParam String rollNo,
            @RequestParam String name,
            @RequestParam String subject,
            @RequestParam String hallId,
            @RequestParam int row,
            @RequestParam int col,
            @RequestParam String examDate,
            @RequestParam String examTime) {
        
        try {
            log.info("Generating admit card for student: {}", rollNo);
            byte[] pdfBytes = admitCardService.generateAdmitCard(
                    rollNo, name, subject, hallId, row, col, examDate, examTime);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    String.format("admit_card_%s.pdf", rollNo));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (IOException | WriterException e) {
            log.error("Error generating admit card for {}: {}", rollNo, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bulk/{hallId}")
    public ResponseEntity<byte[]> generateBulkAdmitCards(
            @PathVariable String hallId,
            @RequestParam String examDate,
            @RequestParam String examTime) {
        
        try {
            log.info("Generating bulk admit cards for hall: {}", hallId);
            ExamHall hall = hallManagementService.getHallById(hallId);
            byte[] pdfBytes = admitCardService.generateBulkAdmitCards(hall, examDate, examTime);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    String.format("admit_cards_%s.pdf", hallId));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (IOException | WriterException e) {
            log.error("Error generating bulk admit cards for hall {}: {}", hallId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
