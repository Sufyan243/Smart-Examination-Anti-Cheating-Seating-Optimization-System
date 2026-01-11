package com.examseating.anticheating.model;

import lombok.Getter;

@Getter
public enum RiskLevel {
    SAFE(0, "#4CAF50", "🟩 Safe"),           // Green
    MEDIUM(1, "#FFC107", "🟨 Medium Risk"),  // Yellow
    HIGH(2, "#F44336", "🟥 High Risk");      // Red
    
    private final int threshold;
    private final String colorCode;
    private final String displayName;
    
    RiskLevel(int threshold, String colorCode, String displayName) {
        this.threshold = threshold;
        this.colorCode = colorCode;
        this.displayName = displayName;
    }
    
    public static RiskLevel fromConflictCount(int conflictCount) {
        if (conflictCount == 0) return SAFE;
        if (conflictCount == 1) return MEDIUM;
        return HIGH;
    }
}