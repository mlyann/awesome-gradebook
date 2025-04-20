package org.fp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IDGenTest {

    @Test
    void sequentialIDsForSinglePrefix() {
        String first = IDGen.generate("X");
        String second = IDGen.generate("X");
        assertTrue(first.startsWith("X"));
        assertTrue(second.startsWith("X"));
        assertNotEquals(first, second);
    }

    @Test
    void separateCountersForDifferentPrefixes() {
        String a1 = IDGen.generate("A");
        String b1 = IDGen.generate("B");
        String a2 = IDGen.generate("A");
        assertTrue(a1.startsWith("A"));
        assertTrue(b1.startsWith("B"));
        assertTrue(a2.startsWith("A"));
        assertNotEquals(a1, b1);
        assertNotEquals(a1, a2);
    }

    @Test
    void zeroPaddingAndIncrement() {
        IDGen.generate("P"); // P00000
        String next = IDGen.generate("P"); // P00001
        assertTrue(next.matches("P\\d{5}"));
        assertTrue(next.endsWith("00001"));
    }
}