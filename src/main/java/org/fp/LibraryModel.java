package org.fp;

import java.lang.reflect.Array;
import java.util.*;

public class LibraryModel {
    private final HashMap<String, Student> stuID_Map;
    private final HashMap<String, Course> courseID_Map;
    private final HashMap<String, Assignment> assignmentID_Map;

    //couID                 stuID                         assID   grade
    private final HashMap<String, HashMap<String, HashMap<String, Score>>> bigHashMap;

    public LibraryModel() {
        this.stuID_Map = new HashMap<>();
        this.courseID_Map = new HashMap<>();
        this.assignmentID_Map = new HashMap<>();
        this.bigHashMap = new HashMap<>();
    }

    // ----------- Student methods -----------



    // ----------- Teacher methods -----------


    public void state() {
        // Create students
        Student s1 = new Student("Ming", "Yang", "mingyang@example.com");
        Student s2 = new Student("Li", "Wei", "liwei@example.com");
        Student s3 = new Student("Zhang", "Hua", "zhanghua@example.com");

        stuID_Map.put(s1.getStuID(), s1);
        stuID_Map.put(s2.getStuID(), s2);
        stuID_Map.put(s3.getStuID(), s3);

        // Create courses
        Course c1 = new Course("Intro to Programming", "Basic programming in Java.");
        Course c2 = new Course("Data Structures", "Learn about lists, stacks, queues.");
        Course c3 = new Course("Database Systems", "Learn relational DB and SQL.");

        courseID_Map.put(c1.getCourseID(), c1);
        courseID_Map.put(c2.getCourseID(), c2);
        courseID_Map.put(c3.getCourseID(), c3);

        // Creating assignments
        Assignment a1 = new Assignment("sim4", "Building a single cycle CPU based on MIPs architecture");
        Assignment a2 = new Assignment("asm7", "Write quicksort and mergesort in MIPs");
        Assignment a3 = new Assignment("asm6", "Working with binary trees in MIPs, including traversals, insertions and deletions");

        // Adding assignments
        assignmentID_Map.put(a1.getAssignmentID(), a1);
        assignmentID_Map.put(a2.getAssignmentID(), a2);
        assignmentID_Map.put(a3.getAssignmentID(), a3);

    }
}
