package models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Grade {
    private final LocalDateTime recordedAt;
    private LocalDateTime updatedAt;
    private final Assessment assessment;
    private final Attendee attendee;
    private double number;

    private final static Set<Grade> emptyGrades = new HashSet<>();
    public static Grade getEmptyGrade(Assessment assessment) {
        Optional<Grade> emptyGrade = emptyGrades.stream().filter(grade -> grade.ofAssessment(assessment)).findFirst();
        if (emptyGrade.isEmpty()) {
            emptyGrade = Optional.of(new Grade(assessment, null, 0.0));
        }

        emptyGrades.add(emptyGrade.get());
        return emptyGrade.get();
    }

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
