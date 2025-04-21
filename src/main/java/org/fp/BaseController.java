package org.fp;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseController {
    protected final LibraryModel model;
    protected List<Course> cachedCourses = new ArrayList<>();
    protected List<Assignment> cachedAssignments = new ArrayList<>();
    protected Assignment currentAssignment = null;

    public BaseController(LibraryModel model) {
        this.model = model;
    }

    public List<Course> getCachedCourses() {
        List<Course> copyCourse = new ArrayList<>();
        for (Course c : cachedCourses){
            copyCourse.add(new Course(c));
        }
        return copyCourse;
    }

    public Course getCachedCourse(int index) {
        return (index >= 0 && index < cachedCourses.size()) ? new Course(cachedCourses.get(index)) : null;
    }

    public enum CourseSort {
        NONE, NAME, STATUS
    }

    public static CourseSort nextCourseSort(CourseSort current) {
        return switch (current) {
            case NONE -> CourseSort.NAME;
            case NAME -> CourseSort.STATUS;
            case STATUS -> CourseSort.NONE;
        };
    }


    public void sortCachedCourses(CourseSort sort) {
        switch (sort) {
            case NAME -> cachedCourses.sort(Comparator.comparing(Course::getCourseName));
            case STATUS -> cachedCourses.sort(Comparator.comparing(Course::getCourseDescription));
            case NONE -> {}
        }
    }

    public List<List<String>> getFormattedCourseListForDisplayRows() {
        List<List<String>> rows = new ArrayList<>();
        for (Course c : cachedCourses) {
            rows.add(List.of(c.getCourseName(), c.getCourseDescription()));
        }
        return rows;
    }



    public Assignment getCachedAssignment(int index) {
        return (index >= 0 && index < cachedAssignments.size()) ? new Assignment(cachedAssignments.get(index)) : null;
    }

    public void setCurrentAssignment(Assignment a) {
        currentAssignment = a;
    }

    public Assignment getCurrentAssignment() {
        return new Assignment(currentAssignment);
    }

    public List<Assignment> getCachedAssignments() {
        List<Assignment> copyAssignments = new ArrayList<>();
        for (Assignment a : cachedAssignments){
            copyAssignments.add(new Assignment(a));
        }
        return copyAssignments;
    }

    public Score getScoreForAssignment(String assignmentID) {
        return new Score(model.getScoreForAssignment(assignmentID));
    }
}