package com.examseating.anticheating.model;

import com.examseating.anticheating.util.RollNumberUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private String rollNo;
    private String name;
    private String subject;
    private String batch;
    private String department;
    private String section;
    private Integer rollNumber;

    public Student(String rollNo, String name, String subject) {
        this.rollNo = rollNo;
        this.name = name;
        this.subject = subject;
        if (rollNo != null && !rollNo.trim().isEmpty()) {
            Map<String, Object> parsed = parseRollNumber(rollNo);
            this.batch = (String) parsed.get("batch");
            this.department = (String) parsed.get("department");
            this.rollNumber = (Integer) parsed.get("rollNumber");
            this.section = (String) parsed.get("section");
        }
    }

    public static String extractSectionFromRollNumber(int rollNumber) {
        return RollNumberUtils.determineSection(rollNumber);
    }

    public static Map<String, Object> parseRollNumber(String fullRollNo) {
        Map<String, Object> result = new HashMap<>();
        
        if (fullRollNo == null || fullRollNo.trim().isEmpty()) {
            result.put("batch", null);
            result.put("department", null);
            result.put("rollNumber", null);
            result.put("section", null);
            return result;
        }
        
        String batch = RollNumberUtils.extractBatch(fullRollNo);
        String department = RollNumberUtils.extractDepartment(fullRollNo);
        Integer numericRoll = RollNumberUtils.extractNumericPart(fullRollNo);
        String section = numericRoll != null ? RollNumberUtils.determineSection(numericRoll) : null;
        
        result.put("batch", batch);
        result.put("department", department);
        result.put("rollNumber", numericRoll);
        result.put("section", section);
        
        return result;
    }
}