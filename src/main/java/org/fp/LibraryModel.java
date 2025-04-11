package org.fp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LibraryModel {
    private final HashMap<String, Student> stuID_Map;
    private final HashMap<String, Course> courseID_Map;
    private final HashMap<String, Assignment> assignmentID_Map;

    // This map record the courses enrolled by each student, student is a Key:
    // { "StudentID" : [CourseID1, CourseID2, ...] , ... }
    private final HashMap<String, ArrayList<Course>> stuID_couID_Map;
    // This map record the students enrolled in each course, course is a Key:
    // { "CourseID" : [StudentID1, StudentID2, ...] , ... }
    private final HashMap<String, ArrayList<String>> couID_stuID_Map;
    // This map record the assignments in each course, course is a Key:
    // { "CourseID" : [AssignmentID1, AssignmentID2, ...] , ... }
    private final HashMap<String, ArrayList<String>> stuID_assID_Map;
    // This map record the student who submitted each assignment, assignment is a Key:
    // { "StudentID" : [AssignmentID1, AssignmentID2, ...] , ... }
    private final HashMap<String, ArrayList<String>> assID_Map_stuID_Map;
    // This map record the assignments submitted by each student, student is a Key:
    // { "AssignmentID1" : [StudentID1, StudentID2, ...] , ... }

    public LibraryModel() {
        this.stuID_Map = new HashMap<>();
        this.stuID_couID_Map = new HashMap<>();
        this.couID_stuID_Map = new HashMap<>();
        this.stuID_assID_Map = new HashMap<>();
        this.assID_Map_stuID_Map = new HashMap<>();
        this.courseID_Map = new HashMap<>();
        this.assignmentID_Map = new HashMap<>();

    }

    // Design for function

    public ArrayList<ArrayList<String>> getCoursesString(String stuID) {
        ArrayList<Course> courses = stuID_couID_Map.get(stuID);
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for (Course course : courses) {
            ArrayList<String> courseInfo = new ArrayList<>();
            courseInfo.add(course.getCourseName());
            courseInfo.add(course.getCourseDescription());
            result.add(courseInfo);
        }
        return result;
    }

    public String getStuName(String stuID) {
        StringBuilder result = new StringBuilder();
        Student student = stuID_Map.get(stuID);
        if (student != null) {
            result.append(student.getFirstName()).append(" ").append(student.getLastName());
        } else {
            result.append("Student not found");
        }
        return result.toString();
    }

    public void state() {
        // Create students
        Student s1 = new Student("20250001", "Ming", "Yang", "mingyang@example.com");
        Student s2 = new Student("20250002", "Li", "Wei", "liwei@example.com");
        Student s3 = new Student("20250003", "Zhang", "Hua", "zhanghua@example.com");

        stuID_Map.put(s1.getStuID(), s1);
        stuID_Map.put(s2.getStuID(), s2);
        stuID_Map.put(s3.getStuID(), s3);

        // Create courses
        Course c1 = new Course("CSE101", "Intro to Programming", "Basic programming in Java.");
        Course c2 = new Course("CSE102", "Data Structures", "Learn about lists, stacks, queues.");
        Course c3 = new Course("CSE103", "Database Systems", "Learn relational DB and SQL.");

        courseID_Map.put(c1.getCourseID(), c1);
        courseID_Map.put(c2.getCourseID(), c2);
        courseID_Map.put(c3.getCourseID(), c3);

        // Assign courses to students
        stuID_couID_Map.put(s1.getStuID(), new ArrayList<>(List.of(c1, c2)));
        stuID_couID_Map.put(s2.getStuID(), new ArrayList<>(List.of(c2, c3)));
        stuID_couID_Map.put(s3.getStuID(), new ArrayList<>(List.of(c1, c3)));

        // Assign students to courses
        couID_stuID_Map.put(c1.getCourseID(), new ArrayList<>(List.of(s1.getStuID(), s3.getStuID())));
        couID_stuID_Map.put(c2.getCourseID(), new ArrayList<>(List.of(s1.getStuID(), s2.getStuID())));
        couID_stuID_Map.put(c3.getCourseID(), new ArrayList<>(List.of(s2.getStuID(), s3.getStuID())));


    }


}
