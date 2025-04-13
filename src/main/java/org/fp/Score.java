package org.fp;


//TODO: Implement the Score class as described in the README.md file.

// Score s1 = new Score(85, 100);
//System.out.println(s1);  // 85/100 (85.00%, B)
//
//Score s2 = new Score(-1, 100);
//System.out.println(s2);  // UNGRADED
//拿下, this is a flyweight class, so we need to implement a cache for the scores.

import java.util.HashMap;
import java.util.Map;

public class Score {
    public static final int UNGRADED = -1;
    private static final Score UNGRADED_SCORE = new Score(UNGRADED, 1); // any positive total is fine
    private static final Map<String, Score> CACHE = new HashMap<>();

    private final int earned;
    private final int total;

    static {
        for (int total = 1; total <= 100; total++) {
            for (int earned = 0; earned <= total; earned++) {
                Score score = new Score(earned, total);
                CACHE.put(key(earned, total), score);
            }
        }
    }

    private static String key(int earned, int total) {
        return earned + "/" + total;
    }

    // Private constructor: use factory method instead
    private Score(int earned, int total) {
        if (earned != UNGRADED && (earned < 0 || earned > total || total <= 0)) {
            throw new IllegalArgumentException("Invalid score: earned must be between 0 and total, or -1 for ungraded.");
        }
        this.earned = earned;
        this.total = total;
    }

    public static Score of(int earned, int total) {
        if (earned == UNGRADED) {
            return UNGRADED_SCORE;
        }
        String k = key(earned, total);
        Score result = CACHE.get(k);
        if (result == null) {
            throw new IllegalArgumentException("Score not available in flyweight cache. Total must be between 1 and 100.");
        }
        return result;
    }

    public boolean isGraded() {
        return earned != UNGRADED;
    }

    public int getEarned() {
        return earned;
    }

    public int getTotal() {
        return total;
    }

    public double getPercentage() {
        if (!isGraded()) {
            throw new IllegalStateException("Score is ungraded.");
        }
        return 100.0 * earned / total;
    }

    public String getLetterGrade() {
        if (!isGraded()) {
            return "UNGRADED";
        }
        double percent = getPercentage();
        if (percent >= 90) return "A";
        if (percent >= 80) return "B";
        if (percent >= 70) return "C";
        if (percent >= 60) return "D";
        return "F";
    }

    @Override
    public String toString() {
        return isGraded()
                ? String.format("%d/%d (%.2f%%, %s)", earned, total, getPercentage(), getLetterGrade())
                : "UNGRADED";
    }
}