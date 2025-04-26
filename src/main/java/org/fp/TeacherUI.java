package org.fp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

import org.fp.TeacherController.AssignmentSort;

import com.openai.client.OpenAIClient;


/**
 * Console‑based **Teacher UI** for the grade‑book system.
 * <p>
 * All business logic lives in {@link LibraryModel}.  This UI focuses on:
 *  • Course roster & analytics
 *  • Assignment and student management
 *  • Grade entry / update
 *  • GPT‑powered feedback and class summaries
 *
 * Every function name used here matches (or suggests) a helper in {@link LibraryModel}.
 */
public class TeacherUI {

    /* =============================================================
     *  Runtime state & helpers
     * ============================================================= */
    private static final Scanner sc = new Scanner(System.in);
    private static TeacherController TeacherController;
    private static final LocalDate SYSTEM_DATE = LocalDate.now();
    // GPT client
    private static OpenAIClient GPT;
    private static LibraryModel model;

    public static void start(LibraryModel modelInstance, String teacherID) {
        TeacherUI.model = modelInstance;

        //construct the controller
        TeacherController tc = new TeacherController(model);   // ← 用局部名 tc
        tc.setCurrentTeacher(teacherID);
        TeacherUI.TeacherController = tc;

        level_1(tc, sc);
    }

    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }


    //DASHBOARD – list courses taught by this instructor
    private static void level_1(TeacherController controller, Scanner sc) {
        System.out.println("Current date: " + SYSTEM_DATE);
        TeacherController.CourseSort sort = BaseController.CourseSort.NONE;

        while (true) {
            Teacher teacher = controller.getCurrentTeacher();
            if (teacher == null) {
                System.out.println("❌ No teacher is logged in.");
                return;
            }

            String teacherName = teacher.getFullName();
            controller.loadTeacherCourses();
            controller.sortCachedCourses(sort);
            List<List<String>> courseData = controller.getFormattedCourseListForDisplayRows();

            printCourseTable(teacherName, new ArrayList<>(courseData), sort);

            System.out.println("1) 🔍 Select a course");
            System.out.println("m) 🛠️ Course Management");
            System.out.println("s) 🔀 Change sort");
            System.out.println("0) 🚪 Exit");
            System.out.print("👉 Choice: ");
            String choice = sc.nextLine().trim();

            switch (choice.toLowerCase()) {
                case "0" -> {
                    return;
                }
                case "s" -> {
                    sort = BaseController.nextCourseSort(sort);
                }
                case "m" -> {
                    manageNewCourses(controller, sc);
                }
                default -> {
                    if (choice.matches("[1-9][0-9]*") && Integer.parseInt(choice) <= courseData.size()) {
                        int index = Integer.parseInt(choice) - 1;
                        Course selected = controller.getCachedCourse(index);
                        level_2(controller, selected);
                    } else {
                        System.out.println("❌ Invalid choice. Enter again.");
                    }
                }
            }
        }
    }

    private static void manageNewCourses(TeacherController controller, Scanner sc) {
        controller.initCourseManagementCache();

        boolean exit = false;
        while (!exit) {
            clear();
            List<List<String>> data = controller.getNewCourseDisplayRows();
            printCourseTable("[Unsaved]", data, BaseController.CourseSort.NONE);

            System.out.println("a) ➕ Add new course");
            System.out.println(controller.isCourseCacheDirty() ? "s) 💾 Save changes" : "s)  No changes to save");
            System.out.println("0) 🔙 Back");
            System.out.print("👉 Choice: ");
            String input = sc.nextLine().trim();

            switch (input.toLowerCase()) {
                case "0" -> {
                    if (controller.isCourseCacheDirty()) {
                        System.out.print("⚠️ Unsaved changes. Save now? (y/n): ");
                        String confirm = sc.nextLine().trim().toLowerCase();
                        if (confirm.equals("y")) {
                            controller.commitCourseChanges();
                            System.out.println("\uD83D\uDCBE Course changes saved to model.");
                        }
                        else controller.discardCourseChanges();
                    }
                    exit = true;
                }
                case "a" -> {
                    System.out.print("📘 Course Name: ");
                    String name = sc.nextLine().trim();
                    System.out.print("📝 Description: ");
                    String desc = sc.nextLine().trim();
                    Course course = new Course(name, desc, controller.getCurrentTeacher().getTeacherID());
                    controller.addCourseToCache(course);
                    controller.setCourseCacheDirty(true);
                    System.out.println(" Course added (not yet saved).");
                }
                case "s" -> {
                    controller.commitCourseChanges();
                }
                default -> System.out.println("❌ Invalid input.");
            }
        }
    }




    private static void printCourseTable(String teacherName, List<List<String>> data, TeacherController.CourseSort mode) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("No.", "Course Name", "Description"));

        int idx = 1;
        for (List<String> row : data) {
            String courseName = row.size() > 0 ? row.get(0) : "";
            String courseDesc = row.size() > 1 ? row.get(1) : "";
            rows.add(List.of(String.valueOf(idx++), courseName, courseDesc));
        }

        String title = String.format("Courses taught by %s (sorted by %s)", teacherName, mode.name().toLowerCase());
        TablePrinter.printDynamicTable(title, rows);
    }

    private static void level_2(TeacherController controller, Course course) {
        SortMode rosterSort = SortMode.FIRST_NAME;
        ViewMode view = ViewMode.ASSIGNMENTS;
        AssignmentSort sort = AssignmentSort.NONE;
        boolean filterActive = false;
        Teacher teacher = controller.getCurrentTeacher();
        if (teacher == null) {
            System.out.println("\u274c No teacher logged in.");
            return;
        }

        while (true) {
            clear();
            System.out.println("\ud83d\udcd8 Course: " + course.getCourseName());
            System.out.println("\ud83d\udc69\u200d\ud83c\udfeb Instructor: " + teacher.getFullName());
            System.out.println("==============================");

            switch (view) {
                case ASSIGNMENTS -> viewAssignments(controller, course, sort, filterActive);
                case ROSTER      -> viewRoster(controller, course, rosterSort);
            }

            System.out.println();
            System.out.print(
                    view == ViewMode.ASSIGNMENTS
                            ? "a) 📄 Assignments    r) 👥 Roster    g) 🏁 Final Grades    "
                            + "c) ⚙️ Grading setup    s) 🔍 Search    f) 🧮 Filter    \n"
                            + "o) 🔀 Sort    d) 🛠️ Assignments Manage    n) 📊 Analytics    "
                            + "m) ✅ Mark Completed    0) 🔙 Back\n"
                            : "a) 📄 Assignments    r) 👥 Roster    g) 🏁 Final Grades    "
                            + "c) ⚙️ Grading setup    f) 🧮 Filter    o) 🔀 Sort    \n"
                            + "d) 👩‍🏫 Roster Manage    n) 📊 Analytics    "
                            + "m) ✅ Mark Completed    p) \uD83D\uDC65 Split into groups    0) 🔙 Back\n"
            );
            System.out.print("\ud83d\udd0a Choice: ");
            String choice = sc.nextLine().trim().toLowerCase();

            if (choice.equals("0")) return;
            if (choice.equals("a")) { view = ViewMode.ASSIGNMENTS; continue; }
            if (choice.equals("r")) { view = ViewMode.ROSTER; continue; }
            if (choice.equals("s") && view == ViewMode.ASSIGNMENTS) { searchAssignments(controller, course); continue; }
            if (choice.equals("f")) { filterActive = !filterActive; continue; }
            if (choice.equals("o")) {
                if (view == ViewMode.ASSIGNMENTS) sort = nextSort(sort);
                else rosterSort = nextRosterSort(rosterSort);
                continue;
            }
            if (choice.equals("d")) {
                if (view == ViewMode.ASSIGNMENTS) assignmentManage(controller, course);
                else studentManage(controller, course);
                continue;
            }
            if (choice.equals("g")) { viewFinalGrades(course.getCourseID()); continue; }
            if (choice.equals("c")) {
                chooseGradingMode(course);
                if (course.isUsingWeightedGrading())
                    setCategoryWeightsAndDrops(course);
                continue;
            }
            if (choice.equals("m")) {
                LibraryModel model = TeacherController.getModel();
                model.markCourseAsCompleted(course.getCourseID());
                System.out.println("✅ Course marked as completed.");
                continue;
            }
            if (choice.equals("n")) {
                analyticsMenu(controller, course);
                continue;
            }
            if (choice.equals("p")) {
                groupStudentsInCourse(controller, course);
                continue;
            }

            if (choice.matches("\\d+")) {
                int idx = Integer.parseInt(choice) - 1;
                if (view == ViewMode.ASSIGNMENTS) {
                    controller.refreshGroupedAssignments(course.getCourseID());
                    controller.sortGroupedAssignments(sort, filterActive);
                    List<String> names = controller.getSortedAssignmentNames();
                    if (idx >= 0 && idx < names.size()) {
                        String name = names.get(idx);
                        List<Assignment> group = controller.getAssignmentGroup(name);
                        controller.setSelectedAssignmentGroup(name);
                        viewAssignmentDetails(controller, name, group);
                    }
                } else {
                    List<Student> students = viewRoster(controller, course, rosterSort);
                    if (idx >= 0 && idx < students.size()) {
                        viewStudentSubmissions(controller, course.getCourseID(), students.get(idx));
                    }
                }
                continue;
            }

            System.out.println("\u274c Invalid input.");
        }
    }


    private enum ViewMode {
        ASSIGNMENTS, ROSTER
    }

    private enum SortMode { FIRST_NAME, LAST_NAME, EMAIL }


    private static AssignmentSort nextSort(AssignmentSort current) {
        return switch (current) {
            case NONE -> AssignmentSort.NAME;
            case NAME -> AssignmentSort.ASSIGN_DATE;
            case ASSIGN_DATE -> AssignmentSort.DUE_DATE;
            case DUE_DATE -> AssignmentSort.SUBMISSION;
            case SUBMISSION -> AssignmentSort.GRADED_PERCENT;
            case GRADED_PERCENT -> AssignmentSort.NONE;
        };
    }

    private static SortMode nextRosterSort(SortMode current) {
        return switch (current) {
            case FIRST_NAME -> SortMode.LAST_NAME;
            case LAST_NAME -> SortMode.EMAIL;
            case EMAIL -> SortMode.FIRST_NAME;
        };
    }

    private static void viewAssignments(TeacherController controller, Course course, AssignmentSort sort, boolean filter) {
        // 刷新分组与排序
        controller.refreshGroupedAssignments(course.getCourseID());
        controller.sortGroupedAssignments(sort, filter);

        Map<String, List<Assignment>> grouped = controller.getGroupedAssignmentCache();
        List<String> names = controller.getSortedAssignmentNames();

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(
                "No.", "Assignment Name", "Assigned", "Due",
                "Progress", "Submissions", "Graded", "Published?"
        ));

        int totalStudents = controller.getStudentsInCourse(course.getCourseID()).size();
        int index = 1;

        for (String name : names) {
            List<Assignment> group = grouped.get(name);
            if (group == null || group.isEmpty()) continue;

            Assignment sample = group.get(0);
            String assignDate = sample.getAssignDate().toString();
            String dueDate = sample.getDueDate().toString();
            String progress = ProgressBar.fullBar(sample.getAssignDate(), sample.getDueDate(), SYSTEM_DATE);

            long submitted = group.stream().filter(a -> a.getStatus() != Assignment.SubmissionStatus.UNSUBMITTED).count();
            long graded = group.stream().filter(a -> a.getStatus() == Assignment.SubmissionStatus.GRADED).count();
            int percent = (submitted == 0) ? 0 : (int) ((graded * 100.0) / submitted);
            String gradedBar = generateGradedBar(percent, (int) graded, (int) submitted);
            boolean allPublished = group.stream().allMatch(Assignment::isPublished);

            rows.add(List.of(
                    String.valueOf(index++),
                    name,
                    assignDate,
                    dueDate,
                    progress,
                    submitted + "/" + totalStudents,
                    gradedBar,
                    allPublished ? " Yes" : "❌ No"
            ));
        }

        TablePrinter.printDynamicTable("Assignments for Course: " + course.getCourseName() +
                " (sorted by " + sort.name().toLowerCase() + (filter ? ", filter on)" : ")"), rows);
    }


    private static String generateGradedBar(int percent, int graded, int total) {
        int barCount = percent / 5;
        String bar = "[" + "#".repeat(barCount) + "-".repeat(20 - barCount) + "] ";
        return bar + graded + "/" + total;
    }


    private static int extractAssignmentNumber(String name) {
        String[] parts = name.trim().split(" ");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    /* 
     *  View a single assignment detail for a teacher
     */
    private static void searchAssignments(TeacherController controller, Course course) {
        System.out.print("🔍 Enter letters to match: ");
        String pattern = sc.nextLine().trim().toLowerCase();

        if (pattern.isEmpty()) {
            System.out.println("⚠️ Empty input. Returning...");
            return;
        }

        Map<String, List<Assignment>> grouped = controller.getGroupedAssignmentCache();
        List<String> matchedNames = grouped.keySet().stream()
                .filter(name -> isSubsequence(pattern, name.toLowerCase()))
                .sorted()
                .toList();

        if (matchedNames.isEmpty()) {
            System.out.println("❌ No matching assignments found.");
            return;
        }

        int totalStudents = controller.getStudentsInCourse(course.getCourseID()).size();
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of(
                "No.", "Assignment Name", "Assigned", "Due",
                "Progress", "Submissions", "Graded", "Published?"
        ));

        int index = 1;
        for (String name : matchedNames) {
            List<Assignment> group = grouped.get(name);
            if (group == null || group.isEmpty()) continue;

            Assignment sample = group.get(0);
            String assignDate = sample.getAssignDate().toString();
            String dueDate = sample.getDueDate().toString();
            String progress = ProgressBar.fullBar(sample.getAssignDate(), sample.getDueDate(), SYSTEM_DATE);
            long submitted = group.stream().filter(a -> a.getStatus() != Assignment.SubmissionStatus.UNSUBMITTED).count();
            long graded = group.stream().filter(a -> a.getStatus() == Assignment.SubmissionStatus.GRADED).count();
            int percent = (submitted == 0) ? 0 : (int) ((graded * 100.0) / submitted);
            String gradedBar = generateGradedBar(percent, (int) graded, (int) submitted);
            boolean allPublished = group.stream().allMatch(Assignment::isPublished);

            rows.add(List.of(
                    String.valueOf(index++),
                    name,
                    assignDate,
                    dueDate,
                    progress,
                    submitted + "/" + totalStudents,
                    gradedBar,
                    allPublished ? " Yes" : "❌ No"
            ));
        }

        TablePrinter.printDynamicTable("Search Results: \"" + pattern + "\"", rows);

        System.out.println("0) 🔙 Back");
        System.out.print("👉 Choice: ");
        String input = sc.nextLine().trim();
        if (input.matches("[1-9][0-9]*")) {
            int idx = Integer.parseInt(input) - 1;
            if (idx >= 0 && idx < matchedNames.size()) {
                String selectedGroup = matchedNames.get(idx);
                controller.setSelectedAssignmentGroup(selectedGroup);

                // You can continue to level_3_teacher(controller, selectedGroup);
                System.out.println("📌 Selected assignment group: " + selectedGroup);
                List<Assignment> selectedGroupList = grouped.get(selectedGroup);
                if (selectedGroupList != null) {
                    viewAssignmentDetails(controller, selectedGroup, selectedGroupList);
                }
            }
        }
    }

    /* 
     *  Check if pattern is a subsequence of target
     */
    private static boolean isSubsequence(String pattern, String target) {
        int i = 0, j = 0;
        while (i < pattern.length() && j < target.length()) {
            if (pattern.charAt(i) == target.charAt(j)) i++;
            j++;
        }
        return i == pattern.length();
    }

    private enum GradeSort {
        NONE, ASCENDING, DESCENDING
    }

    /*
     *  View a single assignment detail for a teacher
     */
    private static void viewAssignmentDetails(TeacherController controller, String groupName, List<Assignment> group) {
        System.out.println("Current date: " + SYSTEM_DATE);
        GradeSort gradeSort = GradeSort.NONE;

        while (true) {
            if (group == null || group.isEmpty()) {
                System.out.println("❌ No assignments found for: " + groupName);
                return;
            }

            //  sorting!!
            List<Assignment> displayGroup;
            if (gradeSort == GradeSort.NONE) {
                displayGroup = new ArrayList<>(group);
            } else {
                List<Map.Entry<Assignment, Double>> list = new ArrayList<>();
                for (Assignment a : group) {
                    Score s = controller.getScoreForAssignment(a.getAssignmentID());
                    double percent = (s != null) ? s.getPercentage() : -1.0;
                    list.add(new AbstractMap.SimpleEntry<>(a, percent));
                }

                Comparator<Map.Entry<Assignment, Double>> comparator = Comparator.comparingDouble(Map.Entry::getValue);
                if (gradeSort == GradeSort.DESCENDING) comparator = comparator.reversed();
                list.sort(comparator);

                displayGroup = list.stream().map(Map.Entry::getKey).toList();
            }

            //  缓存当前 group 用于后续选择操作
            controller.setCurrentAssignmentGroupList(displayGroup);

            // 📋 表格展示
            List<List<String>> rows = new ArrayList<>();
            rows.add(List.of("No.", "Student", "Email", "Status", "Score", "Grade"));

            int index = 1;
            for (Assignment a : displayGroup) {
                Student stu = controller.getStudent(a.getStudentID());
                if (stu == null) continue;

                String name = stu.getFullName();
                String email = stu.getEmail();
                String status = switch (a.getStatus()) {
                    case UNSUBMITTED -> "⛔ Not submitted";
                    case SUBMITTED_UNGRADED -> "✉️ Submitted";
                    case GRADED -> " Graded";
                };

                Score score = controller.getScoreForAssignment(a.getAssignmentID());
                String scoreStr = (score != null) ? score.getEarned() + "/" + score.getTotal() : "—";
                String gradeStr = (score != null) ? score.getLetterGrade().name() : "N/A";

                rows.add(List.of(
                        String.valueOf(index++),
                        name,
                        email,
                        status,
                        scoreStr,
                        gradeStr
                ));
            }

            String sortLabel = switch (gradeSort) {
                case NONE -> "NONE";
                case ASCENDING -> "ASCENDING";
                case DESCENDING -> "DESCENDING";
            };

            TablePrinter.printDynamicTable("🎉 Student Submissions for: " + groupName + " (Sorted: " + sortLabel + ")", rows);

            System.out.println("0) 🔙 Back\n[number] View submission    s) 🔀 Sort by score");
            System.out.print("👉 Choice: ");
            String input = sc.nextLine().trim();

            if (input.equals("0")) return;

            if (input.equalsIgnoreCase("s")) {
                gradeSort = switch (gradeSort) {
                    case NONE -> GradeSort.DESCENDING;
                    case DESCENDING -> GradeSort.ASCENDING;
                    case ASCENDING -> GradeSort.NONE;
                };
            } else if (input.matches("[1-9][0-9]*")) {
                int pos = Integer.parseInt(input) - 1;
                if (pos >= 0 && pos < displayGroup.size()) {
                    Assignment selected = displayGroup.get(pos);
                    viewAssignmentDetailForTeacher(controller, selected);
                } else {
                    System.out.println("❌ Invalid selection.");
                }
            } else {
                System.out.println("❌ Invalid input.");
            }
        }
    }

    /* 
     *  View a single assignment detail for a teacher
     */
    private static void viewFinalGrades(String courseID) {
        List<List<String>> table = getFinalGradesForCourse(courseID);
        TablePrinter.printDynamicTable("📋 Final Grades with GPA", table);
        System.out.println("⬅️ Press ENTER to return...");
        sc.nextLine();
    }

    /*
     *  View a single assignment detail for a teacher
     */
    private static void setCategoryWeightsAndDrops(Course viewCopy) {
        String cid   = viewCopy.getCourseID();
        LibraryModel model = TeacherController.getModel();
        model.setGradingMode(cid, true);
        Course snap = model.getCourse(cid);// DEEP COPY
        Map<String, Double> w = snap.getCategoryWeights();
        Map<String, Integer> d = snap.getCategoryDropCounts();
        int count = w.size();

        if (count > 0) {
            System.out.println("\nCurrent categories (" + count + " total):");
            System.out.println("  Category        Weight   Drop");
            System.out.println("  --------        ------   ----");
            for (String c : w.keySet()) {
                System.out.printf("  %-14s  %5.2f    %d%n", c, w.get(c), d.getOrDefault(c,0));
            }
            double total = w.values().stream().mapToDouble(Double::doubleValue).sum();
            System.out.printf("  %-14s  %5.2f%n%n", "Total weight =", total);
        } else {
            System.out.println("\nNo categories yet – start adding below.\n");
        }

        while (true) {
            System.out.print("Enter category name (or blank to finish): ");
            String cat = sc.nextLine().trim();
            if (cat.isEmpty()) break;

            double weight = -1;
            while (true) {
                System.out.print("  → Weight for " + cat + " (0‑1): ");
                String input = sc.nextLine().trim();
                try {
                    weight = Double.parseDouble(input);
                    if (weight < 0 || weight > 1) {
                        System.out.println("    Please enter a value between 0 and 1.");
                    } else {
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("    Invalid input. Please enter a decimal number.");
                }
            }
            model.setCategoryWeight(cid, cat, weight);

            int drop = -1;
            while (true) {
                System.out.print("  → Drop how many lowest scores? ");
                String input = sc.nextLine().trim();
                try {
                    drop = Integer.parseInt(input);
                    if (drop < 0) {
                        System.out.println("    Please enter a non-negative number.");
                    } else {
                        break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("    Invalid input. Please enter an integer.");
                }
            }
            model.setCategoryDrop(cid, cat, drop);
        }

        System.out.println("\n Grading mode set to category‑based. Use analytics to verify.\n");
    }

    /*
     *  Get final grades for a course
     */
    private static List<List<String>> getFinalGradesForCourse(String courseID) {
        LibraryModel model = TeacherController.getModel();
        Course course = model.getCourse(courseID);
        boolean weighted = course.isUsingWeightedGrading();

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("No.", "Full Name", "Email",
                weighted ? "Weighted %" : "Raw %",
                "Grade", "GPA"));


        int index = 1;
        for (Student s : TeacherController.getStudentsInCourse(courseID)) {
            double pct   = model.getFinalPercentage(s.getStuID(), courseID);
            Grade grade  = Grade.fromScore(pct);
            double gpa   = model.calculateGPA(s.getStuID());

            rows.add(List.of(
                    String.valueOf(index++),
                    s.getFullName(),
                    s.getEmail(),
                    String.format("%.1f%%", pct),
                    grade.name(),
                    String.format("%.2f", gpa)
            ));
        }
        return rows;
    }

    /* View all students in a course  */
    private static void chooseGradingMode(Course viewCopy){
        String cid   = viewCopy.getCourseID();
        LibraryModel model = TeacherController.getModel();

        boolean current = model.getCourse(cid).isUsingWeightedGrading();
        System.out.println("\n⚙️  Current mode: "
                + (current ? "Option=2 ‑ category weights" : "Option=1 ‑ total points"));

        System.out.println("""
        Choose new mode:
          1) Total points earned / total points possible
          2) Categories with weights (allows drops)
          0) Cancel
        """);
        System.out.print("👉 ");
        switch (sc.nextLine().trim()) {
            case "1" -> model.setGradingMode(cid, false);
            case "2" -> model.setGradingMode(cid, true);
            default  -> { System.out.println("❌ Cancelled."); return; }
        }
        System.out.println(" Mode saved.\n");
    }

    private static List<Student> viewRoster(TeacherController controller, Course course, SortMode sortMode) {
        List<Student> students = controller.getStudentsInCourse(course.getCourseID());
        if (students.isEmpty()) {
            System.out.println("❌ No students enrolled in this course.");
            return List.of();
        }

        switch (sortMode) {
            case FIRST_NAME -> students.sort(Student.firstNameAscendingComparator());
            case LAST_NAME -> students.sort(Student.lastNameAscendingComparator());
            case EMAIL -> students.sort(Student.userNameAscendingComparator());
        }

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("No.", "First Name", "Last Name", "Email", "Submissions"));

        int index = 1;
        for (Student s : students) {
            String stat = controller.getSubmissionStatsForStudent(s.getStuID(), course.getCourseID());
            rows.add(List.of(
                    String.valueOf(index++),
                    s.getFirstName(),
                    s.getLastName(),
                    s.getEmail(),
                    stat
            ));
        }

        String title = "👥 Roster for " + course.getCourseName() + " (sorted by " +
                sortMode.name().toLowerCase().replace("_", " ") + ")";
        TablePrinter.printDynamicTable(title, rows);

        return students;
    }

