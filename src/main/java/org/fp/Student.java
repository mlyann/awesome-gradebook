package org.fp;

import java.util.*;

public class Student {
    private final String stuID;
    private final String firstName;
    private final String lastName;
    private final String email;

    private Set<String> enrolledCourseIDs = new HashSet<>();
    private Set<String> assignmentIDs = new HashSet<>();

    public Student(String stuID, String firstName, String lastName, String email) {
        if (stuID == null || stuID.isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be null or empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address.");
        }

        this.stuID = stuID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    // Copy constructor
    public Student(Student other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null Student.");
        }
        this.stuID = other.stuID;
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.email = other.email;
        this.enrolledCourseIDs = new HashSet<>(other.enrolledCourseIDs);
        this.assignmentIDs = new HashSet<>(other.assignmentIDs);
    }

    public String getStuID() {
        return stuID;
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

    public Set<String> getEnrolledCourseIDs() {
        return Collections.unmodifiableSet(enrolledCourseIDs);
    }

    public Set<String> getAssignmentIDs() {
        return Collections.unmodifiableSet(assignmentIDs);
    }

    public void enrollInCourse(String courseID) {
        if (courseID != null && !courseID.isEmpty()) {
            enrolledCourseIDs.add(courseID);
        }
    }

    public void addAssignment(String assignmentID) {
        if (assignmentID != null && !assignmentID.isEmpty()) {
            assignmentIDs.add(assignmentID);
        }
    }

    public void removeAssignment(String assignmentID) {
        if (assignmentID != null && !assignmentID.isEmpty()) {
            assignmentIDs.remove(assignmentID);
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %s", getFullName(), stuID, email);
    }

    public static Comparator<Student> firstNameAscendingComparator() {
        return Comparator.comparing(s -> s.firstName);
    }

    public static Comparator<Student> firstNameDescendingComparator() {
        return firstNameAscendingComparator().reversed();
    }

    public static Comparator<Student> lastNameAscendingComparator() {
        return Comparator.comparing(s -> s.lastName);
    }

    public static Comparator<Student> lastNameDescendingComparator() {
        return lastNameAscendingComparator().reversed();
    }

    public static Comparator<Student> userNameAscendingComparator() {
        return Comparator.comparing(s -> s.email);
    }

    public static Comparator<Student> userNameDescendingComparator() {
        return userNameAscendingComparator().reversed();
    }

}
