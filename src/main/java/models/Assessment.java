package models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Assessment {
    private final UUID uuid;
    private final LocalDateTime createdAt;
    private String name;
    private int percentage;

    public Assessment(int percentage, String name) {
        this.uuid = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.name = name;
        this.percentage = percentage;
    }

    public Assessment(UUID uuid, LocalDateTime createdAt, int percentage, String name) {
        this.uuid = uuid;
        this.createdAt = createdAt;
        this.name = name;
        this.percentage = percentage;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public LocalDateTime getCreatedAt() { return this.createdAt; }

    public String getName() {
        return this.name;
    }

    public int getPercentage() {
        return this.percentage;
    }

    public void setName(String newName) { this.name = newName; }

    public void setPercentage(int newPercentage) { this.percentage = newPercentage; }

    @Override
    public String toString() {
        return "%s ( worth: %d%% )".formatted(name, percentage);
    }
}
