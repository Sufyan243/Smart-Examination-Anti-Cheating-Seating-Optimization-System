package com.examseating.anticheating.service;

import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.Seat;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Slf4j
public class PDFExportService {
    
    public byte[] generateSeatingChartPDF(ExamHall hall, Map<String, Object> riskReport) throws IOException {
        log.info("Generating PDF for hall: {}", hall.getHallId());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());
        
        // Title
        document.add(new Paragraph("Exam Seating Chart")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("\n"));
        
        // Hall metadata
        addMetadata(document, hall, riskReport);
        
        document.add(new Paragraph("\n"));
        
        // Seating Grid
        renderSeatGrid(document, hall);
        
        document.add(new Paragraph("\n"));
        
        // Legend
        addLegend(document);
        
        document.close();
        
        log.info("PDF generated successfully for hall: {}", hall.getHallId());
        return baos.toByteArray();
    }
    
    private void addMetadata(Document document, ExamHall hall, Map<String, Object> riskReport) {
        document.add(new Paragraph("Hall Information")
                .setFontSize(14)
                .setBold());
        
        String metadata = String.format(
                "Hall ID: %s | Date: %s | Capacity: %d | Occupied: %d | Risk Score: %.2f%%",
                hall.getHallId(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                hall.getCapacity(),
                riskReport != null ? riskReport.get("occupiedSeats") : hall.getOccupiedSeats(),
                riskReport != null ? (Double) riskReport.get("totalRiskScore") : 0.0
        );
        
        document.add(new Paragraph(metadata).setFontSize(10));
        
        // Add batch and section statistics
        if (hall.getSeats() != null) {
            Map<String, Integer> batchStats = new java.util.HashMap<>();
            Map<String, Integer> sectionStats = new java.util.HashMap<>();
            
            for (int row = 0; row < hall.getRows(); row++) {
                for (int col = 0; col < hall.getCols(); col++) {
                    Seat seat = hall.getSeats()[row][col];
                    if (seat.isOccupied() && seat.getStudent().getBatch() != null) {
                        batchStats.merge(seat.getStudent().getBatch(), 1, Integer::sum);
                        sectionStats.merge(seat.getStudent().getSection(), 1, Integer::sum);
                    }
                }
            }
            
            if (!batchStats.isEmpty()) {
                document.add(new Paragraph("Batch Distribution: " + batchStats.toString()).setFontSize(10));
                document.add(new Paragraph("Section Distribution: " + sectionStats.toString()).setFontSize(10));
            }
        }
    }
    
    private void renderSeatGrid(Document document, ExamHall hall) {
        Table table = new Table(hall.getCols());
        
        for (int row = 0; row < hall.getRows(); row++) {
            for (int col = 0; col < hall.getCols(); col++) {
                Seat seat = hall.getSeats()[row][col];
                Cell cell = new Cell();
                cell.setPadding(5);
                
                String seatLabel = String.valueOf((char)('A' + row)) + (col + 1);
                
                if (seat.isOccupied()) {
                    String batchSection = "";
                    if (seat.getStudent().getBatch() != null && seat.getStudent().getSection() != null) {
                        batchSection = seat.getStudent().getBatch() + "-" + seat.getStudent().getSection();
                    }
                    
                    String content = String.format("%s\n%s\n%s\n%s\n%s",
                            seatLabel,
                            seat.getStudent().getRollNo(),
                            seat.getStudent().getName(),
                            seat.getStudent().getSubject(),
                            batchSection);
                    cell.add(new Paragraph(content).setFontSize(8));
                    
                    // Apply risk-based coloring
                    switch (seat.getRiskLevel()) {
                        case SAFE -> cell.setBackgroundColor(new DeviceRgb(76, 175, 80));
                        case MEDIUM -> cell.setBackgroundColor(new DeviceRgb(255, 193, 7));
                        case HIGH -> cell.setBackgroundColor(new DeviceRgb(244, 67, 54));
                    }
                } else {
                    cell.add(new Paragraph(seatLabel + "\nEMPTY").setFontSize(8));
                    cell.setBackgroundColor(new DeviceRgb(245, 245, 245));
                }
                
                table.addCell(cell);
            }
        }
        
        document.add(table);
    }
    
    private void addLegend(Document document) {
        document.add(new Paragraph("Legend")
                .setFontSize(12)
                .setBold());
        
        Table legendTable = new Table(3);
        
        // Safe
        Cell safeColor = new Cell().setBackgroundColor(new DeviceRgb(76, 175, 80))
                .setWidth(10).setHeight(10);
        legendTable.addCell(safeColor);
        legendTable.addCell(new Cell().add(new Paragraph("Safe (0 conflicts)").setFontSize(10)));
        legendTable.addCell(new Cell()); // Empty cell
        
        // Medium
        Cell mediumColor = new Cell().setBackgroundColor(new DeviceRgb(255, 193, 7))
                .setWidth(10).setHeight(10);
        legendTable.addCell(mediumColor);
        legendTable.addCell(new Cell().add(new Paragraph("Medium Risk (1 conflict)").setFontSize(10)));
        legendTable.addCell(new Cell()); // Empty cell
        
        // High
        Cell highColor = new Cell().setBackgroundColor(new DeviceRgb(244, 67, 54))
                .setWidth(10).setHeight(10);
        legendTable.addCell(highColor);
        legendTable.addCell(new Cell().add(new Paragraph("High Risk (2+ conflicts)").setFontSize(10)));
        legendTable.addCell(new Cell()); // Empty cell
        
        document.add(legendTable);
        
        // Footer with timestamp
        document.add(new Paragraph("\nGenerated on: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER));
    }
}