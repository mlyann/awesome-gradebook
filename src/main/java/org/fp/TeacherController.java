package org.fp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.fp.Assignment.SubmissionStatus.GRADED;
import static org.fp.Assignment.SubmissionStatus.UNSUBMITTED;

public class TeacherController extends BaseController {
    private String currentTeacherID;
    private Map<String, List<Assignment>> groupedAssignments = new HashMap<>();
    private List<String> sortedAssignmentNames = new ArrayList<>();
    private boolean cacheValid = false;
    private String currentAssignmentGroupName = null;
    private List<Assignment> currentAssignmentGroupList = new ArrayList<>();
    private final Map<String, Assignment> assignmentIDMap = new HashMap<>();
    private String currentCourseID;
    private final Set<String> deletedAssignmentIDs = new HashSet<>();

    private List<Student> cachedStudents = new ArrayList<>();
    private boolean studentCacheDirty = false;
    private final Set<String> deletedStudentIDs = new HashSet<>();

    private List<Course> cachedCoursesManagement = new ArrayList<>();
    private final Set<String> addedCourseIDs = new HashSet<>();
    private boolean courseCacheDirty = false;

    private final Set<String> deletedCourseIDs = new HashSet<>();

    public TeacherController(LibraryModel model) {
        super(model);
    }

    public void setCurrentTeacher(String id) {
        if (model.teacherExists(id)) {
            currentTeacherID = id;
            System.out.println(" Current student set: " + model.getTeacher(id).getFullName());
        } else  {
            System.out.println("❌ Tried to set non-existent student: " + id);
        }

    }

    public Teacher getCurrentTeacher() {
        return model.getTeacher(currentTeacherID);
    }

    public Student getStudent(String stuID) {
        return model.getStudent(stuID);
    }



