package repository.json;

import models.Assessment;

import java.time.LocalDateTime;
import java.util.UUID;

public class AssessmentData {
    private UUID uuid;
    private LocalDateTime createdAt;
    private String name;
    private int percentage;

    public AssessmentData() { }

    public AssessmentData(Assessment assessment) {
        this.uuid = assessment.getUUID();
        this.createdAt = assessment.getCreatedAt();
        this.name = assessment.getName();
        this.percentage = assessment.getPercentage();
    }

    public Assessment toDomain() { return new Assessment(uuid, createdAt, percentage, name); }
    public UUID getUuid() { return uuid; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }
}
