package org.fp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GradeTest {

    @Test
    void fromScoreAtLeast90ReturnsA() {
        assertEquals(Grade.A, Grade.fromScore(90));
        assertEquals(Grade.A, Grade.fromScore(100));
    }

    @Test
    void fromScore80to89ReturnsB() {
        assertEquals(Grade.B, Grade.fromScore(80));
        assertEquals(Grade.B, Grade.fromScore(89.99));
    }

    @Test
    void fromScore70to79ReturnsC() {
        assertEquals(Grade.C, Grade.fromScore(70));
        assertEquals(Grade.C, Grade.fromScore(79.99));
    }

    @Test
    void fromScore60to69ReturnsD() {
        assertEquals(Grade.D, Grade.fromScore(60));
        assertEquals(Grade.D, Grade.fromScore(69.99));
    }

    @Test
    void below60ReturnsF() {
        assertEquals(Grade.F, Grade.fromScore(59.99));
        assertEquals(Grade.F, Grade.fromScore(0));
        assertEquals(Grade.F, Grade.fromScore(-5));
    }
}