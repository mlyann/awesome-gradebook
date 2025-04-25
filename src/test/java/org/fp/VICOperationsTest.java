package org.fp;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.*;

public class VICOperationsTest {

    @Test
    public void testNoCarryAddition() {
        assertEquals("579", VICOperations.noCarryAddition("123", "456"));
        assertEquals("90", VICOperations.noCarryAddition("999", "101")); // 9+1 = 10 -> 0 (no carry), etc.
        assertEquals("1", VICOperations.noCarryAddition("000", "001"));
    }

    @Test
    public void testChainAddition() {
        assertEquals("12358", VICOperations.chainAddition("12", 5));
        assertEquals("11235", VICOperations.chainAddition("1", 5));  // padded
        assertEquals("123", VICOperations.chainAddition("12345", 3)); // trimmed
    }

    @Test
    public void testDigitPermutation() {
        String result = VICOperations.digitPermutation("abcdefghij");
        assertNotNull(result);
        assertEquals(10, result.length());
        assertTrue(result.matches("[0-9]{10}"));
    }



    @Test
    public void testCreateStraddlingCheckerboardInvalid() {
        assertNull(VICOperations.createStraddlingCheckerboard("012345678", "ab defghij")); // not 10 digits
        assertNull(VICOperations.createStraddlingCheckerboard("0123456789", "abcdefghij")); // missing 2 spaces
    }


    @Test
    void testnoCarryAddition() {
        assertEquals("412", VICOperations.noCarryAddition("359", "163"));
        assertEquals("15", VICOperations.noCarryAddition("017", "8"));
        assertEquals("0", VICOperations.noCarryAddition("0", "0"));
        assertEquals("777", VICOperations.noCarryAddition("999", "888"));
        assertEquals("12324", VICOperations.noCarryAddition("89", "12345"));
        assertEquals("0", VICOperations.noCarryAddition("6", "4"));
        assertEquals("52344", VICOperations.noCarryAddition("52342", "2"));
        assertEquals("52347", VICOperations.noCarryAddition("52349", "8"));

    }

    @Test
    void testChainAddition2() {
        assertEquals("52", VICOperations.chainAddition("52342", 2));
        assertEquals("52", VICOperations.chainAddition("52", 2));
        assertEquals("527", VICOperations.chainAddition("52", 3));
        assertEquals("76238513", VICOperations.chainAddition("762", 8));
        assertEquals("77415", VICOperations.chainAddition("7", 5));
        assertEquals("99875", VICOperations.chainAddition("9", 5));
        assertEquals("64044", VICOperations.chainAddition("64", 5));
        assertEquals("6", VICOperations.chainAddition("64456", 1));
    }

    @Test
    void testDigitPermutation2() {
        assertEquals("4071826395", VICOperations.digitPermutation("BANANALAND"));
        assertEquals("6704821539", VICOperations.digitPermutation("STARTEARLY"));
        assertEquals("0123456789", VICOperations.digitPermutation("ABZZZZZZZZ"));
        assertEquals("9876543210", VICOperations.digitPermutation("ZXWVUTSRQO"));

        assertEquals("6789012345", VICOperations.digitPermutation("6789012345"));
        assertEquals("0549716823", VICOperations.digitPermutation("1759827834"));
        assertEquals("4071826395", VICOperations.digitPermutation("BANANALAND"));
        assertEquals("2390178456", VICOperations.digitPermutation("GGRADINGGG"));

    }

    @Test
    void testStraddlingCheckerboard() {
        ArrayList<String> list = VICOperations.straddlingCheckerboard("4071826395", "A TIN SHOE");
        System.out.println(list);

        ArrayList<String> list2 = VICOperations.straddlingCheckerboard("4170953826", "ON FLY BAT");
        System.out.println(list2);
    }

    @Test
    void testGet2DTable_NullPath() {
        assertNull(VICOperations.get2DTable("01234", "ABCDEFGH  ")); // Invalid digit permutation
        assertNull(VICOperations.get2DTable("0123456789", "ABCDEFGHIJ")); // No 2 spaces
    }

    @Test
    void testCheckerboardDecodeTwoDigitPath() {
        String number = "0123456789";
        String anagram = "AB DEFG HI"; // spaces at index 2 and 3
        String message = "HELLO";
        String encoded = VICOperations.checkerboardEncode(number, anagram, message);

        assertNotNull(encoded);
        assertTrue(encoded.matches("\\d+"));

        String decoded = VICOperations.checkerboardDecode(number, anagram, encoded);
        assertEquals(message, decoded);
    }

    @Test
    void testGetLabelSingleSpace() {
        assertEquals("5", VICOperations.getLabel("0123456789", "ABCDE FGHI")); // only one space
    }

    @Test
    void testNullDigitPermuation(){
        assertNull(VICOperations.digitPermutation(null));
        assertNull(VICOperations.digitPermutation("123"));
    }

    @Test
    void testIsValidAnagram(){
        assertNull(VICOperations.createStraddlingCheckerboard(null, ""));
        assertNull(VICOperations.createStraddlingCheckerboard("abcdefghij", ""));
    }
}
