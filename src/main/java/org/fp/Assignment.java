package org.fp;

public class Assignment {
    final String assignmentID;
    final String assignmentName;
    private Score score;
    public Assignment(String assignmentName) {
        this.assignmentID = IDGen.getUniqueID();
        this.assignmentName = assignmentName;
        this.score=Score.of(Score.UNGRADED,1);

        if (assignmentID == null){
            throw new IllegalArgumentException("Assignment ID cannot be null or empty.");
        }
    }

    public Assignment(String assignmentName,int earned,int total) {
        this.assignmentID = IDGen.getUniqueID();
        this.assignmentName = assignmentName;
        this.score=Score.of(earned,total);

        if (assignmentID == null){
            throw new IllegalArgumentException("Assignment ID cannot be null or empty.");
        }
    }


    public String getAssignmentID() {
        return assignmentID;
    }

    // Implement this
    public void setScore(int earned,int total){
        if(!score.isGraded()){
            score=Score.of(earned,total);
        }

    }

    // Implement this
    public Score returnScore(){
        return score;
    }
}
