package org.fp;

import java.util.*;
import java.util.stream.Collectors;

public class StudentController extends BaseController {
    private String currentStudentID;

    public StudentController(LibraryModel model) {
        super(model); // ✅ 使用 super() 初始化 model
    }

    public void setCurrentStudent(String id) {
        if (model.studentExists(id)) {
            currentStudentID = id;
            System.out.println("✅ Current student set: " + model.getStudent(id).getFullName());
        } else {
            System.out.println("❌ Tried to set non-existent student: " + id);
        }
    }

    public Student getCurrentStudent() {
        return model.getStudent(currentStudentID);
    }



    public void createStudent(String first, String last, String email) {
        Student s = new Student(first, last, email);
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

    public List<Assignment> getFilteredCachedAssignments(boolean onlyUnsubmitted) {
        return cachedAssignments.stream()
                .filter(a -> !onlyUnsubmitted || a.getStatus() == Assignment.SubmissionStatus.UNSUBMITTED)
                .toList();
    }

    public void loadAssignmentsForCourse(String courseID) {
        Student s = getCurrentStudent();
        if (s == null) return;
        cachedAssignments = new ArrayList<>(model.getAssignmentsForStudentInCourse(s.getStuID(), courseID));
    }

    public List<List<String>> getFormattedAssignmentTableRowsWithState(boolean onlyUnsubmitted) {
        return cachedAssignments.stream()
                .filter(a -> !onlyUnsubmitted || a.getStatus() == Assignment.SubmissionStatus.UNSUBMITTED)
                .map(a -> {
                    Score score = model.getScoreForAssignment(a.getAssignmentID());
                    String grade = (score != null) ? score.getLetterGrade().name() : "N/A";
                    String pct = (score != null) ? String.format("%d/%d", score.getEarned(), score.getTotal()) : "—";
                    String state = switch (a.getStatus()) {
                        case UNSUBMITTED -> "⛔";
                        case SUBMITTED_UNGRADED -> "✉️";
                        case GRADED -> "✅";
                    };
                    return List.of(
                            a.getAssignmentName(),
                            state,
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
//TODO: Change it to public it's fine? @Haocheng
    public int extractAssignmentNumber(String name) {
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

            boolean is1Graded = (g1 != null);
            boolean is2Graded = (g2 != null);

            if (!is1Graded && !is2Graded) return 0; // both ungraded
            if (!is1Graded) return 1;  // a1 ungraded → push back
            if (!is2Graded) return -1; // a2 ungraded → push back

            return g1.getLetterGrade().compareTo(g2.getLetterGrade());
        });
    }
    public Course getCourseByAssignment(String assignmentID) {
        Assignment a = model.getAssignment(assignmentID);
        if (a == null) return null;
        return model.getCourse(a.getCourseID());
    }
}
