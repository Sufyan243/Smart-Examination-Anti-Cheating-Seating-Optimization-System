package com.examseating.anticheating.service;

import com.examseating.anticheating.exception.*;
import com.examseating.anticheating.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CSVServiceTest {
    
    private CSVService csvService;
    
    @BeforeEach
    void setUp() {
        csvService = new CSVService();
    }
    
    @Test
    void testParseStudentsFromCSV() throws Exception {
        String csvContent = "RollNo,Name,Subject\n2024F-BSE-025,Alice,DSA\n2024F-BSE-075,Bob,OOP";
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes());
        
        List<Student> students = csvService.parseStudentsFromCSV(file);
        
        assertEquals(2, students.size());
        assertEquals("2024F-BSE-025", students.get(0).getRollNo());
        assertEquals("Alice", students.get(0).getName());
        assertEquals("DSA", students.get(0).getSubject());
    }
    
    @Test
    void testParseCSVWithInvalidStructure() {
        String csvContent = "RollNo,Name\nS001,Alice\nS002,Bob";
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes());
        
        assertThrows(InvalidCSVStructureException.class, () -> 
                csvService.parseStudentsFromCSV(file));
    }
    
    @Test
    void testParseCSVWithDuplicates() {
        String csvContent = "RollNo,Name,Subject\n2024F-BSE-025,Alice,DSA\n2024F-BSE-025,Bob,OOP";
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes());
        
        DuplicateStudentException exception = assertThrows(DuplicateStudentException.class, () -> 
                csvService.parseStudentsFromCSV(file));
        assertEquals("2024F-BSE-025", exception.getDuplicateRollNo());
    }
    
    @Test
    void testParseCSVWithEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.csv", "text/csv", "".getBytes());
        
        assertThrows(EmptyCSVException.class, () -> 
                csvService.parseStudentsFromCSV(file));
    }
    
    @Test
    void testParseCSVWithInvalidRow() {
        String csvContent = "RollNo,Name,Subject\n2025F-BSE-001,Alice,DSA";
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes());
        
        CSVRowValidationException exception = assertThrows(CSVRowValidationException.class, () -> 
                csvService.parseStudentsFromCSV(file));
        assertEquals(2, exception.getLineNumber());
        assertEquals("RollNo", exception.getFieldName());
    }
    
    @Test
    void testGenerateSampleCSV() {
        byte[] csvBytes = csvService.generateSampleCSV();
        String csvContent = new String(csvBytes);
        
        assertTrue(csvContent.startsWith("RollNo,Name,Subject"));
        assertTrue(csvContent.contains("2024F-BSE-001,Alice Johnson,DSA"));
        assertTrue(csvContent.contains("2024F-BSE-300,Ruby Harris,SRE"));
    }
    
    @Test
    void testValidateRowWithEmptyName() {
        String[] row = {"2024F-BSE-001", "", "DSA"};
        
        CSVRowValidationException exception = assertThrows(CSVRowValidationException.class, () -> 
                csvService.validateRow(row, 2));
        assertEquals("Name", exception.getFieldName());
    }
    
    @Test
    void testValidateRowWithInvalidRollNo() {
        String[] row = {"2025F-BSE-001", "Alice", "DSA"};
        
        CSVRowValidationException exception = assertThrows(CSVRowValidationException.class, () -> 
                csvService.validateRow(row, 2));
        assertEquals("RollNo", exception.getFieldName());
    }
    
    @Test
    void testValidateUniversityFormatValid() {
        String[][] validRows = {
            {"2024F-BSE-025", "Alice", "DSA"},
            {"2023S-BSE-150", "Bob", "OOP"},
            {"2024F-BSE-300", "Carol", "SRE"}
        };
        
        for (String[] row : validRows) {
            assertDoesNotThrow(() -> csvService.validateRow(row, 1));
        }
    }
    
    @Test
    void testValidateUniversityFormatInvalidYear() {
        String[] row = {"2025F-BSE-001", "Alice", "DSA"};
        
        CSVRowValidationException exception = assertThrows(CSVRowValidationException.class, () -> 
                csvService.validateRow(row, 2));
        assertTrue(exception.getMessage().contains("Batch year must be 2023 or 2024"));
    }
    
    @Test
    void testValidateUniversityFormatInvalidDepartment() {
        String[] row = {"2024F-CS-001", "Alice", "DSA"};
        
        CSVRowValidationException exception = assertThrows(CSVRowValidationException.class, () -> 
                csvService.validateRow(row, 2));
        assertTrue(exception.getMessage().contains("Department must be BSE"));
    }
    
    @Test
    void testValidateUniversityFormatInvalidRange() {
        String[] row = {"2024F-BSE-301", "Alice", "DSA"};
        
        CSVRowValidationException exception = assertThrows(CSVRowValidationException.class, () -> 
                csvService.validateRow(row, 2));
        assertTrue(exception.getMessage().contains("must be between 1 and 300"));
    }
    
    @Test
    void testValidateUniversityFormatInvalidPattern() {
        String[] row = {"2024-BSE-001", "Alice", "DSA"};
        
        CSVRowValidationException exception = assertThrows(CSVRowValidationException.class, () -> 
                csvService.validateRow(row, 2));
        assertTrue(exception.getMessage().contains("Must match format"));
    }
    
    @Test
    void testValidateCSVFormat() {
        MockMultipartFile validFile = new MockMultipartFile(
                "file", "test.csv", "text/csv", "content".getBytes());
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());
        
        assertTrue(csvService.validateCSVFormat(validFile));
        assertFalse(csvService.validateCSVFormat(invalidFile));
    }
}