package repository.json;

import models.Course;

public class CourseData {
    private String courseId;
    private String courseName;

    public CourseData() { }

    public CourseData(Course course) {
        this.courseId = course.getCourseId();
        this.courseName = course.getCourseName();
    }

    public Course toDomain() { return new Course(courseId, courseName); }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
}
