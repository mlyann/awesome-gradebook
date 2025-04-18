package org.fp;

import java.util.HashMap;
import java.util.Map;

public class Course {
    private final String courseID;
    private final String courseName;
    private String courseDescription;
    private final Map<String, Assignment> assignments;

    public Course(String courseName, String courseDescription) {
        this.courseID = IDGen.generate("CRS");
        this.courseName = courseName;
        this.courseDescription = courseDescription;
        this.assignments = new HashMap<>();

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

    public void addAssignment(Assignment assignment) {
        assignments.put(assignment.getAssignmentID(), assignment);
    }

    public Assignment getAssignmentByID(String assignmentID) {
        return assignments.get(assignmentID);
    }

    public void setAssignmentScore(String assignmentID, int earned, int total) {
        Assignment assignment = assignments.get(assignmentID);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment ID not found: " + assignmentID);
        }
        assignment.setScore(earned, total);
    }

    public Map<String, Assignment> getAllAssignments() {
        return assignments;
    }
}