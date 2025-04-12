package org.fp;

public class Course {
    private final String courseID;
    private final String courseName;
    private String courseDescription;

    public Course(String courseName, String courseDescription) {
        this.courseID = IDGen.getUniqueID();
        this.courseName = courseName;
        this.courseDescription = courseDescription;

        if (courseID == null || courseID.isEmpty()) {
            throw new IllegalArgumentException("Course ID cannot be null or empty.");
        }
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
