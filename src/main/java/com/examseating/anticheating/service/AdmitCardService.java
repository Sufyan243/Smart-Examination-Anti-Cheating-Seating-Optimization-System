package com.examseating.anticheating.service;

import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.Seat;
import com.google.zxing.WriterException;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdmitCardService {

    private final QRCodeService qrCodeService;

    public byte[] generateAdmitCard(String rollNo, String name, String subject, 
                                     String hallId, int row, int col, 
                                     String examDate, String examTime) throws IOException, WriterException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        document.add(new Paragraph("Software Engineering Department")
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));
        
        document.add(new Paragraph("EXAMINATION ADMIT CARD")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        Table mainTable = new Table(2);
        mainTable.setWidth(UnitValue.createPercentValue(100));

        Table detailsTable = new Table(2);
        detailsTable.setWidth(UnitValue.createPercentValue(100));
        
        addDetailRow(detailsTable, "Roll Number:", rollNo);
        addDetailRow(detailsTable, "Name:", name);
        addDetailRow(detailsTable, "Subject:", subject);
        
        // Extract batch, department, section from roll number if available
        try {
            String batch = com.examseating.anticheating.util.RollNumberUtils.extractBatch(rollNo);
            String department = com.examseating.anticheating.util.RollNumberUtils.extractDepartment(rollNo);
            Integer numericRoll = com.examseating.anticheating.util.RollNumberUtils.extractNumericPart(rollNo);
            String section = numericRoll != null ? com.examseating.anticheating.util.RollNumberUtils.determineSection(numericRoll) : null;
            
            if (batch != null) addDetailRow(detailsTable, "Batch:", batch);
            if (department != null) addDetailRow(detailsTable, "Department:", department);
            if (section != null) addDetailRow(detailsTable, "Section:", section);
        } catch (Exception e) {
            // Continue without university-specific fields if parsing fails
        }
        
        addDetailRow(detailsTable, "Hall:", hallId);
        addDetailRow(detailsTable, "Seat:", String.format("Row %d, Col %d", row + 1, col + 1));
        addDetailRow(detailsTable, "Exam Date:", examDate);
        addDetailRow(detailsTable, "Exam Time:", examTime);

        mainTable.addCell(new Cell().add(detailsTable).setBorder(null));

        byte[] qrCode = qrCodeService.generateAdmitCardQRCode(rollNo, name, hallId, row, col);
        Image qrImage = new Image(ImageDataFactory.create(qrCode));
        qrImage.setWidth(150);
        qrImage.setHeight(150);
        
        Cell qrCell = new Cell()
                .add(new Paragraph("Scan for Verification").setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                .add(qrImage)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(null);
        mainTable.addCell(qrCell);

        document.add(mainTable);

        document.add(new Paragraph("\nInstructions:").setBold().setMarginTop(20));
        document.add(new Paragraph("1. Bring this admit card to the examination hall").setFontSize(10));
        document.add(new Paragraph("2. Report to your assigned seat 15 minutes before exam time").setFontSize(10));
        document.add(new Paragraph("3. Carry a valid photo ID for verification").setFontSize(10));
        document.add(new Paragraph("4. Mobile phones and electronic devices are strictly prohibited").setFontSize(10));

        document.add(new Paragraph(String.format("Generated on: %s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30));

        document.close();
        log.info("Generated admit card for student: {}", rollNo);
        
        return baos.toByteArray();
    }

    public byte[] generateBulkAdmitCards(ExamHall hall, String examDate, String examTime) throws IOException, WriterException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        Seat[][] seats = hall.getSeats();
        int cardCount = 0;

        for (int row = 0; row < hall.getRows(); row++) {
            for (int col = 0; col < hall.getCols(); col++) {
                if (seats[row][col].isOccupied()) {
                    if (cardCount > 0) {
                        document.add(new AreaBreak());
                    }

                    String rollNo = seats[row][col].getStudent().getRollNo();
                    String name = seats[row][col].getStudent().getName();
                    String subject = seats[row][col].getStudent().getSubject();

                    addAdmitCardToDocument(document, rollNo, name, subject, 
                            hall.getHallId(), row, col, examDate, examTime);
                    cardCount++;
                }
            }
        }

        document.close();
        log.info("Generated {} admit cards for hall: {}", cardCount, hall.getHallId());
        
        return baos.toByteArray();
    }

    private void addAdmitCardToDocument(Document document, String rollNo, String name, String subject,
                                         String hallId, int row, int col, String examDate, String examTime) 
            throws WriterException, IOException {
        document.add(new Paragraph("Software Engineering Department")
                .setFontSize(16)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));
        
        // Add batch information if available
        try {
            String batch = com.examseating.anticheating.util.RollNumberUtils.extractBatch(rollNo);
            if (batch != null) {
                document.add(new Paragraph("Batch " + batch)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10));
            }
        } catch (Exception e) {
            // Continue without batch info if parsing fails
        }
        
        document.add(new Paragraph("EXAMINATION ADMIT CARD")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        Table mainTable = new Table(2);
        mainTable.setWidth(UnitValue.createPercentValue(100));

        Table detailsTable = new Table(2);
        detailsTable.setWidth(UnitValue.createPercentValue(100));
        
        addDetailRow(detailsTable, "Roll Number:", rollNo);
        addDetailRow(detailsTable, "Name:", name);
        addDetailRow(detailsTable, "Subject:", subject);
        
        // Extract batch, department, section from roll number if available
        try {
            String batch = com.examseating.anticheating.util.RollNumberUtils.extractBatch(rollNo);
            String department = com.examseating.anticheating.util.RollNumberUtils.extractDepartment(rollNo);
            Integer numericRoll = com.examseating.anticheating.util.RollNumberUtils.extractNumericPart(rollNo);
            String section = numericRoll != null ? com.examseating.anticheating.util.RollNumberUtils.determineSection(numericRoll) : null;
            
            if (batch != null) addDetailRow(detailsTable, "Batch:", batch);
            if (department != null) addDetailRow(detailsTable, "Department:", department);
            if (section != null) addDetailRow(detailsTable, "Section:", section);
        } catch (Exception e) {
            // Continue without university-specific fields if parsing fails
        }
        
        addDetailRow(detailsTable, "Hall:", hallId);
        addDetailRow(detailsTable, "Seat:", String.format("Row %d, Col %d", row + 1, col + 1));
        addDetailRow(detailsTable, "Exam Date:", examDate);
        addDetailRow(detailsTable, "Exam Time:", examTime);

        mainTable.addCell(new Cell().add(detailsTable).setBorder(null));

        byte[] qrCode = qrCodeService.generateAdmitCardQRCode(rollNo, name, hallId, row, col);
        Image qrImage = new Image(ImageDataFactory.create(qrCode));
        qrImage.setWidth(150);
        qrImage.setHeight(150);
        
        Cell qrCell = new Cell()
                .add(new Paragraph("Scan for Verification").setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                .add(qrImage)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorder(null);
        mainTable.addCell(qrCell);

        document.add(mainTable);

        document.add(new Paragraph("\nInstructions:").setBold().setMarginTop(20));
        document.add(new Paragraph("1. Bring this admit card to the examination hall").setFontSize(10));
        document.add(new Paragraph("2. Report to your assigned seat 15 minutes before exam time").setFontSize(10));
        document.add(new Paragraph("3. Carry a valid photo ID for verification").setFontSize(10));
        document.add(new Paragraph("4. Mobile phones and electronic devices are strictly prohibited").setFontSize(10));

        document.add(new Paragraph(String.format("Generated on: %s", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(30));
    }

    private void addDetailRow(Table table, String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setBold())
                .setBorder(null)
                .setPadding(5));
        table.addCell(new Cell()
                .add(new Paragraph(value))
                .setBorder(null)
                .setPadding(5));
    }
}
