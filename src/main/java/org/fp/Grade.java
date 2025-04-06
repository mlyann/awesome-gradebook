package org.fp;

public enum Grade {
    A, B, C, D, F;

    public static Grade fromScore(double score) {
        if (score >= 90) return A;
        else if (score >= 80) return B;
        else if (score >= 70) return C;
        else if (score >= 60) return D;
        else return F;
    }
}