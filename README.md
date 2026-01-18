# Smart Examination Anti-Cheating & Seating Optimization System

## Overview
Algorithm visualization + decision system for intelligent exam seat allocation using DSA (Graph Coloring, Greedy Algorithms).

## Technology Stack
- **Backend**: Java 17, Spring Boot 3.2.x
- **Frontend**: HTML, CSS, JavaScript, Tailwind CSS
- **Build Tool**: Maven
- **Libraries**: Apache POI (CSV), iText (PDF), Lombok

## Project Structure
- `model/` - Domain models (Student, Seat, ExamHall)
- `service/` - Core DSA algorithms
- `controller/` - REST API endpoints
- `static/` - Frontend UI

## Running the Application
```bash
mvn clean install
mvn spring-boot:run
```
Access at: http://localhost:8080

## Exhibition Pitch

### Problem Statement
Traditional exam seating arrangements are random, creating significant cheating opportunities when students from the same subject sit together. Manual seat allocation is time-consuming, error-prone, and doesn't consider anti-cheating strategies.

### Core Algorithm Approach
- **Graph Coloring Algorithm**: Models students as nodes and same-subject relationships as edges, ensuring optimal separation
- **Greedy Optimization**: Minimizes cheating risk through distance-based penalty calculation and conflict detection

### Key Outcomes
- **Risk Reduction**: Quantified risk scoring with color-coded visualization (referenced in riskReport.totalRiskScore)
- **Fairness**: Equal distribution across all subjects using algorithmic optimization
- **Automation**: Processes 1000+ students in seconds vs hours of manual work

### 60-90 Second Demo Script
1. **Upload CSV** (10s): "Load 100 students instantly using our bulk upload feature"
2. **Show Random Allocation** (15s): "Notice the red clusters - high cheating risk areas"
3. **Apply Graph Coloring** (20s): "Watch our algorithm optimize in real-time"
4. **Results Analysis** (20s): "Risk score drops dramatically, conflicts minimized"
5. **Export PDF** (15s): "Professional seating chart ready for exam day"

*Key metrics align with implemented features: risk scoring, multi-hall management, CSV processing, and PDF export capabilities.*

## Key Features
- Intelligent seat allocation using Graph Coloring
- Anti-cheating risk detection (color-coded visualization)
- Before vs After optimization comparison
- Multi-hall management
- CSV upload & PDF export

## API Documentation

### CSV Upload & Management

#### Upload Students CSV
```bash
POST /api/seating/upload-csv
Content-Type: multipart/form-data

curl -X POST -F "file=@students.csv" http://localhost:8080/api/seating/upload-csv
```

**CSV Format Requirements:**
- Header: `RollNo,Name,Subject`
- Roll numbers must be alphanumeric
- No duplicate roll numbers allowed
- Maximum 1000 rows
- File size limit: 10MB

**Response (200 OK):**
```json
[
  {
    "rollNo": "S001",
    "name": "Alice Johnson",
    "subject": "Mathematics"
  }
]
```

**Error Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Duplicate student found",
  "errorDetails": "Duplicate roll number found: S001",
  "lineNumber": 3
}
```

#### Download Sample CSV
```bash
GET /api/seating/sample-csv

curl -O http://localhost:8080/api/seating/sample-csv
```

### Seat Allocation

#### Allocate Seats
```bash
POST /api/seating/allocate
Content-Type: application/json

curl -X POST -H "Content-Type: application/json" -d '{
  "students": [
    {"rollNo": "S001", "name": "Alice", "subject": "Math"},
    {"rollNo": "S002", "name": "Bob", "subject": "Physics"}
  ],
  "hallId": "HALL_001",
  "rows": 5,
  "cols": 5,
  "useOptimization": true
}' http://localhost:8080/api/seating/allocate
```

**Response:**
```json
{
  "hallId": "HALL_001",
  "seats": [[...]], 
  "riskReport": {
    "totalRiskScore": 15.5,
    "totalConflicts": 2,
    "occupiedSeats": 10,
    "totalSeats": 25
  },
  "success": true,
  "message": "Seats allocated successfully"
}
```

### PDF Export

#### Export Seating Chart
```bash
GET /api/export/pdf/{hallId}

curl -O http://localhost:8080/api/export/pdf/HALL_001
```

**Response:** PDF file download with seating chart

### Hall Management

#### Get All Halls
```bash
GET /api/halls

curl http://localhost:8080/api/halls
```

#### Create Hall
```bash
POST /api/halls?hallId=HALL_002&rows=6&cols=8

curl -X POST "http://localhost:8080/api/halls?hallId=HALL_002&rows=6&cols=8"
```

## Error Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 400 | Bad Request (validation errors) |
| 404 | Hall not found |
| 413 | File too large (>10MB) |
| 415 | Unsupported file type |
| 500 | Internal server error |

## CSV Validation Rules

| Rule | Validation | Error Type |
|------|------------|------------|
| File extension | Must be .csv | 415 Unsupported Media Type |
| File size | Max 10MB | 413 Payload Too Large |
| Header format | RollNo,Name,Subject | InvalidCSVStructureException |
| Column count | Exactly 3 columns | CSVRowValidationException |
| Roll number | Alphanumeric, not empty | CSVRowValidationException |
| Name | Not empty | CSVRowValidationException |
| Subject | Not empty | CSVRowValidationException |
| Duplicate roll numbers | Must be unique | DuplicateStudentException |
| Max rows | 1000 rows | CSVParsingException |
| Empty file | Must have content | EmptyCSVException |