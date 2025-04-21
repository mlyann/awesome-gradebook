package org.fp;

import java.time.LocalDate;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

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

    public void addStudent(Student s) {
        studentMap.put(s.getStuID(), s);
    }

    public Student getStudent(String id) {
        return new Student(studentMap.get(id));
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
        return new Teacher(teacherMap.get(id));
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
        return new Course(courseMap.get(id));
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



    // Seed demo data for UI testing
    public void state() {
        Student s = new Student("20250001", "Ming", "Yang", "ming@example.com");
        Teacher t = new Teacher("Alice", "Zhao", "T001");

        Course c1 = new Course("Math", "Basic Algebra", "T001");
        Course c2 = new Course("Physics", "Newtonian Mechanics", "T001");
        Course c3 = new Course("Biology", "Molecular Biology", "T001");
        Course c4 = new Course("History", "World History", "T001");
        Course c5 = new Course("CS", "Data Structures", "T001");

        addStudent(s);
        addTeacher(t);
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

        t.addCourse(c1.getCourseID());
        t.addCourse(c2.getCourseID());
        t.addCourse(c3.getCourseID());
        t.addCourse(c4.getCourseID());
        t.addCourse(c5.getCourseID());

        int assignmentCount = 20;
        LocalDate assignDate = LocalDate.of(2025, 4, 1);
        for (int i = 1; i <= assignmentCount; i++) {
            String aid = String.format("A%03d", i);
            String gid = String.format("G%03d", i);
            String title = "Assignment " + i;
            LocalDate due = LocalDate.of(2025, 4, 10 + i);

            Assignment a = new Assignment(aid, title, s.getStuID(), c1.getCourseID(), assignDate, due);
            addAssignment(a);
            s.addAssignment(aid);

            if (i % 3 == 0 || i % 5 == 0) {
                a.submit();
                if (i % 5 == 0) {
                    a.markGraded(gid);
                    addScore(new Score(gid, aid, s.getStuID(), 60 + (i % 40), 100));
                }
            }
        }
    }

    public void state1() {
        // 单个老师
        Teacher t = new Teacher("Alice", "Zhao", "T001");
        addTeacher(t);

        // 创建 5 门课程
        Course c1 = new Course("Math", "Basic Algebra", "T001");
        Course c2 = new Course("Physics", "Newtonian Mechanics", "T001");
        Course c3 = new Course("Biology", "Molecular Biology", "T001");
        Course c4 = new Course("History", "World History", "T001");
        Course c5 = new Course("CS", "Data Structures", "T001");
        addCourse(c1); addCourse(c2); addCourse(c3); addCourse(c4); addCourse(c5);

        // 老师加入课程
        t.addCourse(c1.getCourseID());
        t.addCourse(c2.getCourseID());
        t.addCourse(c3.getCourseID());
        t.addCourse(c4.getCourseID());
        t.addCourse(c5.getCourseID());

        // 创建 10 个学生
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String sid = String.format("STU%05d", i);
            Student s = new Student(sid, "Stu" + i, "Test", "stu" + i + "@test.com");
            students.add(s);
            addStudent(s);

            enrollStudentInCourse(sid, c1.getCourseID());  // ✅ 正确添加到 model
        }


        // 创建 20 个作业，所有学生共享
        LocalDate assignDate = LocalDate.of(2025, 4, 1);
        for (int i = 1; i <= 20; i++) {
            String aid = String.format("A%03d", i);
            LocalDate due = LocalDate.of(2025, 4, 10 + i);
            for (Student s : students) {
                Assignment a = new Assignment(aid + "_" + s.getStuID(), "Assignment " + i,
                        s.getStuID(), c1.getCourseID(), assignDate, due);
                addAssignment(a);
                s.addAssignment(a.getAssignmentID());

                if (Math.random() < 0.6) {
                    a = new Assignment(aid + "_" + s.getStuID(), "Assignment " + i,
                            s.getStuID(), c1.getCourseID(), assignDate, due);
                    a.submit();

                    if (Math.random() < 0.5) {
                        String gid = "G" + UUID.randomUUID().toString().substring(0, 6);
                        int earned = 60 + (i % 40);
                        a.markGraded(gid);
                        addScore(new Score(gid, a.getAssignmentID(), s.getStuID(), earned, 100));
                    }

                    addAssignment(a);         //
                    s.addAssignment(a.getAssignmentID());
                }

            }
        }
    }
    public void state2() {
        // 1) 单个老师
        Teacher t = new Teacher("Alice", "Zhao", "T001");
        addTeacher(t);

        // 2) 创建 5 门课程，并关联到老师
        Course c1 = new Course("Math", "Basic Algebra", t.getTeacherID());
        Course c2 = new Course("Physics", "Newtonian Mechanics", t.getTeacherID());
        Course c3 = new Course("Biology", "Molecular Biology", t.getTeacherID());
        Course c4 = new Course("History", "World History", t.getTeacherID());
        Course c5 = new Course("CS", "Data Structures", t.getTeacherID());
        addCourse(c1); addCourse(c2); addCourse(c3); addCourse(c4); addCourse(c5);
        t.addCourse(c1.getCourseID());
        t.addCourse(c2.getCourseID());
        t.addCourse(c3.getCourseID());
        t.addCourse(c4.getCourseID());
        t.addCourse(c5.getCourseID());

        // 3) 把所有从 CSV 加载进来的学生，选到第一门课
        for (Student s : getAllStudents()) {
            enrollStudentInCourse(s.getStuID(), c1.getCourseID());
        }

        // 4) 给每位学生生成 20 个作业，并随机提交 & 批改
        LocalDate assignDate = LocalDate.of(2025, 4, 1);
        for (int i = 1; i <= 20; i++) {
            String aid = String.format("A%03d", i);
            LocalDate due = LocalDate.of(2025, 4, 10 + i);

            for (Student s : getAllStudents()) {
                // 新作业
                Assignment a = new Assignment(
                        aid + "_" + s.getStuID(),
                        "Assignment " + i,
                        s.getStuID(),
                        c1.getCourseID(),
                        assignDate,
                        due
                );
                addAssignment(a);
                s.addAssignment(a.getAssignmentID());

                // 随机提交
                if (Math.random() < 0.6) {
                    a.submit();
                    // 随机批改
                    if (Math.random() < 0.5) {
                        String gid = "G" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                        int earned = 60 + (i % 40);
                        a.markGraded(gid);
                        addScore(new Score(gid, a.getAssignmentID(), s.getStuID(), earned, 100));
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
                    String sid   = generateStudentID();
                    String first = parts[0].trim();
                    String last  = parts[1].trim();
                    String email = parts[2].trim();
                    Student s = new Student(sid, first, last, email);
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

    private String generateStudentID() {
        int next = studentMap.size() + 1;
        return String.format("STU%05d", next);
    }

    public double calculateClassAverage(String courseID) {
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
    public double calculateGPA(String studentID) {
        List<String> courseIDs = studentCourses.getOrDefault(studentID, List.of());
        int totalGradePoints = 0;
        int countedCourses = 0;

        for (String courseID : courseIDs) {
            List<Assignment> assignments = getAssignmentsForStudentInCourse(studentID, courseID);
            int earned = 0, total = 0;

            for (Assignment a : assignments) {
                Score s = getScoreForAssignment(a.getAssignmentID());
                if (s != null) {
                    earned += s.getEarned();
                    total += s.getTotal();
                }
            }

            if (total > 0) {
                double percent = 100.0 * earned / total;
                Grade grade = Grade.fromScore(percent);
                int points = switch (grade) {
                    case A -> 4;
                    case B -> 3;
                    case C -> 2;
                    case D -> 1;
                    case F -> 0;
                };
                totalGradePoints += points;
                countedCourses++;
            }
        }

        return countedCourses == 0 ? 0.0 : 1.0 * totalGradePoints / countedCourses;
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
        for (String sid : getStudentIDsInCourse(courseID)) {
            List<Assignment> assignments = getAssignmentsForStudentInCourse(sid, courseID);
            int earned = 0, total = 0;
            for (Assignment a : assignments) {
                Score s = getScoreForAssignment(a.getAssignmentID());
                if (s != null) {
                    earned += s.getEarned();
                    total += s.getTotal();
                }
            }
            if (total > 0) {
                double pct = 100.0 * earned / total;
                result.put(sid, Grade.fromScore(pct));
            }
        }
        return result;
    }

}