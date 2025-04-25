package org.fp;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TeacherTest {
    @Test
    void gettersAndFullName() {
        Teacher t = new Teacher("Jane", "Smith");
        assertTrue(t.getTeacherID().contains("TCH"));
        assertEquals("Jane", t.getFirstName());
        assertEquals("Smith", t.getLastName());
        assertEquals("Jane Smith", t.getFullName());
    }

    @Test
    void addCourseAddsNonNullNonEmpty() {
        Teacher t = new Teacher("A", "B");
        t.addCourse("C1");
        t.addCourse("");
        t.addCourse(null);
        Set<String> courses = t.getTeachingCourseIDs();
        assertEquals(1, courses.size());
        assertTrue(courses.contains("C1"));
    }

    @Test
    void unmodifiableTeachingCoursesCannotBeAltered() {
        Teacher t = new Teacher("A", "B");
        t.addCourse("C1");
        Set<String> courses = t.getTeachingCourseIDs();
        assertThrows(UnsupportedOperationException.class, () -> courses.add("X"));
    }

    @Test
    void toStringFormat() {
        Teacher t = new Teacher("Jane", "Doe");
        String str = t.toString();
        assertTrue(str.contains("Jane Doe"));
        assertTrue(str.contains("TCH"));
    }

    @Test
    void testCopyConstructor(){
        Teacher teacher = new Teacher("Jane", "Smith");
        Teacher t = new Teacher(teacher);
        assertTrue(t.getTeacherID().contains("TCH"));
        assertEquals("Jane", t.getFirstName());
        assertEquals("Smith", t.getLastName());
        assertEquals("Jane Smith", t.getFullName());
    }

    @Test
    void testCopyNullTeacher(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Teacher(null);
        });
    }
}