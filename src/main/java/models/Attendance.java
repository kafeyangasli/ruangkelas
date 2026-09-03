package models;

import java.time.LocalDateTime;

public class Attendance {
    private final LocalDateTime recordedAt;
    private LocalDateTime updatedAt;
    private final Session session;
    private AttendanceRecord status;

    public Attendance(Session session, AttendanceRecord status) {
        this.recordedAt = LocalDateTime.now();
        this.updatedAt = recordedAt;
        this.session = session;
        this.status = status;
    }

    public Attendance(LocalDateTime recordedAt, LocalDateTime updatedAt, Session session, AttendanceRecord status) {
        this.recordedAt = recordedAt;
        this.updatedAt = updatedAt;
        this.session = session;
        this.status = status;
    }

    public LocalDateTime getRecordedAt() {
        return this.recordedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Session getSession() {
        return this.session;
    }

    public AttendanceRecord getStatus() {
        return this.status;
    }

    public boolean ofSession(Session check) {
        return this.session.equals(check);
    }
    
    public void setStatus(AttendanceRecord status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
