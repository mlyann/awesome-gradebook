package org.fp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TeacherControllerTest {
    private LibraryModel model;
    private TeacherController tc;
    private String teacherId, stu1, stu2, courseId;
    private Assignment a1, a2;

    @BeforeEach
    void setUp() {
        model = new LibraryModel();
        tc = new TeacherController(model);
        Teacher t = new Teacher("Anna", "Lee");
        // create teacher and set current
        model.addTeacher(t);
        tc.setCurrentTeacher(t.getTeacherID());

        // create course and enroll teacher
        Course c = new Course("History", "Desc", teacherId);
        model.addCourse(c);
        model.getTeacher(teacherId).addCourse(c.getCourseID());
        courseId = c.getCourseID();

        Student s1 = new Student("S", "One", "s1@x.com");
        Student s2 = new Student("S", "Two", "s2@x.com");
        model.addStudent(s1);
        model.addStudent(s2);
        model.enrollStudentInCourse(s1.getStuID(), courseId);
        model.enrollStudentInCourse(s2.getStuID(), courseId);

        // create two assignments for each student
        LocalDate now = LocalDate.of(2025, 4, 1);
        a1 = new Assignment("Asg", stu1, courseId, now, now.plusDays(1));
        a2 = new Assignment("Asg", stu2, courseId, now, now.plusDays(2));
        model.addAssignment(a1);
        model.addAssignment(a2);
        model.getStudent(stu1).addAssignment(a1.getAssignmentID());
        model.getStudent(stu2).addAssignment(a2.getAssignmentID());
    }

    @Test
    void getCurrentTeacherAndInvalid() {
        TeacherController tc2 = new TeacherController(model);
        assertNull(tc2.getCurrentTeacher());
        assertEquals(teacherId, tc.getCurrentTeacher().getTeacherID());
    }

    @Test
    void getStudentMethod() {
        assertEquals(stu1, tc.getStudent(stu1).getStuID());
        assertNull(tc.getStudent("NONEXISTENT"));
    }

    @Test
    void loadTeacherCourses_and_studentsInCourse() {
        tc.loadTeacherCourses();
        assertEquals(1, tc.getCachedCourses().size());
        List<Student> studs = tc.getStudentsInCourse(courseId);
        assertEquals(2, studs.size());
    }

    @Test
    void loadAssignmentsForCourse_and_groupedAssignments() {
        tc.loadAssignmentsForCourse(courseId);
        Map<String, List<Assignment>> grouped0 = tc.getGroupedAssignments(courseId);
        assertTrue(grouped0.containsKey("Asg"));
        assertEquals(2, grouped0.get("Asg").size());
    }

    @Test
    void countSubmittedAndGraded_and_percentage() {
        // none submitted
        assertEquals(0, tc.countSubmitted("A1"));
        assertEquals(0, tc.countGraded("A1"));
        assertEquals(0, tc.getGradedPercentage("A1"));

        // submit and grade one
        a1.submit();
        assertEquals(1, tc.countSubmitted("A1"));
        a1.markGraded("G1");
        model.addScore(new Score("G1", "A1", stu1, 70, 100));
        assertEquals(1, tc.countGraded("A1"));
        assertEquals(100, tc.getGradedPercentage("A1"));
    }

    @Test
    void groupingAndRefreshAndGetAssignmentGroup() {
        Map<String, List<Assignment>> grp0 = tc.getGroupedAssignments(courseId);
        assertTrue(grp0.containsKey("Asg"));

        tc.refreshGroupedAssignments(courseId);
        assertTrue(tc.getGroupedAssignmentCache().isEmpty());

        a1.submit(); a2.submit();
        tc.refreshGroupedAssignments(courseId);
        List<Assignment> group = tc.getAssignmentGroup("Asg");
        assertEquals(2, group.size());
        assertTrue(tc.getAssignmentGroup("X").isEmpty());
    }

    @Test
    void extractAssignmentNumberAndFallback() {
        assertEquals(1, TeacherController.extractAssignmentNumber("Foo 1"));
        assertEquals(Integer.MAX_VALUE, TeacherController.extractAssignmentNumber("FooX"));
    }

    @Test
    void gradedPercentageForGroup() {
        a1.submit(); a2.submit();
        tc.refreshGroupedAssignments(courseId);
        List<Assignment> grp = tc.getAssignmentGroup("Asg");
        assertEquals(0.0, tc.getGradedPercentageForGroup(grp));
        a1.markGraded("G2");
        assertEquals(50.0, tc.getGradedPercentageForGroup(grp));
    }

    @Test
    void sortGroupedAssignmentsAllModesAndFilter() {
        // create additional assignments with distinct names/dates
        LocalDate now = LocalDate.of(2025, 4, 1);
        Assignment b1 = new Assignment("A 2", stu1, courseId, now, now.plusDays(2));
        Assignment b2 = new Assignment("A 10", stu1, courseId, now, now.plusDays(1));
        Assignment b3 = new Assignment("A 1", stu1, courseId, now, now.plusDays(3));
        model.addAssignment(b1); model.addAssignment(b2); model.addAssignment(b3);
        model.getStudent(stu1).addAssignment(b1.getAssignmentID());
        model.getStudent(stu1).addAssignment(b2.getAssignmentID());
        model.getStudent(stu1).addAssignment(b3.getAssignmentID());

        // submit all to include in groupedAssignments
        b1.submit(); b2.submit(); b3.submit();
        tc.refreshGroupedAssignments(courseId);

        // NAME
        tc.sortGroupedAssignments(TeacherController.AssignmentSort.NAME, false);
        assertEquals(List.of("A 1","A 2","A 10"), tc.getSortedAssignmentNames());

        // ASSIGN_DATE (all same) → preserves insertion
        tc.refreshGroupedAssignments(courseId);
        tc.sortGroupedAssignments(TeacherController.AssignmentSort.ASSIGN_DATE, false);
        assertEquals(List.of("A 10","A 1","A 2"), tc.getSortedAssignmentNames());

        // DUE_DATE
        tc.refreshGroupedAssignments(courseId);
        tc.sortGroupedAssignments(TeacherController.AssignmentSort.DUE_DATE, false);
        assertEquals(List.of("A 10","A 2","A 1"), tc.getSortedAssignmentNames());

        // SUBMISSION (all submitted) → preserves insertion
        tc.refreshGroupedAssignments(courseId);
        tc.sortGroupedAssignments(TeacherController.AssignmentSort.SUBMISSION, false);
        assertEquals(List.of("A 10","A 1","A 2"), tc.getSortedAssignmentNames());

        // GRADED_PERCENT
        b1.markGraded("Gx");
        tc.refreshGroupedAssignments(courseId);
        tc.sortGroupedAssignments(TeacherController.AssignmentSort.GRADED_PERCENT, false);
        assertEquals(List.of("A 2","A 10","A 1"), tc.getSortedAssignmentNames());

        // filter removes all-graded&published
        b1.publish(); b2.markGraded("Gy"); b2.publish(); b3.markGraded("Gz"); b3.publish();
        tc.refreshGroupedAssignments(courseId);
        tc.sortGroupedAssignments(TeacherController.AssignmentSort.NAME, true);
        assertTrue(tc.getSortedAssignmentNames().isEmpty());
    }

    @Test
    void setAndGetSelectedAssignmentGroup() {
        a1.submit(); a2.submit();
        tc.refreshGroupedAssignments(courseId);
        tc.setSelectedAssignmentGroup("Asg");
        assertEquals("Asg", tc.getSelectedAssignmentGroup());
        tc.setSelectedAssignmentGroup("X");
        assertNull(tc.getSelectedAssignmentGroup());
    }
}
