package org.fp;

public class Assignment {
    private final String assignmentID;
    private final String assignmentName;
    private final String assignmentDescription;
    private Score score;

    public Assignment(String assignmentName, String assignmentDescription) {
        this.assignmentID = IDGen.getUniqueID();
        this.assignmentName = assignmentName;
        this.assignmentDescription = assignmentDescription;
        this.score=Score.of(Score.UNGRADED,1);

        if (assignmentID == null){
            throw new IllegalArgumentException("Assignment ID cannot be null or empty.");
        }
    }

    // Copy constructor for deep copy
    public Assignment(Assignment asg){
        this.assignmentID = asg.assignmentID;
        this.assignmentName = asg.assignmentName;
        this.assignmentDescription = asg.assignmentDescription;
        this.score = asg.score;
    }

    public String getAssignmentID() {
        return assignmentID;
    }

    public String getAssignmentName(){
        return assignmentName;
    }

    public String getAssignmentDescription(){
        return assignmentDescription;
    }

    public void setScore(int earned,int total){
        score=Score.of(earned,total);
    }

    public Score returnScore(){
        return score;
    }
}
