package org.fp;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CourseTest {

    @Test
    void constructorGeneratesNonNullID() {
        Course c = new Course("Math", "Basic Algebra", "T001");
        String id = c.getCourseID();
        assertNotNull(id, "Course ID 不应为 null");
        assertFalse(id.isEmpty(), "Course ID 不应为空串");
        // 默认 IDGen.generate("CRS") 应该以 "CRS" 开头
        assertTrue(id.startsWith("CRS"), "Course ID 应以 CRS 开头，实际为 " + id);
    }

    @Test
    void testCopyConstructorNotNull(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Course(null);
        });
    }

    @Test
    void gettersReturnConstructorValues() {
        Course c = new Course("Physics", "Mechanics", "T002");
        assertEquals("Physics", c.getCourseName());
        assertEquals("Mechanics", c.getCourseDescription());
        assertEquals("T002", c.getTeacherID());
    }

    @Test
    void setCourseDescriptionUpdatesDescription() {
        Course c = new Course("Bio", "Old Desc", "T003");
        c.setCourseDescription("New Desc");
        assertEquals("New Desc", c.getCourseDescription());
    }

    @Test
    void addAndGetAssignmentByID() {
        Course c = new Course("CS", "Data Structures", "T004");
        // 新建一个 Assignment
        Assignment a1 = new Assignment("Homework 1", "STU01", c.getCourseID(),
                LocalDate.of(2025,4,1), LocalDate.of(2025,4,10)
        );
        // 调用 addAssignment
        c.addAssignment(a1);
        // 通过 ID 检索
        Assignment fetched = c.getAssignmentByID(a1.getAssignmentID());
        assertNotNull(fetched, "应能检索到刚添加的作业");
        assertSame(a1, fetched, "返回的实例应与添加时一致");
    }

    @Test
    void getAssignmentByIDReturnsNullIfMissing() {
        Course c = new Course("Chem", "Organic", "T005");
        assertNull(c.getAssignmentByID("不存在的ID"), "未添加的作业应返回 null");
    }

    @Test
    void testGradingModeToggle() {
        Course c = new Course("Econ", "Microeconomics", "T010");
        assertFalse(c.isUsingWeightedGrading(), "Default should not use weighted grading");

        c.setGradingMode(true);
        assertTrue(c.isUsingWeightedGrading(), "Should use weighted grading after being set to true");
    }

    @Test
    void testCategoryWeightAndDropCountDefaults() {
        Course c = new Course("Eng", "Grammar", "T011");

        // Default weight for nonexistent category should be 0.0
        assertEquals(0.0, c.getCategoryWeight("Quiz"), 1e-9);
        // Default drop count for nonexistent category should be 0
        assertEquals(0, c.getDropCountForCategory("Quiz"));
    }

    @Test
    void testSetCategoryWeightAndDropCount() {
        Course c = new Course("Music", "Theory", "T012");
        c.setCategoryWeight("Homework", 0.4);
        c.setCategoryDropCount("Quiz", 2);

        assertEquals(0.4, c.getCategoryWeight("Homework"), 1e-9);
        assertEquals(2, c.getDropCountForCategory("Quiz"));
    }

    @Test
    void testCategoryMapsAreUnmodifiable() {
        Course c = new Course("PE", "Fitness", "T013");
        c.setCategoryWeight("Project", 0.5);
        c.setCategoryDropCount("Exam", 1);

        Map<String, Double> weights = c.getCategoryWeights();
        Map<String, Integer> drops = c.getCategoryDropCounts();

        assertThrows(UnsupportedOperationException.class, () -> weights.put("New", 0.3));
        assertThrows(UnsupportedOperationException.class, () -> drops.put("New", 2));
    }

    @Test
    void testCopyConstructorCreatesDeepCopy() {
        Course original = new Course("Drama", "Acting", "T014");
        original.setCategoryWeight("Scene", 0.6);
        original.setGradingMode(true);
        original.addAssignment(new Assignment("Test Assignment",
                "STU00001",
                "C001",
                LocalDate.of(2025, 4, 1),
                LocalDate.of(2025, 4, 10)));

        Course copy = new Course(original);
        assertNotSame(original, copy);
        assertEquals(original.getCourseName(), copy.getCourseName());
        assertTrue(copy.isUsingWeightedGrading());
        assertEquals(0.6, copy.getCategoryWeight("Scene"), 1e-9);

        // Changing the copy should not affect the original
        copy.setCategoryWeight("Scene", 0.1);
        assertEquals(0.6, original.getCategoryWeight("Scene"), 1e-9);
    }
}