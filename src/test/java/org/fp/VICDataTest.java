package org.fp;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;


class VICDataTest {

    @Test
    void testVICDataFields() {
        VICData data = new VICData("12345","250314","HELLOWORLD","AB CD EFGH","MESSAGE");
        assertEquals("12345", data.agentID);
        assertEquals("250314", data.date);
        assertEquals("HELLOWORLD", data.phrase);
        assertEquals("AB CD EFGH", data.anagram);
        assertEquals("MESSAGE", data.message);
    }

    @Test
    void testVICDataEmptyConstructor() {
        VICData data = new VICData();
        assertNull(data.agentID);
        assertNull(data.date);
        assertNull(data.phrase);
        assertNull(data.anagram);
        assertNull(data.message);
    }
}
