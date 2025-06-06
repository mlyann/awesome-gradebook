package org.fp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Teacher {
    private final String teacherID;
    private final String firstName;
    private final String lastName;

    private Set<String> teachingCourseIDs = new HashSet<>();


    // Constructor
    public Teacher(String firstName, String lastName) {
        this.teacherID = IDGen.generate("TCH");
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

    // Add a course to the teacher's list of courses
    public void addCourse(String courseID) {
        if (courseID != null && !courseID.isEmpty()) {
            teachingCourseIDs.add(courseID);
        }
    }

    // Remove a course from the teacher's list of courses
    public Set<String> getTeachingCourseIDs() {
        return Collections.unmodifiableSet(teachingCourseIDs);
    }

    @Override
    public String toString() {
        return String.format("Teacher: %s (%s)", getFullName(), getTeacherID());
    }
}