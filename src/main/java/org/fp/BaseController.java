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

    public LibraryModel getModel() {
        return model;
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


    public void setCurrentAssignment(Assignment a) {
        currentAssignment = a;
    }


    public Score getScoreForAssignment(String assignmentID) {
        if (model.getScoreForAssignment(assignmentID) == null){
            return null;
        }
        return new Score(model.getScoreForAssignment(assignmentID));
    }
}