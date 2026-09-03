package models;

public enum AttendanceRecord {
    PRESENT("Present"),
    SICK("Sick Leave"),
    LEAVE("Formal Leave");

    private final String value;

    // Constructor
    AttendanceRecord(String value) {
        this.value = value;
    }

    @Override
    public String toString() { return value; }
}
