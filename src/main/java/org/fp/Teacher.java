package org.fp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Teacher {
    private final String teacherID;
    private final String firstName;
    private final String lastName;

    private Set<String> teachingCourseIDs = new HashSet<>();


    public Teacher(String teacherID, String firstName, String lastName) {
        if (teacherID == null || teacherID.isEmpty()) {
            throw new IllegalArgumentException("Teacher ID cannot be null or empty.");
        }
        this.teacherID = teacherID;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Copy constructor
    public Teacher(Teacher other) {
        if (other == null) {
            throw new IllegalArgumentException("Cannot copy a null Teacher.");
        }
        this.teacherID = other.teacherID;
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.teachingCourseIDs = new HashSet<>(other.teachingCourseIDs);
    }

    public String getTeacherID() {
        return teacherID;
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

    public void addCourse(String courseID) {
        if (courseID != null && !courseID.isEmpty()) {
            teachingCourseIDs.add(courseID);
        }
    }

    public Set<String> getTeachingCourseIDs() {
        return Collections.unmodifiableSet(teachingCourseIDs);
    }


    @Override
    public String toString() {
        return String.format("Teacher: %s (%s)", getFullName(), getTeacherID());
    }
}