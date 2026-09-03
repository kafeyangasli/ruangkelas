package repository.json;

import models.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KelasData {
    private UUID uuid;
    private String kelasId;
    private String courseId;
    private int semesterYear;
    private Semester.Jenis_Semester semesterJenis;
    private List<AttendeeData> attendees = new ArrayList<>();
    private List<AssessmentData> assessments = new ArrayList<>();
    private List<SessionData> sessions = new ArrayList<>();
    private List<GradeData> grades = new ArrayList<>();

    public KelasData() { }

    public KelasData(Kelas kelas) {
        this.uuid = kelas.getUUID();
        this.kelasId = kelas.getKelasId();
        this.courseId = kelas.getCourseInfo().getCourseId();
        this.semesterYear = kelas.getCohortSemester().getYear();
        this.semesterJenis = kelas.getCohortSemester().getJenis_Semester();
        this.attendees = kelas.getAttendees().stream().map(AttendeeData::new).toList();
        this.assessments = kelas.getAssessments().stream().map(AssessmentData::new).toList();
        this.sessions = kelas.getSessions().stream().map(SessionData::new).toList();
        this.grades = kelas.getGrades().stream().map(GradeData::new).toList();
    }

    public Kelas toDomain(java.util.Map<String, Course> courses,
                          java.util.Map<String, Student> students) {
        Course course = courses.get(courseId);
        if (course == null) throw new IllegalStateException("Missing course " + courseId);

        Kelas kelas = new Kelas(
                uuid,
                kelasId,
                course,
                new Semester(semesterYear, semesterJenis)
        );

        java.util.Map<UUID, Session> sessionMap = new java.util.HashMap<>();
        for (SessionData data : sessions) {
            Session session = data.toDomain();
            kelas.addLoadedSession(session);
            sessionMap.put(session.getUUID(), session);
        }

        java.util.Map<UUID, Attendee> attendeeMap = new java.util.HashMap<>();
        for (AttendeeData data : attendees) {
            Student student = students.get(data.getStudentNim());
            if (student == null) throw new IllegalStateException("Missing student " + data.getStudentNim());
            Attendee attendee = data.toDomain(student, sessionMap);
            kelas.addAttendee(attendee);
            attendeeMap.put(attendee.getUUID(), attendee);
        }

        java.util.Map<UUID, Assessment> assessmentMap = new java.util.HashMap<>();
        for (AssessmentData data : assessments) {
            Assessment assessment = data.toDomain();
            kelas.addLoadedAssessment(assessment);
            assessmentMap.put(assessment.getUUID(), assessment);
        }

        for (GradeData data : grades) {
            kelas.addGrade(data.toDomain(attendeeMap, assessmentMap));
        }

        return kelas;
    }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getKelasId() { return kelasId; }
    public void setKelasId(String kelasId) { this.kelasId = kelasId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public int getSemesterYear() { return semesterYear; }
    public void setSemesterYear(int semesterYear) { this.semesterYear = semesterYear; }
    public Semester.Jenis_Semester getSemesterJenis() { return semesterJenis; }
    public void setSemesterJenis(Semester.Jenis_Semester semesterJenis) { this.semesterJenis = semesterJenis; }
    public List<AttendeeData> getAttendees() { return attendees; }
    public void setAttendees(List<AttendeeData> attendees) { this.attendees = attendees; }
    public List<AssessmentData> getAssessments() { return assessments; }
    public void setAssessments(List<AssessmentData> assessments) { this.assessments = assessments; }
    public List<SessionData> getSessions() { return sessions; }
    public void setSessions(List<SessionData> sessions) { this.sessions = sessions; }
    public List<GradeData> getGrades() { return grades; }
    public void setGrades(List<GradeData> grades) { this.grades = grades; }
}
