package org.fp;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

class StudentTest {

    @Test
    void constructorRejectsBadEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student( "First", "Last", "emailexample.com"));
        assertThrows(IllegalArgumentException.class,
                () -> new Student( "First", "Last", null));
    }

    @Test
    void constructorRejectsInvalidEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student( "First", "Last", "no-at-symbol"));
    }

    @Test
    void gettersAndFullName() {
        Student s = new Student( "John", "Doe", "john.doe@example.com");
        assertTrue(s.getStuID().contains("STU"));
        assertEquals("John", s.getFirstName());
        assertEquals("Doe", s.getLastName());
        assertEquals("John Doe", s.getFullName());
        assertEquals("john.doe@example.com", s.getEmail());
    }

    @Test
    void testCopyConstructor(){
        Student stu = new Student( "John", "Doe", "john.doe@example.com");
        Student s = new Student(stu);
        assertTrue(s.getStuID().contains("STU"));
        assertEquals("John", s.getFirstName());
        assertEquals("Doe", s.getLastName());
        assertEquals("John Doe", s.getFullName());
        assertEquals("john.doe@example.com", s.getEmail());
    }

    @Test
    void testCopyNullStudent(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Student(null);
        });
    }

    @Test
    void enrollInCourseAddsNonNullNonEmpty() {
        Student s = new Student( "A", "B", "a@b.com");
        s.enrollInCourse("C1");
        s.enrollInCourse("");
        s.enrollInCourse(null);
        Set<String> courses = s.getEnrolledCourseIDs();
        assertEquals(1, courses.size());
        assertTrue(courses.contains("C1"));
    }

    @Test
    void addAssignmentAddsNonNullNonEmpty() {
        Student s = new Student( "A", "B", "a@b.com");
        s.addAssignment("A1");
        s.addAssignment("");
        s.addAssignment(null);
        Set<String> asg = s.getAssignmentIDs();
        assertEquals(1, asg.size());
        assertTrue(asg.contains("A1"));
    }

    @Test
    void unmodifiableSetsCannotBeAltered() {
        Student s = new Student( "A", "B", "a@b.com");
        s.enrollInCourse("C1");
        Set<String> courses = s.getEnrolledCourseIDs();
        assertThrows(UnsupportedOperationException.class, () -> courses.add("X"));
        s.addAssignment("A1");
        Set<String> asg = s.getAssignmentIDs();
        assertThrows(UnsupportedOperationException.class, () -> asg.remove("A1"));
    }

    @Test
    void toStringFormat() {
        Student s = new Student( "John", "Doe", "john@doe.com");
        String str = s.toString();
        assertTrue(str.contains("John Doe"));
        assertTrue(str.contains("STU"));
        assertTrue(str.contains("john@doe.com"));
    }

    @Test
    void comparatorsOrderCorrectly() {
        Student s1 = new Student("Alice", "Brown", "a@x.com");
        Student s2 = new Student("Bob",   "Adams", "b@y.com");
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

    @Test
    void testRemoveAssignment(){
        Student s1 = new Student("Alice", "Brown", "a@x.com");
        s1.removeAssignment(null);
        s1.removeAssignment("");

        Assignment a1 = new Assignment(
                "Test Assignment",
                "STU00001",
                "C001",
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 4, 10)
        );
        s1.addAssignment(a1.getAssignmentID());
        s1.removeAssignment(a1.getAssignmentID());
    }
}