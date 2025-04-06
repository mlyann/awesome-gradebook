package org.fp;

public class GradeCalculator {

    private double[] scores;

    public GradeCalculator(double... scores) {
        this.scores = scores;
    }

    public double getTotalScore() {
        double total = 0;
        for (double s : scores) {
            total += s;
        }
        return total;
    }

    public double getAverageScore() {
        if (scores.length == 0) return 0;
        return getTotalScore() / scores.length;
    }

    public Grade getFinalGrade() {
        return Grade.fromScore(getAverageScore());
    }

//    Testing the GradeCalculator class
//    public static void main(String[] args) {
//        GradeCalculator gc = new GradeCalculator(85, 90, 78, 92);
//        System.out.println("Total Score: " + gc.getTotalScore());
//        System.out.println("Average Score: " + gc.getAverageScore());
//        System.out.println("Final Grade: " + gc.getFinalGrade());
//    }
}