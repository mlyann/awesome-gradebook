package org.fp;

import org.fp.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collection;
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
}
