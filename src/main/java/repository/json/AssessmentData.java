package repository.json;

import models.Assessment;

import java.util.UUID;

public class AssessmentData {
    private UUID uuid;
    private String name;
    private int percentage;

    public AssessmentData() { }

    public AssessmentData(Assessment assessment) {
        this.uuid = assessment.getUUID();
        this.name = assessment.getName();
        this.percentage = assessment.getPercentage();
    }

    public Assessment toDomain() { return new Assessment(uuid, percentage, name); }
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }
}