    public void loadTeacherCourses() {
        Teacher teacher = model.getTeacher(currentTeacherID);
        if (teacher == null) return;
        cachedCourses = teacher.getTeachingCourseIDs().stream()
                .map(model::getCourse)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public void loadAssignmentsForCourse(String courseID) {
        this.currentCourseID = courseID;
        this.cachedAssignments = model.getAssignmentsInCourse(courseID);
    }


    public List<Student> getStudentsInCourse(String courseID) {
        List<String> studentIDs = model.getStudentIDsInCourse(courseID);
        return studentIDs.stream()
                .map(model::getStudent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<String> getSortedAssignmentNames() {
        return cacheValid ? sortedAssignmentNames : List.of();
    }

    public Map<String, List<Assignment>> getGroupedAssignmentCache() {
        return groupedAssignments;
    }

    public List<Assignment> getAssignmentGroup(String name) {
        return groupedAssignments.getOrDefault(name, List.of());
    }


    enum AssignmentSort {
        NONE, NAME, ASSIGN_DATE, DUE_DATE, SUBMISSION, GRADED_PERCENT
    }

    public void sortGroupedAssignments(AssignmentSort sort, boolean filter) {
        if (!cacheValid || groupedAssignments.isEmpty()) return;

        List<String> names = new ArrayList<>(groupedAssignments.keySet());

        if (filter) {
            names.removeIf(name -> {
                List<Assignment> list = groupedAssignments.get(name);
                boolean allGraded = list.stream().allMatch(a -> a.getStatus() == GRADED);
                boolean allPublished = list.stream().allMatch(Assignment::isPublished);
                return allGraded && allPublished;
            });
        }

        switch (sort) {
            case NAME -> names.sort(Comparator
                    .comparing((String fullName) -> {
                        int i = fullName.lastIndexOf(' ');
                        return i > 0
                                ? fullName.substring(0, i).toLowerCase()
                                : fullName.toLowerCase();
                    })
                    .thenComparingInt(TeacherController::extractAssignmentNumber)
            );
            case ASSIGN_DATE -> names.sort(Comparator.comparing(n -> groupedAssignments.get(n).get(0).getAssignDate()));
            case DUE_DATE -> names.sort(Comparator.comparing(n -> groupedAssignments.get(n).get(0).getDueDate()));
            case SUBMISSION -> names.sort(Comparator.comparingInt(n ->
                    (int) groupedAssignments.get(n).stream()
                            .filter(a -> a.getStatus() != UNSUBMITTED).count()).reversed());
            case GRADED_PERCENT -> names.sort(Comparator.comparingDouble(n ->
                    getGradedPercentageForGroup(groupedAssignments.get(n))).reversed());
            case NONE -> names.sort(String::compareToIgnoreCase);
        }

        sortedAssignmentNames = names;
    }


    public static int extractAssignmentNumber(String name) {
        try {
            String[] parts = name.trim().split(" ");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (Exception e) {
            return Integer.MAX_VALUE; // fallback for non-matching patterns
        }
    }



    public double getGradedPercentageForGroup(List<Assignment> assignments) {
        long submitted = assignments.stream().filter(a -> a.getStatus() != UNSUBMITTED).count();
        long graded = assignments.stream().filter(a -> a.getStatus() == GRADED).count();
        return submitted == 0 ? 0.0 : (graded * 100.0) / submitted;
    }


    public void refreshGroupedAssignments(String courseID) {
        List<Assignment> allAssignments = model.getAssignmentsInCourse(courseID);

        // 按 assignment name 分组
        groupedAssignments = allAssignments.stream()
                .collect(Collectors.groupingBy(Assignment::getAssignmentName));

        sortedAssignmentNames = new ArrayList<>(groupedAssignments.keySet());

        cacheValid = true;
    }


    public void setSelectedAssignmentGroup(String name) {
        if (groupedAssignments.containsKey(name)) {
            currentAssignmentGroupName = name;
        } else {
            currentAssignmentGroupName = null;
        }
    }


    public String getSubmissionStatsForStudent(String studentID, String courseID) {
        List<Assignment> all = model.getAssignmentsForStudentInCourse(studentID, courseID);
        long submitted = all.stream().filter(a -> a.getStatus() != Assignment.SubmissionStatus.UNSUBMITTED).count();
        return submitted + "/" + all.size();
    }

    private List<Assignment> currentStudentAssignmentList = new ArrayList<>();

    public void setCurrentStudentAssignmentList(List<Assignment> list) {
        this.currentStudentAssignmentList = list;
    }

    public List<Assignment> getCurrentStudentAssignmentList() {
        return this.currentStudentAssignmentList;
    }

    public List<Assignment> getAssignmentsForStudentInCourse(String courseID, String studentID) {
        return model.getAssignmentsForStudentInCourse(studentID, courseID);
    }

    public void setCurrentAssignmentGroupList(List<Assignment> list) {
        currentAssignmentGroupList = new ArrayList<>(list); // avoid escaping reference
    }

    public Course getCourseByAssignment(String assignmentID) {
        Assignment a = model.getAssignment(assignmentID);
        if (a == null) return null;

        return model.getCourse(a.getCourseID());
    }

    private boolean assignmentCacheDirty = false;

    public void addAssignmentToCache(Assignment a) {
        groupedAssignments.computeIfAbsent(a.getAssignmentName(), k -> new ArrayList<>()).add(a);
        assignmentIDMap.put(a.getAssignmentID(), a);
        cacheValid = false;
    }

    public void removeAssignmentGroupFromCache(String name) {
        List<Assignment> list = groupedAssignments.remove(name);
        if (list != null) {
            for (Assignment a : list) {
                assignmentIDMap.remove(a.getAssignmentID());
                deletedAssignmentIDs.add(a.getAssignmentID());
            }
            cacheValid = false;
            assignmentCacheDirty = true;
        }
    }


    public void setAssignmentCacheDirty(boolean dirty) {
        this.assignmentCacheDirty = dirty;
    }

    public boolean isAssignmentCacheDirty() {
        return assignmentCacheDirty;
    }

    public void commitAssignmentChanges() {
        for (String id : deletedAssignmentIDs) {
            Assignment a = model.getAssignment(id);
            if (a != null) {
                model.removeAssignment(id);
                Student s = model.getStudent(a.getStudentID());
                if (s != null) s.removeAssignment(id);
            }
        }

        for (List<Assignment> group : groupedAssignments.values()) {
            for (Assignment a : group) {
                model.addAssignment(a);
                Student s = model.getStudent(a.getStudentID());
                if (s != null && !s.getAssignmentIDs().contains(a.getAssignmentID())) {
                    s.addAssignment(a.getAssignmentID());
                }
            }
        }

        deletedAssignmentIDs.clear();
        assignmentCacheDirty = false;
        System.out.println("💾 Changes saved to model.");
    }


    public void discardAssignmentChanges() {
        refreshGroupedAssignments(getCurrentCourseID());
        assignmentCacheDirty = false;
        System.out.println("🚫 Changes discarded.");
    }

    public String getCurrentCourseID() {
        return currentCourseID;
    }

    public void refreshStudentCache(String courseID) {
        cachedStudents = getStudentsInCourse(courseID);
        currentCourseID = courseID;
        studentCacheDirty = false;
    }

    public List<Student> getCachedStudents() {
        return cachedStudents;
    }

    public boolean isStudentCacheDirty() {
        return studentCacheDirty;
    }

    public void setStudentCacheDirty(boolean dirty) {
        studentCacheDirty = dirty;
    }

    public void addStudentToCache(Student student) {
        cachedStudents.add(student);
        studentCacheDirty = true;
    }

    public void removeStudentFromCache(String studentID) {
        cachedStudents.removeIf(s -> s.getStuID().equals(studentID));
        deletedStudentIDs.add(studentID);
        studentCacheDirty = true;
    }

    public void addCourseToStudentCache(String studentID, String courseID) {
        for (Student s : cachedStudents) {
            if (s.getStuID().equals(studentID)) {
                s.enrollInCourse(courseID);
                return;
            }
        }
        Student s = model.getStudent(studentID);
        if (s != null) s.enrollInCourse(courseID);
    }

    public List<Assignment> getAllAssignmentsInCourse(String courseID) {
        return model.getAssignmentsInCourse(courseID);
    }

    public void commitStudentChanges() {
        List<Assignment> allAssignments = model.getAssignmentsInCourse(currentCourseID);
        Map<String, List<Assignment>> assignmentGroups = allAssignments.stream()
                .collect(Collectors.groupingBy(Assignment::getAssignmentName));

        for (Student s : cachedStudents) {
            String sid = s.getStuID();
            boolean existed = model.getStudent(sid) != null;

            if (!existed) {
                model.addStudent(s);
            }

            model.enrollStudentInCourse(sid, currentCourseID);
            // add course to student
            if (!existed) {
                for (Map.Entry<String, List<Assignment>> entry : assignmentGroups.entrySet()) {
                    Assignment sample = entry.getValue().get(0);
                    Assignment newA = new Assignment(
                            entry.getKey(),
                            sid,
                            currentCourseID,
                            sample.getAssignDate(),
                            sample.getDueDate()
                    );
                    model.addAssignment(newA);
                    s.addAssignment(newA.getAssignmentID());
                }
            }
        }

        for (String sid : deletedStudentIDs) {
            model.removeStudentFromCourse(sid, currentCourseID);
            model.getAssignmentsForStudentInCourse(sid, currentCourseID)
                    .forEach(a -> model.removeAssignment(a.getAssignmentID()));
            model.removeStudent(sid);
        }

        studentCacheDirty = false;
        deletedStudentIDs.clear();

        System.out.println("💾 Student changes saved to model.");
    }


    public void discardStudentChanges() {
        refreshStudentCache(currentCourseID);
        System.out.println("❌ Changes discarded.");
    }

    public boolean isCourseCacheDirty() {
        return courseCacheDirty;
    }

    //  setCourseCacheDirty
    public void setCourseCacheDirty(boolean dirty) {
        courseCacheDirty = dirty;
    }

    //  addCourseToCache, add a new course to the cache
    public void addCourseToCache(Course course) {
        if (course != null) {
            cachedCoursesManagement.add(course);
            addedCourseIDs.add(course.getCourseID());
            courseCacheDirty = true;
        }
    }


    //  removeCourseFromCache, remove a course from the cache
    public void discardCourseChanges() {
        loadTeacherCourses();  // load the original courses
        addedCourseIDs.clear();
        cachedCoursesManagement.clear();
        courseCacheDirty = false;
        System.out.println("❌ Course changes discarded.");
    }

    public List<List<String>> getNewCourseDisplayRows() {
        List<List<String>> rows = new ArrayList<>();
        for (Course c : cachedCoursesManagement) {
            rows.add(List.of(c.getCourseName(), c.getCourseDescription()));
        }
        return rows;
    }


    public void commitCourseChanges() {
        // add new courses
        for (Course course : cachedCoursesManagement) {
            if (addedCourseIDs.contains(course.getCourseID())) {
                model.addCourse(course);
                Teacher teacher = model.getTeacher(course.getTeacherID());
                if (teacher != null) {
                    teacher.addCourse(course.getCourseID());
                }
            }
        }


        // delete courses
        deletedCourseIDs.clear();
        addedCourseIDs.clear();
        courseCacheDirty = false;

    }

    public void initCourseManagementCache() {
        // Correctly copy to avoid immutable references
        cachedCoursesManagement = new ArrayList<>(cachedCourses);
    }


    public List<Student> getAvailableStudents(String courseID) {
        // get students in course
        Set<String> enrolled = getStudentsInCourse(courseID)
                .stream()
                .map(Student::getStuID)
                .collect(Collectors.toSet());
        // All students, filter out enrolled ones
        return model.getAllStudents()
                .stream()
                .filter(s -> !enrolled.contains(s.getStuID()))
                .sorted(Comparator.comparing(Student::getFullName))
                .collect(Collectors.toList());
    }

    /**
     * Search available students by keyword
     */
    public List<Student> searchAvailableStudents(String courseID, String keyword) {
        String kw = keyword.toLowerCase();
        return getAvailableStudents(courseID)
                .stream()
                .filter(s ->
                        s.getFirstName().toLowerCase().contains(kw) ||
                                s.getLastName().toLowerCase().contains(kw)  ||
                                s.getEmail().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    /**
     * Add an existing student to the cache
     */
    public String addExistingStudentToCache(String studentID, String courseID) {
        boolean alreadyInCache = cachedStudents.stream()
                .anyMatch(s -> s.getStuID().equals(studentID));
        if (alreadyInCache) {
            return "Student " + studentID + " is already enrolled in this course.";
        }

        Student s = model.getStudent(studentID);
        if (s == null) {
            return "No student found with ID " + studentID + ".";
        }

        addStudentToCache(s);
        addCourseToStudentCache(studentID, courseID);
        studentCacheDirty = true;

        return null;
    }


    public List<String> getUngradedAssignmentIDs(String courseID) {
        List<Assignment> all = model.getAssignmentsInCourse(courseID);
        return all.stream()
                .filter(a -> a.getStatus() == Assignment.SubmissionStatus.SUBMITTED_UNGRADED)
                .map(Assignment::getAssignmentName)
                .collect(Collectors.toList());
    }

    public void populateDemoDataForCourse(String courseID) {
        model.populateDemoData(courseID);
    }
}
