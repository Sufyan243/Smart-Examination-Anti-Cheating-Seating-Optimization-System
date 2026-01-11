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
        String csvContent = "RollNo,Name,Subject\nS001,Alice,Math\nS002,Bob,Physics";
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes());
        
        List<Student> students = csvService.parseStudentsFromCSV(file);
        
        assertEquals(2, students.size());
        assertEquals("S001", students.get(0).getRollNo());
        assertEquals("Alice", students.get(0).getName());
        assertEquals("Math", students.get(0).getSubject());
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
        String csvContent = "RollNo,Name,Subject\nS001,Alice,Math\nS001,Bob,Physics";
        MockMultipartFile file = new MockMultipartFile(
                "file", "students.csv", "text/csv", csvContent.getBytes());
        
        DuplicateStudentException exception = assertThrows(DuplicateStudentException.class, () -> 
                csvService.parseStudentsFromCSV(file));
        assertEquals("S001", exception.getDuplicateRollNo());
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
        String csvContent = "RollNo,Name,Subject\n,Alice,Math";
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
        assertTrue(csvContent.contains("S001,Alice Johnson,Mathematics"));
    }
    
    @Test
    void testValidateRowWithEmptyName() {
        String[] row = {"S001", "", "Math"};
        
        CSVRowValidationException exception = assertThrows(CSVRowValidationException.class, () -> 
                csvService.validateRow(row, 2));
        assertEquals("Name", exception.getFieldName());
    }
    
    @Test
    void testValidateRowWithInvalidRollNo() {
        String[] row = {"S@01", "Alice", "Math"};
        
        CSVRowValidationException exception = assertThrows(CSVRowValidationException.class, () -> 
                csvService.validateRow(row, 2));
        assertEquals("RollNo", exception.getFieldName());
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