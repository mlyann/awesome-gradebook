package org.fp;

//Student s = new Student("20251234", "Ming", "Yang", "mingyang@example.com");
//System.out.println(s);  // 输出：Ming Yang (20251234): mingyang@example.com

public class Student {
    private final String studentId;
    private final String firstName;
    private final String lastName;
    private final String email;

    public Student(String studentId, String firstName, String lastName, String email) {
        if (studentId == null || studentId.isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public String getStudentId() {
        return studentId;
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

    @Override
    public String toString() {
        return String.format("%s (%s): %s", getFullName(), studentId, email);
    }
}