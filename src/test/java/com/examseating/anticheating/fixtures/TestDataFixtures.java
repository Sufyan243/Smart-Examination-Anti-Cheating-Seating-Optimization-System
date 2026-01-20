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
        String[] subjects = {"DSA", "Discrete Mathematics", "Communication Skills", "SRE", "OOP"};
        
        for (int i = 0; i < count; i++) {
            String rollNo = String.format("2024F-BSE-%03d", i + 1);
            students.add(new Student(
                rollNo,
                "Student " + (i + 1),
                subjects[i % subjectCount]
            ));
        }
        
        return students;
    }
    
    public static List<Student> createStudentsWithSubject(int count, String subject) {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String rollNo = String.format("2024F-BSE-%03d", i + 1);
            students.add(new Student(
                rollNo,
                "Student " + (i + 1),
                subject
            ));
        }
        return students;
    }
    
    public static String getValidCSVContent() {
        StringBuilder csv = new StringBuilder("RollNo,Name,Subject\n");
        for (int i = 1; i <= 50; i++) {
            csv.append(String.format("2024F-BSE-%03d,Student %d,DSA\n", i, i));
        }
        return csv.toString();
    }
    
    public static String getCSVWithDuplicates() {
        return "RollNo,Name,Subject\n" +
               "2024F-BSE-001,Student 1,DSA\n" +
               "2024F-BSE-002,Student 2,OOP\n" +
               "2024F-BSE-001,Student 3,SRE\n";
    }
    
    public static Seat[][] createSeatsWithNoConflicts() {
        Seat[][] seats = createSeats(10, 10);
        
        // Place students with different subjects around center seat
        seats[5][5].setStudent(new Student("2024F-BSE-025", "Alice", "DSA"));
        seats[4][5].setStudent(new Student("2024F-BSE-075", "Bob", "OOP"));
        seats[6][5].setStudent(new Student("2024F-BSE-125", "Charlie", "SRE"));
        seats[5][4].setStudent(new Student("2024F-BSE-175", "Diana", "Discrete Mathematics"));
        seats[5][6].setStudent(new Student("2024F-BSE-225", "Eve", "Communication Skills"));
        
        return seats;
    }
    
    public static Seat[][] createSeatsWithOneConflict() {
        Seat[][] seats = createSeats(10, 10);
        
        // Place students with one same-subject neighbor
        seats[5][5].setStudent(new Student("2024F-BSE-025", "Alice", "DSA"));
        seats[4][5].setStudent(new Student("2024F-BSE-075", "Bob", "DSA")); // Same subject
        seats[6][5].setStudent(new Student("2024F-BSE-125", "Charlie", "OOP"));
        seats[5][4].setStudent(new Student("2024F-BSE-175", "Diana", "SRE"));
        seats[5][6].setStudent(new Student("2024F-BSE-225", "Eve", "Discrete Mathematics"));
        
        return seats;
    }
    
    public static Seat[][] createSeatsWithMultipleConflicts() {
        Seat[][] seats = createSeats(10, 10);
        
        // Place students with multiple same-subject neighbors
        seats[5][5].setStudent(new Student("2024F-BSE-025", "Alice", "DSA"));
        seats[4][5].setStudent(new Student("2024F-BSE-075", "Bob", "DSA")); // Same subject
        seats[6][5].setStudent(new Student("2024F-BSE-125", "Charlie", "DSA")); // Same subject
        seats[5][4].setStudent(new Student("2024F-BSE-175", "Diana", "OOP"));
        seats[5][6].setStudent(new Student("2024F-BSE-225", "Eve", "SRE"));
        
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
        seats[0][0].setStudent(new Student("2024F-BSE-025", "Alice", "DSA"));
        seats[0][0].setRiskLevel(RiskLevel.SAFE);
        seats[0][0].setRiskScore(0.0);
        
        seats[1][1].setStudent(new Student("2024F-BSE-075", "Bob", "OOP"));
        seats[1][1].setRiskLevel(RiskLevel.MEDIUM);
        seats[1][1].setRiskScore(25.0);
        
        seats[2][2].setStudent(new Student("2024F-BSE-125", "Charlie", "SRE"));
        seats[2][2].setRiskLevel(RiskLevel.HIGH);
        seats[2][2].setRiskScore(75.0);
        
        return hall;
    }
    
    public static Student createStudent2024FSecA() {
        return new Student("2024F-BSE-025", "Alice Johnson", "DSA");
    }
    
    public static Student createStudent2023SSecB() {
        return new Student("2023S-BSE-075", "Bob Smith", "OOP");
    }
    
    public static Student createStudent2024FSecC() {
        return new Student("2024F-BSE-125", "Charlie Brown", "SRE");
    }
}