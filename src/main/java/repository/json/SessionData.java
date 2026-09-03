package repository.json;

import models.Session;

import java.time.LocalDateTime;
import java.util.UUID;

public class SessionData {
    private UUID uuid;
    private LocalDateTime createdAt;
    private LocalDateTime start;
    private LocalDateTime end;
    private String topic;
    private String notes;

    public SessionData() { }

    public SessionData(Session session) {
        this.uuid = session.getUUID();
        this.createdAt = session.getCreatedAt();
        this.start = session.getStartTime();
        this.end = session.getEndTime();
        this.topic = session.getTopic();
        this.notes = session.getNotes();
    }

    public Session toDomain() {
        return new Session(uuid, createdAt, start, end, topic, notes);
    }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }
    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
