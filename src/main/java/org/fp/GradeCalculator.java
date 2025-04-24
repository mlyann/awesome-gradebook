package org.fp;

public class GradeCalculator {

    private double[] scores;

    public GradeCalculator(double... scores) {
        this.scores = scores;
    }

    // Copy constructor
    public double getTotalScore() {
        double total = 0;
        for (double s : scores) {
            total += s;
        }
        return total;
    }

    // Method to calculate the average score
    public double getAverageScore() {
        if (scores.length == 0) return 0;
        return getTotalScore() / scores.length;
    }

    // Method to calculate the final grade based on average score
    public Grade getFinalGrade() {
        return Grade.fromScore(getAverageScore());
    }
}