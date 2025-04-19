package org.fp;

import java.time.LocalDate;
import java.util.*;

public class LibraryModel {
    private final Map<String, Student> studentMap = new HashMap<>();
    private final Map<String, Course> courseMap = new HashMap<>();
    private final Map<String, Assignment> assignmentMap = new HashMap<>();
    private final Map<String, Score> gradeMap = new HashMap<>();

    private final Map<String, List<String>> studentCourses = new HashMap<>();
    private final Map<String, List<String>> courseStudents = new HashMap<>();
    private final Map<String, List<String>> courseAssignments = new HashMap<>();
    private final Map<String, List<String>> studentAssignments = new HashMap<>();
    private final Map<String, String> assignmentGrades = new HashMap<>();

    public void addStudent(Student s) {
        studentMap.put(s.getStuID(), s);
    }

    public Student getStudent(String id) {
        return studentMap.get(id);
    }

    public boolean studentExists(String id) {
        return studentMap.containsKey(id);
    }

    public Collection<Student> getAllStudents() {
        return Collections.unmodifiableCollection(studentMap.values());
    }

    public void addCourse(Course c) {
        courseMap.put(c.getCourseID(), c);
    }

    public Course getCourse(String id) {
        return courseMap.get(id);
    }

    public Collection<Course> getAllCourses() {
        return Collections.unmodifiableCollection(courseMap.values());
    }

    public void addAssignment(Assignment a) {
        assignmentMap.put(a.getAssignmentID(), a);
    }

    public Assignment getAssignment(String id) {
        return assignmentMap.get(id);
    }

    public Collection<Assignment> getAllAssignments() {
        return Collections.unmodifiableCollection(assignmentMap.values());
    }

    public void addScore(Score score) {
        gradeMap.put(score.getGradeID(), score);
    }

    public Score getScore(String gradeID) {
        return gradeMap.get(gradeID);
    }

    public Map<String, Score> getAllScores() {
        return Collections.unmodifiableMap(gradeMap);
    }

    public List<Assignment> getAssignmentsForStudentInCourse(String studentID, String courseID) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment a : assignmentMap.values()) {
            if (a.getStudentID().equals(studentID) && a.getCourseID().equals(courseID)) {
                result.add(a);
            }
        }
        return result;
    }

    public Score getScoreForAssignment(String assignmentID) {
        Assignment a = assignmentMap.get(assignmentID);
        return a == null ? null : gradeMap.get(a.getGradeID());
    }

    // Seed demo data for UI testing
    public void state() {
        Student s = new Student("20250001", "Ming", "Yang", "ming@example.com");
        Course c1 = new Course("Math", "Basic Algebra", "T001");
        Course c2 = new Course("Physics", "Newtonian Mechanics", "T001");
        Course c3 = new Course("Biology", "Molecular Biology", "T001");
        Course c4 = new Course("History", "World History", "T001");
        Course c5 = new Course("CS", "Data Structures", "T001");

        addStudent(s);
        addCourse(c1);
        addCourse(c2);
        addCourse(c3);
        addCourse(c4);
        addCourse(c5);

        s.enrollInCourse(c1.getCourseID());
        s.enrollInCourse(c2.getCourseID());
        s.enrollInCourse(c3.getCourseID());
        s.enrollInCourse(c4.getCourseID());
        s.enrollInCourse(c5.getCourseID());

        int assignmentCount = 20;
        LocalDate assignDate = LocalDate.of(2025, 4, 1);
        for (int i = 1; i <= assignmentCount; i++) {
            String aid = String.format("A%03d", i);
            String gid = String.format("G%03d", i);
            String title = "Assignment " + i;
            LocalDate due = LocalDate.of(2025, 4, 10 + i);
            Assignment a = new Assignment(aid, title, s.getStuID(), c1.getCourseID(), gid, assignDate, due);
            Score g = new Score(gid, aid, s.getStuID(), 60 + (i % 40), 100);

            addAssignment(a);
            addScore(g);
            s.addAssignment(aid);
        }
    }

}