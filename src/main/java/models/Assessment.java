package models;

import java.util.UUID;

public class Assessment {
    private final UUID uuid;
    private String name;
    private int percentage;

    public Assessment(int percentage, String name) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.percentage = percentage;
    }

    public Assessment(UUID uuid, int percentage, String name) {
        this.uuid = uuid;
        this.name = name;
        this.percentage = percentage;
    }

    public UUID getUUID() {
        return this.uuid;
    }

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
