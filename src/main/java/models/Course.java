package models;

import java.util.Objects;

public class Course {
    private final String courseId;
    private final String courseName;

    public Course(String courseId, String courseName) {
        this.courseId = courseId;
        this.courseName = courseName;
    }

    public String getCourseId() {
        return this.courseId;
    }

    public String getCourseName() {
        return this.courseName;
    }

    @Override
    public String toString() {
        return "[%s] %s".formatted(courseId, courseName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Course other)) return false;

        return this.courseId.equals(other.courseId);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.courseId);
        return hash;
    }
}
