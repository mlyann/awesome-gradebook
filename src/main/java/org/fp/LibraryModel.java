package org.fp;

import java.time.LocalDate;
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
        return Collections.unmodifiableCollection(teacherMap.values());
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
        return assignmentMap.get(id);
    }

    public Collection<Assignment> getAllAssignments() {
        return Collections.unmodifiableCollection(assignmentMap.values());
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
        return (a == null || a.getGradeID() == null) ? null : gradeMap.get(a.getGradeID());
    }

    public List<String> getStudentIDsInCourse(String courseID) {
        return courseStudents.getOrDefault(courseID, new ArrayList<>());
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

                    addAssignment(a);         // ✅ 只在提交时加入系统
                    s.addAssignment(a.getAssignmentID());
                }

            }
        }
    }
}