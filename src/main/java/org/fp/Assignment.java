package org.fp;

public class Assignment {
    final String assignmentID;
    final String assignmentName;
    Score score;

    public Assignment(String assignmentName) {
        this.assignmentID = IDGen.getUniqueID();
        this.assignmentName = assignmentName;
        // Default to ungraded

        if (assignmentID == null){
            throw new IllegalArgumentException("Assignment ID cannot be null or empty.");
        }
    }

    public String getAssignmentID() {
        return assignmentID;
    }

    // Implement this
    public void setScore(){

    }

    // Implement this
    public Score returnScore(){
        return null;
    }
}
