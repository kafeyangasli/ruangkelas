package models;

import java.time.LocalDateTime;

public class Grade {
    private final LocalDateTime recordedAt;
    private LocalDateTime updatedAt;
    private final Assessment assessment;
    private final Attendee attendee;
    private double number;

    private final static Grade emptyGrade = new Grade(null, null, 0);
    public static Grade getEmptyGrade() { return emptyGrade; }

    public Grade(Assessment assessment, Attendee attendee, double number) {
        this.recordedAt = LocalDateTime.now();
        this.updatedAt = recordedAt;
        this.assessment = assessment;
        this.attendee = attendee;
        this.number = number;
    }

    public Grade(LocalDateTime recordedAt, LocalDateTime updatedAt, Assessment assessment, Attendee attendee, double number) {
        this.recordedAt = recordedAt;
        this.updatedAt = updatedAt;
        this.assessment = assessment;
        this.attendee = attendee;
        this.number = number;
    }

    public LocalDateTime getRecordedAt() {
        return this.recordedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Assessment getAssessment() {
        return this.assessment;
    }

    public Attendee getAttendee() {
        return this.attendee;
    }

    public double getGrade() {
        return this.number;
    }

    public void setGrade(double number) {
        this.number = number;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean ofAttendee(Attendee attendee) {
        return this.attendee.equals(attendee);
    }

    public boolean ofAssessment(Assessment check) {
        return this.assessment.equals(check);
    }

    public double calculateGrade() {
        return this.number * ((double) assessment.getPercentage() / 100);
    }
}
