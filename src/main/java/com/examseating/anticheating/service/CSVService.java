package com.examseating.anticheating.service;

import com.examseating.anticheating.exception.*;
import com.examseating.anticheating.model.Student;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class CSVService {
    
    @Value("${csv.max-rows:1000}")
    private int maxRows = 1000;
    
    public List<Student> parseStudentsFromCSV(MultipartFile file) {
        log.info("Parsing students from CSV file: {}", file.getOriginalFilename());
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> records = reader.readAll();
            
            if (records.isEmpty()) {
                throw new EmptyCSVException();
            }
            
            if (records.size() > maxRows + 1) { // +1 for header
                throw new CSVParsingException("CSV exceeds maximum allowed rows: " + maxRows);
            }
            
            // Validate header
            validateCSVStructure(records.get(0));
            
            List<Student> students = new ArrayList<>();;
            
            for (int i = 1; i < records.size(); i++) {
                String[] record = records.get(i);
                validateRow(record, i + 1);
                
                Student student = new Student(
                    record[0].trim(),
                    record[1].trim(),
                    record[2].trim()
                );
                students.add(student);
            }
            
            checkDuplicateRollNumbers(students);
            
            log.info("Successfully parsed {} students from CSV", students.size());
            return students;
            
        } catch (IOException e) {
            log.error("IO error reading CSV file: {}", e.getMessage());
            throw new CSVParsingException("Error reading CSV file: " + e.getMessage(), e);
        } catch (CsvException e) {
            log.error("CSV parsing error: {}", e.getMessage());
            throw new CSVParsingException("Invalid CSV format: " + e.getMessage(), e);
        }
    }
    
    public void validateCSVStructure(String[] header) {
        if (header.length != 3) {
            throw new InvalidCSVStructureException("CSV must have exactly 3 columns: RollNo,Name,Subject");
        }
        
        String[] expectedHeaders = {"rollno", "name", "subject"};
        for (int i = 0; i < header.length; i++) {
            if (!header[i].toLowerCase().trim().equals(expectedHeaders[i])) {
                throw new InvalidCSVStructureException("Invalid header format. Expected: RollNo,Name,Subject");
            }
        }
    }
    
    public void validateRow(String[] row, int lineNumber) {
        if (row.length != 3) {
            throw new CSVRowValidationException("Row must have exactly 3 columns", lineNumber);
        }
        
        String rollNo = row[0].trim();
        String name = row[1].trim();
        String subject = row[2].trim();
        
        if (rollNo.isEmpty() || !rollNo.matches("[A-Za-z0-9]+")) {
            throw new CSVRowValidationException("Invalid roll number format (must be alphanumeric)", lineNumber, "RollNo");
        }
        
        if (name.isEmpty()) {
            throw new CSVRowValidationException("Name cannot be empty", lineNumber, "Name");
        }
        
        if (subject.isEmpty()) {
            throw new CSVRowValidationException("Subject cannot be empty", lineNumber, "Subject");
        }
    }
    
    public void checkDuplicateRollNumbers(List<Student> students) {
        Set<String> rollNumbers = new HashSet<>();
        for (Student student : students) {
            if (!rollNumbers.add(student.getRollNo())) {
                throw new DuplicateStudentException(student.getRollNo());
            }
        }
    }
    
    public byte[] generateSampleCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("RollNo,Name,Subject\n");
        csv.append("S001,Alice Johnson,Mathematics\n");
        csv.append("S002,Bob Smith,Physics\n");
        csv.append("S003,Charlie Brown,Chemistry\n");
        csv.append("S004,Diana Prince,Biology\n");
        csv.append("S005,Eve Wilson,Computer Science\n");
        
        return csv.toString().getBytes();
    }
    
    public boolean validateCSVFormat(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }
}