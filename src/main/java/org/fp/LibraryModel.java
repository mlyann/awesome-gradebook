package org.fp;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.*;

/**
 * This class serves as the model for the library system, managing students, teachers,
 * courses, assignments, and scores. It provides methods to add, remove, and retrieve
 * these entities, as well as to perform various calculations related to grades and
 * course averages.
 */
public class LibraryModel {
    private final Map<String, Student> studentMap = new HashMap<>();
    private final Map<String, Teacher> teacherMap = new HashMap<>();
    private final Map<String, Course> courseMap = new HashMap<>();
    private final Map<String, Assignment> assignmentMap = new HashMap<>();
    private final Map<String, Score> gradeMap = new HashMap<>();

    public void clearAllData() {
        studentMap.clear();
        teacherMap.clear();
        courseMap.clear();
        assignmentMap.clear();
        gradeMap.clear();
    }

    public int getCourseCount() {
        return courseMap.size();
    }


    public void addStudent(Student s) {
        studentMap.put(s.getStuID(), s);
    }

    public void removeStudent(String studentID) {
        studentMap.remove(studentID);
    }


    public Student getStudent(String id) {
        Student original = studentMap.get(id);
        return (original != null) ? new Student(original) : null;
    }



    public boolean studentExists(String id) {
        return studentMap.containsKey(id);
    }

    // This method returns a copy of the student list
    public Collection<Student> getAllStudents() {
        List<Student> copiedList = new ArrayList<>();
        for (Student s : studentMap.values()) {
            copiedList.add(new Student(s));  // assumes you have a copy constructor
        }
        return Collections.unmodifiableCollection(copiedList);
    }

    public void addTeacher(Teacher t) {
        teacherMap.put(t.getTeacherID(), t);
    }

    public Teacher getTeacher(String id) {
        return teacherMap.get(id);
    }

    public boolean teacherExists(String id) {
        return teacherMap.containsKey(id);
    }

    public Collection<Teacher> getAllTeachers() {
        List<Teacher> copiedList = new ArrayList<>();
        for (Teacher t : teacherMap.values()){
            copiedList.add(new Teacher(t));
        }
        return Collections.unmodifiableCollection(copiedList);
    }

    public void addCourse(Course c) {
        courseMap.put(c.getCourseID(), c);
    }

    public Course getCourse(String id) {
        Course c = courseMap.get(id);
        if (c == null){
                return null;
        }
        return new Course(c);
    }

    public Collection<Course> getAllCourses() {
        List<Course> copiedList = new ArrayList<>();
        for (Course c : courseMap.values()){
            copiedList.add(new Course(c));
        }
        return Collections.unmodifiableCollection(copiedList);
    }

    public List<Course> getCoursesByTeacher(String teacherID) {
        List<Course> result = new ArrayList<>();
        for (Course c : courseMap.values()) {
            if (c.getTeacherID().equals(teacherID)) {
                result.add(c);
            }
        }
        return result;
    }

    public void addAssignment(Assignment a) {
        assignmentMap.put(a.getAssignmentID(), a);

        // tie into Course
        Course c = courseMap.get(a.getCourseID());
        if (c != null) {
            c.addAssignment(a);
        }

        // tie into Student
        Student s = studentMap.get(a.getStudentID());
        if (s != null) {
            s.addAssignment(a.getAssignmentID());
        }
    }

    /// This method is used to add an assignment from a file
    public Assignment getAssignment(String id) {
        Assignment original = assignmentMap.get(id);
        if (original == null) return null;
        return new Assignment(original);
    }

    public Collection<Assignment> getAllAssignments() {
        List<Assignment> copiedList = new ArrayList<>();
        for (Assignment a : assignmentMap.values()){
            copiedList.add(new Assignment(a));
        }
        return Collections.unmodifiableCollection(copiedList);
    }

    public void submitAssignment(String assignmentID) {
        Assignment a = assignmentMap.get(assignmentID);
        if (a == null) {
            throw new IllegalArgumentException("No such assignment: " + assignmentID);
        }
        a.submit();
    }

