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
import java.util.stream.Stream;
import java.util.*;

public class LibraryModel {
    private final Map<String, Student> studentMap = new HashMap<>();
    private final Map<String, Teacher> teacherMap = new HashMap<>();
    private final Map<String, Course> courseMap = new HashMap<>();
    private final Map<String, Assignment> assignmentMap = new HashMap<>();
    private final Map<String, Score> gradeMap = new HashMap<>();

    private final Map<String, List<String>> studentCourses = new HashMap<>();
    private final Map<String, List<String>> courseStudents = new HashMap<>();
    private final Map<String, List<String>> courseAssignments = new HashMap<>();
    private final Map<String, List<String>> studentAssignments = new HashMap<>();
    private final Map<String, String> assignmentGrades = new HashMap<>();
    private final Map<String, List<String>> courseToStudentIDs = new HashMap<>();


    public void clearAllData() {
        studentMap.clear();
        teacherMap.clear();
        courseMap.clear();
        assignmentMap.clear();
        gradeMap.clear();

        studentCourses.clear();
        courseStudents.clear();
        courseAssignments.clear();
        studentAssignments.clear();
        assignmentGrades.clear();
        courseToStudentIDs.clear();
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
    }

    public Assignment getAssignment(String id) {
        return new Assignment(assignmentMap.get(id));
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
        return new Score(gradeMap.get(gradeID));
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
        if (courseStudents.containsKey(courseID)){
            return new ArrayList<>(courseStudents.get(courseID));
        }
        else{
            return new ArrayList<>();
        }
    }

    public void enrollStudentInCourse(String studentID, String courseID) {
        studentMap.get(studentID).enrollInCourse(courseID);

        courseStudents.putIfAbsent(courseID, new ArrayList<>());
        if (!courseStudents.get(courseID).contains(studentID)) {
            courseStudents.get(courseID).add(studentID);
        }

        studentCourses.putIfAbsent(studentID, new ArrayList<>());
        if (!studentCourses.get(studentID).contains(courseID)) {
            studentCourses.get(studentID).add(courseID);
        }
    }


    public void removeAssignment(String assignmentID) {
        Assignment a = assignmentMap.remove(assignmentID);
        if (a == null) return;
        List<String> saList = studentAssignments.get(a.getStudentID());
        if (saList != null) saList.remove(assignmentID);
        List<String> caList = courseAssignments.get(a.getCourseID());
        if (caList != null) caList.remove(assignmentID);
        if (a.getGradeID() != null) {
            gradeMap.remove(a.getGradeID());
            assignmentGrades.remove(assignmentID);
        }
    }

    // Remove student from a specific course
    public void removeStudentFromCourse(String studentID, String courseID) {
        List<String> studentList = courseToStudentIDs.get(courseID);
        if (studentList != null) {
            studentList.remove(studentID);
        }

        Student student = studentMap.get(studentID);
        if (student != null) {
            student.dropCourse(courseID); // Student object should update its internal list
        }
    }


    public void removeCourse(String courseID) {
        // 1. Remove all assignments associated with the course
        List<String> assignments = courseAssignments.getOrDefault(courseID, List.of());
        for (String aid : assignments) {
            Assignment a = assignmentMap.get(aid);
            if (a != null) {
                // Remove from student's assignment list
                String studentID = a.getStudentID();
                studentAssignments.getOrDefault(studentID, new ArrayList<>()).remove(aid);
                String gradeID = assignmentGrades.remove(aid);
                if (gradeID != null) {
                    gradeMap.remove(gradeID);
                }
                assignmentMap.remove(aid);
            }
        }
        courseAssignments.remove(courseID);

        // Remove course from students' enrolled course list
        List<String> students = courseStudents.getOrDefault(courseID, List.of());
        for (String sid : students) {
            studentCourses.getOrDefault(sid, new ArrayList<>()).remove(courseID);
        }
        courseStudents.remove(courseID);
        courseToStudentIDs.remove(courseID);

        // Remove course from teacher
        Course c = courseMap.get(courseID);
        if (c != null) {
            String teacherID = c.getTeacherID();
            Teacher t = teacherMap.get(teacherID);
            if (t != null) {
                t.removeCourse(courseID);
            }
        }

        // remove the course itself
        courseMap.remove(courseID);
    }

