package org.fp;

//Student s = new Student("20251234", "Ming", "Yang", "mingyang@example.com");
//System.out.println(s);  // output: Ming Yang (20251234): mingyang@example.com

public class Student {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String stuID;

    public Student(String firstName, String lastName, String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.stuID = IDGen.getUniqueID();

        if(stuID == null){
            throw new IllegalArgumentException("Student ID cannot be null or empty.");
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getStuID(){
        return stuID;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %s", getFullName(), getStuID(), email);
    }
}