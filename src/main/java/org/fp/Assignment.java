package org.fp;

import java.time.LocalDate;

public class Assignment {
    public enum SubmissionStatus {
        UNSUBMITTED,
        SUBMITTED_UNGRADED,
        GRADED
    }

    private final String assignmentID;
    private final String assignmentName;
    private final String studentID;
    private final String courseID;
    private String gradeID = null;
    private final LocalDate assignDate;
    private final LocalDate dueDate;
    private SubmissionStatus status = SubmissionStatus.UNSUBMITTED;
    private boolean published = false;

    public Assignment(String assignmentName,
                      String studentID, String courseID,
                      LocalDate assignDate, LocalDate dueDate) {
        if (studentID == null || courseID == null || dueDate == null || assignDate == null) {
            throw new IllegalArgumentException("Assignment requires all fields.");
        }
        this.assignmentID = IDGen.generate("ASG");
        this.assignmentName = assignmentName;
        this.studentID = studentID;
        this.courseID = courseID;
        this.assignDate = assignDate;
        this.dueDate = dueDate;
    }

    // Copy constructor
    public Assignment(Assignment other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null Assignment.");
        }
        this.assignmentID = other.assignmentID;
        this.assignmentName = other.assignmentName;
        this.studentID = other.studentID;
        this.courseID = other.courseID;
        this.assignDate = other.assignDate;
        this.dueDate = other.dueDate;
        this.gradeID = other.gradeID;
        this.status = other.status;
        this.published = other.published;
        this.category  = other.category;
    }

    public void submit() {
        if (this.status != SubmissionStatus.UNSUBMITTED) return;
        this.status = SubmissionStatus.SUBMITTED_UNGRADED;
    }

    public void markGraded(String gradeID) {
        if (this.status != SubmissionStatus.SUBMITTED_UNGRADED) {
            throw new IllegalStateException("Assignment must be submitted before grading.");
        }
        if (gradeID == null || gradeID.isEmpty()) {
            throw new IllegalArgumentException("Grade ID cannot be null or empty when grading.");
        }
        this.gradeID = gradeID;
        this.status = SubmissionStatus.GRADED;
    }

    public void publish() {
        if (status != SubmissionStatus.GRADED) {
            throw new IllegalStateException("Assignment must be graded before publishing.");
        }
        this.published = true;
    }

    public boolean isPublished() {
        return published;
    }
    public String getAssignmentID() { return assignmentID; }
    public String getAssignmentName() { return assignmentName; }
    public String getStudentID() { return studentID; }
    public String getCourseID() { return courseID; }
    public String getGradeID() { return gradeID; }
    public LocalDate getAssignDate() { return assignDate; }
    public LocalDate getDueDate() { return dueDate; }
    public SubmissionStatus getStatus() { return status; }
    public String getDueDateString() {
        return dueDate.toString();
    }

    @Override
    public String toString() {
        return String.format("%s (%s) [Course: %s, Student: %s, Assigned: %s, Due: %s, Status: %s, Published: %s]",
                assignmentName, assignmentID, courseID, studentID, assignDate, dueDate, status.name(), published);
    }

    private String category = "Default";

    public void setCategory(String category) {
        this.category = category;
    }
    public String getCategory() {
        return category;
    }
}