package org.fp;

import java.time.LocalDate;

public class Assignment {
    private final String assignmentID;
    private final String assignmentName;
    private final String studentID;   // 属于哪个学生
    private final String courseID;    // 属于哪个课程
    private final String gradeID;     // 在 LibraryModel 的 gradeMap 中查找
    private final LocalDate assignDate;
    private final LocalDate dueDate;

    public Assignment(String assignmentID, String assignmentName,
                      String studentID, String courseID, String gradeID,
                      LocalDate assignDate, LocalDate dueDate) {
        if (assignmentID == null || studentID == null || courseID == null || gradeID == null || dueDate == null || assignDate == null) {
            throw new IllegalArgumentException("Assignment requires all fields.");
        }
        this.assignmentID = assignmentID;
        this.assignmentName = assignmentName;
        this.studentID = studentID;
        this.courseID = courseID;
        this.gradeID = gradeID;
        this.assignDate = assignDate;
        this.dueDate = dueDate;
    }

    public String getAssignmentID() { return assignmentID; }
    public String getAssignmentName() { return assignmentName; }
    public String getStudentID() { return studentID; }
    public String getCourseID() { return courseID; }
    public String getGradeID() { return gradeID; }
    public LocalDate getAssignDate() { return assignDate; }
    public LocalDate getDueDate() { return dueDate; }

    public String getDueDateString() {
        return dueDate.toString();
    }

    @Override
    public String toString() {
        return String.format("%s (%s) [Course: %s, Student: %s, Assigned: %s, Due: %s]",
                assignmentName, assignmentID, courseID, studentID, assignDate, dueDate);
    }
}