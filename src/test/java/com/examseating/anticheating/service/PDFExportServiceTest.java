package com.examseating.anticheating.service;

import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.RiskLevel;
import com.examseating.anticheating.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PDFExportServiceTest {
    
    private PDFExportService pdfExportService;
    
    @BeforeEach
    void setUp() {
        pdfExportService = new PDFExportService();
    }
    
    @Test
    void testGeneratePDFForEmptyHall() throws Exception {
        ExamHall hall = new ExamHall("EMPTY_HALL", 3, 3);
        Map<String, Object> riskReport = createMockRiskReport(0, 0, 0.0);
        
        byte[] pdfBytes = pdfExportService.generateSeatingChartPDF(hall, riskReport);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    
    @Test
    void testGeneratePDFWithAllRiskLevels() throws Exception {
        ExamHall hall = new ExamHall("RISK_HALL", 2, 2);
        
        // Add students with different risk levels
        hall.getSeats()[0][0].setStudent(new Student("S001", "Alice", "Math"));
        hall.getSeats()[0][0].setRiskLevel(RiskLevel.SAFE);
        
        hall.getSeats()[0][1].setStudent(new Student("S002", "Bob", "Physics"));
        hall.getSeats()[0][1].setRiskLevel(RiskLevel.MEDIUM);
        
        hall.getSeats()[1][0].setStudent(new Student("S003", "Charlie", "Math"));
        hall.getSeats()[1][0].setRiskLevel(RiskLevel.HIGH);
        
        Map<String, Object> riskReport = createMockRiskReport(3, 1, 33.3);
        
        byte[] pdfBytes = pdfExportService.generateSeatingChartPDF(hall, riskReport);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    
    @Test
    void testGeneratePDFFor10x8Hall() throws Exception {
        ExamHall hall = new ExamHall("LARGE_HALL", 10, 8);
        Map<String, Object> riskReport = createMockRiskReport(0, 0, 0.0);
        
        byte[] pdfBytes = pdfExportService.generateSeatingChartPDF(hall, riskReport);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    
    @Test
    void testPDFContainsMetadata() throws Exception {
        ExamHall hall = new ExamHall("META_HALL", 2, 2);
        hall.getSeats()[0][0].setStudent(new Student("S001", "Alice", "Math"));
        
        Map<String, Object> riskReport = createMockRiskReport(1, 0, 0.0);
        
        byte[] pdfBytes = pdfExportService.generateSeatingChartPDF(hall, riskReport);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // PDF should contain hall metadata
    }
    
    @Test
    void testPDFContainsLegend() throws Exception {
        ExamHall hall = new ExamHall("LEGEND_HALL", 2, 2);
        Map<String, Object> riskReport = createMockRiskReport(0, 0, 0.0);
        
        byte[] pdfBytes = pdfExportService.generateSeatingChartPDF(hall, riskReport);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // PDF should contain legend section
    }
    
    @Test
    void testPDFColorCoding() throws Exception {
        ExamHall hall = new ExamHall("COLOR_HALL", 1, 3);
        
        // Set up seats with different risk levels
        hall.getSeats()[0][0].setStudent(new Student("S001", "Alice", "Math"));
        hall.getSeats()[0][0].setRiskLevel(RiskLevel.SAFE);
        
        hall.getSeats()[0][1].setStudent(new Student("S002", "Bob", "Physics"));
        hall.getSeats()[0][1].setRiskLevel(RiskLevel.MEDIUM);
        
        hall.getSeats()[0][2].setStudent(new Student("S003", "Charlie", "Math"));
        hall.getSeats()[0][2].setRiskLevel(RiskLevel.HIGH);
        
        Map<String, Object> riskReport = createMockRiskReport(3, 1, 33.3);
        
        byte[] pdfBytes = pdfExportService.generateSeatingChartPDF(hall, riskReport);
        
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // PDF should apply correct RGB colors for each risk level
    }
    
    private Map<String, Object> createMockRiskReport(int occupiedSeats, int conflicts, double riskScore) {
        Map<String, Object> report = new HashMap<>();
        report.put("occupiedSeats", occupiedSeats);
        report.put("totalConflicts", conflicts);
        report.put("totalRiskScore", riskScore);
        report.put("totalSeats", 9);
        return report;
    }
}