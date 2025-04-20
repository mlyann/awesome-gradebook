package org.fp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradeCalculatorTest {

    @Test
    void totalScoreEmpty() {
        GradeCalculator gc = new GradeCalculator();
        assertEquals(0.0, gc.getTotalScore());
    }

    @Test
    void totalScoreMultiple() {
        GradeCalculator gc = new GradeCalculator(1.5, 2.5, 3.0);
        assertEquals(7.0, gc.getTotalScore());
    }

    @Test
    void averageScoreEmpty() {
        GradeCalculator gc = new GradeCalculator();
        assertEquals(0.0, gc.getAverageScore());
    }

    @Test
    void averageScoreNonEmpty() {
        GradeCalculator gc = new GradeCalculator(10, 20, 30);
        assertEquals(20.0, gc.getAverageScore());
    }

    @Test
    void finalGradeMapping() {
        assertEquals(Grade.A, new GradeCalculator(90, 100).getFinalGrade());
        assertEquals(Grade.B, new GradeCalculator(80, 85).getFinalGrade());
        assertEquals(Grade.C, new GradeCalculator(70).getFinalGrade());
        assertEquals(Grade.D, new GradeCalculator(60).getFinalGrade());
        assertEquals(Grade.F, new GradeCalculator(0, 50).getFinalGrade());
    }
}