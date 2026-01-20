package com.examseating.anticheating.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RollNumberUtils {
    private static final Pattern UNIVERSITY_FORMAT = Pattern.compile("^(\\d{4}[FS])-([A-Z]{2,4})-(\\d{1,3})$");
    
    public static String extractBatch(String fullRollNo) {
        if (fullRollNo == null) return null;
        Matcher matcher = UNIVERSITY_FORMAT.matcher(fullRollNo.trim());
        return matcher.matches() ? matcher.group(1) : null;
    }
    
    public static String extractDepartment(String fullRollNo) {
        if (fullRollNo == null) return null;
        Matcher matcher = UNIVERSITY_FORMAT.matcher(fullRollNo.trim());
        return matcher.matches() ? matcher.group(2) : null;
    }
    
    public static Integer extractNumericPart(String fullRollNo) {
        if (fullRollNo == null) return null;
        Matcher matcher = UNIVERSITY_FORMAT.matcher(fullRollNo.trim());
        return matcher.matches() ? Integer.parseInt(matcher.group(3)) : null;
    }
    
    public static String determineSection(int numericRollNo) {
        if (numericRollNo >= 1 && numericRollNo <= 50) return "A";
        if (numericRollNo >= 51 && numericRollNo <= 100) return "B";
        if (numericRollNo >= 101 && numericRollNo <= 150) return "C";
        if (numericRollNo >= 151 && numericRollNo <= 200) return "D";
        if (numericRollNo >= 201 && numericRollNo <= 250) return "E";
        if (numericRollNo >= 251 && numericRollNo <= 300) return "F";
        return "UNKNOWN";
    }
    
    public static boolean validateUniversityFormat(String fullRollNo) {
        if (fullRollNo == null) return false;
        return UNIVERSITY_FORMAT.matcher(fullRollNo.trim()).matches();
    }
    
    public static String buildFullRollNo(String batch, String department, int rollNumber) {
        return String.format("%s-%s-%03d", batch, department, rollNumber);
    }
}