package org.fp;

import java.util.*;
import java.util.stream.Collectors;

public class Controller {
    private final LibraryModel model;
    private String currentStudentID;
    private String currentTeacherID;
    private List<Course> cachedCourses = new ArrayList<>();
    private List<Assignment> cachedAssignments = new ArrayList<>();
    private Assignment currentAssignment = null;

    public enum CourseSort { NONE, NAME, STATUS }

    public Controller(LibraryModel model) {
        this.model = model;
    }

    public void setCurrentStudent(String id) {
        if (model.studentExists(id)) {
            currentStudentID = id;
        }
    }

    public Student getCurrentStudent() {
        return model.getStudent(currentStudentID);
    }

    public void setCurrentTeacher(String id) {
        currentTeacherID = id;
    }

    public void createStudent(String first, String last, String email) {
        String id = IDGen.generate("STU");
        Student s = new Student(id, first, last, email);
        model.addStudent(s);
    }

    public void createCourse(String name, String desc, String teacherID) {
        Course c = new Course(name, desc, teacherID); // 由 Course 自己生成 ID
        model.addCourse(c);
    }

    public void loadStudentCourses() {
        Student student = model.getStudent(currentStudentID);
        if (student == null) return;
        cachedCourses = student.getEnrolledCourseIDs().stream()
                .map(model::getCourse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<String> getFormattedCourseListForDisplay() {
        List<String> output = new ArrayList<>();
        for (int i = 0; i < cachedCourses.size(); i++) {
            Course c = cachedCourses.get(i);
            output.add((i + 1) + ". " + c.getCourseName() + " - " + c.getCourseDescription());
        }
        return output;
    }

    public List<List<String>> getFormattedCourseListForDisplayRows() {
        List<List<String>> rows = new ArrayList<>();
        for (Course c : cachedCourses) {
            rows.add(List.of(c.getCourseName(), c.getCourseDescription()));
        }
        return rows;
    }



    public void sortCachedCourses(CourseSort sort) {
        switch (sort) {
            case NAME -> cachedCourses.sort(Comparator.comparing(Course::getCourseName));
            case STATUS -> cachedCourses.sort(Comparator.comparing(Course::getCourseDescription));
            case NONE -> {}
        }
    }

    public Course getCachedCourse(int index) {
        return cachedCourses.get(index);
    }

    public Assignment getCachedAssignment(int index) {
        if (index >= 0 && index < cachedAssignments.size()) {
            return cachedAssignments.get(index);
        }
        return null;
    }

    public void loadAssignmentsForCourse(String courseID) {
        Student s = getCurrentStudent();
        if (s == null) return;
        cachedAssignments = new ArrayList<>(model.getAssignmentsForStudentInCourse(s.getStuID(), courseID));
    }


    public List<List<String>> getFormattedAssignmentTableRows() {
        return cachedAssignments.stream().map(a -> {
            Score score = model.getScoreForAssignment(a.getAssignmentID());
            String grade = (score != null) ? score.getLetterGrade().name() : "N/A";
            String pct = (score != null) ? String.format("%d/%d", score.getEarned(), score.getTotal()) : "—";
            return List.of(
                    a.getAssignmentName(),
                    a.getAssignDate().toString(),
                    a.getDueDate().toString(),
                    pct,
                    grade
            );
        }).toList();
    }

    public void sortCachedAssignmentsByName() {
        cachedAssignments.sort(Comparator.comparingInt(a -> extractAssignmentNumber(a.getAssignmentName())));
    }

    private int extractAssignmentNumber(String name) {
        // 假设格式为 "Assignment N"，提取 N 的整数
        String[] parts = name.trim().split(" ");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE; // fallback
        }
    }


    public void sortCachedAssignmentsByAssignDate() {
        cachedAssignments.sort(Comparator.comparing(Assignment::getAssignDate));
    }

    public void sortCachedAssignmentsByDueDateAscending() {
        cachedAssignments.sort(Comparator.comparing(Assignment::getDueDate));
    }

    public void sortCachedAssignmentsByGradeAscending() {
        cachedAssignments.sort((a1, a2) -> {
            Score g1 = model.getScoreForAssignment(a1.getAssignmentID());
            Score g2 = model.getScoreForAssignment(a2.getAssignmentID());
            if (g1 == null || g2 == null) return 0;
            return g1.getLetterGrade().compareTo(g2.getLetterGrade());
        });
    }

    public Score getScoreForAssignment(String assignmentID) {
        return model.getScoreForAssignment(assignmentID);
    }

    public void setCurrentAssignment(Assignment assignment) {
        this.currentAssignment = assignment;
    }

    public Assignment getCurrentAssignment() {
        return this.currentAssignment;
    }




    public List<Course> getCoursesForCurrentStudent() {
        Student student = model.getStudent(currentStudentID);
        return student.getEnrolledCourseIDs().stream()
                .map(model::getCourse)
                .collect(Collectors.toList());
    }

    public List<Course> getCoursesForCurrentTeacher() {
        return model.getAllCourses().stream()
                .filter(c -> c.getTeacherID().equals(currentTeacherID))
                .collect(Collectors.toList());
    }
}
