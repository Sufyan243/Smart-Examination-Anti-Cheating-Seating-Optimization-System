package com.examseating.anticheating.service;

import com.examseating.anticheating.exception.*;
import com.examseating.anticheating.model.Student;
import com.examseating.anticheating.util.UniversityRollNumberValidator;
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
        
        try {
            UniversityRollNumberValidator.validateComplete(rollNo);
        } catch (InvalidRollNumberFormatException e) {
            throw new CSVRowValidationException(e.getMessage(), lineNumber, "RollNo");
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
        
        // Comprehensive examples covering all sections and both batches
        csv.append("2024F-BSE-001,Alice Johnson,DSA\n");
        csv.append("2024F-BSE-025,Bob Smith,Discrete Mathematics\n");
        csv.append("2023S-BSE-050,Carol Davis,Communication Skills\n");
        csv.append("2024F-BSE-051,David Wilson,SRE\n");
        csv.append("2023S-BSE-075,Emma Brown,OOP\n");
        csv.append("2024F-BSE-100,Frank Miller,Database Systems\n");
        csv.append("2023S-BSE-101,Grace Lee,Web Engineering\n");
        csv.append("2024F-BSE-125,Henry Taylor,DSA\n");
        csv.append("2023S-BSE-150,Ivy Chen,Discrete Mathematics\n");
        csv.append("2024F-BSE-151,Jack Anderson,Communication Skills\n");
        csv.append("2023S-BSE-175,Kate Thompson,SRE\n");
        csv.append("2024F-BSE-200,Liam Garcia,OOP\n");
        csv.append("2023S-BSE-201,Maya Patel,Database Systems\n");
        csv.append("2024F-BSE-225,Noah Rodriguez,Web Engineering\n");
        csv.append("2023S-BSE-250,Olivia Martinez,DSA\n");
        csv.append("2024F-BSE-251,Paul Jackson,Discrete Mathematics\n");
        csv.append("2023S-BSE-275,Quinn White,Communication Skills\n");
        csv.append("2024F-BSE-300,Ruby Harris,SRE\n");
        csv.append("2023S-BSE-015,Sam Clark,OOP\n");
        csv.append("2024F-BSE-065,Tina Lewis,Database Systems\n");
        csv.append("2023S-BSE-115,Uma Singh,Web Engineering\n");
        csv.append("2024F-BSE-165,Victor Chen,DSA\n");
        csv.append("2023S-BSE-215,Wendy Park,Discrete Mathematics\n");
        csv.append("2024F-BSE-265,Xavier Kim,Communication Skills\n");
        csv.append("2023S-BSE-035,Yara Ahmed,SRE\n");
        csv.append("2024F-BSE-085,Zoe Brown,OOP\n");
        csv.append("2023S-BSE-135,Alex Turner,Database Systems\n");
        csv.append("2024F-BSE-185,Beth Wilson,Web Engineering\n");
        csv.append("2023S-BSE-235,Chris Lee,DSA\n");
        csv.append("2024F-BSE-285,Dana Miller,Discrete Mathematics\n");
        
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