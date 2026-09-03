package models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "uuid"
)
public class Attendee {
    private final UUID uuid;
    private final Student student;
    private final Set<Attendance> attendances;
    
    public Attendee(UUID uuid, Student student) {
        this.uuid = uuid;
        this.student = student;
        this.attendances = new HashSet<>();
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public Student getStudent() {
        return this.student;
    }

    public Set<Attendance> getAttendances() {
        return this.attendances;
    }
    
    public Attendance getAttendanceBySession(Session session) {
        for (Attendance attendance : attendances) {
            if (attendance.ofSession(session)) return attendance;
        }
        
        return null;
    }
    
    public Attendance addAttendance(Session session, AttendanceRecord status) {
        Attendance attendance = new Attendance(session, status);
        attendances.add(attendance);
        
        return attendance;
    }

    public void addLoadedAttendance(Attendance attendance) {
        attendances.add(attendance);
    }

    public void removeAttendance(Session session) {
        Optional<Attendance> attendance = attendances.stream()
                .filter(atd -> atd.ofSession(session))
                .findFirst();

        attendance.ifPresent(attendances::remove);
    }
    
    @Override
    public String toString() {
        return this.student.toString();
    }
}
