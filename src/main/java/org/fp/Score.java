package org.fp;


//TODO: Implement the Score class as described in the README.md file.

// Score s1 = new Score(85, 100);
//System.out.println(s1);  // 85/100 (85.00%, B)
//
//Score s2 = new Score(-1, 100);
//System.out.println(s2);  // UNGRADED
//拿下

public class Score {
    public static final int UNGRADED = -1;

    private final int earned;
    private final int total;

    public Score(int earned, int total) {
        if (earned != UNGRADED && (earned < 0 || earned > total || total <= 0)) {
            throw new IllegalArgumentException("Invalid score: earned must be between 0 and total, or -1 for ungraded.");
        }
        this.earned = earned;
        this.total = total;
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