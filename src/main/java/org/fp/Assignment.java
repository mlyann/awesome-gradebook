package org.fp;

public class Assignment {
    final String assignmentID;
    final String studentID;
    final String teacherID;
    final String assignmentName;
    Score score;

    public Assignment(String assignmentID, String studentID, String teacherID, String assignmentName) {
        this.assignmentID = assignmentID;
        this.studentID = studentID;
        this.teacherID = teacherID;
        this.assignmentName = assignmentName;
    }

    public String getAssignmentID() {
        return assignmentID;
    }
    public String getStudentID() {
        return studentID;
    }
    public String getTeacherID() {
        return teacherID;
    }




}
