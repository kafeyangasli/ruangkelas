package repository.json;

import models.Grade;
import models.Attendee;
import models.Assessment;

import java.time.LocalDateTime;
import java.util.UUID;

public class GradeData {
    private LocalDateTime recordedAt;
    private LocalDateTime updatedAt;
    private UUID attendeeId;
    private UUID assessmentId;
    private double grade;

    public GradeData() { }

    public GradeData(Grade grade) {
        this.recordedAt = grade.getRecordedAt();
        this.updatedAt = grade.getUpdatedAt();
        this.attendeeId = grade.getAttendee().getUUID();
        this.assessmentId = grade.getAssessment().getUUID();
        this.grade = grade.getGrade();
    }

    public Grade toDomain(java.util.Map<UUID, Attendee> attendees,
                          java.util.Map<UUID, Assessment> assessments) {
        Attendee attendee = attendees.get(attendeeId);
        Assessment assessment = assessments.get(assessmentId);
        if (attendee == null) throw new IllegalStateException("Missing attendee " + attendeeId);
        if (assessment == null) throw new IllegalStateException("Missing assessment " + assessmentId);
        return new Grade(recordedAt, updatedAt, assessment, attendee, grade);
    }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UUID getAttendeeId() { return attendeeId; }
    public void setAttendeeId(UUID attendeeId) { this.attendeeId = attendeeId; }
    public UUID getAssessmentId() { return assessmentId; }
    public void setAssessmentId(UUID assessmentId) { this.assessmentId = assessmentId; }
    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }
}
