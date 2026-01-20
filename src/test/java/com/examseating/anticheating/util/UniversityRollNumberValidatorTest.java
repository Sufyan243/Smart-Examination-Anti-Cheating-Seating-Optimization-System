package com.examseating.anticheating.util;

import com.examseating.anticheating.exception.InvalidRollNumberFormatException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UniversityRollNumberValidatorTest {

    @Test
    public void testValidateFormat_ValidFormats() {
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateFormat("2024F-BSE-001"));
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateFormat("2023S-BSE-150"));
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateFormat("2024F-BSE-300"));
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateFormat("2024F-BSE-1"));
    }

    @Test
    public void testValidateFormat_InvalidFormats() {
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateFormat("2024-BSE-001"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateFormat("2024F-CS-001"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateFormat(null));
    }

    @Test
    public void testValidateBatchYear_ValidYears() {
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateBatchYear("2023F-BSE-001"));
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateBatchYear("2024S-BSE-001"));
    }

    @Test
    public void testValidateBatchYear_InvalidYears() {
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateBatchYear("2022F-BSE-001"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateBatchYear("2025S-BSE-001"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateBatchYear("2026F-BSE-001"));
    }

    @Test
    public void testValidateDepartment_Valid() {
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateDepartment("2024F-BSE-001"));
    }

    @Test
    public void testValidateDepartment_Invalid() {
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateDepartment("2024F-CS-001"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateDepartment("2024F-SE-001"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateDepartment("2024F-IT-001"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateDepartment("2024F-CE-001"));
    }

    @Test
    public void testValidateNumericRange_Valid() {
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateNumericRange("2024F-BSE-001"));
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateNumericRange("2024F-BSE-050"));
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateNumericRange("2024F-BSE-150"));
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateNumericRange("2024F-BSE-300"));
    }

    @Test
    public void testValidateNumericRange_Invalid() {
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateNumericRange("2024F-BSE-000"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateNumericRange("2024F-BSE-301"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateNumericRange("2024F-BSE-500"));
    }

    @Test
    public void testValidateSection_ValidSections() {
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-025")); // A
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-075")); // B
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-125")); // C
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-175")); // D
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-225")); // E
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-275")); // F
    }

    @Test
    public void testValidateSection_BoundaryCases() {
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-050")); // A
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-051")); // B
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-100")); // B
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-101")); // C
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-150")); // C
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-151")); // D
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-200")); // D
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-201")); // E
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-250")); // E
        assertDoesNotThrow(() -> UniversityRollNumberValidator.validateSection("2024F-BSE-251")); // F
    }

    @Test
    public void testValidateComplete_ValidRollNumbers() {
        assertTrue(UniversityRollNumberValidator.validateComplete("2024F-BSE-025"));
        assertTrue(UniversityRollNumberValidator.validateComplete("2023S-BSE-150"));
        assertTrue(UniversityRollNumberValidator.validateComplete("2024F-BSE-300"));
    }

    @Test
    public void testValidateComplete_InvalidCombinations() {
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateComplete("2025F-BSE-001"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateComplete("2024F-CS-001"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateComplete("2024F-BSE-301"));
        assertThrows(InvalidRollNumberFormatException.class, 
            () -> UniversityRollNumberValidator.validateComplete("2024-BSE-001"));
    }

    @Test
    public void testGetValidationErrors_ValidRollNumber() {
        List<String> errors = UniversityRollNumberValidator.getValidationErrors("2024F-BSE-025");
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testGetValidationErrors_MultipleErrors() {
        List<String> errors = UniversityRollNumberValidator.getValidationErrors("2025F-CS-301");
        assertFalse(errors.isEmpty());
        assertTrue(errors.size() >= 2); // Should have multiple validation errors
    }

    @Test
    public void testGetValidationErrors_InvalidFormat() {
        List<String> errors = UniversityRollNumberValidator.getValidationErrors("invalid-format");
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("Must match format"));
    }
}