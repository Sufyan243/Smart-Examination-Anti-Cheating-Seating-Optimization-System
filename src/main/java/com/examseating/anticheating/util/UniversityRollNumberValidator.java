package com.examseating.anticheating.util;

import com.examseating.anticheating.exception.InvalidRollNumberFormatException;
import com.examseating.anticheating.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validator for university roll number format and constraints.
 * 
 * University roll number format: YYYY[F/S]-BSE-XXX
 * - YYYY: Batch year (2023 or 2024)
 * - [F/S]: Semester (Fall or Spring)
 * - BSE: Department (Software Engineering only)
 * - XXX: Numeric roll number (1-300)
 * 
 * Examples:
 * - Valid: 2024F-BSE-025, 2023S-BSE-150, 2024F-BSE-300
 * - Invalid: 2025F-BSE-001 (invalid year), 2024F-CS-001 (invalid dept), 2024F-BSE-301 (out of range)
 */
public class UniversityRollNumberValidator {
    
    private static final Pattern UNIVERSITY_ROLL_NUMBER_PATTERN = Pattern.compile("^\\d{4}[FS]-BSE-\\d{1,3}$");
    private static final Set<String> ALLOWED_BATCH_YEARS = Set.of("2023", "2024");
    private static final String ALLOWED_DEPARTMENT = "BSE";
    private static final int MIN_ROLL_NUMBER = 1;
    private static final int MAX_ROLL_NUMBER = 300;
    private static final Set<String> ALLOWED_SECTIONS = Set.of("A", "B", "C", "D", "E", "F");

    /**
     * Validates the basic format pattern of the roll number.
     * @param rollNo Roll number to validate
     * @throws InvalidRollNumberFormatException if format is invalid
     */
    public static void validateFormat(String rollNo) {
        if (rollNo == null || !UNIVERSITY_ROLL_NUMBER_PATTERN.matcher(rollNo).matches()) {
            throw new InvalidRollNumberFormatException(rollNo, "Must match format YYYY[F/S]-BSE-XXX");
        }
    }

    /**
     * Validates the batch year is within allowed range.
     * @param rollNo Roll number to validate
     * @throws InvalidRollNumberFormatException if batch year is invalid
     */
    public static void validateBatchYear(String rollNo) {
        String batch = RollNumberUtils.extractBatch(rollNo);
        if (batch == null) {
            throw new InvalidRollNumberFormatException(rollNo, "Cannot extract batch information");
        }
        String year = batch.substring(0, 4);
        if (!ALLOWED_BATCH_YEARS.contains(year)) {
            throw new InvalidRollNumberFormatException(rollNo, "Batch year must be 2023 or 2024");
        }
    }

    /**
     * Validates the department is BSE.
     * @param rollNo Roll number to validate
     * @throws InvalidRollNumberFormatException if department is invalid
     */
    public static void validateDepartment(String rollNo) {
        String department = RollNumberUtils.extractDepartment(rollNo);
        if (!ALLOWED_DEPARTMENT.equals(department)) {
            throw new InvalidRollNumberFormatException(rollNo, "Department must be BSE");
        }
    }

    /**
     * Validates the numeric part is within allowed range.
     * @param rollNo Roll number to validate
     * @throws InvalidRollNumberFormatException if numeric range is invalid
     */
    public static void validateNumericRange(String rollNo) {
        Integer numericPart = RollNumberUtils.extractNumericPart(rollNo);
        if (numericPart == null) {
            throw new InvalidRollNumberFormatException(rollNo, "Cannot extract numeric part");
        }
        if (numericPart < MIN_ROLL_NUMBER || numericPart > MAX_ROLL_NUMBER) {
            throw new InvalidRollNumberFormatException(rollNo, 
                String.format("Numeric part must be between %d and %d", MIN_ROLL_NUMBER, MAX_ROLL_NUMBER));
        }
    }

    /**
     * Validates the derived section is within allowed range.
     * @param rollNo Roll number to validate
     * @throws InvalidRollNumberFormatException if section is invalid
     */
    public static void validateSection(String rollNo) {
        Integer numericPart = RollNumberUtils.extractNumericPart(rollNo);
        if (numericPart == null) {
            throw new InvalidRollNumberFormatException(rollNo, "Cannot extract numeric part");
        }
        String section = Student.extractSectionFromRollNumber(numericPart);
        if (!ALLOWED_SECTIONS.contains(section)) {
            throw new InvalidRollNumberFormatException(rollNo, "Section must be A-F (derived from numeric range)");
        }
    }

    /**
     * Performs complete validation of the roll number.
     * @param rollNo Roll number to validate
     * @return true if all validations pass
     * @throws InvalidRollNumberFormatException if any validation fails
     */
    public static boolean validateComplete(String rollNo) {
        validateFormat(rollNo);
        validateBatchYear(rollNo);
        validateDepartment(rollNo);
        validateNumericRange(rollNo);
        validateSection(rollNo);
        return true;
    }

    /**
     * Returns all validation errors without throwing exceptions.
     * @param rollNo Roll number to validate
     * @return List of error messages
     */
    public static List<String> getValidationErrors(String rollNo) {
        List<String> errors = new ArrayList<>();
        
        try { validateFormat(rollNo); } 
        catch (InvalidRollNumberFormatException e) { 
            errors.add(e.getReason());
            return errors; // Short-circuit: other validations require valid format
        }
        
        try { validateBatchYear(rollNo); } 
        catch (InvalidRollNumberFormatException e) { errors.add(e.getReason()); }
        
        try { validateDepartment(rollNo); } 
        catch (InvalidRollNumberFormatException e) { errors.add(e.getReason()); }
        
        try { validateNumericRange(rollNo); } 
        catch (InvalidRollNumberFormatException e) { errors.add(e.getReason()); }
        
        try { validateSection(rollNo); } 
        catch (InvalidRollNumberFormatException e) { errors.add(e.getReason()); }
        
        return errors;
    }
}