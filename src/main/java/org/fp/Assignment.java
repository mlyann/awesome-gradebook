package org.fp;

public class Assignment {
    private final String assignmentID;
    private final String assignmentName;
    private int earnedScore;
    private int totalScore;

    public Assignment(String assignmentName) {
        this.assignmentID = IDGen.generate("ASG");
        this.assignmentName = assignmentName;
        this.earnedScore = -1; // ungraded
        this.totalScore = 1;

        if (assignmentID == null) {
            throw new IllegalArgumentException("Assignment ID cannot be null.");
        }
    }

    // Copy constructor
    public Assignment(Assignment asg) {
        this.assignmentID = asg.assignmentID;
        this.assignmentName = asg.assignmentName;
        this.earnedScore = asg.earnedScore;
        this.totalScore = asg.totalScore;
    }

    public String getAssignmentID() {
        return assignmentID;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public void setScore(int earned, int total) {
        this.earnedScore = earned;
        this.totalScore = total;
    }

    public int getEarnedScore() {
        return earnedScore;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public boolean isGraded() {
        return earnedScore >= 0 && totalScore > 0;
    }
}