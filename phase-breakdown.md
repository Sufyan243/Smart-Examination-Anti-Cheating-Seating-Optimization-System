# Phase Breakdown

## Task 1: Setup Spring Boot Project Structure & Core Domain Models

Initialize Spring Boot project with Maven/Gradle:
Create domain models: `Student` (rollNo, name, subject), `Seat` (row, col, student, riskLevel), `ExamHall` (hallId, rows, cols, seats[][])
Add `RiskLevel` enum (SAFE, MEDIUM, HIGH) with color codes
Setup `application.properties` with server configuration
Add dependencies: Spring Web, Lombok, Apache POI (CSV), iText (PDF)


## Task 2: Implement Graph Coloring & Greedy Seat Allocation Algorithm

Build core DSA logic in `SeatAllocationService`:
Implement Greedy Graph Coloring algorithm: treat students as nodes, same-subject adjacency as edges
Create `allocateSeats(List<Student>, ExamHall)` method using 2D array traversal
Ensure same subject students are NOT placed adjacent (4-directional check)
Add `generateRandomSeating()` for before/after comparison
Use Queue for student assignment flow


## Task 3: Implement Anti-Cheating Risk Detection & Scoring System

Create `RiskDetectionService` with adjacency-based risk calculation:
Implement `calculateRiskForSeat(Seat[][], int row, int col)`: check 4 neighbors (up, down, left, right)
Count conflicts: `conflictCount = adjacent seats with same subject`
Map to risk levels: 0→SAFE(🟩), 1→MEDIUM(🟨), 2+→HIGH(🟥)
Add `calculateRiskScore()`: `(conflictCount / maxNeighbors) * 100`
Implement `calculateTotalHallRisk(ExamHall)` for before/after metrics


## Task 4: Build Multiple Halls Management & Distribution Logic

Implement multi-hall allocation in `HallManagementService`:
Create `distributeStudentsAcrossHalls(List<Student>, List<ExamHall>)` method
Use Priority Queue to assign students to halls based on capacity
Ensure balanced distribution across halls
Add `HallRepository` to manage multiple hall configurations
Handle overflow scenarios (students > total capacity)


## Task 5: Create REST API Endpoints for Seat Allocation & Risk Analysis

Build REST controllers in `SeatingController`:
POST `/api/students/upload` - CSV upload endpoint
POST `/api/students/manual` - manual student entry
POST `/api/halls/create` - create hall configuration
POST `/api/seating/random` - generate random seating
POST `/api/seating/optimize` - run greedy allocation
GET `/api/seating/risk/{hallId}` - get risk analysis
Add DTOs: `StudentDTO`, `HallDTO`, `SeatingResponseDTO`


## Task 6: Implement CSV Upload & Parsing Service

Create `CSVService` for file handling:
Use Apache POI/OpenCSV to parse uploaded CSV files
Expected format: `RollNo, Name, Subject`
Validate CSV structure and data integrity
Convert CSV rows to `Student` objects
Add error handling for malformed files
Return validation errors to API layer


## Task 7: Build PDF Export Service for Seating Charts

Implement `PDFExportService` using iText library:
Create `generateSeatingChartPDF(ExamHall)` method
Render 2D grid layout with color-coded seats (green/yellow/red)
Include hall metadata: hall ID, total students, risk score
Add legend: risk level colors and meanings
Return PDF as byte stream for download
Add endpoint: GET `/api/seating/export/{hallId}`


## Task 8: Develop Frontend UI with 3-Panel Layout & Grid Visualization

Create HTML/CSS/JS frontend in `src/main/resources/static`:
Left Panel: Student input form, CSV upload button, hall size inputs, "Generate Random" & "Optimize Seating" buttons
Center Panel: Dynamic seat grid using CSS Grid, color-coded seats (🟩🟨🟥), click seat → show student details modal
Right Panel: Risk legend, total risk score, violations count, before/after comparison stats
Use Fetch API to call REST endpoints
Add Tailwind CSS for clean styling


## Task 9: Implement Real-Time Seat Grid Updates & Before/After Comparison

Add dynamic visualization features in frontend JavaScript:
Implement `renderSeatGrid(hallData)` to dynamically generate seat divs
Add real-time color updates when switching random ↔ optimized
Create `compareSeating()` function: show risk reduction percentage
Add smooth transitions/animations for seat color changes
Display tooltip on seat hover: student name, subject, risk score
Add "Reset" button to clear current allocation


## Task 10: Add Unit Tests for Core DSA Algorithms & Services

Create JUnit tests for critical components:
Test `SeatAllocationService`: verify no same-subject adjacency after allocation
Test `RiskDetectionService`: validate risk calculation logic with edge cases
Test `HallManagementService`: check multi-hall distribution correctness
Test `CSVService`: validate parsing with malformed CSV files
Add test data fixtures for reproducible testing