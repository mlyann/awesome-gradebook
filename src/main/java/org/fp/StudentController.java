package org.fp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StudentController extends BaseController {
    private String currentStudentID;

    public StudentController(LibraryModel model) {
        super(model); // call the constructor
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


    public void loadStudentCourses() {
        Student student = model.getStudent(currentStudentID);
        if (student == null) return;
        cachedCourses = student.getEnrolledCourseIDs().stream()
                .map(model::getCourse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    public List<List<String>> getFormattedCourseListForDisplayRows() {
        List<List<String>> output = new ArrayList<>();
        for (Course c : cachedCourses) {
            String status = c.isCompleted() ? "✅ Completed" : "🟢 In Progress";
            output.add(List.of(c.getCourseName(), c.getCourseDescription(), status));
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
        cachedAssignments.sort(
                Comparator
                        // first compare the prefix (e.g. "HW", "Project", "Quiz")
                        .comparing((Assignment a) -> {
                            String[] parts = a.getAssignmentName().split("\\s+", 2);
                            return parts[0].toLowerCase();
                        })
                        // then compare the numeric suffix
                        .thenComparing(a -> {
                            String[] parts = a.getAssignmentName().split("\\s+", 2);
                            if (parts.length > 1) {
                                try {
                                    return Integer.parseInt(parts[1]);
                                } catch (NumberFormatException e) {
                                    // if it isn’t numeric, push it to the front
                                    return Integer.MIN_VALUE;
                                }
                            } else {
                                return Integer.MIN_VALUE;
                            }
                        })
        );
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
            if (!is1Graded) return 1;
            if (!is2Graded) return -1;

            return g1.getLetterGrade().compareTo(g2.getLetterGrade());
        });
    }

    public Course getCourseByAssignment(String assignmentID) {
        Assignment a = model.getAssignment(assignmentID);
        if (a == null) return null;
        return model.getCourse(a.getCourseID());
    }

    public void submitAssignment(String assignmentID) {
        model.submitAssignment(assignmentID);
        loadAssignmentsForCourse(model.getAssignment(assignmentID).getCourseID());
    }

}
