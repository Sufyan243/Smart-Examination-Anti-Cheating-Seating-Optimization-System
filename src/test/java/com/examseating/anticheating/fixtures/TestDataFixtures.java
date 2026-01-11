package com.examseating.anticheating.fixtures;

import com.examseating.anticheating.model.ExamHall;
import com.examseating.anticheating.model.RiskLevel;
import com.examseating.anticheating.model.Seat;
import com.examseating.anticheating.model.Student;

import java.util.ArrayList;
import java.util.List;

public class TestDataFixtures {
    
    public static List<Student> createStudents(int count, int subjectCount) {
        List<Student> students = new ArrayList<>();
        String[] subjects = {"Math", "Physics", "Chemistry", "Biology", "English"};
        
        for (int i = 0; i < count; i++) {
            students.add(new Student(
                "2021" + String.format("%03d", i + 1),
                "Student " + (i + 1),
                subjects[i % subjectCount]
            ));
        }
        
        return students;
    }
    
    public static List<Student> createStudentsWithSubject(int count, String subject) {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            students.add(new Student(
                "2021" + String.format("%03d", i + 1),
                "Student " + (i + 1),
                subject
            ));
        }
        return students;
    }
    
    public static String getValidCSVContent() {
        StringBuilder csv = new StringBuilder("RollNo,Name,Subject\n");
        for (int i = 1; i <= 50; i++) {
            csv.append(String.format("2021%03d,Student %d,Math\n", i, i));
        }
        return csv.toString();
    }
    
    public static String getCSVWithDuplicates() {
        return "RollNo,Name,Subject\n" +
               "2021001,Student 1,Math\n" +
               "2021002,Student 2,Physics\n" +
               "2021001,Student 3,Chemistry\n";
    }
    
    public static Seat[][] createSeatsWithNoConflicts() {
        Seat[][] seats = createSeats(10, 10);
        
        // Place students with different subjects around center seat
        seats[5][5].setStudent(new Student("S001", "Alice", "Math"));
        seats[4][5].setStudent(new Student("S002", "Bob", "Physics"));
        seats[6][5].setStudent(new Student("S003", "Charlie", "Chemistry"));
        seats[5][4].setStudent(new Student("S004", "Diana", "Biology"));
        seats[5][6].setStudent(new Student("S005", "Eve", "English"));
        
        return seats;
    }
    
    public static Seat[][] createSeatsWithOneConflict() {
        Seat[][] seats = createSeats(10, 10);
        
        // Place students with one same-subject neighbor
        seats[5][5].setStudent(new Student("S001", "Alice", "Math"));
        seats[4][5].setStudent(new Student("S002", "Bob", "Math")); // Same subject
        seats[6][5].setStudent(new Student("S003", "Charlie", "Physics"));
        seats[5][4].setStudent(new Student("S004", "Diana", "Chemistry"));
        seats[5][6].setStudent(new Student("S005", "Eve", "Biology"));
        
        return seats;
    }
    
    public static Seat[][] createSeatsWithMultipleConflicts() {
        Seat[][] seats = createSeats(10, 10);
        
        // Place students with multiple same-subject neighbors
        seats[5][5].setStudent(new Student("S001", "Alice", "Math"));
        seats[4][5].setStudent(new Student("S002", "Bob", "Math")); // Same subject
        seats[6][5].setStudent(new Student("S003", "Charlie", "Math")); // Same subject
        seats[5][4].setStudent(new Student("S004", "Diana", "Physics"));
        seats[5][6].setStudent(new Student("S005", "Eve", "Chemistry"));
        
        return seats;
    }
    
    public static Seat[][] createSeats(int rows, int cols) {
        Seat[][] seats = new Seat[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                seats[i][j] = new Seat(i, j, null, RiskLevel.SAFE, 0.0);
            }
        }
        return seats;
    }
    
    public static ExamHall createHallWithKnownRisks() {
        ExamHall hall = new ExamHall("TEST_HALL", 5, 5);
        Seat[][] seats = hall.getSeats();
        
        // Set up known risk scenarios
        seats[0][0].setStudent(new Student("S001", "Alice", "Math"));
        seats[0][0].setRiskLevel(RiskLevel.SAFE);
        seats[0][0].setRiskScore(0.0);
        
        seats[1][1].setStudent(new Student("S002", "Bob", "Physics"));
        seats[1][1].setRiskLevel(RiskLevel.MEDIUM);
        seats[1][1].setRiskScore(25.0);
        
        seats[2][2].setStudent(new Student("S003", "Charlie", "Chemistry"));
        seats[2][2].setRiskLevel(RiskLevel.HIGH);
        seats[2][2].setRiskScore(75.0);
        
        return hall;
    }
}