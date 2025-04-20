package org.fp;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProgressBarTest {

    @Test
    void fullBarBeforeAssignedShowsEmpty() {
        LocalDate assigned = LocalDate.of(2025, 4, 10);
        LocalDate due = LocalDate.of(2025, 4, 20);
        LocalDate now = LocalDate.of(2025, 4, 5);
        String bar = ProgressBar.fullBar(assigned, due, now);
        assertTrue(bar.startsWith("[--------------------]"));
        assertTrue(bar.contains("⏳ 15 days left"));
        assertEquals(40, bar.length());
    }

    @Test
    void fullBarAtAssignedShowsEmpty() {
        LocalDate assigned = LocalDate.of(2025, 4, 10);
        LocalDate due = LocalDate.of(2025, 4, 20);
        LocalDate now = assigned;
        String bar = ProgressBar.fullBar(assigned, due, now);
        assertTrue(bar.startsWith("[--------------------]"));
    }

    @Test
    void fullBarMidwayShowsHalfFilled() {
        LocalDate assigned = LocalDate.of(2025, 4, 10);
        LocalDate due = LocalDate.of(2025, 4, 20);
        LocalDate now = LocalDate.of(2025, 4, 15);
        String bar = ProgressBar.fullBar(assigned, due, now);
        assertTrue(bar.startsWith("[##########----------]"));
        assertTrue(bar.contains("⏳ 5 days left"));
    }

    @Test
    void fullBarAfterDueShowsFullAndDone() {
        LocalDate assigned = LocalDate.of(2025, 4, 10);
        LocalDate due = LocalDate.of(2025, 4, 20);
        LocalDate now = LocalDate.of(2025, 4, 25);
        String bar = ProgressBar.fullBar(assigned, due, now);
        assertTrue(bar.startsWith("[####################]"));
        assertTrue(bar.contains("⌛ Done"));
    }

    @Test
    void timeRemainingSingularAndPluralAndToday() {
        LocalDate now = LocalDate.of(2025, 4, 10);
        assertEquals("⏳ 2 days left", ProgressBar.timeRemaining(now.plusDays(2), now));
        assertEquals("⏳ 1 day left", ProgressBar.timeRemaining(now.plusDays(1), now));
        assertEquals("⚠️ Due today", ProgressBar.timeRemaining(now, now));
        assertEquals("⌛ Done", ProgressBar.timeRemaining(now.minusDays(1), now));
    }
}