package org.fp;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    @Test
    void constructorRejectsNullID() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student(null, "First", "Last", "email@example.com"));
    }

    @Test
    void constructorRejectsEmptyID() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student("", "First", "Last", "email@example.com"));
    }

    @Test
    void constructorRejectsNullEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student("STU1", "First", "Last", null));
    }

    @Test
    void constructorRejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student("STU1", "First", "Last", "no-at-symbol"));
    }

    @Test
    void gettersAndFullName() {
        Student s = new Student("STU1", "John", "Doe", "john.doe@example.com");
        assertEquals("STU1", s.getStuID());
        assertEquals("John", s.getFirstName());
        assertEquals("Doe", s.getLastName());
        assertEquals("John Doe", s.getFullName());
        assertEquals("john.doe@example.com", s.getEmail());
    }

    @Test
    void enrollInCourseAddsNonNullNonEmpty() {
        Student s = new Student("STU1", "A", "B", "a@b.com");
        s.enrollInCourse("C1");
        s.enrollInCourse("");
        s.enrollInCourse(null);
        Set<String> courses = s.getEnrolledCourseIDs();
        assertEquals(1, courses.size());
        assertTrue(courses.contains("C1"));
    }

    @Test
    void addAssignmentAddsNonNullNonEmpty() {
        Student s = new Student("STU1", "A", "B", "a@b.com");
        s.addAssignment("A1");
        s.addAssignment("");
        s.addAssignment(null);
        Set<String> asg = s.getAssignmentIDs();
        assertEquals(1, asg.size());
        assertTrue(asg.contains("A1"));
    }

    @Test
    void unmodifiableSetsCannotBeAltered() {
        Student s = new Student("STU1", "A", "B", "a@b.com");
        s.enrollInCourse("C1");
        Set<String> courses = s.getEnrolledCourseIDs();
        assertThrows(UnsupportedOperationException.class, () -> courses.add("X"));
        s.addAssignment("A1");
        Set<String> asg = s.getAssignmentIDs();
        assertThrows(UnsupportedOperationException.class, () -> asg.remove("A1"));
    }

    @Test
    void toStringFormat() {
        Student s = new Student("STU1", "John", "Doe", "john@doe.com");
        String str = s.toString();
        assertTrue(str.contains("John Doe"));
        assertTrue(str.contains("STU1"));
        assertTrue(str.contains("john@doe.com"));
    }

    @Test
    void comparatorsOrderCorrectly() {
        Student s1 = new Student("S1", "Alice", "Brown", "a@x.com");
        Student s2 = new Student("S2", "Bob",   "Adams", "b@y.com");
        // firstName
        assertTrue(Student.firstNameAscendingComparator().compare(s1, s2) < 0);
        assertTrue(Student.firstNameDescendingComparator().compare(s2, s1) < 0);
        // lastName
        assertTrue(Student.lastNameAscendingComparator().compare(s2, s1) < 0);
        assertTrue(Student.lastNameDescendingComparator().compare(s1, s2) < 0);
        // email
        assertTrue(Student.userNameAscendingComparator().compare(s1, s2) < 0);
        assertTrue(Student.userNameDescendingComparator().compare(s2, s1) < 0);
        // TreeSet with comparator
        TreeSet<Student> set = new TreeSet<>(Student.firstNameAscendingComparator());
        set.add(s2);
        set.add(s1);
        assertEquals("Alice", set.first().getFirstName());
    }
}