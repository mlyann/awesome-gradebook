package org.fp;

/**
public class Teacher {
	private final String firstName;
    private final String lastName;
    private  final String teacherID;

    public Teacher(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.teacherID = IDGen.getUniqueID();

        if (teacherID == null || teacherID.isEmpty()){
            throw new IllegalArgumentException("Teacher ID cannot be null or empty.");
        }
    }

    public String getFirstName(){
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getTeacherID(){
        return teacherID;
    }

    @Override
    public String toString(){
        return String.format("Teacher: %s (%s)", getFullName(), getTeacherID());
    }
}
 **/