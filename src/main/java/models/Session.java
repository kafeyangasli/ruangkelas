package models;

import exceptions.InvalidStateException;

import java.time.LocalDateTime;
import java.util.UUID;

public class Session {
    public UUID uuid;
    private final LocalDateTime createdAt;
    private LocalDateTime start = null;
    private LocalDateTime end = null;
    private String topic;
    private String notes;

    public Session(String topic) {
        this.uuid = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.topic = topic;
    }

    public Session(UUID uuid, LocalDateTime createdAt, LocalDateTime start, LocalDateTime end, String topic, String notes) {
        this.uuid = uuid;
        this.createdAt = createdAt;
        this.start = start;
        this.end = end;
        this.topic = topic;
        this.notes = notes;
    }

    public UUID getUUID() {
        return uuid;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getTopic() {
        return topic;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getStartTime() { return start; }

    public LocalDateTime getEndTime() { return end; }

    public void start() throws InvalidStateException {
         if (this.start != null) {
            // already started
            throw new InvalidStateException("Session has already started.");
        }

        this.start = LocalDateTime.now();
    }

    public void end() throws InvalidStateException {
        if (this.start == null) {
            // can't be ended when hasn't started
            throw new InvalidStateException("Session has not started yet.");
        } else if (this.end != null) {
            // already ended
            throw new InvalidStateException("Session has already ended.");
        }

        this.end = LocalDateTime.now();
    }

    public boolean hasStarted() {
        return start != null;
    }

    public boolean hasEnded() {
        return end != null;
    }

    public void setNotes(String notes) { this.notes = notes; }

    public void setTopic(String topic) { this.topic = topic; }

    @Override
    public String toString() {
        return "Session %s - Topic: %s".formatted(uuid.toString().substring(0, 5), topic);
    }
}
