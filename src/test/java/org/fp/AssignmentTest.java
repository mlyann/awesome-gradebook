package org.fp;


import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentTest {
    private Assignment makeAssignment() {
        return new Assignment(
                "Test Assignment",
                "STU00001",
                "C001",
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 4, 10)
        );
    }
    @Test
    void constructorRejectsNullAssignmentID() {
        assertThrows(IllegalArgumentException.class, () ->
                new Assignment( "Name", null, "C1",
                        LocalDate.now(), LocalDate.now()));
    }

    @Test
    void constructorRejectsNullStudentID() {
        assertThrows(IllegalArgumentException.class, () ->
                new Assignment( "Name", null, "C1",
                        LocalDate.now(), LocalDate.now()));
    }

    @Test
    void constructorRejectsNullCourseID() {
        assertThrows(IllegalArgumentException.class, () ->
                new Assignment( "Name", "S1", null,
                        LocalDate.now(), LocalDate.now()));
    }

    @Test
    void constructorRejectsNullAssignDate() {
        assertThrows(IllegalArgumentException.class, () ->
                new Assignment( "Name", "S1", "C1",
                        null, LocalDate.now()));
    }
    @Test
    void constructorRejectsNullDueDate() {
        assertThrows(IllegalArgumentException.class, () ->
                new Assignment( "Name", "S1", "C1",
                        LocalDate.now(), null));
    }
    @Test
    void submitChangesStatusOnce() {
        Assignment a = makeAssignment();
        assertEquals(Assignment.SubmissionStatus.UNSUBMITTED, a.getStatus());

        a.submit();
        assertEquals(Assignment.SubmissionStatus.SUBMITTED_UNGRADED, a.getStatus());

        a.submit();
        assertEquals(Assignment.SubmissionStatus.SUBMITTED_UNGRADED, a.getStatus());
    }

    @Test
    void markGradedThrowsIfNotSubmitted() {
        Assignment a = makeAssignment();
        assertThrows(IllegalStateException.class, () -> a.markGraded("G1"));
    }

    @Test
    void markGradedThrowsIfGradeIDNullOrEmpty() {
        Assignment a = makeAssignment();
        a.submit();
        assertThrows(IllegalArgumentException.class, () -> a.markGraded(null));
        assertThrows(IllegalArgumentException.class, () -> a.markGraded(""));
    }

    @Test
    void markGradedSucceeds() {
        Assignment a = makeAssignment();
        a.submit();
        a.markGraded("G123");
        assertEquals(Assignment.SubmissionStatus.GRADED, a.getStatus());
        assertEquals("G123", a.getGradeID());
    }
    @Test
    void publishThrowsIfNotGraded() {
        Assignment a1 = makeAssignment();
        assertThrows(IllegalStateException.class, a1::publish);

        Assignment a2 = makeAssignment();
        a2.submit();
        assertThrows(IllegalStateException.class, a2::publish);
    }

    @Test
    void publishSucceedsAfterGrading() {
        Assignment a = makeAssignment();
        a.submit();
        a.markGraded("G999");
        a.publish();
        assertTrue(a.isPublished());
        a.publish();
        assertTrue(a.isPublished());
    }
    @Test
    void getDueDateStringMatchesISO() {
        Assignment a = makeAssignment();
        assertEquals("2025-04-10", a.getDueDateString());
    }

    @Test
    void toStringContainsAllFields() {
        Assignment a = makeAssignment();
        String s = a.toString();
        System.out.println(s);
        assertTrue(s.contains("Test Assignment"));
        assertTrue(s.contains("ASG"));
        assertTrue(s.contains("C"));
        assertTrue(s.contains("STU"));
        assertTrue(s.contains("2025-04-01"));
        assertTrue(s.contains("2025-04-10"));
        assertTrue(s.contains("UNSUBMITTED"));
        assertTrue(s.contains("false"));  // published=false
        assertEquals("2025-04-01", a.getAssignDate().toString());
        assertEquals("2025-04-10", a.getDueDate().toString());
    }
}