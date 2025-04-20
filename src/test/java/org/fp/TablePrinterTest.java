package org.fp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TablePrinterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        outContent.reset();
    }

    @Test
    public void testPrintDynamicTableValidData() {
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("No.", "Title", "Artist"),
                Arrays.asList("1", "Song A", "Artist A"),
                Arrays.asList("2", "Song B", "Artist B"),
                Arrays.asList("3", "Song C", "Artist C")
        );

        TablePrinter.printDynamicTable("ValidDataTest", rows);
        String output = outContent.toString();

        assertTrue(output.contains("🎉 ValidDataTest 🎉"));
        assertTrue(output.contains("| 1   | Song A | Artist A |"));
    }

    @Test
    public void testPrintDynamicTableWithMarkers() {
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("No.", "Title", "Artist"),
                Arrays.asList("###SEPARATOR###"),
                Arrays.asList("1", "Song A", "Artist A"),
                Arrays.asList("###SEPARATOR###"),
                Arrays.asList("2", "Song B", "Artist B")
        );

        TablePrinter.printDynamicTable("MarkerTest", rows);
        String output = outContent.toString();
        assertTrue(output.contains("🎉 MarkerTest 🎉"));
        assertTrue(output.contains("+-----+--------+----------+"));
        assertTrue(output.contains("| 2   | Song B | Artist B |"));
    }

    @Test
    public void testEmptyOrNullInput() {
        TablePrinter.printDynamicTable("NullInputTest", null);
        String output = outContent.toString().trim();
        assertEquals("No data to display.", output);
        outContent.reset();
        TablePrinter.printDynamicTable("EmptyInputTest", Collections.emptyList());
        output = outContent.toString().trim();
        assertEquals("No data to display.", output);
    }

    @Test
    public void testPrintDynamicTableWithNullCell() {
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("Header1", "Header2"),
                Arrays.asList((String) null, "NonNull")
        );

        TablePrinter.printDynamicTable("NullCellTest", rows);
        String output = outContent.toString();
        assertTrue(output.contains("| "));
        assertTrue(output.contains("NonNull"));
        assertFalse(output.contains("null"));
    }
}
