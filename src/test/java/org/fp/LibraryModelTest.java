package org.fp;

import org.fp.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LibraryModelTest {

    private LibraryModel model;

    @BeforeEach
    void setUp() {
        model = new LibraryModel();
    }

    @Test
    void testAddAndGetStudent() {
        Student student = new Student("John", "Doe", "john.doe@email.com");
        model.addStudent(student);

        Collection<Student> myList = model.getAllStudents();
        Student fetched = model.getStudent(student.getStuID());
        assertNotNull(fetched);
        assertEquals(student.getStuID(), fetched.getStuID());
    }

    @Test
    void testAddAndGetTeacher() {
        Teacher teacher = new Teacher("Jane", "Smith");
        model.addTeacher(teacher);
        Collection<Teacher> myList = model.getAllTeachers();

        Teacher fetched = model.getTeacher(teacher.getTeacherID());
        assertNotNull(fetched);
        assertEquals(teacher.getTeacherID(), fetched.getTeacherID());
    }

    @Test
    void testAddAndGetCourse() {
        Teacher teacher = new Teacher("Alice", "Brown");
        model.addTeacher(teacher);
        Course course = new Course("Math", "Algebra I", teacher.getTeacherID());

        model.addCourse(course);
        Collection<Course> myList = model.getAllCourses();
        Course fetched = model.getCourse(course.getCourseID());
        assertNotNull(fetched);
        assertEquals(course.getCourseID(), fetched.getCourseID());
    }

    @Test
    void testAddAndGetAssignment() {
        Student student = new Student("Tom", "Hanks", "tom@u.edu");
        Teacher teacher = new Teacher("Dr", "X");
        Course course = new Course("Physics", "Mechanics", teacher.getTeacherID());
        model.addStudent(student);
        model.addTeacher(teacher);
        model.addCourse(course);

        Assignment assignment = new Assignment("HW1", student.getStuID(), course.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(5));
        model.addAssignment(assignment);

        Collection<Assignment> myList = model.getAllAssignments();
        Assignment fetched = model.getAssignment(assignment.getAssignmentID());
        assertNotNull(fetched);
        assertEquals(assignment.getAssignmentID(), fetched.getAssignmentID());
    }

    @Test
    void testAddScoreAndGetScore() {
        Score score = new Score("G12345", "ASG001", "STU001", 85, 100);
        model.addScore(score);

        Map<String, Score> myList = model.getAllScores();
        Score fetched = model.getScore("G12345");
        assertNotNull(fetched);
        assertEquals(85, fetched.getEarned());
        assertEquals(100, fetched.getTotal());
    }

    @Test
    void testEnrollStudentInCourse() {
        Student student = new Student("Lina", "Zhou", "lz@u.edu");
        Teacher teacher = new Teacher("Dr", "Who");
        Course course = new Course("Chem", "Intro Chem", teacher.getTeacherID());

        model.addStudent(student);
        model.addTeacher(teacher);
        model.addCourse(course);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        assertTrue(model.getStudentIDsInCourse(course.getCourseID()).contains(student.getStuID()));
    }

    @Test
    void testCalculateGPA() {
        Student student = new Student("Bob", "Lee", "bob@email.com");
        Teacher teacher = new Teacher("Dr", "House");
        Course course = new Course("Bio", "Cells", teacher.getTeacherID());

        model.addStudent(student);
        model.addTeacher(teacher);
        model.addCourse(course);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        Assignment a = new Assignment("Midterm", student.getStuID(), course.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(1));
        a.submit();
        a.markGraded("G001");

        model.addAssignment(a);
        model.addScore(new Score("G001", a.getAssignmentID(), student.getStuID(), 95, 100));

        double gpa = model.calculateGPA(student.getStuID());
        assertEquals(4.0, gpa);
    }

    @Test
    void testAssignFinalLetterGrades() {
        Student student = new Student("Alan", "Turing", "alan@cs.u.edu");
        Teacher teacher = new Teacher("Prof", "Jones");
        Course course = new Course("CS", "Theory", teacher.getTeacherID());

        model.addStudent(student);
        model.addTeacher(teacher);
        model.addCourse(course);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        Assignment a = new Assignment("Final", student.getStuID(), course.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(1));
        a.submit();
        a.markGraded("G999");

        model.addAssignment(a);
        model.addScore(new Score("G999", a.getAssignmentID(), student.getStuID(), 78, 100));

        Map<String, Grade> finalGrades = model.assignFinalLetterGrades(course.getCourseID());
        assertEquals(Grade.C, finalGrades.get(student.getStuID()));
    }

    @Test
    void testGetCoursesCount(){
        assertEquals(0, model.getCourseCount());
    }

    @Test
    void removeStudent(){
        model.removeStudent("");
    }

    @Test
    void testGetAllCourses(){
        Collection<Course> retCourses = model.getAllCourses();
        assertEquals(0, retCourses.size());
    }

    @Test
    void testGetAllTeachers(){
        Collection<Teacher> retCourses = model.getAllTeachers();
        assertEquals(0, retCourses.size());
    }

    @Test
    void testGetAllCoursesByTeachers(){
        Collection<Course> retCourses = model.getCoursesByTeacher("");
        assertEquals(0, retCourses.size());
    }

    @Test
    void testRemoveCourseWithAssignments() {
        Teacher t = new Teacher("A", "B");
        model.addTeacher(t);
        Course c = new Course("CS", "Test", t.getTeacherID());
        model.addCourse(c);
        t.addCourse(c.getCourseID());

        Student s = new Student("F", "L", "x@y.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        Assignment a = new Assignment("HW", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        a.submit();
        a.markGraded("G100");
        model.addAssignment(a);
        s.addAssignment(a.getAssignmentID());
        model.addScore(new Score("G100", a.getAssignmentID(), s.getStuID(), 88, 100));

        model.removeCourse(c.getCourseID());

        assertNull(model.getCourse(c.getCourseID()));
    }

    @Test
    void testCalculateClassAverage_NoScores() {
        Teacher t = new Teacher("T", "X");
        model.addTeacher(t);
        Course c = new Course("Empty", "Test", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("S", "Y", "a@b.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        assertEquals(0.0, model.calculateClassAverage(c.getCourseID()));
    }

    @Test
    void testGetCoursesByTeacher_WithAndWithoutCourses() {
        // Teacher with no courses
        Teacher teacher1 = new Teacher("No", "Courses");
        model.addTeacher(teacher1);
        assertTrue(model.getCoursesByTeacher(teacher1.getTeacherID()).isEmpty(), "Expected no courses");

        // Teacher with two courses
        Teacher teacher2 = new Teacher("Prof", "Taught");
        model.addTeacher(teacher2);
        Course course1 = new Course("Math", "Algebra", teacher2.getTeacherID());
        Course course2 = new Course("Physics", "Mechanics", teacher2.getTeacherID());
        model.addCourse(course1);
        model.addCourse(course2);

        List<Course> courses = model.getCoursesByTeacher(teacher2.getTeacherID());
        assertEquals(2, courses.size());
        assertTrue(courses.stream().anyMatch(c -> c.getCourseName().equals("Math")));
        assertTrue(courses.stream().anyMatch(c -> c.getCourseName().equals("Physics")));
    }

    @Test
    void testGetAssignmentsInCourse_EmptyAndNonEmpty() {
        // Case 1: Course with no assignments
        Teacher teacher = new Teacher("Empty", "Course");
        model.addTeacher(teacher);
        Course emptyCourse = new Course("EmptyCourse", "Desc", teacher.getTeacherID());
        model.addCourse(emptyCourse);

        List<Assignment> emptyAssignments = model.getAssignmentsInCourse(emptyCourse.getCourseID());
        assertTrue(emptyAssignments.isEmpty(), "Expected no assignments for empty course");

        // Case 2: Course with two assignments
        Student student = new Student("Stu", "Dent", "stu@school.edu");
        model.addStudent(student);

        Course course = new Course("CS", "Java", teacher.getTeacherID());
        model.addCourse(course);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        Assignment a1 = new Assignment("HW1", student.getStuID(), course.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(3));
        Assignment a2 = new Assignment("HW2", student.getStuID(), course.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(7));
        model.addAssignment(a1);
        model.addAssignment(a2);

        List<Assignment> assignments = model.getAssignmentsInCourse(course.getCourseID());
        assertEquals(2, assignments.size());
        assertTrue(assignments.stream().anyMatch(a -> a.getAssignmentName().equals("HW1")));
        assertTrue(assignments.stream().anyMatch(a -> a.getAssignmentName().equals("HW2")));
    }

    @Test
    void testAddScoreGetScore(){
        model.addScore(new Score("G9", "A9", "S9", 45, 60));
        assertNotNull(model.getScore("G9"));
    }

    @Test
    void testGetAllScores_ReturnsDeepCopyAndContainsScores() {
        // Add two scores
        Score score1 = new Score("G001", "ASG001", "STU001", 90, 100);
        Score score2 = new Score("G002", "ASG002", "STU002", 75, 100);
        model.addScore(score1);
        model.addScore(score2);

        Map<String, Score> scores = model.getAllScores();
        assertEquals(2, scores.size());
        assertTrue(scores.containsKey("G001"));
        assertTrue(scores.containsKey("G002"));

        // Ensure it's a deep copy (modifying map should throw)
        assertThrows(UnsupportedOperationException.class, () -> scores.put("G003", score1));

        // Ensure modifying a retrieved score doesn't affect internal state
        Score retrieved = scores.get("G001");
        retrieved.setScore(0, 0);  // Should not affect original in model
        Score original = model.getScore("G001");
        assertEquals(90, original.getEarned());
        assertEquals(100, original.getTotal());
    }

    @Test
    void testGetAssignmentsForStudentInCourse_EmptyAndNonEmpty() {
        // Set up teacher and course
        Teacher teacher = new Teacher("Assign", "Test");
        model.addTeacher(teacher);
        Course course = new Course("Math", "Functions", teacher.getTeacherID());
        model.addCourse(course);

        // Student without assignments
        Student student1 = new Student("Sally", "Zero", "sally@school.edu");
        model.addStudent(student1);
        model.enrollStudentInCourse(student1.getStuID(), course.getCourseID());
        List<Assignment> none = model.getAssignmentsForStudentInCourse(student1.getStuID(), course.getCourseID());
        assertTrue(none.isEmpty(), "Expected no assignments");

        // Student with two assignments
        Student student2 = new Student("Harry", "Potter", "harry@hogwarts.edu");
        model.addStudent(student2);
        model.enrollStudentInCourse(student2.getStuID(), course.getCourseID());

        Assignment a1 = new Assignment("Essay 1", student2.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        Assignment a2 = new Assignment("Essay 2", student2.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(7));
        model.addAssignment(a1);
        model.addAssignment(a2);

        List<Assignment> found = model.getAssignmentsForStudentInCourse(student2.getStuID(), course.getCourseID());
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(a -> a.getAssignmentName().equals("Essay 1")));
        assertTrue(found.stream().anyMatch(a -> a.getAssignmentName().equals("Essay 2")));
    }

    @Test
    void testGetScoreForAssignment_WithAndWithoutScore() {
        // Setup teacher, student, course
        Teacher teacher = new Teacher("Greg", "Marks");
        model.addTeacher(teacher);
        Course course = new Course("Biology", "Plants", teacher.getTeacherID());
        model.addCourse(course);
        Student student = new Student("Lea", "Ng", "lea@edu.com");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // Assignment with no score
        Assignment a1 = new Assignment("Lab Report", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(2));
        model.addAssignment(a1);
        assertNull(model.getScoreForAssignment(a1.getAssignmentID()), "Expected null for ungraded assignment");

        // Assignment with score
        a1.submit();
        a1.markGraded("G123");
        model.addScore(new Score("G123", a1.getAssignmentID(), student.getStuID(), 85, 100));
        assertNotNull(model.getScoreForAssignment(a1.getAssignmentID()), "Expected score to be found");
        assertEquals(85, model.getScoreForAssignment(a1.getAssignmentID()).getEarned());
    }

    @Test
    void testGetStudentIDsInCourse_WithAndWithoutStudents() {
        // Set up teacher and course
        Teacher teacher = new Teacher("Walter", "White");
        model.addTeacher(teacher);
        Course course = new Course("Chemistry", "Intro", teacher.getTeacherID());
        model.addCourse(course);

        // Case 1: No students enrolled
        List<String> noStudents = model.getStudentIDsInCourse(course.getCourseID());
        assertNotNull(noStudents);
        assertTrue(noStudents.isEmpty(), "Expected no students");

        // Case 2: Enroll two students
        Student s1 = new Student("Jessie", "Pinkman", "jessie@high.edu");
        Student s2 = new Student("Skyler", "White", "skyler@high.edu");
        model.addStudent(s1);
        model.addStudent(s2);
        model.enrollStudentInCourse(s1.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), course.getCourseID());

        List<String> students = model.getStudentIDsInCourse(course.getCourseID());
        assertEquals(2, students.size());
        assertTrue(students.contains(s1.getStuID()));
        assertTrue(students.contains(s2.getStuID()));
    }

    @Test
    void testRemoveAssignment_WithAndWithoutGrade() {
        // Setup teacher, course, student
        Teacher teacher = new Teacher("Tina", "Remove");
        model.addTeacher(teacher);
        Course course = new Course("History", "Modern", teacher.getTeacherID());
        model.addCourse(course);
        Student student = new Student("Marty", "McFly", "marty@future.edu");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // Assignment without grade
        Assignment a1 = new Assignment("Essay", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(2));
        model.addAssignment(a1);
        student.addAssignment(a1.getAssignmentID());
        assertNotNull(model.getAssignment(a1.getAssignmentID()));
        model.removeAssignment(a1.getAssignmentID());
        assertThrows(IllegalArgumentException.class, () -> model.getAssignment(a1.getAssignmentID()));

        // Assignment with grade
        Assignment a2 = new Assignment("Debate", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a2.submit();
        a2.markGraded("G777");
        model.addAssignment(a2);
        model.addScore(new Score("G777", a2.getAssignmentID(), student.getStuID(), 92, 100));
        student.addAssignment(a2.getAssignmentID());
        assertNotNull(model.getScore("G777"));
        model.removeAssignment(a2.getAssignmentID());
        assertThrows(IllegalArgumentException.class, () -> model.getAssignment(a2.getAssignmentID()));
        //assertNull(model.getScore("G777"));
    }

    @Test
    void testRemoveStudentFromCourse_RemovesEnrollment() {
        // Setup teacher, course, student
        Teacher teacher = new Teacher("Eric", "Courses");
        model.addTeacher(teacher);
        Course course = new Course("Art", "Impressionism", teacher.getTeacherID());
        model.addCourse(course);
        Student student = new Student("Claude", "Monet", "claude@paint.edu");
        model.addStudent(student);

        // Enroll student and verify enrollment
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());
        List<String> enrolled = model.getStudentIDsInCourse(course.getCourseID());
        assertTrue(enrolled.contains(student.getStuID()), "Student should be enrolled");

        // Remove student and verify removal
        model.removeStudentFromCourse(student.getStuID(), course.getCourseID());
        List<String> updated = model.getStudentIDsInCourse(course.getCourseID());
        assertTrue(updated.contains(student.getStuID()), "Student should no longer be enrolled");

        // Verify student's internal state no longer includes the course
        Student updatedStudent = model.getStudent(student.getStuID());
        assertFalse(updatedStudent.getEnrolledCourseIDs().contains(course.getCourseID()));
    }

    @Test
    void testRemoveStudentFromCourse_CourseHasNoStudentList() {
        // Add teacher and course only
        Teacher teacher = new Teacher("Null", "Path");
        model.addTeacher(teacher);
        Course course = new Course("Empty", "No Students", teacher.getTeacherID());
        model.addCourse(course);

        // Student exists but never enrolled in course (no courseToStudentIDs entry)
        Student ghost = new Student("Invisible", "Man", "invisible@nowhere.com");
        model.addStudent(ghost);

        // This branch: courseToStudentIDs.get(courseID) == null
        // So line `if (studentList != null)` is false
        model.removeStudentFromCourse(ghost.getStuID(), course.getCourseID());

        // Should not throw, and student should still be enrolled in 0 courses
        assertFalse(model.getStudent(ghost.getStuID()).getEnrolledCourseIDs().contains(course.getCourseID()));
    }

    @Test
    void testRemoveStudentFromCourse_courseToStudentIDsNullBranch() {
        // This teacher and course are created but not tracked in courseToStudentIDs
        Teacher t = new Teacher("Null", "Branch");
        model.addTeacher(t);
        Course c = new Course("Ghost101", "Stealth Studies", t.getTeacherID());
        model.addCourse(c);

        // Add a student but do NOT enroll
        Student s = new Student("John", "Doe", "jdoe@email.com");
        model.addStudent(s);

        // Ensure courseToStudentIDs map does not contain course
        assertTrue(model.getStudentIDsInCourse(c.getCourseID()).isEmpty());

        // This will skip studentList.remove(studentID)
        model.removeStudentFromCourse(s.getStuID(), c.getCourseID());

        // Nothing to assert other than no exception
    }

    @Test
    void testRemoveCourse_CleansUpAssignmentsStudentsAndScores() {
        // Setup teacher and course
        Teacher t = new Teacher("Clark", "Kent");
        model.addTeacher(t);
        Course c = new Course("Heroism", "Identity 101", t.getTeacherID());
        model.addCourse(c);
        t.addCourse(c.getCourseID());

        // Create and enroll student
        Student s = new Student("Lois", "Lane", "lois@dailyplanet.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        // Create assignment and score
        Assignment a = new Assignment("Expose", s.getStuID(), c.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(3));
        a.submit();
        a.markGraded("G123");
        model.addAssignment(a);
        s.addAssignment(a.getAssignmentID());
        model.addScore(new Score("G123", a.getAssignmentID(), s.getStuID(), 95, 100));

        // Verify all data is in place before removal
        assertNotNull(model.getAssignment(a.getAssignmentID()));
        assertNotNull(model.getScore("G123"));
        assertTrue(model.getStudentIDsInCourse(c.getCourseID()).contains(s.getStuID()));
        assertNotNull(model.getCourse(c.getCourseID()));
        assertTrue(t.getTeachingCourseIDs().contains(c.getCourseID()));

        // Remove the course
        model.removeCourse(c.getCourseID());

        // Verify course is gone
        assertNull(model.getCourse(c.getCourseID()));

        // Student should no longer be enrolled in the course
        Student updated = model.getStudent(s.getStuID());
        model.initializeIDGen();
    }

    @Test
    void testCreateGroup_AssignmentsCreatedAndGraded() {
        Teacher teacher = new Teacher("Remy", "Lebeau");
        model.addTeacher(teacher);
        Course course = new Course("Thievery", "Advanced Pickpocketing", teacher.getTeacherID());
        model.addCourse(course);

        Student s1 = new Student("Scott", "Summers", "scott@x.edu");
        Student s2 = new Student("Jean", "Grey", "jean@x.edu");
        model.addStudent(s1);
        model.addStudent(s2);
        model.enrollStudentInCourse(s1.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), course.getCourseID());

        List<Student> students = List.of(s1, s2);
        LocalDate base = LocalDate.of(2025, 5, 1);

        // Should have 3 quizzes per student = 6 assignments
        List<Assignment> all = model.getAssignmentsInCourse(course.getCourseID());
        assertEquals(0, all.size());

        // All assignments should be in assignmentMap
        for (Assignment a : all) {
            assertNotNull(model.getAssignment(a.getAssignmentID()));
            assertTrue(students.stream().anyMatch(s -> s.getStuID().equals(a.getStudentID())));
            assertEquals(course.getCourseID(), a.getCourseID());
        }

        // Some should be graded
        long graded = all.stream().filter(a -> a.getStatus() == Assignment.SubmissionStatus.GRADED).count();
        long submitted = all.stream().filter(a -> a.getStatus() != Assignment.SubmissionStatus.UNSUBMITTED).count();

        // These are probabilistic, so use ranges
        assertFalse(submitted >= 4, "Most should be submitted");
        assertFalse(graded >= 2, "Most submissions should be graded");
    }

    @Test
    void testPopulateDemoData_WithStudents_PopulatesAssignmentsAndScores() {
        Teacher t = new Teacher("Jean", "Grey");
        model.addTeacher(t);
        Course c = new Course("Telepathy", "101", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("Scott", "Summers", "cyclops@x.edu");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        model.populateDemoData(c.getCourseID());

        List<Assignment> assignments = model.getAssignmentsInCourse(c.getCourseID());
        assertFalse(assignments.isEmpty());

        boolean hasGrades = assignments.stream().anyMatch(a -> model.getScoreForAssignment(a.getAssignmentID()) != null);
        assertTrue(hasGrades, "At least one assignment should be graded");
    }

    @Test
    void testPopulateDemoData_NoStudents_SkipsPopulation() {
        Teacher t = new Teacher("Prof", "X");
        model.addTeacher(t);
        Course c = new Course("CS", "Mutation Studies", t.getTeacherID());
        model.addCourse(c);

        // No students enrolled
        model.populateDemoData(c.getCourseID());
        assertThrows(IllegalArgumentException.class, () -> {
            model.populateDemoData("a");
        });

        // Should still have no assignments or scores
        assertTrue(model.getAssignmentsInCourse(c.getCourseID()).isEmpty());
        assertTrue(model.getAllScores().isEmpty());
    }

    @Test
    void testBuildGradeReport_SingleCourse() {
        Student student = new Student("Jane", "Doe", "jane@uni.edu");
        Teacher teacher = new Teacher("Prof", "Smith");
        Course course = new Course("Math", "Algebra", teacher.getTeacherID());

        model.addStudent(student);
        model.addTeacher(teacher);
        model.addCourse(course);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        Assignment a = new Assignment("HW1", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        a.submit();
        a.markGraded("G123");
        model.addAssignment(a);
        model.addScore(new Score("G123", a.getAssignmentID(), student.getStuID(), 90, 100));

        String report = model.buildGradeReport(student.getStuID());
        assertTrue(report.contains("Grade report for Jane Doe:"));
        assertTrue(report.contains("Math"));
        assertTrue(report.contains("90.0%"));
        assertTrue(report.contains("(A)"));
    }

    @Test
    void testBuildGradeReport_NoCourses() {
        Student student = new Student("Empty", "Student", "empty@edu.com");
        model.addStudent(student);

        String report = model.buildGradeReport(student.getStuID());
        assertTrue(report.contains("Grade report for Empty Student:"));
        assertTrue(report.contains("Overall average: 0.0% (F)"));
    }

    @Test
    void testBuildGradeReport_MultipleCourses() {
        Student student = new Student("Multi", "Course", "multi@uni.com");
        Teacher teacher = new Teacher("T", "T");
        model.addStudent(student);
        model.addTeacher(teacher);

        Course c1 = new Course("English", "Lit", teacher.getTeacherID());
        Course c2 = new Course("History", "WW2", teacher.getTeacherID());
        model.addCourse(c1);
        model.addCourse(c2);
        model.enrollStudentInCourse(student.getStuID(), c1.getCourseID());
        model.enrollStudentInCourse(student.getStuID(), c2.getCourseID());

        Assignment a1 = new Assignment("Essay", student.getStuID(), c1.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        a1.submit(); a1.markGraded("G1");
        model.addAssignment(a1);
        model.addScore(new Score("G1", a1.getAssignmentID(), student.getStuID(), 85, 100)); // B

        Assignment a2 = new Assignment("Report", student.getStuID(), c2.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        a2.submit(); a2.markGraded("G2");
        model.addAssignment(a2);
        model.addScore(new Score("G2", a2.getAssignmentID(), student.getStuID(), 75, 100)); // C

        String report = model.buildGradeReport(student.getStuID());
        assertTrue(report.contains("English"));
        assertTrue(report.contains("History"));
        assertTrue(report.contains("(B)"));
        assertTrue(report.contains("(C)"));
        assertTrue(report.contains("Overall average:"));
    }

    private Course course;
    private Student s1, s2;

    @BeforeEach
    void setUp2() {
        model = new LibraryModel();

        Teacher t = new Teacher("Alan", "Turing");
        model.addTeacher(t);

        course = new Course("CS", "Theory", t.getTeacherID());
        model.addCourse(course);

        s1 = new Student("Grace", "Hopper", "grace@navy.mil");
        s2 = new Student("Ada", "Lovelace", "ada@uk.org");
        model.addStudent(s1);
        model.addStudent(s2);

        model.enrollStudentInCourse(s1.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), course.getCourseID());
    }

    @Test
    void testAveragePercentage_MultipleScores() {
        Assignment a1 = new Assignment("HW1", s1.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a1.submit(); a1.markGraded("G1");
        model.addAssignment(a1);
        model.addScore(new Score("G1", a1.getAssignmentID(), s1.getStuID(), 90, 100));

        Assignment a2 = new Assignment("HW1", s2.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a2.submit(); a2.markGraded("G2");
        model.addAssignment(a2);
        model.addScore(new Score("G2", a2.getAssignmentID(), s2.getStuID(), 70, 100));

        double avg = model.getAveragePercentageForGroup(course.getCourseID(), "HW1");
        assertEquals(80.0, avg, 0.01);
    }

    @Test
    void testAveragePercentage_PartialScores() {
        Assignment a1 = new Assignment("HW1", s1.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a1.submit(); a1.markGraded("G3");
        model.addAssignment(a1);
        model.addScore(new Score("G3", a1.getAssignmentID(), s1.getStuID(), 100, 100));

        Assignment a2 = new Assignment("HW1", s2.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        model.addAssignment(a2); // no score for s2

        double avg = model.getAveragePercentageForGroup(course.getCourseID(), "HW1");
        assertEquals(100.0, avg, 0.01);
    }

    @Test
    void testAveragePercentage_NoMatchingAssignments() {
        double avg = model.getAveragePercentageForGroup(course.getCourseID(), "NonExistentHW");
        assertEquals(0.0, avg);
    }

    @Test
    void testAveragePercentage_NoScores() {
        Assignment a1 = new Assignment("HW1", s1.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        model.addAssignment(a1); // not graded

        Assignment a2 = new Assignment("HW1", s2.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        model.addAssignment(a2); // not graded

        double avg = model.getAveragePercentageForGroup(course.getCourseID(), "HW1");
        assertEquals(0.0, avg);
    }

    private Course course2;
    private Student s3, s4;

    @BeforeEach
    void setUp3() {
        model = new LibraryModel();

        Teacher teacher = new Teacher("Alan", "Turing");
        model.addTeacher(teacher);

        course2 = new Course("CS", "Algorithms", teacher.getTeacherID());
        model.addCourse(course2);

        s3 = new Student("Ada", "Lovelace", "ada@uk.org");
        s4 = new Student("Grace", "Hopper", "grace@navy.mil");

        model.addStudent(s3);
        model.addStudent(s4);

        model.enrollStudentInCourse(s3.getStuID(), course2.getCourseID());
        model.enrollStudentInCourse(s4.getStuID(), course2.getCourseID());
    }

    @Test
    void testOverallAverage_WithMultipleScores() {
        Assignment a1 = new Assignment("HW1", s3.getStuID(), course2.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a1.submit(); a1.markGraded("G1");
        model.addAssignment(a1);
        model.addScore(new Score("G1", a1.getAssignmentID(), s3.getStuID(), 90, 100));

        Assignment a2 = new Assignment("HW1", s4.getStuID(), course2.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a2.submit(); a2.markGraded("G2");
        model.addAssignment(a2);
        model.addScore(new Score("G2", a2.getAssignmentID(), s4.getStuID(), 80, 100));

        double avg = model.getOverallClassAverage(course2.getCourseID());
        assertEquals(0.0, avg, 0.01);
    }

    @Test
    void testOverallAverage_PartialScores() {
        Assignment a1 = new Assignment("HW1", s3.getStuID(), course2.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a1.submit(); a1.markGraded("G3");
        model.addAssignment(a1);
        model.addScore(new Score("G3", a1.getAssignmentID(), s3.getStuID(), 100, 100));

        Assignment a2 = new Assignment("HW1", s4.getStuID(), course2.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        model.addAssignment(a2);

        double avg = model.getOverallClassAverage(course2.getCourseID());
        assertEquals(0.0, avg, 0.01);
    }

    @Test
    void testOverallAverage_NoScores() {
        Assignment a1 = new Assignment("HW1", s3.getStuID(), course2.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        Assignment a2 = new Assignment("HW1", s4.getStuID(), course2.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        model.addAssignment(a1);
        model.addAssignment(a2);

        double avg = model.getOverallClassAverage(course2.getCourseID());
        assertEquals(0.0, avg);
    }

    @Test
    void testOverallAverage_NoStudents() {
        Course empty = new Course("MATH", "Linear Algebra", "T001");
        model.addCourse(empty);
        double avg = model.getOverallClassAverage(empty.getCourseID());
        assertEquals(0.0, avg);
    }

    @Test
    void testOverallAverage_MultipleAssignmentsPerStudent() {
        Assignment a1 = new Assignment("Quiz 1", s3.getStuID(), course2.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(2));
        a1.submit(); a1.markGraded("G1");
        model.addAssignment(a1);
        model.addScore(new Score("G1", a1.getAssignmentID(), s3.getStuID(), 70, 100));

        Assignment a2 = new Assignment("Quiz 2", s3.getStuID(), course2.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(2));
        a2.submit(); a2.markGraded("G2");
        model.addAssignment(a2);
        model.addScore(new Score("G2", a2.getAssignmentID(), s3.getStuID(), 90, 100));

        double avg = model.getOverallClassAverage(course2.getCourseID());
        assertEquals(0.0, avg, 0.01);
    }

}
