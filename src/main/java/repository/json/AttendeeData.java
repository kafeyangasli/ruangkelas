package repository.json;

import models.Attendee;
import models.Student;
import models.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AttendeeData {
    private UUID uuid;
    private String studentNim;
    private List<AttendanceData> attendances = new ArrayList<>();

    public AttendeeData() { }

    public AttendeeData(Attendee attendee) {
        this.uuid = attendee.getUUID();
        this.studentNim = attendee.getStudent().getNIM();
        this.attendances = attendee.getAttendances().stream().map(AttendanceData::new).toList();
    }

    public Attendee toDomain(Student student, java.util.Map<UUID, Session> sessions) {
        Attendee attendee = new Attendee(uuid, student);
        for (AttendanceData data : attendances) {
            Session session = sessions.get(data.getSessionId());
            if (session == null) throw new IllegalStateException("Missing session " + data.getSessionId());
            attendee.addLoadedAttendance(data.toDomain(session));
        }
        return attendee;
    }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    public String getStudentNim() { return studentNim; }
    public void setStudentNim(String studentNim) { this.studentNim = studentNim; }
    public List<AttendanceData> getAttendances() { return attendances; }
    public void setAttendances(List<AttendanceData> attendances) { this.attendances = attendances; }
}
