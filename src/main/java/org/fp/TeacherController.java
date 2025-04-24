package org.fp;

import java.util.*;
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
            System.out.println("✅ Current student set: " + model.getTeacher(id).getFullName());
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
        this.currentCourseID = courseID;  // ✅ 同步缓存当前操作的课程
        this.cachedAssignments = model.getAssignmentsInCourse(courseID);
        this.cacheValid = false; // ⚠️ 如果需要更新 groupedAssignments，可一并刷新缓存
    }


    public List<Student> getStudentsInCourse(String courseID) {
        List<String> studentIDs = model.getStudentIDsInCourse(courseID);
        return studentIDs.stream()
                .map(model::getStudent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public int countSubmitted(String assignmentID) {
        return (int) model.getAllStudents().stream()
                .filter(s -> {
                    Assignment a = model.getAssignment(assignmentID);
                    return a != null && a.getStudentID().equals(s.getStuID())
                            && a.getStatus() != UNSUBMITTED;
                })
                .count();
    }

    public int countGraded(String assignmentID) {
        return (int) model.getAllStudents().stream()
                .filter(s -> {
                    Assignment a = model.getAssignment(assignmentID);
                    return a != null && a.getStudentID().equals(s.getStuID())
                            && a.getStatus() == GRADED;
                })
                .count();
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


    public int getGradedPercentage(String assignmentID) {
        int submitted = countSubmitted(assignmentID);
        int graded = countGraded(assignmentID);
        if (submitted == 0) return 0;
        return (int) ((graded / (double) submitted) * 100);
    }

    public Map<String, List<Assignment>> getGroupedAssignments(String courseID) {
        return model.getAssignmentsInCourse(courseID).stream()
                .collect(Collectors.groupingBy(Assignment::getAssignmentName));
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
            case NAME -> names.sort(Comparator.comparing(TeacherController::extractAssignmentNumber));
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

    public String getSelectedAssignmentGroup() {
        return currentAssignmentGroupName;
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

    public List<Assignment> getCurrentAssignmentGroupList() {
        return currentAssignmentGroupList;
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
                deletedAssignmentIDs.add(a.getAssignmentID());  // ✅ 标记待删除
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
        // ✅ 删除阶段
        for (String id : deletedAssignmentIDs) {
            Assignment a = model.getAssignment(id);
            if (a != null) {
                model.removeAssignment(id);  // 从模型中删除
                Student s = model.getStudent(a.getStudentID());
                if (s != null) s.removeAssignment(id);  // 从学生对象中删除引用
            }
        }

        // ✅ 添加/更新阶段
        for (List<Assignment> group : groupedAssignments.values()) {
            for (Assignment a : group) {
                model.addAssignment(a);  // 添加或覆盖
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

    public void addAssignment(Assignment a) {
        model.addAssignment(a);
        assignmentIDMap.put(a.getAssignmentID(), a);
        cacheValid = false; // 视情况决定
    }

    public void removeAssignment(String assignmentID) {
        model.removeAssignment(assignmentID);
        assignmentIDMap.remove(assignmentID);
        cacheValid = false;
    }

    public Assignment getAssignment(String assignmentID) {
        return assignmentIDMap.get(assignmentID);
    }



    public void setCurrentCourseID(String courseID) {
        this.currentCourseID = courseID;
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
        // 先把所有 assignment 按名字分组，方便为新同学生成对应作业
        List<Assignment> allAssignments = model.getAssignmentsInCourse(currentCourseID);
        Map<String, List<Assignment>> assignmentGroups = allAssignments.stream()
                .collect(Collectors.groupingBy(Assignment::getAssignmentName));

        // 1) 处理 cachedStudents 中的每个学生
        for (Student s : cachedStudents) {
            String sid = s.getStuID();
            boolean existed = model.getStudent(sid) != null;

            // 1a) 如果模型里还没有这位学生，先 addStudent
            if (!existed) {
                model.addStudent(s);
            }

            // 1b) 不论新旧，都要 enroll 到课程里
            model.enrollStudentInCourse(sid, currentCourseID);

            // 1c) 只有新生才需要生成作业副本
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

        // 2) 处理删除
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

    // ✅ 设置缓存状态
    public void setCourseCacheDirty(boolean dirty) {
        courseCacheDirty = dirty;
    }

    // ✅ 添加新课程到缓存
    public void addCourseToCache(Course course) {
        if (course != null) {
            cachedCoursesManagement.add(course);
            addedCourseIDs.add(course.getCourseID());
            courseCacheDirty = true;
        }
    }


    // ✅ 放弃未保存更改
    public void discardCourseChanges() {
        loadTeacherCourses();  // 从 model 重新加载
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


    public void removeCourseFromCache(String courseID) {
        cachedCourses.removeIf(c -> c.getCourseID().equals(courseID));
        deletedCourseIDs.add(courseID);
        setCourseCacheDirty(true);
    }

    public void commitCourseChanges() {
        // ✅ 添加新课程
        for (Course course : cachedCoursesManagement) {
            if (addedCourseIDs.contains(course.getCourseID())) {
                model.addCourse(course);
                Teacher teacher = model.getTeacher(course.getTeacherID());
                if (teacher != null) {
                    teacher.addCourse(course.getCourseID());
                }
            }
        }


        // ✅ 清理缓存
        deletedCourseIDs.clear();
        addedCourseIDs.clear();
        courseCacheDirty = false;

    }

    public void initCourseManagementCache() {
        // ✅ 正确复制，避免不可变引用
        cachedCoursesManagement = new ArrayList<>(cachedCourses);
    }


    public List<Student> getAvailableStudents(String courseID) {
        // 已选学生 ID
        Set<String> enrolled = getStudentsInCourse(courseID)
                .stream()
                .map(Student::getStuID)
                .collect(Collectors.toSet());
        // 全部学生，过滤掉已选
        return model.getAllStudents()                // 需要你的 LibraryModel 提供此方法
                .stream()
                .filter(s -> !enrolled.contains(s.getStuID()))
                .sorted(Comparator.comparing(Student::getFullName))
                .collect(Collectors.toList());
    }

    /**
     * 根据关键字搜索可选学生
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
     * 将指定学生加入缓存（不直接修改 Model），并设置为脏
     */
    public void addExistingStudentToCache(String studentID, String courseID) {
        Student s = model.getStudent(studentID);
        if (s != null) {
            addStudentToCache(s);
            addCourseToStudentCache(studentID, courseID);
            studentCacheDirty = true;
        }
    }




}
