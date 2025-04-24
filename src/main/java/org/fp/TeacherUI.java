package org.fp;
import org.fp.TeacherController.AssignmentSort;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;


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
 * Where a helper does not yet exist we mark it with a TODO comment so you can add it.
 */
public class TeacherUI {

    /* =============================================================
     *  Runtime state & helpers
     * ============================================================= */
    private static final Scanner sc = new Scanner(System.in);
    private static TeacherController TeacherController;
    private static final LocalDate SYSTEM_DATE = LocalDate.of(2025, 4, 2);
    // GPT client – created lazily
    private static OpenAIClient GPT;
    private static LibraryModel model;



    /* =============================================================
     *  Entry
     * ============================================================= */

    /* =============================================================
     *  Entry point – simple username prompt for now
     * ============================================================= */
    public static void start(LibraryModel modelInstance, String teacherID) {
        // 1. 将传入的 model 保存在静态字段
        TeacherUI.model = modelInstance;

        // 2. 用这个 model 创建一个新的 Controller
        TeacherController teacherController = new TeacherController(TeacherUI.model);

        // 3. 设置当前教师为登录时传入的 ID
        //    （你也可以在这里做一次校验：model.containsTeacher(teacherID)）
        teacherController.setCurrentTeacher(teacherID);

        // 4. 进入主菜单
        level_1(teacherController, sc);
    }

    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }


    /* =============================================================
     *  DASHBOARD – list courses taught by this instructor
     * ============================================================= */
    private static void level_1(TeacherController controller, Scanner sc) {
        TeacherController.CourseSort sort = BaseController.CourseSort.NONE;

        while (true) {
            Teacher teacher = controller.getCurrentTeacher();
            if (teacher == null) {
                System.out.println("❌ No teacher is logged in.");
                return;
            }

            String teacherName = teacher.getFullName();
            controller.loadTeacherCourses();                     // 加载课程
            controller.sortCachedCourses(sort);                  // 排序
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
            System.out.println(controller.isCourseCacheDirty() ? "s) 💾 Save changes" : "s) ✅ No changes to save");
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
                    System.out.println("✅ Course added (not yet saved).");
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
            System.out.println("❌ No teacher logged in.");
            return;
        }

        while (true) {
            clear();
            System.out.println("📘 Course: " + course.getCourseName());
            System.out.println("👩‍🏫 Instructor: " + teacher.getFullName());
            System.out.println("==============================");

            switch (view) {
                case ASSIGNMENTS -> viewAssignments(controller, course, sort, filterActive);
                case ROSTER      -> viewRoster(controller, course, rosterSort);
            }

            System.out.println();
            System.out.print(
                    view == ViewMode.ASSIGNMENTS
                            ? "a) 📄 Assignments    r) 👥 Roster    g) 🏁 Final Grades    s) 🔍 Search    f) 🧮 Filter    o) 🔀 Sort    d) 🛠️ Assignments Manage    0) 🔙 Back\n"
                            : "a) 📄 Assignments    r) 👥 Roster    g) 🏁 Final Grades    f) 🧮 Filter    o) 🔀 Sort    d) 👩‍🏫 Roster Manage    0) 🔙 Back\n"
            );
            System.out.print("👉 Choice: ");
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

            System.out.println("❌ Invalid input.");
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
                    allPublished ? "✅ Yes" : "❌ No"
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
                    allPublished ? "✅ Yes" : "❌ No"
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

    private static void viewAssignmentDetails(TeacherController controller, String groupName, List<Assignment> group) {
        GradeSort gradeSort = GradeSort.NONE;

        while (true) {
            if (group == null || group.isEmpty()) {
                System.out.println("❌ No assignments found for: " + groupName);
                return;
            }

            // ⏳ 排序（如果启用）
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

            // ✅ 缓存当前 group 用于后续选择操作
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
                    case GRADED -> "✅ Graded";
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


    private static void viewFinalGrades(String courseID) {
        List<List<String>> table = getFinalGradesForCourse(courseID);
        TablePrinter.printDynamicTable("📋 Final Grades with GPA", table);
        System.out.println("⬅️ Press ENTER to return...");
        sc.nextLine();
    }

    private static void setCategoryWeightsAndDrops(Course viewCopy) {
        String cid   = viewCopy.getCourseID();
        LibraryModel model = TeacherController.getModel();
        model.setGradingMode(cid, true);                 // keep as is

        /* ---------- NEW BLOCK ① : show existing table ---------- */
        Course snap = model.getCourse(cid);              // safe copy
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
        /* ---------- END BLOCK ① ---------- */

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

        System.out.println("\n✅ Grading mode set to category‑based. Use analytics to verify.\n");
    }

    private static List<List<String>> getFinalGradesForCourse(String courseID) {
        LibraryModel model = TeacherController.getModel();
        Course course = model.getCourse(courseID);          // 拿到课程，看它是不是加权
        boolean weighted = course.isUsingWeightedGrading();

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Student ID", "Full Name", "Email",
                weighted ? "Weighted %" : "Raw %",
                "Grade", "GPA"));

        for (Student s : TeacherController.getStudentsInCourse(courseID)) {
            double pct   = model.getFinalPercentage(s.getStuID(), courseID);   // ⭐ 直接调用
            Grade grade  = Grade.fromScore(pct);
            double gpa   = model.calculateGPA(s.getStuID());

            rows.add(List.of(
                    s.getStuID(),
                    s.getFullName(),
                    s.getEmail(),
                    String.format("%.1f%%", pct),
                    grade.name(),
                    String.format("%.2f", gpa)
            ));
        }
        return rows;
    }

    private static void chooseGradingMode(Course viewCopy){
        String cid   = viewCopy.getCourseID();
        LibraryModel model = TeacherController.getModel();

        boolean current = model.getCourse(cid).isUsingWeightedGrading();
        System.out.println("\n⚙️  Current mode: "
                + (current ? "Option 2 ‑ category weights" : "Option 1 ‑ total points"));

        System.out.println("""
        Choose new mode:
          1) Option 1  – Total points earned / total points possible
          2) Option 2  – Categories with weights (allows drops)
          0) Cancel
        """);
        System.out.print("👉 ");
        switch (sc.nextLine().trim()) {
            case "1" -> model.setGradingMode(cid, false);
            case "2" -> model.setGradingMode(cid, true);
            default  -> { System.out.println("❌ Cancelled."); return; }
        }
        System.out.println("✅ Mode saved.\n");
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

        return students; // ✅ 返回排序后的列表
    }



    // 👇 教师 UI：在 Roster 视角下选择某位学生后，查看该学生的所有作业提交记录
    private static void viewStudentSubmissions(TeacherController controller, String courseID, Student student) {
        List<Assignment> assignments = controller.getAssignmentsForStudentInCourse(courseID, student.getStuID());

        if (assignments.isEmpty()) {
            System.out.println("❌ This student has no assignments in this course.");
            return;
        }

        // ✅ 缓存到 controller 以支持后续按序号选择
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
            case GRADED -> "✅ Graded";
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


    private static void assignmentManage(TeacherController controller, Course course) {
        controller.refreshGroupedAssignments(course.getCourseID());
        boolean exit = false;

        while (!exit) {
            clear();
            List<String> names = controller.getSortedAssignmentNames();

            System.out.println("🗂️ Assignment Groups (Manage Mode)");
            for (int i = 0; i < names.size(); i++) {
                System.out.printf("%d) %s\n", i + 1, names.get(i));
            }
            System.out.println("a) ➕ Add new assignment group");
            System.out.println("d) 🗑️ Delete existing assignment group");
            System.out.println(controller.isAssignmentCacheDirty() ? "s) 💾 Save changes" : "s) ✅ No changes to save");
            System.out.println("0) 🔙 Back");

            System.out.print("👉 Choice: ");
            String input = sc.nextLine().trim();

            switch (input.toLowerCase()) {
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
                case "s" -> controller.commitAssignmentChanges();
                default -> {
                    if (input.matches("[1-9][0-9]*")) {
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
        System.out.println("✅ Assignment group added (not yet saved).");
    }


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
        System.out.println("✅ Added " + chosen.getFullName() + " to course.");
        System.out.print("⬅️ Press ENTER to return...");
        sc.nextLine();
    }


    private static void studentManage(TeacherController controller, Course course) {
        controller.refreshStudentCache(course.getCourseID());
        boolean exit = false;
        while (!exit) {
            clear();
            List<Student> cached = controller.getCachedStudents();
            System.out.println("👥 Students in Course (Manage Mode)");
            for (int i = 0; i < cached.size(); i++) {
                Student s = cached.get(i);
                System.out.printf("%d) %s (%s)\n", i+1, s.getFullName(), s.getStuID());
            }
            System.out.println("a) ➕ Add existing student");
            System.out.println("d) 🗑️ Delete existing student");
            System.out.println(controller.isStudentCacheDirty() ? "v) 💾 Save changes" : "v) ✅ No changes to save");
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

    private static void addNewStudent(TeacherController controller, Course course) {
        System.out.print("🧍 First Name: ");
        String fname = sc.nextLine().trim();
        System.out.print("🧍 Last Name: ");
        String lname = sc.nextLine().trim();
        System.out.print("📧 Email: ");
        String email = sc.nextLine().trim();

        Student s = new Student(fname, lname, email);
        controller.addStudentToCache(s);

        // 加入课程
        controller.addCourseToStudentCache(s.getStuID(), course.getCourseID());

        // 添加所有现有作业（一个副本）
        for (Assignment a : controller.getAllAssignmentsInCourse(course.getCourseID())) {
            Assignment newA = new Assignment(a.getAssignmentName(), s.getStuID(), course.getCourseID(), a.getAssignDate(), a.getDueDate());
            controller.addAssignmentToCache(newA);
        }
        controller.setStudentCacheDirty(true);
        System.out.println("✅ Student added (not yet saved).\n");
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


    private static void selectExistingStudent(TeacherController controller, Course course) {
        while (true) {
            List<Student> available = controller.getAvailableStudents(course.getCourseID());
            clear();
            if (available.isEmpty()) {
                System.out.println("❌ No available students to add.");
                System.out.print("⬅️ Press ENTER to return..."); sc.nextLine();
                return;
            }
            List<List<String>> rows = new ArrayList<>();
            rows.add(List.of("No.", "First Name", "Last Name", "Email"));
            for (int i=0; i<available.size(); i++) {
                Student s = available.get(i);
                rows.add(List.of(
                        String.valueOf(i+1), s.getFirstName(), s.getLastName(), s.getEmail()
                ));
            }
            TablePrinter.printDynamicTable("📋 Available Students for " + course.getCourseName(), rows);

            System.out.println("f) 🔍 Search by keyword    0) 🔙 Back");
            System.out.print("👉 Choice or Student No.: ");
            String input = sc.nextLine().trim().toLowerCase();

            if (input.equals("0")) return;
            if (input.equals("f")) {
                System.out.print("🔍 Enter keyword: ");
                String kw = sc.nextLine().trim();
                List<Student> matched = controller.searchAvailableStudents(course.getCourseID(), kw);
                if (matched.isEmpty()) {
                    System.out.println("❌ No matching students.");
                    System.out.print("⬅️ Press ENTER to return..."); sc.nextLine();
                    continue;
                }
                rows.clear();
                rows.add(List.of("No.", "First Name", "Last Name", "Email"));
                for (int i=0; i<matched.size(); i++) {
                    Student s = matched.get(i);
                    rows.add(List.of(
                            String.valueOf(i+1), s.getFirstName(), s.getLastName(), s.getEmail()
                    ));
                }
                TablePrinter.printDynamicTable("🔎 Search Results for '"+kw+"'", rows);
                System.out.print("🔢 Enter number to add or 0 to cancel: ");
                String sel = sc.nextLine().trim();
                if (sel.matches("\\d+")) {
                    int idx = Integer.parseInt(sel);
                    if (idx>0 && idx<=matched.size()) {
                        Student chosen = matched.get(idx-1);
                        controller.addExistingStudentToCache(chosen.getStuID(), course.getCourseID());
                        System.out.println("✅ Added " + chosen.getFullName());
                        System.out.print("⬅️ Press ENTER to return..."); sc.nextLine();
                    }
                }
                continue;
            }
            if (input.matches("\\d+")) {
                int idx = Integer.parseInt(input);
                if (idx>0 && idx<=available.size()) {
                    Student chosen = available.get(idx-1);
                    controller.addExistingStudentToCache(chosen.getStuID(), course.getCourseID());
                    System.out.println("✅ Added " + chosen.getFullName());
                    System.out.print("⬅️ Press ENTER to return..."); sc.nextLine();
                }
                return;
            }
            System.out.println("❌ Invalid input, enter number, 'f', or '0'");
        }
    }






}


    /**

    private enum SortMode { FIRST, LAST, USERNAME, ASSIGN }

    private static void courseMenu(String teacherUser, Course course) {
        String cid = course.getCourseID();
        while (true) {
            clear();
            System.out.println("📘  " + course.getCourseName());
            System.out.println("1) 👥  View / sort roster & grades");
            System.out.println("2) ➕  Add assignment      3) ❌ Remove assignment");
            System.out.println("4) ➕  Add student         5) ❌ Remove student");
            System.out.println("6) ⬆️  Import students (CSV)   7) ✏️  Enter / update grades");
            System.out.println("8) 📊  Analytics & final grades");
            System.out.println("9) 🤖  GPT tools");
            System.out.println("0) 🔙  Back");
            System.out.print("👉  Choice: ");
            switch (SC.nextLine().trim()) {
                case "1" -> rosterMenu(cid, SortMode.FIRST);
                case "2" -> addAssignment(cid);
                case "3" -> removeAssignment(cid);
                case "4" -> addStudent(cid);
                case "5" -> removeStudent(cid);
                case "6" -> importStudents(cid);
                case "7" -> gradeEntryWizard(cid);
                case "8" -> analyticsMenu(cid);
                case "9" -> gptMenu(cid);
                case "0" -> {return;}
                default  -> pause("Invalid option");
            }
        }
    }

    private static void rosterMenu(String cid, SortMode mode) {
        while (true) {
            clear();
            ArrayList<ArrayList<String>> rows = MODEL.getGradeRows(cid); // each [stuID, g1, g2, ..., avg]
            if (rows.isEmpty()) { pause("No students enrolled."); return; }
            sortRoster(rows, mode, cid);
            PRINTER.printDynamicTable("Roster (sorted by " + mode.name().toLowerCase() + ")", rows);
            System.out.println("s) 🔀  Change sort    0) 🔙  Back");
            System.out.print("👉  Choice: ");
            String in = SC.nextLine().trim();
            if (in.equals("0")) return;
            if (in.equalsIgnoreCase("s")) mode = next(mode);
        }
    }

    private static SortMode next(SortMode m) {
        return switch (m) {
            case FIRST -> SortMode.LAST;
            case LAST  -> SortMode.USERNAME;
            case USERNAME -> SortMode.ASSIGN;
            case ASSIGN -> SortMode.FIRST;
        };
    }

    private static void sortRoster(List<ArrayList<String>> rows, SortMode mode, String cid) {
        // the first row is header – skip sorting it
        List<ArrayList<String>> body = rows.subList(1, rows.size());
        switch (mode) {
            case FIRST -> body.sort(Comparator.comparing(r -> MODEL.getStudentFirstName(r.get(0)))); // TODO add helper
            case LAST  -> body.sort(Comparator.comparing(r -> MODEL.getStudentLastName(r.get(0))));  // TODO
            case USERNAME -> body.sort(Comparator.comparing(r -> r.get(0)));
            case ASSIGN -> {
                // sort descending by average column (last col)
                int avgCol = rows.get(0).size() - 1;
                body.sort(Comparator.comparingDouble((ArrayList<String> r) -> Double.parseDouble(r.get(avgCol))).reversed());
            }
        }
    }


    private static void addAssignment(String cid) {
        System.out.print("Identifier: ");
        String id = SC.nextLine().trim();
        System.out.print("Description: ");
        String desc = SC.nextLine().trim();
        System.out.print("Max points: ");
        int max = Integer.parseInt(SC.nextLine().trim());
        MODEL.createAssignment(cid, id, desc, max); // TODO add helper (wraps addAssignmentToCourse)
        pause("✅ Assignment added.");
    }

    private static void removeAssignment(String cid) {
        System.out.print("Assignment ID to remove: ");
        String aid = SC.nextLine().trim();
        if (!MODEL.assignmentExists(cid, aid)) { pause("❌ Not found."); return; }
        MODEL.removeAssignmentFromCourse(aid, cid);
        pause("✅ Removed.");
    }


    private static void addStudent(String cid) {
        System.out.print("Student ID: ");
        String sid = SC.nextLine().trim();
        MODEL.addStudentToCourse(sid, cid);
        pause("✅ Student added.");
    }

    private static void removeStudent(String cid) {
        System.out.print("Student ID: ");
        String sid = SC.nextLine().trim();
        MODEL.removeStudentFromCourse(sid, cid);
        pause("✅ Student removed.");
    }

    private static void importStudents(String cid) {
        System.out.print("Path to CSV: ");
        String path = SC.nextLine().trim();
        MODEL.importStudentAddToCourse(path, cid);
        pause("✅ Students imported.");
    }


    private static void gradeEntryWizard(String cid) {
        System.out.print("Assignment ID: ");
        String aid = SC.nextLine().trim();
        if (!MODEL.assignmentExists(cid, aid)) { pause("❌ assignment not found"); return; }
        List<String> students = MODEL.viewStudentsInCourse(cid);
        int total = MODEL.getAssignmentMaxPoints(cid, aid);
        for (String sid : students) {
            System.out.printf("Score for %s (blank skip): ", sid);
            String in = SC.nextLine().trim();
            if (in.isEmpty()) continue;
            int earned = Integer.parseInt(in);
            MODEL.addGradeForStudent(sid, aid, earned, total);
        }
        pause("✅ Grades saved.");
    }


    private static void analyticsMenu(String cid) {
        while (true) {
            clear();
            System.out.println("📊 Analytics for " + MODEL.getCourseTitle(cid));
            System.out.println("1) View class averages per assignment");
            System.out.println("2) View ungraded assignments");
            System.out.println("3) Assign final letter grades");
            System.out.println("0) 🔙 Back");
            System.out.print("👉 Choice: ");
            switch (SC.nextLine().trim()) {
                case "1" -> showClassAverages(cid);
                case "2" -> showUngraded(cid);
                case "3" -> assignFinalGrades(cid);
                case "0" -> {return;}
                default -> pause("Invalid");
            }
        }
    }

    private static void showClassAverages(String cid) {
        List<String> ass = MODEL.getAssignmentIdentifiers(cid);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Assignment", "Average", "Median"));
        for (String a : ass) {
            double avg = MODEL.getAveragePercetangeAssig(a);
            double med = MODEL.getMedianPercentageAssig(a); // TODO add helper
            rows.add(List.of(a, String.format("%.2f%%", avg), String.format("%.2f%%", med)));
        }
        PRINTER.printDynamicTable("Class Averages", rows);
        pause("");
    }

    private static void showUngraded(String cid) {
        List<String> ungraded = MODEL.getAllUngradedAssignments(cid); // TODO helper
        if (ungraded.isEmpty()) { pause("All assignments graded."); return; }
        System.out.println("Ungraded assignments: " + String.join(", ", ungraded));
        pause("");
    }

    private static void assignFinalGrades(String cid) {
        MODEL.assignFinalLetterGrades(cid); // TODO helper
        pause("✅ Final grades assigned.");
    }


    private static void gptMenu(String cid) {
        while (true) {
            clear();
            System.out.println("🤖 GPT tools for " + MODEL.getCourseTitle(cid));
            System.out.println("1) Feedback for a student");
            System.out.println("2) Class performance summary & tips");
            System.out.println("0) 🔙 Back");
            System.out.print("👉 Choice: ");
            switch (SC.nextLine().trim()) {
                case "1" -> gptFeedbackStudent(cid);
                case "2" -> gptClassSummary(cid);
                case "0" -> {return;}
                default -> pause("Invalid");
            }
        }
    }

    private static void gptFeedbackStudent(String cid) {
        ensureGPT();
        System.out.print("Student ID: ");
        String sid = SC.nextLine().trim();
        if (!MODEL.isStudentInCourse(sid, cid)) { pause("Not in course"); return; }
        String grade = MODEL.getStudentCourseGrade(sid, cid);
        String prompt = "You are a teaching assistant. Provide 4 concise, actionable bullet‑point feedback for student " + sid +
                " in course " + MODEL.getCourseTitle(cid) + ". Current grade: " + grade + ".";
        String reply = callGPT(prompt);
        System.out.println("\n========== GPT Feedback =========\n" + reply + "\n==================================\n");
        pause("");
    }

    private static void gptClassSummary(String cid) {
        ensureGPT();
        double avgOverall = MODEL.getOverallClassAverage(cid); // TODO helper
        String prompt = "Summarise class performance for " + MODEL.getCourseTitle(cid) +
                " with current average " + String.format("%.2f%%", avgOverall) +
                ". Suggest two pedagogical improvements.";
        System.out.println("\n========== GPT Summary =========\n" + callGPT(prompt) + "\n=================================\n");
        pause("");
    }

    private static String callGPT(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addUserMessage(prompt)
                .build();
        ChatCompletion cp = GPT.chat().completions().create(params);
        return cp.choices().get(0).message().content().orElse("No response");
    }

    private static void ensureGPT() {
        if (GPT != null) return;
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        GPT = OpenAIOkHttpClient.builder().apiKey(env.get("OPENAI_API_KEY", "")).build();
    }


    private static void pause(String msg) {
        if (!msg.isEmpty()) System.out.println("\n" + msg);
        System.out.print("<enter> …");
        SC.nextLine();
    }

    private static void quit() {
        System.out.println("Bye ✨");
        System.exit(0);
    }

    private static void clear() {
        System.out.print("\u001B[H\u001B[2J");
        System.out.flush();
    }
 **/

