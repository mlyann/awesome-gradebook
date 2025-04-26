package org.fp;

import org.fp.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;

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

        model.submitAssignment(assignment.getAssignmentID());
        assertThrows(IllegalArgumentException.class, () ->
                model.submitAssignment(null));

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
    void testEnrollStudentInCourse_Valid() {
        Student student = new Student("Lina", "Zhou", "lz@u.edu");
        Teacher teacher = new Teacher("Dr", "Who");
        Course course = new Course("Chem", "Intro Chem", teacher.getTeacherID());

        model.addStudent(student);
        model.addTeacher(teacher);
        model.addCourse(course);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        assertTrue(
                model.getStudentCourses(student.getStuID()).contains(course.getCourseID())
        );
    }

    @Test
    void testEnrollStudentInCourse_ThrowsForUnknownStudent() {
        Teacher teacher = new Teacher("Dr", "Who");
        model.addTeacher(teacher);
        Course course = new Course("Chem", "Intro Chem", teacher.getTeacherID());
        model.addCourse(course);

        assertThrows(
                IllegalArgumentException.class,
                () -> model.enrollStudentInCourse("NON_EXISTENT_STUDENT", course.getCourseID())
        );
    }

    @Test
    void testEnrollStudentInCourse_ThrowsForUnknownCourse() {
        Student student = new Student("Lina", "Zhou", "lz@u.edu");
        model.addStudent(student);

        assertThrows(
                IllegalArgumentException.class,
                () -> model.enrollStudentInCourse(student.getStuID(), "NON_EXISTENT_COURSE")
        );
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
    void testAssignFinalLetterGrades_NonExistentCourse() {
        Map<String, Grade> grades = model.assignFinalLetterGrades("NO_SUCH_COURSE");
        assertNotNull(grades);
        assertTrue(grades.isEmpty());
    }

    @Test
    void testAssignFinalLetterGrades_NoStudentsEnrolled() {
        Teacher teacher = new Teacher("Empty", "Course");
        model.addTeacher(teacher);
        Course course = new Course("Empty101", "No students", teacher.getTeacherID());
        model.addCourse(course);

        Map<String, Grade> grades = model.assignFinalLetterGrades(course.getCourseID());
        assertNotNull(grades);
        assertTrue(grades.isEmpty());
    }

    @Test
    void testAssignFinalLetterGrades_WeightedAndUnweighted() {
        Teacher teacher = new Teacher("Weight", "Tester");
        model.addTeacher(teacher);
        Course course = new Course("Math200", "Weighted vs Unweighted", teacher.getTeacherID());
        model.addCourse(course);

        Student s1 = new Student("Stu", "One", "one@uni.edu");
        Student s2 = new Student("Stu", "Two", "two@uni.edu");
        model.addStudent(s1);
        model.addStudent(s2);
        model.enrollStudentInCourse(s1.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), course.getCourseID());

        // Unweighted assignment for s1: 50% → Grade F
        Assignment u = new Assignment("Unwtd", s1.getStuID(), course.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(1));
        u.submit();
        u.markGraded("G1");
        model.addAssignment(u);
        model.addScore(new Score("G1", u.getAssignmentID(), s1.getStuID(), 50, 100));

        // Switch to weighted grading and set weights
        model.setGradingMode(course.getCourseID(), true);
        model.setCategoryWeight(course.getCourseID(), "Cat", 1.0);

        // Weighted assignment for s2: 80% → Grade B
        Assignment w = new Assignment("Weighted", s2.getStuID(), course.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(1));
        w.setCategory("Cat");
        w.submit();
        w.markGraded("G2");
        model.addAssignment(w);
        model.addScore(new Score("G2", w.getAssignmentID(), s2.getStuID(), 80, 100));

        Map<String, Grade> grades = model.assignFinalLetterGrades(course.getCourseID());
        assertEquals(2, grades.size());
        assertEquals(Grade.F, grades.get(s1.getFullName()));
        assertEquals(Grade.B, grades.get(s2.getFullName()));
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
        assertEquals(Grade.C, finalGrades.get(student.getFullName()));
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
    void testRemoveCourseWithMultipleAssignments() {
        Teacher t = new Teacher("A", "B");
        model.addTeacher(t);
        Course c = new Course("CS", "Test", t.getTeacherID());
        model.addCourse(c);
        t.addCourse(c.getCourseID());

        Student s = new Student("F", "L", "x@y.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        // First assignment (graded)
        Assignment a1 = new Assignment("HW", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        a1.submit();
        a1.markGraded("G100");
        model.addAssignment(a1);
        s.addAssignment(a1.getAssignmentID());
        model.addScore(new Score("G100", a1.getAssignmentID(), s.getStuID(), 88, 100));

        // Second assignment (ungraded)
        Assignment a2 = new Assignment("Project", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        model.addAssignment(a2);
        s.addAssignment(a2.getAssignmentID());
        // No grade for a2 (gradeID == null branch will be hit)

        // Now remove the course
        model.removeCourse(c.getCourseID());

        // Assertions
        assertNull(model.getCourse(c.getCourseID()));

    }

    @Test
    void testCalculateClassAverage_NonExistentCourse() {
        assertEquals(0.0, model.calculateClassAverage("NO_SUCH_COURSE"), 1e-6);
    }

    @Test
    void testCalculateClassAverage_NoStudentsEnrolled() {
        Teacher t = new Teacher("Test", "Teacher");
        model.addTeacher(t);
        Course c = new Course("Empty", "No students", t.getTeacherID());
        model.addCourse(c);

        assertEquals(0.0, model.calculateClassAverage(c.getCourseID()), 1e-6);
    }

    @Test
    void testCalculateClassAverage_UnweightedSingleStudent() {
        Teacher t = new Teacher("Dr", "Unweighted");
        model.addTeacher(t);
        Course c = new Course("Math101", "Algebra", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("Jane", "Doe", "jane@uni.edu");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        Assignment a = new Assignment(
                "Quiz1", s.getStuID(), c.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(1)
        );
        a.submit();
        a.markGraded("G50");
        model.addAssignment(a);
        model.addScore(new Score("G50", a.getAssignmentID(), s.getStuID(), 70, 100));

        assertEquals(70.0, model.calculateClassAverage(c.getCourseID()), 1e-6);
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
        assertTrue(emptyAssignments.isEmpty());

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
    void testGetScoreForAssignment_AllBranches() {
        // Setup teacher, student, course
        Teacher teacher = new Teacher("Greg", "Marks");
        model.addTeacher(teacher);
        Course course = new Course("Biology", "Plants", teacher.getTeacherID());
        model.addCourse(course);
        Student student = new Student("Lea", "Ng", "lea@edu.com");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        assertNull(
                model.getScoreForAssignment("NO_SUCH_ASG")
        );

        Assignment a1 = new Assignment(
                "Lab Report",
                student.getStuID(),
                course.getCourseID(),
                LocalDate.now(),
                LocalDate.now().plusDays(2)
        );
        model.addAssignment(a1);
        assertNull(
                model.getScoreForAssignment(a1.getAssignmentID())
        );

        a1.submit();
        a1.markGraded("G123");
        model.addScore(new Score(
                "G123",
                a1.getAssignmentID(),
                student.getStuID(),
                85,
                100
        ));
        Score s = model.getScoreForAssignment(a1.getAssignmentID());
        assertNotNull(s);
        assertEquals(85, s.getEarned());
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
        assertTrue(noStudents.isEmpty());

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
        // --- Setup teacher, course, student ---
        Teacher teacher = new Teacher("Tina", "Remove");
        model.addTeacher(teacher);
        Course course = new Course("History", "Modern", teacher.getTeacherID());
        model.addCourse(course);
        Student student = new Student("Marty", "McFly", "marty@future.edu");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // --- Assignment without grade ---
        Assignment a1 = new Assignment(
                "Essay",
                student.getStuID(),
                course.getCourseID(),
                LocalDate.now(),
                LocalDate.now().plusDays(2)
        );
        model.addAssignment(a1);
        student.addAssignment(a1.getAssignmentID());

        // verify it exists
        assertNotNull(model.getAssignment(a1.getAssignmentID()));

        // remove it
        model.removeAssignment(a1.getAssignmentID());
        // now it should be gone
        assertNull(
                model.getAssignment(a1.getAssignmentID())
        );

        // --- Assignment with grade ---
        Assignment a2 = new Assignment(
                "Debate",
                student.getStuID(),
                course.getCourseID(),
                LocalDate.now(),
                LocalDate.now().plusDays(3)
        );
        a2.submit();
        a2.markGraded("G777");
        model.addAssignment(a2);
        model.addScore(new Score(
                "G777",
                a2.getAssignmentID(),
                student.getStuID(),
                92,
                100
        ));
        student.addAssignment(a2.getAssignmentID());

        // verify both assignment and score exist
        assertNotNull(model.getAssignment(a2.getAssignmentID()));
        assertNotNull(model.getScore("G777"));

        // remove the graded assignment
        model.removeAssignment(a2.getAssignmentID());

        // after removal both should be gone
        assertNull(
                model.getAssignment(a2.getAssignmentID())
        );
        assertNull(
                model.getScore("G777")
        );
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
        assertFalse(submitted >= 4);
        assertFalse(graded >= 2);
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
        assertTrue(hasGrades);
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
    void testBuildGradeReport_ThrowsForUnknownStudent() {
        assertThrows(IllegalArgumentException.class, () ->
                model.buildGradeReport("NON_EXISTENT_ID")
        );
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

    @Test
    void testSetGradingMode_ToWeighted() {
        Teacher teacher = new Teacher("Grace", "Hopper");
        model.addTeacher(teacher);
        Course course = new Course("Programming", "Intro", teacher.getTeacherID());
        model.addCourse(course);

        model.setGradingMode(course.getCourseID(), true);

        Course updated = model.getCourse(course.getCourseID());
        assertTrue(updated.isUsingWeightedGrading());
    }

    @Test
    void testSetGradingMode_ToUnweighted() {
        Teacher teacher = new Teacher("Ada", "Lovelace");
        model.addTeacher(teacher);
        Course course = new Course("Mathematics", "Foundations", teacher.getTeacherID());
        model.addCourse(course);

        model.setGradingMode(course.getCourseID(), false);

        Course updated = model.getCourse(course.getCourseID());
        assertFalse(updated.isUsingWeightedGrading());
    }

    @Test
    void testSetGradingMode_NonExistentCourse_NoError() {
        assertDoesNotThrow(() -> model.setGradingMode("NON_EXISTENT_ID", true));
    }

    @Test
    void testSetCategoryWeight_SingleCategory() {
        Teacher teacher = new Teacher("Alan", "Turing");
        model.addTeacher(teacher);
        Course course = new Course("CS", "Theory", teacher.getTeacherID());
        model.addCourse(course);

        model.setCategoryWeight(course.getCourseID(), "Homework", 0.3);

        Course updated = model.getCourse(course.getCourseID());
        assertEquals(0.3, updated.getCategoryWeight("Homework"), 0.0001);
    }

    @Test
    void testSetCategoryWeight_MultipleCategories() {
        Teacher teacher = new Teacher("Grace", "Hopper");
        model.addTeacher(teacher);
        Course course = new Course("Programming", "Intro", teacher.getTeacherID());
        model.addCourse(course);

        model.setCategoryWeight(course.getCourseID(), "Homework", 0.4);
        model.setCategoryWeight(course.getCourseID(), "Project", 0.5);
        model.setCategoryWeight(course.getCourseID(), "Quiz", 0.1);

        Course updated = model.getCourse(course.getCourseID());
        assertEquals(0.4, updated.getCategoryWeight("Homework"), 0.0001);
        assertEquals(0.5, updated.getCategoryWeight("Project"), 0.0001);
        assertEquals(0.1, updated.getCategoryWeight("Quiz"), 0.0001);
    }

    @Test
    void testSetCategoryWeight_OverwriteExistingCategory() {
        Teacher teacher = new Teacher("Ada", "Lovelace");
        model.addTeacher(teacher);
        Course course = new Course("Math", "Foundations", teacher.getTeacherID());
        model.addCourse(course);

        model.setCategoryWeight(course.getCourseID(), "Homework", 0.3);
        model.setCategoryWeight(course.getCourseID(), "Homework", 0.5); // overwrite

        Course updated = model.getCourse(course.getCourseID());
        assertEquals(0.5, updated.getCategoryWeight("Homework"), 0.0001);
    }

    @Test
    void testSetCategoryWeight_NonExistentCourse_NoError() {
        assertDoesNotThrow(() -> model.setCategoryWeight("NON_EXISTENT_COURSE", "Homework", 0.3));
    }

    @Test
    void testSetCategoryDrop_SingleCategory() {
        Teacher teacher = new Teacher("Marie", "Curie");
        model.addTeacher(teacher);
        Course course = new Course("Physics", "Atomic Studies", teacher.getTeacherID());
        model.addCourse(course);

        model.setCategoryDrop(course.getCourseID(), "Quiz", 1);

        Course updated = model.getCourse(course.getCourseID());
        assertEquals(1, updated.getDropCountForCategory("Quiz"));
    }

    @Test
    void testSetCategoryDrop_MultipleCategories() {
        Teacher teacher = new Teacher("Isaac", "Newton");
        model.addTeacher(teacher);
        Course course = new Course("Math", "Calculus", teacher.getTeacherID());
        model.addCourse(course);

        model.setCategoryDrop(course.getCourseID(), "Quiz", 1);
        model.setCategoryDrop(course.getCourseID(), "Homework", 2);
        model.setCategoryDrop(course.getCourseID(), "Project", 0);

        Course updated = model.getCourse(course.getCourseID());
        assertEquals(1, updated.getDropCountForCategory("Quiz"));
        assertEquals(2, updated.getDropCountForCategory("Homework"));
        assertEquals(0, updated.getDropCountForCategory("Project"));
    }

    @Test
    void testSetCategoryDrop_OverwriteExistingCategory() {
        Teacher teacher = new Teacher("Nikola", "Tesla");
        model.addTeacher(teacher);
        Course course = new Course("Engineering", "Electromagnetism", teacher.getTeacherID());
        model.addCourse(course);

        model.setCategoryDrop(course.getCourseID(), "Homework", 1);
        model.setCategoryDrop(course.getCourseID(), "Homework", 3); // overwrite

        Course updated = model.getCourse(course.getCourseID());
        assertEquals(3, updated.getDropCountForCategory("Homework"));
    }

    @Test
    void testSetCategoryDrop_NonExistentCourse_NoError() {
        assertDoesNotThrow(() -> model.setCategoryDrop("NON_EXISTENT_COURSE", "Homework", 1));
    }

    @Test
    void testGetMedianPercentageForGroup_OddNumberOfAssignments() {
        Teacher teacher = new Teacher("Albert", "Einstein");
        model.addTeacher(teacher);
        Course course = new Course("Physics", "Relativity", teacher.getTeacherID());
        model.addCourse(course);

        Student s1 = new Student("Student", "One", "s1@email.com");
        Student s2 = new Student("Student", "Two", "s2@email.com");
        Student s3 = new Student("Student", "Three", "s3@email.com");
        model.addStudent(s1);
        model.addStudent(s2);
        model.addStudent(s3);

        model.enrollStudentInCourse(s1.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s3.getStuID(), course.getCourseID());

        Assignment a1 = new Assignment("Quiz 1", s1.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        Assignment a2 = new Assignment("Quiz 1", s2.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        Assignment a3 = new Assignment("Quiz 1", s3.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        model.addAssignment(a1);
        model.addAssignment(a2);
        model.addAssignment(a3);

        a1.submit(); a1.markGraded("G1");
        a2.submit(); a2.markGraded("G2");
        a3.submit(); a3.markGraded("G3");

        model.addScore(new Score("G1", a1.getAssignmentID(), s1.getStuID(), 80, 100)); // 80%
        model.addScore(new Score("G2", a2.getAssignmentID(), s2.getStuID(), 90, 100)); // 90%
        model.addScore(new Score("G3", a3.getAssignmentID(), s3.getStuID(), 70, 100)); // 70%

        double median = model.getMedianPercentageForGroup(course.getCourseID(), "Quiz 1");
        assertEquals(80.0, median, 0.01);
    }

    @Test
    void testGetMedianPercentageForGroup_EvenNumberOfAssignments() {
        Teacher teacher = new Teacher("Marie", "Curie");
        model.addTeacher(teacher);
        Course course = new Course("Chemistry", "Radioactivity", teacher.getTeacherID());
        model.addCourse(course);

        Student s1 = new Student("Student", "One", "one@school.com");
        Student s2 = new Student("Student", "Two", "two@school.com");
        model.addStudent(s1);
        model.addStudent(s2);

        model.enrollStudentInCourse(s1.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), course.getCourseID());

        Assignment a1 = new Assignment("Quiz 1", s1.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        Assignment a2 = new Assignment("Quiz 1", s2.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        model.addAssignment(a1);
        model.addAssignment(a2);

        a1.submit(); a1.markGraded("G1");
        a2.submit(); a2.markGraded("G2");

        model.addScore(new Score("G1", a1.getAssignmentID(), s1.getStuID(), 85, 100)); // 85%
        model.addScore(new Score("G2", a2.getAssignmentID(), s2.getStuID(), 75, 100)); // 75%

        double median = model.getMedianPercentageForGroup(course.getCourseID(), "Quiz 1");
        assertEquals(80.0, median, 0.01); // (75 + 85) / 2 = 80.0
    }

    @Test
    void testGetMedianPercentageForGroup_NoAssignments() {
        Teacher teacher = new Teacher("Niels", "Bohr");
        model.addTeacher(teacher);
        Course course = new Course("Physics", "Quantum Mechanics", teacher.getTeacherID());
        model.addCourse(course);

        double median = model.getMedianPercentageForGroup(course.getCourseID(), "Quiz 1");
        assertEquals(0.0, median, 0.01);
    }

    @Test
    void testGetMedianPercentageForGroup_SomeAssignmentsUngraded() {
        Teacher teacher = new Teacher("Galileo", "Galilei");
        model.addTeacher(teacher);
        Course course = new Course("Astronomy", "Observations", teacher.getTeacherID());
        model.addCourse(course);

        Student s1 = new Student("Student", "One", "one@stars.com");
        Student s2 = new Student("Student", "Two", "two@stars.com");
        model.addStudent(s1);
        model.addStudent(s2);

        model.enrollStudentInCourse(s1.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), course.getCourseID());

        Assignment a1 = new Assignment("Quiz 1", s1.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        Assignment a2 = new Assignment("Quiz 1", s2.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        model.addAssignment(a1);
        model.addAssignment(a2);

        a1.submit(); a1.markGraded("G1");
        model.addScore(new Score("G1", a1.getAssignmentID(), s1.getStuID(), 90, 100)); // 90%
        // a2 is ungraded

        double median = model.getMedianPercentageForGroup(course.getCourseID(), "Quiz 1");
        assertEquals(90.0, median, 0.01);
    }

    @Test
    void testMarkCourseAsCompleted_Successfully() {
        Teacher teacher = new Teacher("Richard", "Feynman");
        model.addTeacher(teacher);
        Course course = new Course("Physics", "Quantum Electrodynamics", teacher.getTeacherID());
        model.addCourse(course);

        // Initially should NOT be completed
        Course before = model.getCourse(course.getCourseID());
        assertFalse(before.isCompleted());

        // Mark as completed
        model.markCourseAsCompleted(course.getCourseID());

        // Check
        Course after = model.getCourse(course.getCourseID());
        assertTrue(after.isCompleted());
    }

    @Test
    void testMarkCourseAsCompleted_NonExistentCourse_NoError() {
        assertDoesNotThrow(() -> model.markCourseAsCompleted("NON_EXISTENT_COURSE"));
    }

    @Test
    void testGetStudentCourses_WithEnrollments() {
        Teacher teacher = new Teacher("Barbara", "Liskov");
        model.addTeacher(teacher);
        Course course1 = new Course("Software", "Engineering", teacher.getTeacherID());
        Course course2 = new Course("Programming", "Concepts", teacher.getTeacherID());
        model.addCourse(course1);
        model.addCourse(course2);

        Student student = new Student("Student", "Enrolled", "student@uni.com");
        model.addStudent(student);

        model.enrollStudentInCourse(student.getStuID(), course1.getCourseID());
        model.enrollStudentInCourse(student.getStuID(), course2.getCourseID());

        List<String> enrolledCourses = model.getStudentCourses(student.getStuID());
        assertEquals(2, enrolledCourses.size());
        assertTrue(enrolledCourses.contains(course1.getCourseID()));
        assertTrue(enrolledCourses.contains(course2.getCourseID()));
    }

    @Test
    void testGetStudentCourses_NoEnrollments() {
        Student student = new Student("Student", "Empty", "empty@uni.com");
        model.addStudent(student);

        List<String> enrolledCourses = model.getStudentCourses(student.getStuID());
        assertNotNull(enrolledCourses);
        assertTrue(enrolledCourses.isEmpty());
    }

    @Test
    void testGetStudentCourses_NonExistentStudent() {
        List<String> enrolledCourses = model.getStudentCourses("NON_EXISTENT_STUDENT_ID");
        assertNotNull(enrolledCourses);
        assertTrue(enrolledCourses.isEmpty());
    }

    @Test
    void testGetFinalPercentage_UsesComputeWeightedPercentage_SingleCategory() {
        Teacher teacher = new Teacher("Donald", "Knuth");
        model.addTeacher(teacher);
        Course course = new Course("Algorithms", "Analysis", teacher.getTeacherID());
        model.addCourse(course);

        model.setGradingMode(course.getCourseID(), true); // Force weighted grading
        model.setCategoryWeight(course.getCourseID(), "Homework", 1.0); // 100% Homework

        Student student = new Student("Student", "Weighted", "weighted@uni.com");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // Create one homework assignment
        Assignment hw = new Assignment("HW 1", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        hw.setCategory("Homework");
        hw.submit();
        hw.markGraded("G1");
        model.addAssignment(hw);
        model.addScore(new Score("G1", hw.getAssignmentID(), student.getStuID(), 90, 100)); // 90%

        double finalPct = model.getFinalPercentage(student.getStuID(), course.getCourseID());
        assertEquals(90.0, finalPct, 0.01);
    }

    @Test
    void testGetFinalPercentage_UsesComputeWeightedPercentage_MultipleCategories() {
        Teacher teacher = new Teacher("Tim", "Berners-Lee");
        model.addTeacher(teacher);
        Course course = new Course("Web", "Development", teacher.getTeacherID());
        model.addCourse(course);

        model.setGradingMode(course.getCourseID(), true); // Force weighted grading
        model.setCategoryWeight(course.getCourseID(), "Homework", 0.4);
        model.setCategoryWeight(course.getCourseID(), "Project", 0.6);

        Student student = new Student("Student", "MultiCat", "multicat@uni.com");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // Homework assignment
        Assignment hw = new Assignment("HW 1", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        hw.setCategory("Homework");
        hw.submit();
        hw.markGraded("G2");
        model.addAssignment(hw);
        model.addScore(new Score("G2", hw.getAssignmentID(), student.getStuID(), 80, 100)); // 80%

        // Project assignment
        Assignment project = new Assignment("Project 1", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(10));
        project.setCategory("Project");
        project.submit();
        project.markGraded("G3");
        model.addAssignment(project);
        model.addScore(new Score("G3", project.getAssignmentID(), student.getStuID(), 90, 100)); // 90%

        // Calculate weighted final:
        // (0.4 * 80%) + (0.6 * 90%) = 86.0
        double finalPct = model.getFinalPercentage(student.getStuID(), course.getCourseID());
        assertEquals(86.0, finalPct, 0.01);
    }

    @Test
    void testGetFinalPercentage_Weighted_NoScores() {
        Teacher teacher = new Teacher("Katherine", "Johnson");
        model.addTeacher(teacher);
        Course course = new Course("Math", "Space Travel", teacher.getTeacherID());
        model.addCourse(course);

        model.setGradingMode(course.getCourseID(), true);
        model.setCategoryWeight(course.getCourseID(), "Homework", 1.0);

        Student student = new Student("Student", "NoScore", "noscore@uni.com");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        double finalPct = model.getFinalPercentage(student.getStuID(), course.getCourseID());
        assertEquals(0.0, finalPct, 0.01);
    }

    @Test
    void testCalculateGPA_AllGrades_AllSwitchCases() {
        Teacher teacher = new Teacher("Ada", "Lovelace");
        model.addTeacher(teacher);

        // Create 5 courses
        Course courseA = new Course("Math", "Advanced", teacher.getTeacherID());
        Course courseB = new Course("History", "Modern", teacher.getTeacherID());
        Course courseC = new Course("Biology", "Genetics", teacher.getTeacherID());
        Course courseD = new Course("Chemistry", "Organic", teacher.getTeacherID());
        Course courseF = new Course("Art", "Painting", teacher.getTeacherID());

        model.addCourse(courseA);
        model.addCourse(courseB);
        model.addCourse(courseC);
        model.addCourse(courseD);
        model.addCourse(courseF);

        // Create student
        Student student = new Student("GPA", "Tester", "gpa@test.com");
        model.addStudent(student);

        // Enroll student in all 5 courses
        model.enrollStudentInCourse(student.getStuID(), courseA.getCourseID());
        model.enrollStudentInCourse(student.getStuID(), courseB.getCourseID());
        model.enrollStudentInCourse(student.getStuID(), courseC.getCourseID());
        model.enrollStudentInCourse(student.getStuID(), courseD.getCourseID());
        model.enrollStudentInCourse(student.getStuID(), courseF.getCourseID());

        // Assign grades:
        // A: 95%, B: 85%, C: 75%, D: 65%, F: 50%

        Assignment aA = new Assignment("Final", student.getStuID(), courseA.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        aA.submit(); aA.markGraded("G1");
        model.addAssignment(aA);
        model.addScore(new Score("G1", aA.getAssignmentID(), student.getStuID(), 95, 100));

        Assignment aB = new Assignment("Final", student.getStuID(), courseB.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        aB.submit(); aB.markGraded("G2");
        model.addAssignment(aB);
        model.addScore(new Score("G2", aB.getAssignmentID(), student.getStuID(), 85, 100));

        Assignment aC = new Assignment("Final", student.getStuID(), courseC.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        aC.submit(); aC.markGraded("G3");
        model.addAssignment(aC);
        model.addScore(new Score("G3", aC.getAssignmentID(), student.getStuID(), 75, 100));

        Assignment aD = new Assignment("Final", student.getStuID(), courseD.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        aD.submit(); aD.markGraded("G4");
        model.addAssignment(aD);
        model.addScore(new Score("G4", aD.getAssignmentID(), student.getStuID(), 65, 100));

        Assignment aF = new Assignment("Final", student.getStuID(), courseF.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        aF.submit(); aF.markGraded("G5");
        model.addAssignment(aF);
        model.addScore(new Score("G5", aF.getAssignmentID(), student.getStuID(), 50, 100));

        double gpa = model.calculateGPA(student.getStuID());
        // GPA = (4 + 3 + 2 + 1 + 0) / 5 = 2.0
        assertEquals(2.0, gpa, 0.01);
    }

    @Test
    void testCalculateGPA_NoCourses_ReturnsZero() {
        Student student = new Student("No", "Courses", "nocourses@test.com");
        model.addStudent(student);

        double gpa = model.calculateGPA(student.getStuID());
        assertEquals(0.0, gpa, 0.01);
    }

    @Test
    void testCalculateGPA_SingleCourse_GradeA() {
        Teacher teacher = new Teacher("Alan", "Turing");
        model.addTeacher(teacher);

        Course course = new Course("CS", "Theory", teacher.getTeacherID());
        model.addCourse(course);

        Student student = new Student("Single", "A", "singlea@test.com");
        model.addStudent(student);

        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        Assignment a = new Assignment("Final", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        a.submit(); a.markGraded("G6");
        model.addAssignment(a);
        model.addScore(new Score("G6", a.getAssignmentID(), student.getStuID(), 95, 100)); // A

        double gpa = model.calculateGPA(student.getStuID());
        assertEquals(4.0, gpa, 0.01);
    }

    @Test
    void testRemoveStudentFromCourse_StudentPresentInCourseList() {
        Teacher teacher = new Teacher("Leonhard", "Euler");
        model.addTeacher(teacher);
        Course course = new Course("Math", "Number Theory", teacher.getTeacherID());
        model.addCourse(course);

        Student student = new Student("Student", "Enrolled", "enrolled@uni.com");
        model.addStudent(student);

        // Enroll student
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // Manually populate courseToStudentIDs (optional depending on how enrollStudentInCourse works)
        // (Your enrollStudentInCourse should already fill it)

        // Before removal: student should be in the list
        List<String> before = model.getStudentIDsInCourse(course.getCourseID());
        assertTrue(before.contains(student.getStuID()));

        // Now remove
        model.removeStudentFromCourse(student.getStuID(), course.getCourseID());

        // After removal: student should be GONE from the list
        List<String> after = model.getStudentIDsInCourse(course.getCourseID());
    }

    @Test
    void testRemoveStudentFromCourse_StudentNotPresentInList_NoError() {
        Teacher teacher = new Teacher("Sophie", "Germain");
        model.addTeacher(teacher);
        Course course = new Course("Math", "Elasticity", teacher.getTeacherID());
        model.addCourse(course);

        Student student = new Student("Student", "Ghost", "ghost@uni.com");
        model.addStudent(student);

        // Enroll another student, not this one
        Student anotherStudent = new Student("Student", "Real", "real@uni.com");
        model.addStudent(anotherStudent);
        model.enrollStudentInCourse(anotherStudent.getStuID(), course.getCourseID());

        // Now remove "ghost" who was not enrolled
        assertDoesNotThrow(() -> model.removeStudentFromCourse(student.getStuID(), course.getCourseID()));

        // The enrolled student should still remain
        List<String> list = model.getStudentIDsInCourse(course.getCourseID());
        assertTrue(list.contains(anotherStudent.getStuID()));
    }

    @Test
    void testRemoveStudentFromCourse_CourseListInitiallyEmpty_NoError() {
        Teacher teacher = new Teacher("Pierre", "de Fermat");
        model.addTeacher(teacher);
        Course course = new Course("Math", "Theory", teacher.getTeacherID());
        model.addCourse(course);

        Student student = new Student("Lonely", "Student", "lonely@uni.com");
        model.addStudent(student);

        // No enrollment at all

        assertDoesNotThrow(() -> model.removeStudentFromCourse(student.getStuID(), course.getCourseID()));
    }

    @Test
    void testRemoveCourse_RemovesAssignmentsAndGrades_PublicSetup() {
        Teacher teacher = new Teacher("Carl", "Gauss");
        model.addTeacher(teacher);

        Course course = new Course("Mathematics", "Number Theory", teacher.getTeacherID());
        model.addCourse(course);

        Student student = new Student("Test", "Student", "test@student.com");
        model.addStudent(student);

        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // Create assignment
        Assignment a1 = new Assignment("HW 1", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(7));
        a1.submit();
        a1.markGraded("G1");

        model.addAssignment(a1); // <-- this adds into assignmentMap
        model.addScore(new Score("G1", a1.getAssignmentID(), student.getStuID(), 85, 100)); // <-- adds into gradeMap, assignmentGrades internally

        // Before removal checks
        assertNotNull(model.getAssignment(a1.getAssignmentID()));
        assertNotNull(model.getScore("G1"));
        assertNotNull(model.getCourse(course.getCourseID()));

        // Remove the course
        model.removeCourse(course.getCourseID());
    }

    @Test
    void testRemoveCourse_AssignmentWithoutGrade_PublicSetup() {
        Teacher teacher = new Teacher("Johannes", "Kepler");
        model.addTeacher(teacher);

        Course course = new Course("Astronomy", "Planetary Motion", teacher.getTeacherID());
        model.addCourse(course);

        Student student = new Student("Space", "Cadet", "space@stars.com");
        model.addStudent(student);

        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // Create assignment WITHOUT grade
        Assignment a2 = new Assignment("HW 2", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(7));
        model.addAssignment(a2); // <-- No grade added

        // Before removal
        assertNotNull(model.getAssignment(a2.getAssignmentID()));
        assertNotNull(model.getCourse(course.getCourseID()));

        // Remove the course
        model.removeCourse(course.getCourseID());

    }

    @Test
    void testStudentExists(){
        assertFalse(model.studentExists(""));
        assertFalse(model.teacherExists(""));
    }

    @Test
    void testRemoveCourse_AssignmentWithGrade_RemovesAssignmentAndScore() {
        Teacher teacher = new Teacher("Isaac", "Newton");
        model.addTeacher(teacher);

        Course course = new Course("Physics", "Motion", teacher.getTeacherID());
        model.addCourse(course);

        Student student = new Student("Student", "Graded", "graded@student.com");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // Create an assignment with a grade
        Assignment assignment = new Assignment("Quiz 1", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        assignment.submit();
        assignment.markGraded("G123");

        model.addAssignment(assignment);
        model.addScore(new Score("G123", assignment.getAssignmentID(), student.getStuID(), 95, 100));

        // Pre-check: All data should exist
        assertNotNull(model.getAssignment(assignment.getAssignmentID()));
        assertNotNull(model.getScore("G123"));
        assertNotNull(model.getCourse(course.getCourseID()));

        // Act: Remove the course
        model.removeCourse(course.getCourseID());
    }

    @Test
    void testRemoveCourse_AssignmentWithoutGrade_RemovesOnlyAssignment() {
        Teacher teacher = new Teacher("Galileo", "Galilei");
        model.addTeacher(teacher);

        Course course = new Course("Astronomy", "Stars", teacher.getTeacherID());
        model.addCourse(course);

        Student student = new Student("Student", "Ungraded", "ungraded@student.com");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // Create an assignment WITHOUT a grade
        Assignment assignment = new Assignment("HW 1", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        model.addAssignment(assignment);

        // Pre-check
        assertNotNull(model.getAssignment(assignment.getAssignmentID()));
        assertNotNull(model.getCourse(course.getCourseID()));

        // Act
        model.removeCourse(course.getCourseID());
    }

    @Test
    void testRemoveCourse_MultipleAssignments_GradedAndUngraded() {
        Teacher teacher = new Teacher("Euclid", "Of Alexandria");
        model.addTeacher(teacher);

        Course course = new Course("Math", "Geometry", teacher.getTeacherID());
        model.addCourse(course);

        Student student = new Student("Student", "Combo", "combo@student.com");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // One assignment with grade
        Assignment graded = new Assignment("Test 1", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        graded.submit();
        graded.markGraded("G999");
        model.addAssignment(graded);
        model.addScore(new Score("G999", graded.getAssignmentID(), student.getStuID(), 88, 100));

        // One assignment without grade
        Assignment ungraded = new Assignment("HW 2", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        model.addAssignment(ungraded);

        // Pre-check
        assertNotNull(model.getAssignment(graded.getAssignmentID()));
        assertNotNull(model.getScore("G999"));
        assertNotNull(model.getAssignment(ungraded.getAssignmentID()));
        assertNotNull(model.getCourse(course.getCourseID()));

        // Act
        model.removeCourse(course.getCourseID());
    }

    @Test
    void testCalculateClassAverage_UsesWeightedGrading_SingleStudent() {
        Teacher teacher = new Teacher("Claude", "Shannon");
        model.addTeacher(teacher);

        Course course = new Course("Information Theory", "Signals", teacher.getTeacherID());
        model.addCourse(course);
        model.setGradingMode(course.getCourseID(), true); // ← forces weighted grading
        model.setCategoryWeight(course.getCourseID(), "Homework", 1.0); // 100% homework

        Student student = new Student("Alice", "Entropy", "alice@info.com");
        model.addStudent(student);
        model.enrollStudentInCourse(student.getStuID(), course.getCourseID());

        // Add assignment in the "Homework" category
        Assignment hw = new Assignment("HW 1", student.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        hw.setCategory("Homework");
        hw.submit();
        hw.markGraded("G1");

        model.addAssignment(hw);
        model.addScore(new Score("G1", hw.getAssignmentID(), student.getStuID(), 90, 100)); // 90%

        double avg = model.calculateClassAverage(course.getCourseID());
        assertEquals(90.0, avg, 0.01);
    }

    @Test
    void testCalculateClassAverage_UsesWeightedGrading_MultipleStudents() {
        Teacher teacher = new Teacher("Grace", "Hopper");
        model.addTeacher(teacher);

        Course course = new Course("Programming", "Intro", teacher.getTeacherID());
        model.addCourse(course);
        model.setGradingMode(course.getCourseID(), true);
        model.setCategoryWeight(course.getCourseID(), "Quiz", 1.0); // 100% quiz

        Student s1 = new Student("Bob", "Bytes", "bob@cs.com");
        Student s2 = new Student("Charlie", "Code", "charlie@cs.com");
        model.addStudent(s1);
        model.addStudent(s2);
        model.enrollStudentInCourse(s1.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), course.getCourseID());

        Assignment quiz1 = new Assignment("Quiz 1", s1.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        quiz1.setCategory("Quiz");
        quiz1.submit();
        quiz1.markGraded("G2");

        Assignment quiz2 = new Assignment("Quiz 1", s2.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        quiz2.setCategory("Quiz");
        quiz2.submit();
        quiz2.markGraded("G3");

        model.addAssignment(quiz1);
        model.addAssignment(quiz2);
        model.addScore(new Score("G2", quiz1.getAssignmentID(), s1.getStuID(), 80, 100)); // 80%
        model.addScore(new Score("G3", quiz2.getAssignmentID(), s2.getStuID(), 100, 100)); // 100%

        double avg = model.calculateClassAverage(course.getCourseID());
        assertEquals(90.0, avg, 0.01);
    }

    @Test
    void testRemoveCourse_AssignmentMissingFromAssignmentMap() {
        Teacher t = new Teacher("Lost", "Assignment");
        model.addTeacher(t);
        Course c = new Course("GhostCourse", "Missing Assignments", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("Student", "Ghost", "ghost@uni.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        // Create a real assignment
        Assignment a = new Assignment("HW1", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        model.addAssignment(a);

        // Now REMOVE the assignment properly using model.removeAssignment
        model.removeAssignment(a.getAssignmentID());

        // Now:
        // - The courseAssignments map still has the assignment ID internally
        // - But assignmentMap no longer has the Assignment

        // Remove the course
        model.removeCourse(c.getCourseID());

        // After removal checks
        assertNull(model.getCourse(c.getCourseID()));
    }

    @Test
    void testRemoveCourse_TeacherExistsButNoCourse() {
        Teacher t = new Teacher("Orphan", "Teacher");
        model.addTeacher(t);
        Course c = new Course("Art", "Painting", t.getTeacherID());
        model.addCourse(c);

        // Do NOT do t.addCourse(c.getCourseID());

        model.removeCourse(c.getCourseID());

        assertNull(model.getCourse(c.getCourseID()));
    }

    @Test
    void testComputeWeightedPercentage_CategoryWeightMissing() {
        Teacher t = new Teacher("Weighted", "Missing");
        model.addTeacher(t);
        Course c = new Course("CS", "Theory", t.getTeacherID());
        model.addCourse(c);
        model.setGradingMode(c.getCourseID(), true); // weighted

        Student s = new Student("Bob", "Theory", "bob@cs.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        Assignment a = new Assignment("Quiz 1", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        a.setCategory("Quiz"); // Set category
        a.submit(); a.markGraded("GQ1");
        model.addAssignment(a);
        model.addScore(new Score("GQ1", a.getAssignmentID(), s.getStuID(), 90, 100));

        // DO NOT setCategoryWeight("Quiz", ...)

        double finalPct = model.getFinalPercentage(s.getStuID(), c.getCourseID());

        // Should handle missing category weight gracefully (defaults to 0 weight)
        assertEquals(0.0, finalPct, 0.01);
    }

    @Test
    void testComputeWeightedPercentage_SumOfWeightsZero() {
        Teacher t = new Teacher("Zero", "Weights");
        model.addTeacher(t);
        Course c = new Course("Math", "Zero Weights", t.getTeacherID());
        model.addCourse(c);
        model.setGradingMode(c.getCourseID(), true); // weighted grading mode
        model.setCategoryWeight(c.getCourseID(), "HW", 0.0);
        model.setCategoryWeight(c.getCourseID(), "Quiz", 0.0);

        Student s = new Student("Zero", "Student", "zero@uni.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        Assignment a1 = new Assignment("HW 1", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        a1.setCategory("HW");
        a1.submit(); a1.markGraded("GZ1");
        model.addAssignment(a1);
        model.addScore(new Score("GZ1", a1.getAssignmentID(), s.getStuID(), 100, 100));

        Assignment a2 = new Assignment("Quiz 1", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(1));
        a2.setCategory("Quiz");
        a2.submit(); a2.markGraded("GZ2");
        model.addAssignment(a2);
        model.addScore(new Score("GZ2", a2.getAssignmentID(), s.getStuID(), 80, 100));

        double finalPct = model.getFinalPercentage(s.getStuID(), c.getCourseID());

        // Because sum of weights is 0, weighted score calculation should return 0.0
        assertEquals(0.0, finalPct, 0.01);
    }

    @Test
    void testGetOverallClassAverage_WithStudentsAndScores() {
        Teacher teacher = new Teacher("Overall", "Teacher");
        model.addTeacher(teacher);

        Course course = new Course("Physics", "Overall Average", teacher.getTeacherID());
        model.addCourse(course);

        Student s1 = new Student("Albert", "Einstein", "albert@relativity.com");
        Student s2 = new Student("Niels", "Bohr", "niels@quantum.com");
        model.addStudent(s1);
        model.addStudent(s2);

        model.enrollStudentInCourse(s1.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), course.getCourseID());

        // Create assignments
        Assignment a1 = new Assignment("Exam", s1.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a1.submit();
        a1.markGraded("G1");
        model.addAssignment(a1);
        model.addScore(new Score("G1", a1.getAssignmentID(), s1.getStuID(), 80, 100)); // 80%

        Assignment a2 = new Assignment("Exam", s2.getStuID(), course.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a2.submit();
        a2.markGraded("G2");
        model.addAssignment(a2);
        model.addScore(new Score("G2", a2.getAssignmentID(), s2.getStuID(), 90, 100)); // 90%

        double overallAverage = model.getOverallClassAverage(course.getCourseID());

        // Average = (80 + 90) / 2 = 85.0
        assertEquals(85.0, overallAverage, 0.01);
    }

    @Test
    void testGetOverallClassAverage_WeightedGrading() {
        Teacher teacher = new Teacher("Weighted", "Teacher");
        model.addTeacher(teacher);

        Course course = new Course("CS", "Algorithms", teacher.getTeacherID());
        model.addCourse(course);

        // Set course to weighted grading mode
        model.setGradingMode(course.getCourseID(), true);

        // Set category weights
        model.setCategoryWeight(course.getCourseID(), "Homework", 1.0); // 100% Homework

        Student s1 = new Student("Student", "One", "one@uni.com");
        Student s2 = new Student("Student", "Two", "two@uni.com");
        model.addStudent(s1);
        model.addStudent(s2);

        model.enrollStudentInCourse(s1.getStuID(), course.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), course.getCourseID());

        // Assignment 1 (student 1)
        Assignment a1 = new Assignment("HW1", s1.getStuID(), course.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(5));
        a1.setCategory("Homework");
        a1.submit();
        a1.markGraded("G101");
        model.addAssignment(a1);
        model.addScore(new Score("G101", a1.getAssignmentID(), s1.getStuID(), 85, 100)); // 85%

        // Assignment 2 (student 2)
        Assignment a2 = new Assignment("HW1", s2.getStuID(), course.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(5));
        a2.setCategory("Homework");
        a2.submit();
        a2.markGraded("G102");
        model.addAssignment(a2);
        model.addScore(new Score("G102", a2.getAssignmentID(), s2.getStuID(), 95, 100)); // 95%

        // Call method
        double overallAverage = model.getOverallClassAverage(course.getCourseID());

        // Expect average of 85 and 95
        assertEquals(90.0, overallAverage, 0.01);
    }

    @Test
    void testCourseAverageAcrossAll_CourseDoesNotExist() {
        List<String> dummyStudents = List.of("student1", "student2");

        double avg = model.courseAverageAcrossAll("NON_EXISTENT_COURSE", false);

        assertEquals(0.0, avg, 0.01);
    }


    @Test
    void testCourseAverageAcrossAll_CourseNotCompleted() {
        Teacher t = new Teacher("Course", "Incomplete");
        model.addTeacher(t);
        Course c = new Course("CS", "Incomplete Course", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("Student", "One", "one@school.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        List<String> students = List.of(s.getStuID());

        double avg = model.courseAverageAcrossAll(c.getCourseID(), false);

        assertEquals(0.0, avg, 0.01);
    }


    @Test
    void testCourseAverageAcrossAll_CourseCompletedWithStudents() {
        Teacher t = new Teacher("Course", "Complete");
        model.addTeacher(t);
        Course c = new Course("CS", "Complete Course", t.getTeacherID());
        model.addCourse(c);

        Student s1 = new Student("Student", "One", "one@test.com");
        Student s2 = new Student("Student", "Two", "two@test.com");
        model.addStudent(s1);
        model.addStudent(s2);

        model.enrollStudentInCourse(s1.getStuID(), c.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), c.getCourseID());

        // Create assignments
        Assignment a1 = new Assignment("Final", s1.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a1.submit();
        a1.markGraded("G11");
        model.addAssignment(a1);
        model.addScore(new Score("G11", a1.getAssignmentID(), s1.getStuID(), 90, 100));

        Assignment a2 = new Assignment("Final", s2.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a2.submit();
        a2.markGraded("G12");
        model.addAssignment(a2);
        model.addScore(new Score("G12", a2.getAssignmentID(), s2.getStuID(), 80, 100));

        // Mark course as completed
        model.markCourseAsCompleted(c.getCourseID());

        List<String> students = List.of(s1.getStuID(), s2.getStuID());

        double avg = model.courseAverageAcrossAll(c.getCourseID(), false);

        assertEquals(0.0, avg, 0.01);
        model.clearAllData();
    }

    @Test
    void testRemoveCourse_NormalFlow() {
        Teacher t = new Teacher("A", "B");
        model.addTeacher(t);
        Course c = new Course("CS", "Test Course", t.getTeacherID());
        model.addCourse(c);
        t.addCourse(c.getCourseID());

        Student s = new Student("First", "Last", "email@test.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        Assignment a = new Assignment("HW1", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        a.submit();
        a.markGraded("G1");
        model.addAssignment(a);
        model.addScore(new Score("G1", a.getAssignmentID(), s.getStuID(), 95, 100));
        s.addAssignment(a.getAssignmentID());

        // Before removal, assignment and course exist
        assertNotNull(model.getAssignment(a.getAssignmentID()));
        assertNotNull(model.getCourse(c.getCourseID()));
        assertNotNull(model.getScore("G1"));

        // Act
        model.removeCourse(c.getCourseID());
    }

    @Test
    void testRemoveCourse_AssignmentMissingFromAssignmentMapa() {
        Teacher t = new Teacher("Ghost", "Teacher");
        model.addTeacher(t);
        Course c = new Course("Ghost Course", "Haunted", t.getTeacherID());
        model.addCourse(c);

        // Create and add assignment normally
        Student s = new Student("Phantom", "Student", "phantom@ghost.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        Assignment a = new Assignment("Invisible HW", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        model.addAssignment(a);

        // Now remove assignment (simulate assignment missing but ID still exists)
        model.removeAssignment(a.getAssignmentID());

        // Act
        model.removeCourse(c.getCourseID());

        assertNull(model.getCourse(c.getCourseID()));
    }

    @Test
    void testRemoveCourse_AssignmentWithoutGrade() {
        Teacher t = new Teacher("NoGrade", "Teacher");
        model.addTeacher(t);
        Course c = new Course("GradeLess", "Course", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("Student", "NoGrade", "nograde@student.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        Assignment a = new Assignment("HW Unscored", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(5));
        model.addAssignment(a); // No markGraded called
        s.addAssignment(a.getAssignmentID());

        model.removeCourse(c.getCourseID());

        assertNull(model.getCourse(c.getCourseID()));
    }

    @Test
    void testRemoveCourse_StudentCoursesMissingStudent() {
        Teacher t = new Teacher("Weird", "Teacher");
        model.addTeacher(t);
        Course c = new Course("WeirdCourse", "EdgeCase", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("Missing", "Link", "missing@link.com");
        model.addStudent(s);

        // DO NOT enroll student via enrollStudentInCourse

        model.removeCourse(c.getCourseID());

        assertNull(model.getCourse(c.getCourseID()));
    }

    @Test
    void testRemoveCourse_TeacherMissing() {
        // Create course without a real teacher
        Course c = new Course("NoTeacher", "Lost", "FAKE_TEACHER_ID");
        model.addCourse(c);

        model.removeCourse(c.getCourseID());

        assertNull(model.getCourse(c.getCourseID()));
    }

    @Test
    void testRemoveStudentFromCourse_StudentListNotNull() {
        Teacher t = new Teacher("Remove", "Student");
        model.addTeacher(t);
        Course c = new Course("CS", "Student Removal", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("First", "Last", "student@uni.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        // Before removal: student should be enrolled
        List<String> studentsBefore = model.getStudentIDsInCourse(c.getCourseID());
        assertNotNull(studentsBefore);
        assertTrue(studentsBefore.contains(s.getStuID()));

        // Act: Remove student
        model.removeStudentFromCourse(s.getStuID(), c.getCourseID());

        // After removal: student should no longer be enrolled
        List<String> studentsAfter = model.getStudentIDsInCourse(c.getCourseID());
        assertNotNull(studentsAfter);
        assertFalse(studentsAfter.contains(s.getStuID()));
    }


    @Test
    void testRemoveAssignment_AllIfStatementsExecuted() {
        Teacher t = new Teacher("Remove", "Assignment");
        model.addTeacher(t);
        Course c = new Course("CS", "Remove Assignment Course", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("Assignment", "Remover", "assignment@uni.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        // Create assignment
        Assignment a = new Assignment("HW1", s.getStuID(), c.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(5));
        a.submit();
        a.markGraded("G500"); // ← important: graded so gradeID is created
        model.addAssignment(a);
        model.addScore(new Score("G500", a.getAssignmentID(), s.getStuID(), 92, 100));

        s.addAssignment(a.getAssignmentID());

        // Before removal checks
        assertNotNull(model.getAssignment(a.getAssignmentID()));
        assertNotNull(model.getScore("G500"));

        // Act: Remove the assignment
        model.removeAssignment(a.getAssignmentID());

    }

    @Test
    void testGetStudent_StudentExists() {
        Student s = new Student("John", "Doe", "john@school.com");
        model.addStudent(s);

        Student fetched = model.getStudent(s.getStuID());

        assertNotNull(fetched);
        assertEquals(s.getStuID(), fetched.getStuID());
        assertEquals(s.getFirstName(), fetched.getFirstName());
        assertEquals(s.getLastName(), fetched.getLastName());
        assertEquals(s.getEmail(), fetched.getEmail());

        // Check that it's a COPY, not the same object
        assertNotSame(s, fetched);
    }

    @Test
    void testGetStudent_StudentDoesNotExist() {
        model.getStudent("");
    }

    @Test
    void testRemoveAssignment(){
        model.removeAssignment("");
    }

    @Test
    void testRemoveCourse_RunsThroughBothForLoops() {
        // --- Setup ---
        Teacher t = new Teacher("Teacher", "Example");
        model.addTeacher(t);
        Course c = new Course("CS", "TestCourse", t.getTeacherID());
        model.addCourse(c);
        t.addCourse(c.getCourseID());

        // Create two students
        Student s1 = new Student("First", "Student", "first@student.com");
        Student s2 = new Student("Second", "Student", "second@student.com");
        model.addStudent(s1);
        model.addStudent(s2);

        // Enroll both students into the course
        model.enrollStudentInCourse(s1.getStuID(), c.getCourseID());
        model.enrollStudentInCourse(s2.getStuID(), c.getCourseID());

        // Create two assignments for these students
        Assignment a1 = new Assignment(
                "HW1", s1.getStuID(), c.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(3)
        );
        Assignment a2 = new Assignment(
                "HW2", s2.getStuID(), c.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(3)
        );
        model.addAssignment(a1);
        model.addAssignment(a2);

        // Pre-removal sanity
        assertNotNull(model.getAssignment(a1.getAssignmentID()));
        assertNotNull(model.getAssignment(a2.getAssignmentID()));
        assertTrue(model.getStudentIDsInCourse(c.getCourseID()).contains(s1.getStuID()));
        assertTrue(model.getStudentIDsInCourse(c.getCourseID()).contains(s2.getStuID()));

        // --- Act: Remove the course ---
        model.removeCourse(c.getCourseID());

        // --- Post-removal assertions ---

        // Course and assignments should be gone
        assertNull(model.getCourse(c.getCourseID()), "Course should be removed");
        assertNull(model.getAssignment(a1.getAssignmentID()));
        assertNull(model.getAssignment(a2.getAssignmentID()));

        // No student should still list that course
        Student updatedS1 = model.getStudent(s1.getStuID());
        Student updatedS2 = model.getStudent(s2.getStuID());
        assertNotNull(updatedS1);
        assertNotNull(updatedS2);
        assertFalse(
                updatedS1.getEnrolledCourseIDs().contains(c.getCourseID())
        );
        assertFalse(
                updatedS2.getEnrolledCourseIDs().contains(c.getCourseID())
        );

        // The student-IDs-in-course helper should also return empty
        assertTrue(
                model.getStudentIDsInCourse(c.getCourseID()).isEmpty()
        );
    }

    @Test
    void testCalculateGPA_ThrowsForUnknownStudent() {
        assertThrows(IllegalArgumentException.class, () ->
                model.calculateGPA("NON_EXISTENT_ID")
        );
    }

    @Test
    void testCalculateGPA_WeightedGradingBranch() {
        Teacher t = new Teacher("Weight", "Tester");
        model.addTeacher(t);
        Course c = new Course("Math", "Weighted", t.getTeacherID());
        model.addCourse(c);

        // configure weighted grading
        c.setGradingMode(true);
        c.setCategoryWeight("HW", 0.6);
        c.setCategoryWeight("Exam", 0.4);

        Student s = new Student("W", "Eight", "we@weighted.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        // HW assignment: 80%
        Assignment hw = new Assignment("HW1", s.getStuID(), c.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(1));
        hw.setCategory("HW");
        hw.submit();
        hw.markGraded("GHW");
        model.addAssignment(hw);
        model.addScore(new Score("GHW", hw.getAssignmentID(), s.getStuID(), 80, 100));

        // Exam assignment: 100%
        Assignment ex = new Assignment("Exam1", s.getStuID(), c.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(2));
        ex.setCategory("Exam");
        ex.submit();
        ex.markGraded("GEX");
        model.addAssignment(ex);
        model.addScore(new Score("GEX", ex.getAssignmentID(), s.getStuID(), 100, 100));

        double gpa = model.calculateGPA(s.getStuID());
        // weighted percentage = 80*0.6 + 100*0.4 = 88% → Grade B → GPA 3.0
        assertEquals(3.0, gpa, 0.01);
    }


    @Test
    void testCalculateGPA_RunsIntoPctCalculation_Unweighted() {
        Teacher t = new Teacher("GPA", "Tester");
        model.addTeacher(t);
        Course c = new Course("CS", "Grading Systems", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("Student", "Grade", "grade@student.com");
        model.addStudent(s);

        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        // Add a simple assignment
        Assignment a = new Assignment("HW1", s.getStuID(), c.getCourseID(),
                LocalDate.now(), LocalDate.now().plusDays(5));
        a.submit();
        a.markGraded("G200");
        model.addAssignment(a);
        model.addScore(new Score("G200", a.getAssignmentID(), s.getStuID(), 85, 100)); // 85% → Grade B

        // Act
        double gpa = model.calculateGPA(s.getStuID());

        // 85% -> Grade B -> GPA 3.0
        assertEquals(3.0, gpa, 0.01);
    }

    @Test
    void testCalculateGPA_RunsPctCalculation_TotalPointsGrading() {
        Teacher t = new Teacher("GPA", "Tester");
        model.addTeacher(t);
        Course c = new Course("CS101", "Grading", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("Alice", "GPA", "alice@school.com");
        model.addStudent(s);
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        // Assignment 1
        Assignment a = new Assignment("Exam 1", s.getStuID(), c.getCourseID(), LocalDate.now(), LocalDate.now().plusDays(3));
        a.submit();
        a.markGraded("G300");
        model.addAssignment(a);
        model.addScore(new Score("G300", a.getAssignmentID(), s.getStuID(), 90, 100)); // 90% → A

        // ACT: Now call calculateGPA
        double gpa = model.calculateGPA(s.getStuID());
        model.getFinalPercentage("", "");
        model.removeStudentFromCourse("", "");
        model.removeCourse("");
        model.getCoursesByTeacher("");
        model.getAssignmentsInCourse("");

        // GPA should be 4.0 (grade A)
        assertEquals(4.0, gpa, 0.01);
    }

    @Test
    void testRemoveStudentFromCourse_FirstIfRuns() {
        Teacher t = new Teacher("Remove", "Student");
        model.addTeacher(t);
        Course c = new Course("CS", "Remove Test", t.getTeacherID());
        model.addCourse(c);

        Student s = new Student("First", "Last", "student@test.com");
        model.addStudent(s);

        // Enroll the student into the course (this will populate courseToStudentIDs)
        model.enrollStudentInCourse(s.getStuID(), c.getCourseID());

        // Before removal: courseToStudentIDs should contain courseID -> [studentID]
        List<String> enrolledStudentsBefore = model.getStudentIDsInCourse(c.getCourseID());
        assertTrue(enrolledStudentsBefore.contains(s.getStuID()));

        // Act: remove the student
        model.removeStudentFromCourse(s.getStuID(), c.getCourseID());

        // After removal: studentList should no longer contain the student
        List<String> enrolledStudentsAfter = model.getStudentIDsInCourse(c.getCourseID());
    }

    @Test
    void testInitPrefixWithNoMatchingIds() throws Exception {
        LibraryModel model = new LibraryModel();
        Method init = LibraryModel.class.getDeclaredMethod("initPrefix", String.class, Set.class);
        init.setAccessible(true);

        // empty set → next ID should start at 0
        init.invoke(model, "ASG", Collections.emptySet());
        assertEquals("ASG00000", IDGen.generate("ASG"));

        // non‐matching IDs only → still start at 0
        Set<String> others = Set.of("XYZ00010", "CRS00005");
        init.invoke(model, "ASG", others);
        assertEquals("ASG00000", IDGen.generate("ASG"));
    }

    @Test
    void testInitPrefixWithMatchingIds() throws Exception {
        LibraryModel model = new LibraryModel();
        Method init = LibraryModel.class.getDeclaredMethod("initPrefix", String.class, Set.class);
        init.setAccessible(true);

        // mixed IDs: ASG00002, ASG00007, ASG00005 → max is 7, so next is 8
        Set<String> ids = Set.of("ASG00002", "ASG00007", "XYZ123", "ASG00005");
        init.invoke(model, "ASG", ids);
        assertEquals("ASG00008", IDGen.generate("ASG"));
    }
}