    /*
    public void removeScore(String scoreID) {
        gradeMap.remove(scoreID);
    } // Simple removal for scores by ID
     */

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
     * existing teacher / course信息保持不变。
     *
     * @param courseID the course to populate
     */
    // File: LibraryModel.java
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
     * Unchanged helper — 会逐个给 studs 添加 Assignment 并随机提交/评分
     */
    private void createGroup(String prefix, int count, String cat, int span,
                             LocalDate base, List<Student> studs, String cid,
                             int minEarned, int maxEarned) {

        for (int i = 1; i <= count; i++) {
            for (Student s : studs) {
                Assignment a = new Assignment(
                        prefix + " " + i,
                        s.getStuID(),
                        cid,
                        base,
                        base.plusDays(span)
                );
                a.setCategory(cat);
                addAssignment(a);            // 加入 model.assignmentMap
                courseAssignments.computeIfAbsent(cid, k -> new ArrayList<>())
                        .add(a.getAssignmentID());
                studentAssignments.computeIfAbsent(s.getStuID(), k -> new ArrayList<>())
                        .add(a.getAssignmentID());

                // 随机提交与评分
                if (Math.random() < 0.85) {
                    a.submit();
                    if (Math.random() < 0.75) {
                        String gid = "G_" + a.getAssignmentID();
                        a.markGraded(gid);
                        int earned = minEarned + (int)(Math.random() * (maxEarned - minEarned + 1));
                        Score score = new Score(gid, a.getAssignmentID(), s.getStuID(), earned, maxEarned);
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
        List<String> courseIDs = studentCourses.getOrDefault(studentID, List.of());
        int totalPoints = 0;
        int count = 0;

        for (String courseID : courseIDs) {
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

        return count == 0 ? 0.0 : (1.0 * totalPoints / count);
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
            result.put(sid, Grade.fromScore(pct));
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

    // LibraryModel.java
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
    public List<String> getStudentCourses(String stuID) {
        return new ArrayList<>(studentCourses.getOrDefault(stuID, List.of()));
    }

    /**
     * Build a plain-text grade report for one student
     */
    public String buildGradeReport(String stuID) {
        StringBuilder sb = new StringBuilder();
        sb.append("Grade report for ").append(getStudent(stuID).getFullName()).append(":\n");

        List<String> cids = studentCourses.getOrDefault(stuID, List.of());
        for (String cid : cids) {
            Course c = getCourse(cid);
            double pct = getFinalPercentage(stuID, cid);
            Grade  g   = Grade.fromScore(pct);
            sb.append("  • ").append(c.getCourseName())
                    .append(" – ").append(String.format("%.1f%%", pct))
                    .append(" (").append(g).append(")\n");
        }
        double overall = courseAverageAcrossAll(stuID, false);
        sb.append("Overall average: ").append(String.format("%.1f%%", overall))
                .append(" (").append(Grade.fromScore(overall)).append(")\n");
        return sb.toString();
    }
    /**
     * Average of the final percentage for every course the student is enrolled in.
     */
    public double courseAverageAcrossAll(String stuID, boolean completedOnly) {
        List<String> cids = studentCourses.getOrDefault(stuID, List.of());
        double total  = 0.0;
        int    counted = 0;

        for (String cid : cids) {
            Course c = courseMap.get(cid);
            if (c == null) continue;
            if (completedOnly && !c.isCompleted()) continue;

            total += getFinalPercentage(stuID, cid);   // already in percent
            counted++;
        }
        return counted == 0 ? 0.0 : total / counted;
    }
}