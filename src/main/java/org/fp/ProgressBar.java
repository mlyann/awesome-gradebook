package org.fp;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProgressBar {
    private static final int BAR_LENGTH = 20;
    private static final int TOTAL_WIDTH = 40; // total characters in final display (bar + status)

    /**
     * Returns a combined progress bar and time left string, fixed width.
     * Progress is based on assigned date to due date.
     * Example: [#####---------------] ⏳ 10 days left
     *          [####################] ⌛ Overdue by 2 days
     */
    public static String fullBar(LocalDate assigned, LocalDate due, LocalDate now) {
        String bar;
        if (now.isAfter(due)) {
            bar = "[" + "#".repeat(BAR_LENGTH) + "]";
        } else if (now.isBefore(assigned)) {
            bar = "[" + "-".repeat(BAR_LENGTH) + "]";
        } else {
            long totalDays = ChronoUnit.DAYS.between(assigned, due);
            long passedDays = ChronoUnit.DAYS.between(assigned, now);

            int filled = (int) Math.round(((double) passedDays / totalDays) * BAR_LENGTH);
            int empty = BAR_LENGTH - filled;
            bar = "[" + "#".repeat(filled) + "-".repeat(empty) + "]";
        }

        String time = timeRemaining(due, now);
        String combined = bar + " " + time;
        return padRight(combined, TOTAL_WIDTH);
    }

    /** Countdown label */
    public static String timeRemaining(LocalDate due, LocalDate now) {
        long daysBetween = ChronoUnit.DAYS.between(now, due);
        if (daysBetween > 0) {
            return "⏳ " + daysBetween + " day" + (daysBetween > 1 ? "s" : "") + " left";
        } else if (daysBetween == 0) {
            return "⚠️ Due today";
        } else {
            return "⌛ Done";
        }
    }

    /** Pads the string with spaces to the right to fit exactly n chars */
    private static String padRight(String s, int width) {
        return String.format("%-" + width + "s", s);
    }

}