    public List<Assignment> getAssignmentsInCourse(String courseID) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment a : assignmentMap.values()) {
            if (a.getCourseID().equals(courseID)) {
                result.add(a);
            }
        }
        return result;
    }

    public void addScore(Score score) {
        gradeMap.put(score.getGradeID(), score);
    }

    public Score getScore(String gradeID) {
        Score original = gradeMap.get(gradeID);
        if (original == null) {
            return null;
        }
        return new Score(original);
    }

    public Map<String, Score> getAllScores() {
        Map<String, Score> copiedMap = new HashMap<>();
        for (Map.Entry<String, Score> entry : gradeMap.entrySet()) {
            copiedMap.put(entry.getKey(), new Score(entry.getValue())); // deep copy
        }
        return Collections.unmodifiableMap(copiedMap);
    }

    public List<Assignment> getAssignmentsForStudentInCourse(String studentID, String courseID) {
        List<Assignment> result = new ArrayList<>();
        for (Assignment a : assignmentMap.values()) {
            if (a.getStudentID().equals(studentID) && a.getCourseID().equals(courseID)) {
                result.add(new Assignment(a));
            }
        }
        return result;
    }

    public Score getScoreForAssignment(String assignmentID) {
        Assignment a = assignmentMap.get(assignmentID);
        return (a == null || a.getGradeID() == null) ? null : new Score(gradeMap.get(a.getGradeID()));
    }

    public List<String> getStudentIDsInCourse(String courseID) {
        // Optionally check courseMap.containsKey(courseID) first
        return studentMap.values().stream()
                .filter(s -> s.getEnrolledCourseIDs().contains(courseID))
                .map(Student::getStuID)
                .collect(Collectors.toList());
    }

    public void enrollStudentInCourse(String studentID, String courseID) {
        Student s = studentMap.get(studentID);
        if (s == null) {
            throw new IllegalArgumentException("No such student: " + studentID);
        }
        Course c = courseMap.get(courseID);
        if (c == null) {
            throw new IllegalArgumentException("No such course: " + courseID);
        }
        s.enrollInCourse(courseID);
    }


    public void removeAssignment(String assignmentID) {
        Assignment a = assignmentMap.remove(assignmentID);
        if (a == null) return;
        // detach from Course
        Course course = courseMap.get(a.getCourseID());
        if (course != null) {
            course.removeAssignmentByID(assignmentID);
        }

        // detach from Student
        Student student = studentMap.get(a.getStudentID());
        if (student != null) {
            student.removeAssignment(assignmentID);
        }
        String gradeID = a.getGradeID();
        if (gradeID != null) {
            gradeMap.remove(gradeID);
        }
    }

    public void removeStudentFromCourse(String studentID, String courseID) {
        Student student = studentMap.get(studentID);
        if (student != null) {
            student.dropCourse(courseID);
        }
    }


    public void removeCourse(String courseID) {
        // pull out and delete the Course itself
        Course c = courseMap.remove(courseID);
        if (c == null) return;
        for (String aid : new ArrayList<>(c.getAssignments().keySet())) {
            removeAssignment(aid);
        }

        //drop this course from every student’s enrolled list
        for (Student s : studentMap.values()) {
            s.dropCourse(courseID);
        }
    }


    public void initializeIDGen() {
        initPrefix("STU", studentMap.keySet());
        initPrefix("TCH", teacherMap.keySet());
        initPrefix("CRS", courseMap.keySet());
        initPrefix("ASG", assignmentMap.keySet());
    }

    private void initPrefix(String prefix, Set<String> ids) {
        int max = ids.stream()
                .flatMap(id -> {
                    if (id.startsWith(prefix)) {
                        return Stream.of(id.substring(prefix.length()));
                    } else {
                        return Stream.empty();
                    }
                })
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(-1);
        IDGen.initialize(prefix, max + 1);
    }

    /**
     * Populate a course with demo data (5 students + HW/Project/Quiz assignments).
     * All objects are created inside <code>courseID</code>;
     * @param courseID the course to populate
     */
    public void populateDemoData(String courseID) {
        // see if the course exists
        Course c = courseMap.get(courseID);
        if (c == null) {
            throw new IllegalArgumentException("Course " + courseID + " not found.");
        }
        //quick check
        Teacher tch = teacherMap.get(c.getTeacherID());
        if (tch != null && !tch.getTeachingCourseIDs().contains(courseID)) {
            tch.addCourse(courseID);
        }

        c.setGradingMode(true);
        c.setCategoryWeight("Homework", 0.3);
        c.setCategoryWeight("Project",  0.4);
        c.setCategoryWeight("Quiz",     0.3);
        c.setCategoryDropCount("Quiz", 1);

        List<String> sids = getStudentIDsInCourse(courseID);
        if (sids.isEmpty()) {
            System.out.println("⚠️ No students enrolled in course " + courseID + " – skipping demo data.");
            return;
        }
        List<Student> studs = sids.stream()
                .map(studentMap::get)
                .filter(Objects::nonNull)
                .toList();
        LocalDate base = LocalDate.of(2025, 4, 1);
        createGroup("HW", 4, "Homework", 5, base, studs, courseID, 60, 100);
        createGroup("Project", 2, "Project", 10, base, studs, courseID, 80, 120);
        createGroup("Quiz", 3, "Quiz", 2, base, studs, courseID, 70, 100);

        System.out.println("✅ Demo data populated for course " + courseID);
    }

    /**
     * Unchanged helper method to create a group of assignments.
     */
    private void createGroup(
            String prefix,
            int count,
            String category,
            int span,
            LocalDate base,
            List<Student> studs,
            String courseID,
            int minEarned,
            int maxEarned
    ) {
        for (int i = 1; i <= count; i++) {
            for (Student s : studs) {
                Assignment a = new Assignment(
                        prefix + " " + i,
                        s.getStuID(),
                        courseID,
                        base,
                        base.plusDays(span)
                );
                a.setCategory(category);
                addAssignment(a);
                if (Math.random() < 0.85) {
                    a.submit();
                    if (Math.random() < 0.75) {
                        String gradeID = "G_" + a.getAssignmentID();
                        a.markGraded(gradeID);
                        int earned = minEarned
                                + (int)(Math.random() * (maxEarned - minEarned + 1));

                        // create, init and store the Score
                        Score score = new Score(
                                gradeID,
                                a.getAssignmentID(),
                                s.getStuID(),
                                earned,
                                maxEarned
                        );
                        addScore(score);
                    }
                }
            }
        }
    }


    public double calculateClassAverage(String courseID) {
        Course course = courseMap.get(courseID);
        if (course == null) return 0.0;

        List<String> studentIDs = getStudentIDsInCourse(courseID);
        double total = 0.0;

        for (String sid : studentIDs) {
            total += course.isUsingWeightedGrading()
                    ? computeWeightedPercentage(sid, courseID)
                    : computeTotalPointsPercentage(sid, courseID);
        }

        return studentIDs.isEmpty() ? 0.0 : total / studentIDs.size();
    }

    public double calculateGPA(String studentID) {
        Student s = studentMap.get(studentID);
        if (s == null) {
            throw new IllegalArgumentException("No such student: " + studentID);
        }

        int totalPoints = 0, count = 0;
        for (String courseID : s.getEnrolledCourseIDs()) {
            Course course = courseMap.get(courseID);
            if (course == null) continue;

            double pct = course.isUsingWeightedGrading()
                    ? computeWeightedPercentage(studentID, courseID)
                    : computeTotalPointsPercentage(studentID, courseID);

            Grade grade = Grade.fromScore(pct);
            int gpaPoints = switch (grade) {
                case A -> 4;
                case B -> 3;
                case C -> 2;
                case D -> 1;
                case F -> 0;
            };
            totalPoints += gpaPoints;
            count++;
        }
        return count == 0 ? 0.0 : (double) totalPoints / count;
    }

    public double getOverallClassAverage(String courseID) {
        Course course = courseMap.get(courseID);
        if (course == null) return 0.0;

        List<String> studentIDs = getStudentIDsInCourse(courseID);
        double total = 0.0;

        for (String sid : studentIDs) {
            total += course.isUsingWeightedGrading()
                    ? computeWeightedPercentage(sid, courseID)
                    : computeTotalPointsPercentage(sid, courseID);
        }

        return studentIDs.isEmpty() ? 0.0 : total / studentIDs.size();
    }

    public Map<String, Grade> assignFinalLetterGrades(String courseID) {
        Map<String, Grade> result = new HashMap<>();
        Course course = courseMap.get(courseID);
        if (course == null) return result;

        for (String sid : getStudentIDsInCourse(courseID)) {
            double pct = course.isUsingWeightedGrading()
                    ? computeWeightedPercentage(sid, courseID)
                    : computeTotalPointsPercentage(sid, courseID);
            String name = studentMap.get(sid).getFullName();
            result.put(name, Grade.fromScore(pct));
        }
        return result;
    }

    private double computeTotalPointsPercentage(String studentID, String courseID) {
        List<Assignment> assignments = getAssignmentsForStudentInCourse(studentID, courseID);
        int earned = 0, total = 0;

        for (Assignment a : assignments) {
            Score s = getScoreForAssignment(a.getAssignmentID());
            if (s != null) {
                earned += s.getEarned();
                total += s.getTotal();
            }
        }
        return total == 0 ? 0.0 : (100.0 * earned / total);
    }

    private double computeWeightedPercentage(String studentID, String courseID) {
        Course course = courseMap.get(courseID);
        if (course == null) return 0.0;

        Map<String, List<Score>> byCategory = new HashMap<>();

        for (Assignment a : getAssignmentsForStudentInCourse(studentID, courseID)) {
            if (a.getGradeID() == null) continue;
            Score s = getScoreForAssignment(a.getAssignmentID());
            if (s == null) continue;

            String cat = a.getCategory();
            byCategory.putIfAbsent(cat, new ArrayList<>());
            byCategory.get(cat).add(s);
        }

        double weightedTotal = 0.0;

        for (String cat : course.getCategoryWeights().keySet()) {
            List<Score> scores = byCategory.getOrDefault(cat, new ArrayList<>());
            scores.sort(Comparator.comparingDouble(Score::getPercentage)); // low → high
            int drop = course.getDropCountForCategory(cat);
            if (drop >= scores.size()) continue;

            List<Score> used = scores.subList(drop, scores.size());
            int earned = used.stream().mapToInt(Score::getEarned).sum();
            int total = used.stream().mapToInt(Score::getTotal).sum();
            if (total > 0) {
                double pct = 1.0 * earned / total;
                double weight = course.getCategoryWeight(cat);
                weightedTotal += pct * weight;
            }
        }

        return weightedTotal * 100.0; // final percent
    }
    public double getFinalPercentage(String studentID, String courseID) {
        Course c = courseMap.get(courseID);
        if (c == null) return 0.0;
        return c.isUsingWeightedGrading()
                ? computeWeightedPercentage(studentID, courseID)
                : computeTotalPointsPercentage(studentID, courseID);
    }

    public void setGradingMode(String courseID, boolean weighted) {
        Course c = courseMap.get(courseID);
        if (c != null) c.setGradingMode(weighted);
    }

    public void setCategoryWeight(String courseID, String category, double weight) {
        Course c = courseMap.get(courseID);
        if (c != null) c.setCategoryWeight(category, weight);
    }

    public void setCategoryDrop(String courseID, String category, int drop) {
        Course c = courseMap.get(courseID);
        if (c != null) c.setCategoryDropCount(category, drop);
    }

    public double getAveragePercentageForGroup(String courseID, String assignmentName) {
        List<Assignment> all = getAssignmentsInCourse(courseID);
        List<Double> percentages = all.stream()
                .filter(a -> assignmentName.equals(a.getAssignmentName()))
                .map(a -> {
                    Score s = getScoreForAssignment(a.getAssignmentID());
                    return (s == null) ? null : s.getPercentage();
                })
                .filter(Objects::nonNull)
                .toList();

        if (percentages.isEmpty()) return 0.0;
        return percentages.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public double getMedianPercentageForGroup(String courseID, String assignmentName) {
        List<Assignment> all = getAssignmentsInCourse(courseID);
        List<Double> sorted = all.stream()
                .filter(a -> assignmentName.equals(a.getAssignmentName()))
                .map(a -> {
                    Score s = getScoreForAssignment(a.getAssignmentID());
                    return (s == null) ? null : s.getPercentage();
                })
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        if (sorted.isEmpty()) return 0.0;
        int mid = sorted.size() / 2;
        return (sorted.size() % 2 == 1)
                ? sorted.get(mid)
                : (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
    }


    public void markCourseAsCompleted(String courseID) {
        Course course = courseMap.get(courseID);
        if (course != null) {
            course.markCompleted();
        }
    }

    public List<String> getStudentCourses(String studentID) {
        Student s = studentMap.get(studentID);
        if (s == null) {
            // No such student → empty list
            return Collections.emptyList();
        }
        // Return a copy so callers can’t mutate the internal set
        return new ArrayList<>(s.getEnrolledCourseIDs());
    }

    /**
     * Build a plain-text grade report for one student
     */
    public String buildGradeReport(String studentID) {
        Student s = studentMap.get(studentID);
        if (s == null) {
            throw new IllegalArgumentException("No such student: " + studentID);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Grade report for ")
                .append(s.getFullName())
                .append(":\n");

        for (String courseID : s.getEnrolledCourseIDs()) {
            Course c = courseMap.get(courseID);
            if (c == null) continue;

            double pct = getFinalPercentage(studentID, courseID);
            Grade  g   = Grade.fromScore(pct);

            sb.append("  • ")
                    .append(c.getCourseName())
                    .append(" – ")
                    .append(String.format("%.1f%%", pct))
                    .append(" (")
                    .append(g)
                    .append(")\n");
        }

        double overall = courseAverageAcrossAll(studentID, false);
        sb.append("Overall average: ")
                .append(String.format("%.1f%%", overall))
                .append(" (")
                .append(Grade.fromScore(overall))
                .append(")\n");

        return sb.toString();
    }
    /**
     * Average of the final percentage for every course the student is enrolled in.
     */
    public double courseAverageAcrossAll(String stuID, boolean completedOnly) {
        Student s = studentMap.get(stuID);
        if (s == null) {
            // no such student → no data → average zero
            return 0.0;
        }

        double total   = 0.0;
        int    counted = 0;
        for (String cid : s.getEnrolledCourseIDs()) {
            Course c = courseMap.get(cid);
            if (c == null) continue;
            if (completedOnly && !c.isCompleted()) continue;

            total   += getFinalPercentage(stuID, cid);
            counted++;
        }
        return counted == 0 ? 0.0 : total / counted;
    }
}