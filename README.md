![ruangkelas](ruangkelas.png)

---

# ruangkelas

**ruangkelas** is a desktop application for managing classroom data in one place. It provides tools for managing courses, classes, students, attendance, sessions, assessments, and grades.

This application was originally built to assist myself as a Practicum Assistant, where I needed an elegant way to track attendee attendance and grades across multiple lab sessions without relying on scattered spreadsheets. It's built as a native desktop app rather than a web app, so it works fully offline and stores data locally in JSON format to provide easy and quick access should a modification is required that is not accessible through interactables in the application.

Feel free to adjust this application to your own needs, in accordance with the MIT License under which it's shared.

## User Story

This project grew out of a real workflow at the Informatics Department, Universitas Diponegoro:

> As a Practicum Assistant, I want to manage my attendees' attendance and grades in one place, so that I don't have to cross-reference multiple spreadsheets every session.

Some of the smaller stories that shaped individual features along the way:

- As a Practicum Assistant, I want to create a class (*Kelas*) and enroll students into it, so that I have a single roster to work from all semester.
- As a Practicum Assistant, I want to record attendance per session, so that each session keeps its own accurate record.
- As a Practicum Assistant, I want to define weighted assessments and enter grades against them, so that final grades are calculated consistently.
- As a Practicum Assistant, I want to export class data and reports, so that I can share results without re-entering data elsewhere.

## Contributors

- [kafeyangasli (Maulana Ghazzam Adil Al Faiq)](https://kafeyangasli.com)

## Getting Started

### Prerequisites

- JDK 26
- Maven 3.9+

### Run

```bash
mvn javafx:run
```

### Build

```bash
mvn clean package
```

## Usage Guide

### 1. Manage Students
Use **File → Students → Manage Student Data** to maintain your student database. Students can also be imported from CSV, XLSX, or JSON using **File → Students → Import**.

### 2. Create Course
Use **File → Courses → Manage Courses** to maintain your course database. Courses can be a part of multiple Kelas in the same cohort year with different IDs, or different cohort year with same IDs.

### 3. Create a Kelas
Open **File → Kelas → Create a new Kelas**. Select the course of which the Kelas is associated with and create it. Select the newly created Kelas from the left-hand list to manage it.

### 4. Add Students to a Kelas
Select a Kelas, then add students to its attendee list. A student can only be enrolled once in the same Kelas.

### 5. View an Attendee
Select a student from the attendee list and choose **View Attendee** to inspect their attendance and grades.

### 6. Manage Assessments
Use **Manage Assessments** to add or edit assessments. Each assessment has a description and a percentage weight. The weights represent how much each assessment contributes to the final grade. 
> **NOTE**: Assessment weights may overflow. Future updates will address this issue.

### 7. Record Grades
Select an assessment and an attendee, then enter or update the student's grade. The final grade is calculated from the assessment weights.

### 8. Manage Sessions
Use **Manage Sessions** to create class sessions. A session can have a date, time, and notes. Sessions are also used when recording attendance.

### 9. Record Attendance
Start or select a session and set each attendee's attendance status. Attendance is associated with the specific session, so each session keeps its own record.

### 10. Export Data
Use **File → Kelas → Export Kelas Data → To JSON** to export the Kelas data. Use **Export Kelas Report** when you need a report-oriented spreadsheet.

### 11. Data Persistence
ruangkelas automatically saves application data when changes are made and saves all data when the application closes. Keep your data directory backed up if the information is important.

> **Tip:** If a button is disabled, select the relevant Kelas, attendee, assessment, or session first. Many actions intentionally require a selection before they become available.

## Future Updates

- Stable persistence, including rollovers in case of outages
- SQL Connection using JDBC

## A.I Usage Statement

The use of Artificial Intelligence in this project is limited to assisting with persistence and UI error-handling code, as well as debugging. Other pieces of code, such as entity models and its services (excluding persistence-related) are entirely handcoded by myself. Regardless, all code in this repository was reviewed, tested, and is fully my own responsibility.