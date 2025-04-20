package org.fp;

import java.util.*;
import java.util.stream.Collectors;

import static org.fp.Assignment.SubmissionStatus.GRADED;
import static org.fp.Assignment.SubmissionStatus.UNSUBMITTED;

public class TeacherController extends BaseController {
    private String currentTeacherID;
    private Map<String, List<Assignment>> groupedAssignments = new HashMap<>();
    private List<String> sortedAssignmentNames = new ArrayList<>();
    private boolean cacheValid = false;
    private String currentAssignmentGroupName = null;

    public TeacherController(LibraryModel model) {
        super(model);
    }

    public void setCurrentTeacher(String id) {
        if (model.teacherExists(id)) {
            currentTeacherID = id;
        }
    }

    public Teacher getCurrentTeacher() {
        return model.getTeacher(currentTeacherID);
    }

    public Student getStudent(String stuID) {
        return model.getStudent(stuID);
    }



    public void loadTeacherCourses() {
        Teacher teacher = model.getTeacher(currentTeacherID);
        if (teacher == null) return;
        cachedCourses = teacher.getTeachingCourseIDs().stream()
                .map(model::getCourse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public void loadAssignmentsForCourse(String courseID) {
        cachedAssignments = model.getAssignmentsInCourse(courseID);
    }

    public List<Student> getStudentsInCourse(String courseID) {
        List<String> studentIDs = model.getStudentIDsInCourse(courseID);
        return studentIDs.stream()
                .map(model::getStudent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public int countSubmitted(String assignmentID) {
        return (int) model.getAllStudents().stream()
                .filter(s -> {
                    Assignment a = model.getAssignment(assignmentID);
                    return a != null && a.getStudentID().equals(s.getStuID())
                            && a.getStatus() != UNSUBMITTED;
                })
                .count();
    }

    public int countGraded(String assignmentID) {
        return (int) model.getAllStudents().stream()
                .filter(s -> {
                    Assignment a = model.getAssignment(assignmentID);
                    return a != null && a.getStudentID().equals(s.getStuID())
                            && a.getStatus() == GRADED;
                })
                .count();
    }

    public List<String> getSortedAssignmentNames() {
        return cacheValid ? sortedAssignmentNames : List.of();
    }

    public Map<String, List<Assignment>> getGroupedAssignmentCache() {
        return groupedAssignments;
    }

    public List<Assignment> getAssignmentGroup(String name) {
        return groupedAssignments.getOrDefault(name, List.of());
    }


    public int getGradedPercentage(String assignmentID) {
        int submitted = countSubmitted(assignmentID);
        int graded = countGraded(assignmentID);
        if (submitted == 0) return 0;
        return (int) ((graded / (double) submitted) * 100);
    }

    public Map<String, List<Assignment>> getGroupedAssignments(String courseID) {
        return model.getAssignmentsInCourse(courseID).stream()
                .collect(Collectors.groupingBy(Assignment::getAssignmentName));
    }

    enum AssignmentSort {
        NONE, NAME, ASSIGN_DATE, DUE_DATE, SUBMISSION, GRADED_PERCENT
    }

    public void sortGroupedAssignments(AssignmentSort sort, boolean filter) {
        if (groupedAssignments.isEmpty()) return;

        List<String> names = new ArrayList<>(groupedAssignments.keySet());

        if (filter) {
            names.removeIf(name -> {
                List<Assignment> list = groupedAssignments.get(name);
                boolean allGraded = list.stream().allMatch(a -> a.getStatus() == GRADED);
                boolean allPublished = list.stream().allMatch(Assignment::isPublished);
                return allGraded && allPublished;
            });
        }

        switch (sort) {
            case NAME -> names.sort(Comparator.comparing(TeacherController::extractAssignmentNumber));
            case ASSIGN_DATE -> names.sort(Comparator.comparing(n -> groupedAssignments.get(n).get(0).getAssignDate()));
            case DUE_DATE -> names.sort(Comparator.comparing(n -> groupedAssignments.get(n).get(0).getDueDate()));
            case SUBMISSION -> names.sort(Comparator.comparingInt((String n) ->
                    (int) groupedAssignments.get(n).stream().filter(a -> a.getStatus() != UNSUBMITTED).count()).reversed());
            case GRADED_PERCENT -> names.sort(Comparator.comparingDouble((String n) ->
                    getGradedPercentageForGroup(groupedAssignments.get(n))).reversed());

        }

        sortedAssignmentNames = names;
        cacheValid = true;
    }

    public static int extractAssignmentNumber(String name) {
        try {
            String[] parts = name.trim().split(" ");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (Exception e) {
            return Integer.MAX_VALUE; // fallback for non-matching patterns
        }
    }



    public double getGradedPercentageForGroup(List<Assignment> assignments) {
        long submitted = assignments.stream().filter(a -> a.getStatus() != UNSUBMITTED).count();
        long graded = assignments.stream().filter(a -> a.getStatus() == GRADED).count();
        return submitted == 0 ? 0.0 : (graded * 100.0) / submitted;
    }


    public void refreshGroupedAssignments(String courseID) {
        groupedAssignments = model.getAssignmentsInCourse(courseID).stream()
                .filter(a -> a.getStatus() != Assignment.SubmissionStatus.UNSUBMITTED)  // ✅ 只要已提交的
                .collect(Collectors.groupingBy(Assignment::getAssignmentName));
        cacheValid = false;
    }


    public void setSelectedAssignmentGroup(String name) {
        if (groupedAssignments.containsKey(name)) {
            currentAssignmentGroupName = name;
        } else {
            currentAssignmentGroupName = null;
        }
    }

    public String getSelectedAssignmentGroup() {
        return currentAssignmentGroupName;
    }
}
