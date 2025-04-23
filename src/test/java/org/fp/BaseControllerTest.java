package org.fp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BaseControllerTest {

    static class DummyController extends BaseController {
        DummyController(LibraryModel model) { super(model); }
    }

    private LibraryModel model;
    private DummyController ctrl;

    @BeforeEach
    void setUp() {
        model = new LibraryModel();
        ctrl = new DummyController(model);
    }

    @Test
    void nextCourseSortCycles() {
        assertEquals(BaseController.CourseSort.NAME, BaseController.nextCourseSort(BaseController.CourseSort.NONE));
        assertEquals(BaseController.CourseSort.STATUS, BaseController.nextCourseSort(BaseController.CourseSort.NAME));
        assertEquals(BaseController.CourseSort.NONE, BaseController.nextCourseSort(BaseController.CourseSort.STATUS));
    }

    @Test
    void sortCachedCoursesByNoneNameStatus() {
        Course c1 = new Course("B", "beta", "T1");
        Course c2 = new Course("A", "alpha", "T1");
        Course c3 = new Course("C", "gamma", "T1");
        ctrl.cachedCourses = Arrays.asList(c1, c2, c3);

        ctrl.sortCachedCourses(BaseController.CourseSort.NONE);
        assertEquals(List.of(c1, c2, c3), ctrl.getCachedCourses());

        ctrl.sortCachedCourses(BaseController.CourseSort.NAME);
        assertEquals(List.of(c2, c1, c3), ctrl.getCachedCourses());

        ctrl.sortCachedCourses(BaseController.CourseSort.STATUS);
        assertEquals(List.of(c2, c1, c3), ctrl.getCachedCourses()); // alpha, beta, gamma
    }

    @Test
    void getFormattedCourseListForDisplayRows() {
        Course c = new Course("Math", "Algebra", "T1");
        model.addCourse(c);
        ctrl.cachedCourses = List.of(c);
        List<List<String>> rows = ctrl.getFormattedCourseListForDisplayRows();
        assertEquals(1, rows.size());
        assertEquals(List.of("Math", "Algebra"), rows.get(0));
    }

    @Test
    void getCachedCourseAndAssignmentEdgeCases() {
        assertNull(ctrl.getCachedCourse(0));
        assertNull(ctrl.getCachedCourse(-1));
        assertNull(ctrl.getCachedAssignment(0));
        assertNull(ctrl.getCachedAssignment(-1));
    }

    @Test
    void setAndGetCurrentAssignment() {
        Assignment a = new Assignment("A1","HW","S1","C1",
                LocalDate.now(), LocalDate.now().plusDays(1));
        ctrl.setCurrentAssignment(a);
        assertSame(a, ctrl.getCurrentAssignment());
    }

    @Test
    void getScoreForAssignment() {
        Assignment a = new Assignment("HW2", "S2", "C2",
                LocalDate.now(), LocalDate.now().plusDays(1));
        model.addAssignment(a);
        a.submit();
        a.markGraded("G1");
        model.addScore(new Score("G1", "A2", "S2", 80, 100));

        assertEquals(80, ctrl.getScoreForAssignment("A2").getEarned());
        assertNull(ctrl.getScoreForAssignment("X"));
    }
    @Test
    void testGetCachedAssignments() {
        Assignment a1 = new Assignment("HW1", "S1", "C1",
                LocalDate.now(), LocalDate.now().plusDays(1));
        Assignment a2 = new Assignment("HW2", "S2", "C2",
                LocalDate.now(), LocalDate.now().plusDays(2));
        ctrl.cachedAssignments = List.of(a1, a2);
        List<Assignment> fetched = ctrl.getCachedAssignments();
        assertEquals(2, fetched.size());
        assertSame(a1, fetched.get(0));
        assertSame(a2, fetched.get(1));
    }
    @Test
    void testGetCachedCourseBounds() {
        Course c1 = new Course("Math", "Algebra", "T1");
        Course c2 = new Course("Physics", "Mechanics", "T1");
        ctrl.cachedCourses = List.of(c1, c2);

        // valid indices
        assertSame(c1, ctrl.getCachedCourse(0));
        assertSame(c2, ctrl.getCachedCourse(1));

        // out‑of‑bounds: too large
        assertNull(ctrl.getCachedCourse(2));
        assertNull(ctrl.getCachedCourse(100));

        // out‑of‑bounds: negative
        assertNull(ctrl.getCachedCourse(-1));
    }
}