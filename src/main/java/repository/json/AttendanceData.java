package repository.json;

import models.Attendance;
import models.AttendanceRecord;
import models.Session;

import java.time.LocalDateTime;
import java.util.UUID;

public class AttendanceData {
    private LocalDateTime recordedAt;
    private LocalDateTime updatedAt;
    private UUID sessionId;
    private AttendanceRecord status;

    public AttendanceData() { }

    public AttendanceData(Attendance attendance) {
        this.recordedAt = attendance.getRecordedAt();
        this.updatedAt = attendance.getUpdatedAt();
        this.sessionId = attendance.getSession().getUUID();
        this.status = attendance.getStatus();
    }

    public Attendance toDomain(Session session) {
        return new Attendance(recordedAt, updatedAt, session, status);
    }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public AttendanceRecord getStatus() { return status; }
    public void setStatus(AttendanceRecord status) { this.status = status; }
}
