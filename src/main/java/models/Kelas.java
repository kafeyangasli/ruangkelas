package models;

import java.util.*;

public class Kelas {
    private final UUID uuid;
    private final String kelasId;
    private final Course courseInfo;
    private final Semester cohortSemester;
    private final Set<Attendee> attendees;
    private final Set<Assessment> assessments;
    private final Set<Session> sessions;
    private final Set<Grade> grades;
    
    public Kelas(String kelasId, Course courseInfo, Semester cohortSemester) {
        this.uuid = UUID.randomUUID();
        this.kelasId = kelasId;
        this.courseInfo = courseInfo;
        this.cohortSemester = cohortSemester;
        attendees = new HashSet<>();
        sessions = new HashSet<>();
        assessments = new HashSet<>();
        grades = new HashSet<>();
    }

    public Kelas(UUID uuid, String kelasId, Course courseInfo, Semester cohortSemester) {
        this.uuid = uuid;
        this.kelasId = kelasId;
        this.courseInfo = courseInfo;
        this.cohortSemester = cohortSemester;
        attendees = new HashSet<>();
        sessions = new HashSet<>();
        assessments = new HashSet<>();
        grades = new HashSet<>();
    }

    public UUID getUUID() {
        return this.uuid;
    }
    
    public String getKelasId() {
        return this.kelasId;
    }

    public Set<Attendee> getAttendees() {
        return Collections.unmodifiableSet(attendees);
    }
    
    public Set<Assessment> getAssessments() {
        return Collections.unmodifiableSet(assessments);
    }
    
    public Set<Session> getSessions() {
        return Collections.unmodifiableSet(sessions);
    }

    public Set<Grade> getGrades() {
        return Collections.unmodifiableSet(grades);
    }

    public Semester getCohortSemester() {
        return this.cohortSemester;
    }

    public Course getCourseInfo() {
        return this.courseInfo;
    }
    
    public void addAssessment(String description, int percentage) {
        Assessment assessment = new Assessment(percentage, description);
        this.assessments.add(assessment);
    }

    public void addLoadedAssessment(Assessment assessment) {
        this.assessments.add(assessment);
    }

    public void removeAssessment(Assessment assessment) {
        grades.removeIf(grade -> grade.ofAssessment(assessment));
        this.assessments.remove(assessment);
    }
    
    public void addGrade(Grade grade) {
        this.grades.add(grade);
    }
    
    public void removeGrade(Grade grade) {
        this.grades.remove(grade);
    }

    public void createSession(String topic) {
        Session session = new Session(topic);
        this.sessions.add(session);
    }

    public void removeSession(Session session) {
        for (Attendee attendee : attendees) {
            attendee.removeAttendance(session);
        }
        this.sessions.remove(session);
    }

    public void addLoadedSession(Session session) {
        this.sessions.add(session);
    }

    public boolean studentEnrolled(Student student) {
        return attendees.stream().anyMatch(a -> a.getStudent().equals(student));
    }
    
    public boolean ofCourse(Course course) {
        return courseInfo.equals(course);
    }

    public Grade getGradeByAssessment(Assessment assessment, Attendee attendee) {
        return grades.stream()
                .filter(grade -> grade.ofAssessment(assessment) && grade.ofAttendee(attendee))
                .findFirst()
                .orElse(null);
    }

    public void resetGradeByAssessment(Assessment assessment, Attendee attendee) {
        Grade grade = getGradeByAssessment(assessment, attendee);
        if (grade != null) {
            grades.remove(grade);
        }
    }

    public Set<Grade> getGrades(Assessment assessment) {
        Set<Grade> toReturn = new HashSet<>();
        for (Grade grade : this.grades) {
            if (grade.ofAssessment(assessment)) toReturn.add(grade);
        }

        return toReturn;
    }

    public Set<Grade> getAttendeeGrades(Attendee attendee) {
        Set<Grade> toReturn = new HashSet<>();
        for (Grade grade : this.grades) {
            if (grade.ofAttendee(attendee)) toReturn.add(grade);
        }

        return toReturn;
    }

    public Map<String, Object> getGradeStatistics(Assessment assessment) {
        Map<String, Object> toReturn = new HashMap<>();

        Optional<Grade> max = grades.stream()
                .filter(grade -> grade.ofAssessment(assessment))
                .max(Comparator.comparing(Grade::getGrade));

        Optional<Grade> min = grades.stream()
                .filter(grade -> grade.ofAssessment(assessment))
                .min(Comparator.comparing(Grade::getGrade));

        double mean = grades.stream()
                .filter(grade -> grade.ofAssessment(assessment))
                .mapToDouble(Grade::getGrade)
                .average()
                .orElse(0.0);

        toReturn.put("max", max.orElse(null));
        toReturn.put("min", min.orElse(null));
        toReturn.put("mean", mean);

        return toReturn;
    }

    public double getAttendeeFinalGrade(Attendee attendee) {
        Set<Grade> attendeeGrades = getAttendeeGrades(attendee);
        double toReturn = 0.0;
        for (Grade grade : attendeeGrades) {
            toReturn += grade.calculateGrade();
        }

        return toReturn;
    }

    public void addAttendee(Attendee attendee) {
        this.attendees.add(attendee);
    }
    
    public void removeAttendee(Attendee attendee) {
        // Delete all grades related to the attendee
        grades.removeIf(grade -> grade.ofAttendee(attendee));
        
        // and bye bye!
        this.attendees.remove(attendee);
    }
    
    public boolean similar(Kelas other) {
        return this.kelasId.equals(other.kelasId)
                && this.cohortSemester.equals(other.cohortSemester)
                && this.courseInfo.equals(other.courseInfo);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Kelas other)) return false;
        
        return this.uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
    
    @Override
    public String toString() {
        return "%s %s - %s".formatted(this.courseInfo.getCourseName(), this.kelasId, this.cohortSemester.toString());
    }
}
