package org.fp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Course {
    private final String courseID;
    private final String courseName;
    private String courseDescription;
    private final String teacherID;
    private final Map<String, Assignment> assignments;
    private boolean useWeightedGrading = false;  // true = use category weights
    private Map<String, Double> categoryWeights = new HashMap<>(); // e.g., "Homework" → 0.4
    private Map<String, Integer> categoryDropCount = new HashMap<>(); // e.g., "Quiz" → 1
    private boolean isCompleted = false;
    public Course(String courseName, String courseDescription, String teacherID) {
        this.courseID = IDGen.generate("CRS");
        this.courseName = courseName;
        this.teacherID = teacherID;
        this.courseDescription = courseDescription;
        this.assignments = new HashMap<>();
    }

    // Copy constructor
    public Course (Course c){
        if (c == null){
            throw new IllegalArgumentException("Cannot copy a null Course.");
        }
        this.courseID= c.courseID;
        this.courseName = c.courseName;
        this.courseDescription = c.courseDescription;
        this.teacherID= c.teacherID;
        this.assignments = new HashMap<>();
        for (var e : c.assignments.entrySet()) {
            this.assignments.put(e.getKey(), new Assignment(e.getValue()));
        }
        this.useWeightedGrading=c.useWeightedGrading;
        this.categoryWeights= new HashMap<>(c.categoryWeights);
        this.categoryDropCount= new HashMap<>(c.categoryDropCount);
        this.isCompleted= c.isCompleted;
    }

    public String getCourseID() {
        return courseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public String getTeacherID() { return teacherID; }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public void addAssignment(Assignment assignment) {
        assignments.put(assignment.getAssignmentID(), assignment);
    }

    public Assignment getAssignmentByID(String assignmentID) {
        return assignments.get(assignmentID);
    }

    public void setGradingMode(boolean useWeighted) {
        this.useWeightedGrading = useWeighted;
    }
    public void setCategoryWeight(String category, double weight) {
        categoryWeights.put(category, weight);
    }
    public void setCategoryDropCount(String category, int count) {
        categoryDropCount.put(category, count);
    }
    public boolean isUsingWeightedGrading() {
        return useWeightedGrading;
    }
    public double getCategoryWeight(String category) {
        return categoryWeights.getOrDefault(category, 0.0);
    }
    public int getDropCountForCategory(String category) {
        return categoryDropCount.getOrDefault(category, 0);
    }
    public Map<String, Double> getCategoryWeights() {
        return Collections.unmodifiableMap(categoryWeights);
    }
    public Map<String, Integer> getCategoryDropCounts() {
        return Collections.unmodifiableMap(categoryDropCount);
    }
    public void markCompleted() {
        this.isCompleted = true;
    }
    public boolean isCompleted() {
        return isCompleted;
    }
}