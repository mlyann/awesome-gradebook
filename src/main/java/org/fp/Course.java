package org.fp;

public class Course {
    private final String courseID;
    private final String courseName;
    private String courseDescription;

    public Course(String courseID, String courseName, String courseDescription) {
        if (courseID == null || courseID.isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be null or empty.");
        }
        this.courseID = courseID;
        this.courseName = courseName;
        this.courseDescription = courseDescription;
    }

    public String getCourseID() {
        return courseID;
    }
    public String getCourseName() {
        return courseName;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }
}