//  teacher UI
    private static void viewStudentSubmissions(TeacherController controller, String courseID, Student student) {
        List<Assignment> assignments = controller.getAssignmentsForStudentInCourse(courseID, student.getStuID());

        if (assignments.isEmpty()) {
            System.out.println("❌ This student has no assignments in this course.");
            return;
        }

        //  Cache to controller to support subsequent selection by number
        controller.setCurrentStudentAssignmentList(assignments);

        while (true) {
            List<List<String>> rows = new ArrayList<>();
            rows.add(List.of("No.", "Assignment", "Score"));

            int index = 1;
            for (Assignment a : assignments) {
                Score score = controller.getScoreForAssignment(a.getAssignmentID());
                String scoreStr = (score != null) ? score.getEarned() + "/" + score.getTotal() : "-";
                rows.add(List.of(
                        String.valueOf(index++),
                        a.getAssignmentName(),
                        scoreStr
                ));
            }

            TablePrinter.printDynamicTable("📄 Submissions for " + student.getFullName(), rows);
            System.out.println("0) 🔙 Back    [number] View assignment detail");
            System.out.print("👉 Choice: ");
            String input = sc.nextLine().trim();

            if (input.equals("0")) return;
            if (input.matches("[1-9][0-9]*")) {
                int idx = Integer.parseInt(input) - 1;
                if (idx >= 0 && idx < assignments.size()) {
                    Assignment selected = assignments.get(idx);
                    viewAssignmentDetailForTeacher(controller, selected);
                } else {
                    System.out.println("❌ Invalid selection.");
                }
            } else {
                System.out.println("❌ Invalid input.");
            }
        }
    }

    /**
     * View assignment detail for teacher.
     *
     * @param controller the controller
     * @param assignment the assignment
     */
    private static void viewAssignmentDetailForTeacher(TeacherController controller, Assignment assignment) {
        Student stu = controller.getStudent(assignment.getStudentID());
        if (stu == null) {
            System.out.println("❌ Student not found.");
            return;
        }

        Course course = controller.getCourseByAssignment(assignment.getAssignmentID());
        Score score = controller.getScoreForAssignment(assignment.getAssignmentID());
        Assignment.SubmissionStatus status = assignment.getStatus();

        System.out.println("📘 Assignment Detail: " + assignment.getAssignmentName());
        System.out.println("👤 Student: " + stu.getFullName() + " (" + stu.getStuID() + ")");
        System.out.println("🧾 Course: " + course.getCourseName());
        System.out.println("🗓️ Assigned: " + assignment.getAssignDate());
        System.out.println("⏰ Due: " + assignment.getDueDate());

        System.out.println("📌 Status: " + switch (status) {
            case UNSUBMITTED -> "⛔ Not submitted";
            case SUBMITTED_UNGRADED -> "✉️ Submitted but not graded";
            case GRADED -> " Graded";
        });

        if (score != null) {
            System.out.println("📊 Score: " + score.getEarned() + "/" + score.getTotal());
            System.out.println("🎓 Grade: " + score.getLetterGrade());
        } else {
            System.out.println("📊 Score: —");
            System.out.println("🎓 Grade: N/A");
        }

        System.out.println("⬅️ Press ENTER to return...");
        sc.nextLine();
    }


    /*
     * Manage assignment groups for a course.
     */
    private static void assignmentManage(TeacherController controller, Course course) {
        controller.refreshGroupedAssignments(course.getCourseID());
        boolean exit = false;

        while (!exit) {
            clear();
            List<String> names = controller.getSortedAssignmentNames();

            System.out.println("🗂️ Assignment Groups (Manage Mode)");
            for (int i = 0; i < names.size(); i++) {
                System.out.printf("%d) %s%n", i + 1, names.get(i));
            }
            System.out.println("a) ➕ Add new assignment group");
            System.out.println("d) 🗑️ Delete existing assignment group");
            System.out.println("g) 🎲 Generate Assignment and Grading");      // ← new line
            System.out.println(controller.isAssignmentCacheDirty()
                    ? "s) 💾 Save changes"
                    : "s)  No changes to save");
            System.out.println("0) 🔙 Back");

            System.out.print("👉 Choice: ");
            String input = sc.nextLine().trim().toLowerCase();

            switch (input) {
                case "0" -> {
                    if (controller.isAssignmentCacheDirty()) {
                        System.out.print("⚠️ Unsaved changes. Save now? (y/n): ");
                        String confirm = sc.nextLine().trim().toLowerCase();
                        if (confirm.equals("y")) controller.commitAssignmentChanges();
                        else controller.discardAssignmentChanges();
                    }
                    exit = true;
                }
                case "a" -> addNewAssignmentGroup(controller, course);
                case "d" -> deleteAssignmentGroup(controller);
                case "g" -> {
                    // Delegate to controller – UI never touches LibraryModel directly
                    controller.populateDemoDataForCourse(course.getCourseID());
                    System.out.println("✅ Demo assignments & grading generated.");
                    System.out.print("⬅️ Press ENTER to continue...");
                    sc.nextLine();
                    // After generation, you may want to refresh the cache:
                    controller.refreshGroupedAssignments(course.getCourseID());
                }
                case "s" -> controller.commitAssignmentChanges();
                default -> {
                    if (input.matches("\\d+")) {
                        int idx = Integer.parseInt(input) - 1;
                        if (idx >= 0 && idx < names.size()) {
                            System.out.println("⚠️ No edit mode yet for group: " + names.get(idx));
                            System.out.print("⬅️ Press ENTER to return...");
                            sc.nextLine();
                        }
                    }
                }
            }
        }
    }


    /**
     * Add a new assignment group to the course.
     * @param controller The TeacherController instance.
     * @param course The Course instance.
     */
    private static void addNewAssignmentGroup(TeacherController controller, Course course) {
        System.out.print("📝 Title: ");
        String title = sc.nextLine().trim();
        if (title.isEmpty()) return;

        LocalDate assignDate;
        while (true) {
            System.out.print("📆 Assigned Date (YYYY-MM-DD): ");
            String asn = sc.nextLine().trim();
            try {
                assignDate = LocalDate.parse(asn);
                break;
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Please use YYYY-MM-DD.");
            }
        }

        LocalDate dueDate;
        while (true) {
            System.out.print("📆 Due Date (YYYY-MM-DD): ");
            String due = sc.nextLine().trim();
            try {
                dueDate = LocalDate.parse(due);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Please use YYYY-MM-DD.");
                continue;
            }
            if (!dueDate.isAfter(assignDate)) {
                System.out.println("❌ Due date must be after assigned date.");
                continue;
            }
            break;
        }

        List<Student> students = controller.getStudentsInCourse(course.getCourseID());
        if (students.isEmpty()) {
            System.out.println("❌ No students in this course to assign.");
            return;
        }

        for (Student s : students) {
            Assignment a = new Assignment(
                    title,
                    s.getStuID(),
                    course.getCourseID(),
                    assignDate,
                    dueDate
            );
            controller.addAssignmentToCache(a);
        }
        controller.setAssignmentCacheDirty(true);
        System.out.println(" Assignment group added (not yet saved).");
    }


    /**
     * Delete an existing assignment group.
     * @param controller The TeacherController instance.
     */
    private static void deleteAssignmentGroup(TeacherController controller) {
        List<String> names = controller.getSortedAssignmentNames();
        if (names.isEmpty()) {
            System.out.println("❌ No groups to delete.");
            return;
        }
        System.out.print("🔢 Group number to delete: ");
        String input = sc.nextLine().trim();
        if (!input.matches("[1-9][0-9]*")) return;

        int idx = Integer.parseInt(input) - 1;
        if (idx >= 0 && idx < names.size()) {
            String name = names.get(idx);
            controller.removeAssignmentGroupFromCache(name);
            controller.setAssignmentCacheDirty(true);
            System.out.println("🗑️ Deleted group: " + name);
        }
    }

    /**
     * Select an existing student to add to the course.
     * @param controller The TeacherController instance.
     * @param course The Course instance.
     */
    private static void searchStudents(TeacherController controller, Course course) {
        System.out.print("🔍 Enter letters to match (name or email): ");
        String pattern = sc.nextLine().trim();
        if (pattern.isEmpty()) {
            System.out.println("⚠️ Empty input. Returning...");
            return;
        }

        List<Student> matched = controller.searchAvailableStudents(course.getCourseID(), pattern);
        if (matched.isEmpty()) {
            System.out.println("❌ No matching students found.");
            System.out.print("⬅️ Press ENTER to return...");
            sc.nextLine();
            return;
        }

        System.out.println("📋 Search Results:");
        for (int i = 0; i < matched.size(); i++) {
            Student s = matched.get(i);
            System.out.printf("%d) %s (%s) - %s%n",
                    i + 1, s.getFullName(), s.getStuID(), s.getEmail());
        }
        System.out.print("🔢 Enter number to add (0 to cancel): ");
        String input = sc.nextLine().trim();
        if (!input.matches("\\d+")) return;
        int idx = Integer.parseInt(input);
        if (idx <= 0 || idx > matched.size()) return;

        Student chosen = matched.get(idx - 1);
        controller.addExistingStudentToCache(chosen.getStuID(), course.getCourseID());
        System.out.println(" Added " + chosen.getFullName() + " to course.");
        System.out.print("⬅️ Press ENTER to return...");
        sc.nextLine();
    }


    /**
     * Manage students in the course.
     * @param controller The TeacherController instance.
     * @param course The Course instance.
     */
    private static void studentManage(TeacherController controller, Course course) {
        controller.refreshStudentCache(course.getCourseID());
        boolean exit = false;
        while (!exit) {
            clear();
            List<Student> cached = controller.getCachedStudents();
            System.out.println("👥 Students in Course (Manage Mode)");
            for (int i = 0; i < cached.size(); i++) {
                Student s = cached.get(i);
                System.out.printf("%d) %s (%s)\n", i+1, s.getFullName());
            }
            System.out.println("a) ➕ Add existing student");
            System.out.println("d) 🗑️ Delete existing student");
            System.out.println(controller.isStudentCacheDirty() ? "v) 💾 Save changes" : "v)  No changes to save");
            System.out.println("0) 🔙 Back");
            System.out.print("👉 Choice: ");
            String input = sc.nextLine().trim().toLowerCase();

            switch (input) {
                case "0" -> {
                    if (controller.isStudentCacheDirty()) {
                        System.out.print("⚠️ Unsaved changes. Save now? (y/n): ");
                        String confirm = sc.nextLine().trim().toLowerCase();
                        if (confirm.equals("y")) controller.commitStudentChanges();
                        else controller.discardStudentChanges();
                    }
                    exit = true;
                }
                case "a" -> selectExistingStudent(controller, course);
                case "d" -> deleteStudent(controller);
                case "v" -> controller.commitStudentChanges();
                default -> {
                    if (input.matches("\\d+")) {
                        int idx = Integer.parseInt(input) - 1;
                        if (idx >= 0 && idx < cached.size()) {
                            System.out.println("⚠️ No edit mode for: " + cached.get(idx).getFullName());
                            System.out.print("⬅️ Press ENTER to return..."); sc.nextLine();
                        }
                    }
                }
            }
        }
    }

    private static void deleteStudent(TeacherController controller) {
        List<Student> cached = controller.getCachedStudents();
        if (cached.isEmpty()) {
            System.out.println("❌ No students to delete.");
            return;
        }
        System.out.print("🔢 Student number to delete: ");
        String input = sc.nextLine().trim();
        if (!input.matches("[1-9][0-9]*")) return;

        int idx = Integer.parseInt(input) - 1;
        if (idx >= 0 && idx < cached.size()) {
            Student s = cached.get(idx);
            controller.removeStudentFromCache(s.getStuID());
            controller.setStudentCacheDirty(true);
            System.out.println("🗑️ Deleted student: " + s.getFullName());
        }
    }


    /**
     * Select an existing student to add to the course.
     * @param controller The TeacherController instance.
     * @param course The Course instance.
     */
    private static void selectExistingStudent(TeacherController controller, Course course) {
        // get current students in course
        List<Student> available = controller.getAvailableStudents(course.getCourseID());
        clear();
        if (available.isEmpty()) {
            System.out.println("❌ No available students to add.");
            System.out.print("⬅️ Press ENTER to return..."); sc.nextLine();
            return;
        }

        // print the whole table
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("No.", "First Name", "Last Name", "Email"));
        for (int i = 0; i < available.size(); i++) {
            Student s = available.get(i);
            rows.add(List.of(
                    String.valueOf(i + 1),
                    s.getFirstName(),
                    s.getLastName(),
                    s.getEmail()
            ));
        }
        TablePrinter.printDynamicTable(
                "📋 Available Students for " + course.getCourseName(), rows
        );
        while (true) {
            System.out.println("f) 🔍 Search by keyword    0) 🔙 Back");
            System.out.print("👉 Choice or Student No.: ");
            String input = sc.nextLine().trim().toLowerCase();

            if (input.equals("0")) {
                return;  // directly exit!!
            }
            else if (input.equals("f")) {
                System.out.print("🔍 Enter keyword: ");
                String kw = sc.nextLine().trim();
                List<Student> matched = controller.searchAvailableStudents(
                        course.getCourseID(), kw
                );
                if (matched.isEmpty()) {
                    System.out.println("❌ No matching students.");
                    System.out.print("⬅️ Press ENTER to continue..."); sc.nextLine();
                } else {
                    rows.clear();
                    rows.add(List.of("No.", "First Name", "Last Name", "Email"));
                    for (int i = 0; i < matched.size(); i++) {
                        Student s = matched.get(i);
                        rows.add(List.of(
                                String.valueOf(i + 1),
                                s.getFirstName(),
                                s.getLastName(),
                                s.getEmail()
                        ));
                    }
                    TablePrinter.printDynamicTable(
                            "🔎 Search Results for '" + kw + "'", rows
                    );
                    available = matched;
                }
            }
            else if (input.matches("\\d+")) {
                int idx = Integer.parseInt(input);
                if (idx > 0 && idx <= available.size()) {
                    Student chosen = available.get(idx - 1);

                    String err = controller.addExistingStudentToCache(
                            chosen.getStuID(), course.getCourseID()
                    );
                    if (err == null) {
                        System.out.println("✅ Added " + chosen.getFullName());
                    } else {
                        System.out.println("❌ " + err);
                    }
                } else {
                    System.out.println("❌ Number out of range.");
                }
            } else {
                System.out.println("❌ Invalid input, enter number, 'f', or '0'");
            }
        }
    }



    private static void showClassAverages(TeacherController ctl, Course course) {
        List<String> groups = ctl.getSortedAssignmentNames();   // Uses current sort order
        LibraryModel m = TeacherController.getModel();

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Assignment", "Average %", "Median %"));
        for (String name : groups) {
            double avg = m.getAveragePercentageForGroup(course.getCourseID(), name);
            double med = m.getMedianPercentageForGroup(course.getCourseID(), name);
            rows.add(List.of(name, String.format("%.2f", avg), String.format("%.2f", med)));
        }
        TablePrinter.printDynamicTable("📈 Class averages (" + course.getCourseName() + ")", rows);
        pause("");
    }

    private static void showUngraded(TeacherController ctl, Course course) {
        List<String> ungraded = ctl.getUngradedAssignmentIDs(course.getCourseID());
        if (ungraded.isEmpty()) {
            System.out.println("✅ All submissions graded.");
        } else {
            System.out.println("❗ Ungraded submissions: " + ungraded.size());
            System.out.println(String.join(", ", ungraded));
        }
        pause("");
    }

    private static void showOverallAverage(LibraryModel m, String cid) {
        double pct = m.getOverallClassAverage(cid);
        System.out.printf("Current overall average: %.2f%%\n\n", pct);
        pause("");
    }

    private static void assignFinalGrades(LibraryModel m, String cid) {
        Map<String, Grade> map = m.assignFinalLetterGrades(cid);
        System.out.println("Final letter grades assigned for " + map.size() + " students:");
        map.forEach((sid, g) -> System.out.printf("  %s → %s%n", sid, g));
        pause("");
    }

    private static void pause(String msg) {
        if (!msg.isEmpty()) System.out.println(msg);
        System.out.println("⬅️ Press ENTER to return...");
        sc.nextLine();
    }

    private static void analyticsMenu(TeacherController ctl, Course course) {
        LibraryModel m = TeacherController.getModel();
        String cid = course.getCourseID();

        while (true) {
            clear();
            System.out.println("📊 Analytics for " + course.getCourseName());
            System.out.println("1) View class averages per assignment");
            System.out.println("2) View ungraded assignments");
            System.out.println("3) Overall course average");
            System.out.println("4) Assign final letter grades (overwrite)");
            System.out.println("5) Set or edit category weights and drop rules");
            System.out.println("0) 🔙 Back");
            System.out.print("👉 Choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> showClassAverages(ctl, course);
                case "2" -> showUngraded(ctl, course);
                case "3" -> showOverallAverage(m, cid);
                case "4" -> assignFinalGrades(m, cid);
                case "5" -> setCategoryWeightsAndDrops(course);
                case "0" -> { return; }
                default  -> { System.out.println("❌ Invalid option."); pause(""); }
            }
        }
    }

    private static void groupStudentsInCourse(TeacherController ctl, Course course) {
        List<Student> roster = ctl.getStudentsInCourse(course.getCourseID());
        if (roster.size() < 2) {
            System.out.println("❌ Need at least 2 students to form groups.");
            pause(""); return;
        }
        int groups = 0;
        while (true) {
            System.out.printf("🔢 How many groups (2-%d)? ", roster.size() - 1);
            String in = sc.nextLine().trim();
            try {
                groups = Integer.parseInt(in);
                if (groups >= 2 && groups < roster.size()) break;
            } catch (NumberFormatException ignored) { }
            System.out.println("❌ Invalid number – try again.");
        }

        Collections.shuffle(roster);

        int base  = roster.size() / groups;
        int extra = roster.size() % groups;

        System.out.println("\n📦 Student Groups for " + course.getCourseName());
        int idx = 0;
        for (int g = 0; g < groups; g++) {
            int size = base + (g < extra ? 1 : 0);
            List<Student> sub = roster.subList(idx, idx + size);
            idx += size;

            char label = (char) ('A' + g);     // A, B, C…
            System.out.print("  Group " + label + ": ");
            for (int i = 0; i < sub.size(); i++) {
                Student s = sub.get(i);
                System.out.print(s.getFirstName() + " " + s.getLastName());
                if (i < sub.size() - 1) System.out.print(", ");
            }
            System.out.println();
        }
        pause("");
    }
}

