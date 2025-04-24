package org.fp;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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
        Assignment fetched = c.getAssignmentByID("A100");
        assertNotNull(fetched, "应能检索到刚添加的作业");
        assertSame(a1, fetched, "返回的实例应与添加时一致");
    }

    @Test
    void getAssignmentByIDReturnsNullIfMissing() {
        Course c = new Course("Chem", "Organic", "T005");
        assertNull(c.getAssignmentByID("不存在的ID"), "未添加的作业应返回 null");
    }
}