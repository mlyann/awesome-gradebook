package org.fp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentControllerTest {
    private LibraryModel model;
    private StudentController ctrl;
    private String stuId, courseId;
    private Assignment a1, a2, aInvalid;

    @BeforeEach
    void setUp() {
        model = new LibraryModel();
        ctrl = new StudentController(model);

        // prepare student and current
        stuId = IDGen.generate("STU");
        model.addStudent(new Student(stuId, "Alice", "Smith", "alice@example.com"));
        ctrl.setCurrentStudent(stuId);

        // prepare teacher, course, enroll student
        model.addTeacher(new Teacher("T1","Teacher","One"));
        ctrl.createCourse("Math","Desc","T1");
        courseId = model.getAllCourses().iterator().next().getCourseID();
        model.enrollStudentInCourse(stuId, courseId);

        // prepare assignments
        LocalDate base = LocalDate.of(2025,4,1);
        a1 = new Assignment("A1","Assignment 1",stuId,courseId,base,base.plusDays(1));
        a2 = new Assignment("A2","Assignment 2",stuId,courseId,base.minusDays(1),base.plusDays(2));
        aInvalid = new Assignment("A3","FooX",stuId,courseId,base,base.plusDays(3));
        model.addAssignment(a1);
        model.addAssignment(a2);
        model.addAssignment(aInvalid);
        model.getStudent(stuId).addAssignment(a1.getAssignmentID());
        model.getStudent(stuId).addAssignment(a2.getAssignmentID());
        model.getStudent(stuId).addAssignment(aInvalid.getAssignmentID());
    }

    @Test
    void testCreateStudentValidAndInvalid() {
        // valid
        ctrl.createStudent("Bob","Brown","bob@domain.com");
        assertTrue(model.getAllStudents().stream().anyMatch(s->s.getEmail().equals("bob@domain.com")));
        // invalid email
        assertThrows(IllegalArgumentException.class, ()->ctrl.createStudent("X","Y","invalid-email"));
    }

    @Test
    void testSetCurrentStudent() {
        StudentController tmp = new StudentController(model);
        tmp.setCurrentStudent("NOPE");
        assertNull(tmp.getCurrentStudent());
        tmp.setCurrentStudent(stuId);
        assertEquals(stuId, tmp.getCurrentStudent().getStuID());
    }

    @Test
    void testLoadStudentCoursesAndFormat() {
        ctrl.loadStudentCourses();
        List<String> list = ctrl.getFormattedCourseListForDisplay();
        assertEquals(1, list.size());
        assertTrue(list.get(0).startsWith("1. Math - Desc"));
    }

    @Test
    void testLoadAssignmentsForCourseGuards() {
        StudentController tmp = new StudentController(model);
        tmp.loadAssignmentsForCourse(courseId);
        assertTrue(tmp.getCachedAssignments().isEmpty());
        // valid
        ctrl.loadAssignmentsForCourse(courseId);
        assertEquals(3, ctrl.getCachedAssignments().size());
    }

    @Test
    void testGetFilteredCachedAssignments() {
        ctrl.loadAssignmentsForCourse(courseId);
        // none submitted → all returned
        List<Assignment> all = ctrl.getFilteredCachedAssignments(false);
        assertEquals(3, all.size());
        // submit one → onlyUnsubmitted filters out submitted
        a1.submit();
        List<Assignment> filtered = ctrl.getFilteredCachedAssignments(true);
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().noneMatch(a->a.getAssignmentID().equals("A1")));
    }

    @Test
    void testGetFormattedAssignmentTableRowsWithState() {
        ctrl.loadAssignmentsForCourse(courseId);
        List<List<String>> rows = ctrl.getFormattedAssignmentTableRowsWithState(false);
        // first row corresponds to a1
        List<String> r1 = rows.stream().filter(r->r.get(0).equals("Assignment 1")).findFirst().orElseThrow();
        assertEquals("⛔", r1.get(1));
        // mark submitted ungraded
        a2.submit();
        List<String> r2 = rows.stream().filter(r->r.get(0).equals("Assignment 2")).findFirst().orElseThrow();
        assertEquals("⛔", r2.get(1));
        // mark graded
        a2.markGraded("G1");
        model.addScore(new Score("G1","A2",stuId,50,100));
        List<List<String>> rows2 = ctrl.getFormattedAssignmentTableRowsWithState(false);
        List<String> r2g = rows2.stream().filter(r->r.get(0).equals("Assignment 2")).findFirst().orElseThrow();
        assertEquals("✅", r2g.get(1));
        assertEquals("50/100", r2g.get(4));
        assertEquals("F", r2g.get(5));
    }

    @Test
    void testSortCachedAssignmentsByNameAndFallback() {
        ctrl.cachedAssignments = new ArrayList<>(List.of(a2, aInvalid, a1));
        ctrl.sortCachedAssignmentsByName();
        List<Assignment> sorted = ctrl.getFilteredCachedAssignments(false);
        assertEquals("A1", sorted.get(0).getAssignmentID());
        assertEquals("A2", sorted.get(1).getAssignmentID());
        assertEquals("A3", sorted.get(2).getAssignmentID()); // fallback last
    }

    @Test
    void testExtractAssignmentNumber() {
        assertEquals(2, ctrl.extractAssignmentNumber("Foo 2"));
        assertEquals(Integer.MAX_VALUE, ctrl.extractAssignmentNumber("FooX"));
    }

    @Test
    void testSortCachedAssignmentsByDates() {
        ctrl.cachedAssignments = new ArrayList<>(List.of(a2, a1, aInvalid));
        ctrl.sortCachedAssignmentsByAssignDate();
        assertEquals("A2", ctrl.getFilteredCachedAssignments(false).get(0).getAssignmentID());
        ctrl.sortCachedAssignmentsByDueDateAscending();
        assertEquals("A1", ctrl.getFilteredCachedAssignments(false).get(0).getAssignmentID());
    }

    @Test
    void testSortCachedAssignmentsByGradeAscendingScenarios() {
        ctrl.cachedAssignments = new ArrayList<>(List.of(a1, a2));
        ctrl.sortCachedAssignmentsByGradeAscending();
        assertEquals(List.of(a1,a2), ctrl.getCachedAssignments());
        a2.submit(); a2.markGraded("G1"); model.addScore(new Score("G1","A2",stuId,88,100));
        ctrl.cachedAssignments = new ArrayList<>(List.of(a1, a2));
        ctrl.sortCachedAssignmentsByGradeAscending();
        assertEquals("A2", ctrl.getFilteredCachedAssignments(false).get(0).getAssignmentID());
        a1.submit(); a1.markGraded("G2"); model.addScore(new Score("G2","A1",stuId,70,100));
        ctrl.cachedAssignments = new ArrayList<>(List.of(a1, a2));
        ctrl.sortCachedAssignmentsByGradeAscending();
        assertEquals("A2", ctrl.getFilteredCachedAssignments(false).get(0).getAssignmentID());
    }
    @Test
    void testSortComplicateCachedAssignmentsByGradeAscendingScenarios() {
        ctrl.cachedAssignments = new ArrayList<>(List.of(a1, a2));
        ctrl.sortCachedAssignmentsByGradeAscending();
        assertEquals(List.of(a1, a2), ctrl.getCachedAssignments());
        a2.submit();
        a2.markGraded("G1");
        model.addScore(new Score("G1", "A2", stuId, 88, 100));
        ctrl.cachedAssignments = new ArrayList<>(List.of(a1, a2));
        ctrl.sortCachedAssignmentsByGradeAscending();
        assertEquals("A2", ctrl.getFilteredCachedAssignments(false).get(0).getAssignmentID());
        a1.submit();
        a1.markGraded("G2");
        model.addScore(new Score("G2", "A1", stuId, 75, 100));
        ctrl.cachedAssignments = new ArrayList<>(List.of(a1, a2));
        ctrl.sortCachedAssignmentsByGradeAscending();
        assertEquals("A2", ctrl.getFilteredCachedAssignments(false).get(0).getAssignmentID());
        ctrl.cachedAssignments = new ArrayList<>(List.of(a1, a2));
        ctrl.sortCachedAssignmentsByGradeAscending();
        assertEquals("A2", ctrl.getFilteredCachedAssignments(false).get(0).getAssignmentID());
    }
    @Test
    void testFormattedAssignmentStateIcons() {
        ctrl.loadAssignmentsForCourse(courseId);
        List<List<String>> rows = ctrl.getFormattedAssignmentTableRowsWithState(false);
        List<String> rowA1 = rows.stream()
                .filter(r -> r.get(0).equals("Assignment 1"))
                .findFirst()
                .orElseThrow();
        assertEquals("⛔", rowA1.get(1));

        a1.submit();
        rows = ctrl.getFormattedAssignmentTableRowsWithState(false);
        rowA1 = rows.stream()
                .filter(r -> r.get(0).equals("Assignment 1"))
                .findFirst()
                .orElseThrow();
        assertEquals("✉️", rowA1.get(1));

        a1.markGraded("GX1");
        model.addScore(new Score("GX1", a1.getAssignmentID(), stuId, 95, 100));
        rows = ctrl.getFormattedAssignmentTableRowsWithState(false);
        rowA1 = rows.stream()
                .filter(r -> r.get(0).equals("Assignment 1"))
                .findFirst()
                .orElseThrow();
        assertEquals("✅", rowA1.get(1));
    }
    @Test
    void bothUngraded_comparatorReturnsZero_keepsOrder() {
        ctrl.cachedAssignments = new ArrayList<>(List.of(a1, a2));
        ctrl.sortCachedAssignmentsByGradeAscending();
        assertEquals("A1", ctrl.getCachedAssignments().get(0).getAssignmentID());
        assertEquals("A2", ctrl.getCachedAssignments().get(1).getAssignmentID());
    }

    @Test
    void onlyA1Ungraded_pushA1Back() {
        a2.submit();
        a2.markGraded("G10");
        model.addScore(new Score("G10", a2.getAssignmentID(), stuId, 85, 100));

        ctrl.cachedAssignments = new ArrayList<>(List.of(a1, a2));
        ctrl.sortCachedAssignmentsByGradeAscending();
        assertEquals("A2", ctrl.getCachedAssignments().get(0).getAssignmentID());
        assertEquals("A1", ctrl.getCachedAssignments().get(1).getAssignmentID());
    }

    @Test
    void loadStudentCourses_whenStudentNull_noExceptionAndNoCourses() {
        StudentController tmp = new StudentController(model);
        tmp.loadStudentCourses();
        assertTrue(tmp.getCachedCourses().isEmpty());
    }
    @Test
    void sortCachedAssignmentsByGrade_onlyA1Ungraded_pushesA1Back() {
        a2.submit();
        a2.markGraded("G1");
        model.addScore(new Score("G1", a2.getAssignmentID(), stuId, 80, 100));

        ctrl.cachedAssignments = new ArrayList<>(List.of(a1, a2));
        ctrl.sortCachedAssignmentsByGradeAscending();
        assertEquals("A2", ctrl.getCachedAssignments().get(0).getAssignmentID());
        assertEquals("A1", ctrl.getCachedAssignments().get(1).getAssignmentID());
    }

}