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

    public void removeScore(String scoreID) {
        gradeMap.remove(scoreID);
    } // Simple removal for scores by ID

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


    public void state3() {
        Teacher t = new Teacher("Zhao", "T001");
        addTeacher(t);
        Course c1 = new Course("CS", "Advanced Programming", t.getTeacherID());
        c1.setGradingMode(true);
        c1.setCategoryWeight("Homework", 0.3);
        c1.setCategoryWeight("Project", 0.4);
        c1.setCategoryWeight("Quiz", 0.3);
        c1.setCategoryDropCount("Quiz", 1);  // drop lowest quiz
        addCourse(c1);
        t.addCourse(c1.getCourseID());

        // Create students
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Student s = new Student("Stu" + i, "Demo", "stu" + i + "@cs.arizona.edu");
            students.add(s);
            addStudent(s);
            enrollStudentInCourse(s.getStuID(), c1.getCourseID());
        }

        LocalDate assignDate = LocalDate.of(2025, 4, 1);
        int hwCount = 4, projCount = 2, quizCount = 3;

        for (int i = 1; i <= hwCount; i++) {
            for (Student s : students) {
                String sid = s.getStuID();
                Assignment a = new Assignment("HW " + i, sid, c1.getCourseID(), assignDate, assignDate.plusDays(5));
                a.setCategory("Homework");
                addAssignment(a);
                s.addAssignment(a.getAssignmentID());

                if ( i != 4 && i != 5 ) {
                    a.submit();
                    a.markGraded("G_" + a.getAssignmentID());
                    int earned = 60 + (int)(Math.random() * 41);
                    addScore(new Score("G_" + a.getAssignmentID(), a.getAssignmentID(), sid, earned, 100));
                }
            }
        }

        // 示例：Project 作业
        for (int i = 1; i <= projCount; i++) {
            for (Student s : students) {
                String sid = s.getStuID();
                Assignment a = new Assignment("Project " + i, sid, c1.getCourseID(), assignDate, assignDate.plusDays(10));
                a.setCategory("Project");
                addAssignment(a);
                s.addAssignment(a.getAssignmentID());

                if (i != 4) {
                    a.submit();
                    a.markGraded("G_" + a.getAssignmentID());
                    int earned = 80 + (int)(Math.random() * 41);
                    addScore(new Score("G_" + a.getAssignmentID(), a.getAssignmentID(), sid, earned, 100));
                }
            }
        }


        for (int i = 1; i <= quizCount; i++) {
            for (Student s : students) {
                Assignment a = new Assignment("Quiz " + i, s.getStuID(), c1.getCourseID(), assignDate, assignDate.plusDays(2));
                a.setCategory("Quiz");
                addAssignment(a);
                s.addAssignment(a.getAssignmentID());
                a.submit();
                a.markGraded("G_" + a.getAssignmentID());
                int score = switch (i) {
                    case 1 -> 100; case 2 -> 80; default -> 60;
                };
                int earned = 70 + (int)(Math.random() * 41); // 60 ~ 100
                addScore(new Score("G_" + a.getAssignmentID(), a.getAssignmentID(), s.getStuID(), earned, 100));
            }
        }
    }

    /**
     * Populate a course with demo data (5 students + HW/Project/Quiz assignments).
     * All objects are created inside <code>courseID</code>;
     * existing teacher / course信息保持不变。
     *
     * @param courseID the course to populate
     */
    public void populateDemoData(String courseID) {
        /* ---------- 基本检查 ---------- */
        Course c = courseMap.get(courseID);
        if (c == null) {
            throw new IllegalArgumentException("Course " + courseID + " not found.");
        }
        // 可选：确保其教师持有该课程
        Teacher tch = teacherMap.get(c.getTeacherID());
        if (tch != null) tch.addCourse(courseID);

        /* ---------- 课程权重与丢弃规则 ---------- */
        c.setGradingMode(true);                 // Option 2 - category weights
        c.setCategoryWeight("Homework", 0.3);
        c.setCategoryWeight("Project",  0.4);
        c.setCategoryWeight("Quiz",     0.3);
        c.setCategoryDropCount("Quiz", 1);      // drop lowest quiz

        /* ---------- 创建学生 ---------- */
        List<Student> studs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Student s = new Student("Stu" + i, "Demo", "stu" + i + "@cs.arizona.edu");
            addStudent(s);
            enrollStudentInCourse(s.getStuID(), courseID);
            studs.add(s);
        }

        /* ---------- 生成作业 ---------- */
        LocalDate base = LocalDate.of(2025, 4, 1);

        // Homework ×4
        createGroup("HW", 4, "Homework", 5, base, studs, courseID, 60, 100);

        // Project ×2
        createGroup("Project", 2, "Project", 10, base, studs, courseID, 80, 120);

        // Quiz ×3
        createGroup("Quiz", 3, "Quiz", 2, base, studs, courseID, 70, 100);
    }

    /* ========== helper ========== */
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
                addAssignment(a);
                s.addAssignment(a.getAssignmentID());

                /* 随机决定是否提交并评分（作演示） */
                if (Math.random() < 0.85) {          // ≈15 % 未提交
                    a.submit();
                    if (Math.random() < 0.75) {      // ≈25 % 已交未评
                        a.markGraded("G_" + a.getAssignmentID());
                        int earned = minEarned + (int) (Math.random() * (maxEarned - minEarned + 1));
                        addScore(new Score("G_" + a.getAssignmentID(),
                                a.getAssignmentID(),
                                s.getStuID(),
                                earned,
                                maxEarned));
                    }
                }
            }
        }
    }

    public void stateStudent() {
        // create a new course and assign it to a teacher
        Teacher t = new Teacher("Zhao", "Chang");
        addTeacher(t);

        Course c = new Course("CS", "Intro to Programming", t.getTeacherID());
        addCourse(c);
        t.addCourse(c.getCourseID());
        for (Student s : getAllStudents()) {
            enrollStudentInCourse(s.getStuID(), c.getCourseID());
        }

        // create assignments
        LocalDate assignDate = LocalDate.of(2025, 4, 20);
        for (int i = 1; i <= 3; i++) {
            String name = "HW " + i;
            for (Student s : getAllStudents()) {
                Assignment a = new Assignment(
                        name,
                        s.getStuID(),
                        c.getCourseID(),
                        assignDate,
                        assignDate.plusDays(5)
                );
                addAssignment(a);
                s.addAssignment(a.getAssignmentID());

                // simulate partial submission and grading
                if (Math.random() < 0.8) {
                    a.submit();
                    if (Math.random() < 0.7) {
                        a.markGraded("G_" + a.getAssignmentID());
                        int earned = 70 + (int)(Math.random() * 31);  // 70~100
                        addScore(new Score("G_" + a.getAssignmentID(), a.getAssignmentID(), s.getStuID(), earned, 100));
                    }
                }
            }
        }
    }

    public void loadStudentsFromCSV(Path csvPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    String first = parts[0].trim();
                    String last  = parts[1].trim();
                    String email = parts[2].trim();
                    Student s = new Student(first, last, email);
                    addStudent(s);
                }
            }
        }
        System.out.println(studentMap.size() + " students loaded");
    }
    public String getFirstStudentID() {
        return studentMap.keySet()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No students loaded"));
    }
    /**
     * @param dirPath CSV， "src/main/DataBase/Students"
     */
    public void loadStudentsFromDirectory(Path dirPath) throws IOException {
        if (!Files.isDirectory(dirPath)) {
            throw new IllegalArgumentException(dirPath + " 不是一个目录");
        }
        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(dirPath, "*.csv")) {
            for (Path csvFile : stream) {
                loadStudentsFromCSV(csvFile);
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
        List<String> studentIDs = getStudentIDsInCourse(courseID);
        int totalEarned = 0;
        int totalPossible = 0;

        for (String sid : studentIDs) {
            for (Assignment a : getAssignmentsForStudentInCourse(sid, courseID)) {
                Score s = getScoreForAssignment(a.getAssignmentID());
                if (s != null) {
                    totalEarned += s.getEarned();
                    totalPossible += s.getTotal();
                }
            }
        }
        return totalPossible == 0 ? 0.0 : (100.0 * totalEarned / totalPossible);
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

}