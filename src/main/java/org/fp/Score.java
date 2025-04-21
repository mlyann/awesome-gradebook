package org.fp;

public class Score {
    private final String gradeID;
    private final String assignmentID;
    private final String studentID;
    private int earned;
    private int total;

    public Score(String gradeID, String assignmentID, String studentID, int earned, int total) {
        this.gradeID = gradeID;
        this.assignmentID = assignmentID;
        this.studentID = studentID;
        this.earned = earned;
        this.total = total;
    }

    // Copy constructor
    public Score(Score other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null Score.");
        }
        this.gradeID = other.gradeID;
        this.assignmentID = other.assignmentID;
        this.studentID = other.studentID;
        this.earned = other.earned;
        this.total = other.total;
    }

    public String getGradeID() { return gradeID; }
    public String getAssignmentID() { return assignmentID; }
    public String getStudentID() { return studentID; }
    public int getEarned() { return earned; }
    public int getTotal() { return total; }

    public void setScore(int earned, int total) {
        this.earned = earned;
        this.total = total;
    }

    public double getPercentage() {
        return total == 0 ? 0 : ((double) earned / total) * 100.0;
    }

    public Grade getLetterGrade() {
        return Grade.fromScore(getPercentage());
    }

    @Override
    public String toString() {
        return String.format("%d/%d (%.1f%%) [%s]", earned, total, getPercentage(), getLetterGrade());
    }
}

