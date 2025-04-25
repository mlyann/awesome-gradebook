package org.fp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreTest {

    @Test
    void percentageZeroWhenTotalZero() {
        Score s = new Score("G1", "A1", "S1", 10, 0);
        assertEquals(0.0, s.getPercentage());
    }

    @Test
    void percentageCalculatedCorrectly() {
        Score s = new Score("G2", "A2", "S2", 30, 40);
        assertEquals(75.0, s.getPercentage());
    }

    @Test
    void letterGradeMapping() {
        assertEquals(Grade.A, new Score("G3", "A3", "S3", 90, 100).getLetterGrade());
        assertEquals(Grade.B, new Score("G4", "A4", "S4", 80, 100).getLetterGrade());
        assertEquals(Grade.C, new Score("G5", "A5", "S5", 70, 100).getLetterGrade());
        assertEquals(Grade.D, new Score("G6", "A6", "S6", 60, 100).getLetterGrade());
        assertEquals(Grade.F, new Score("G7", "A7", "S7", 59, 100).getLetterGrade());
    }

    @Test
    void setScoreUpdatesFields() {
        Score s = new Score("G8", "A8", "S8", 0, 50);
        s.setScore(25, 50);
        assertEquals(25, s.getEarned());
        assertEquals(50, s.getTotal());
        assertEquals(50.0, s.getPercentage());
    }

    @Test
    void toStringFormat() {
        Score s = new Score("G9", "A9", "S9", 45, 60);
        String str = s.toString();
        assertTrue(str.contains("45/60"));
        assertTrue(str.contains("75.0%"));
        assertTrue(str.contains("[C]"));
    }

    @Test
    void testCopyConstructor(){
        Score sco = new Score("G9", "A9", "S9", 45, 60);
        Score s = new Score(sco);
        String str = s.toString();
        assertTrue(str.contains("45/60"));
        assertTrue(str.contains("75.0%"));
        assertTrue(str.contains("[C]"));
    }

    @Test
    void testCopyNullScore(){
        assertThrows(IllegalArgumentException.class, () -> {
            new Score(null);
        });
    }

    @Test
    void testOtherGetters(){
        Score sco = new Score("G9", "A9", "S9", 45, 60);
        assertEquals("G9", sco.getGradeID());
        assertEquals("A9", sco.getAssignmentID());
        assertEquals("S9", sco.getStudentID());
    }
}