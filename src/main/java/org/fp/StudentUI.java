package org.fp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import io.github.cdimascio.dotenv.Dotenv;
/**
* REQUIRED FUNCTIONS NOT CHECKED YET.
 */
public class StudentUI {

    /* =============================================================
     *  Runtime state
     * ============================================================= */
    private static final Scanner sc = new Scanner(System.in);
    private static StudentController studentController;
    private static final LocalDate SYSTEM_DATE = LocalDate.of(2025,4,6);
    private static LibraryModel model;    // ← 共享的 model
    // GPT client – created lazily
    private static OpenAIClient gpt;
    /* =============================================================
     *  Entry
     * ============================================================= */

    public static void start(LibraryModel modelInstance, String studentID) {
        // 1) use this model instance
        StudentUI.model = modelInstance;

        // 2) StudentController
        studentController = new StudentController(StudentUI.model);

        // 3) Check if student exists
        if (!StudentUI.model.studentExists(studentID)) {
            System.out.println("❌ 学生 ID 不存在: " + studentID);
            return;
        }
        studentController.setCurrentStudent(studentID);

        // 4) Clear the console
        clear();

        // 5) Start the main menu
        level_1(studentController, sc);
    }

    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }


    /* =============================================================
     *  LEVEL‑1  – list courses with dynamic sort & GPA option
     * ============================================================= */


    private static void level_1(StudentController studentController, Scanner sc) {
        System.out.println("Current date: " + SYSTEM_DATE);
        StudentController.CourseSort sort = BaseController.CourseSort.NONE;

        studentController.loadStudentCourses();
        while (true) {
            Student currentStudent = studentController.getCurrentStudent();
            if (currentStudent == null) {
                System.out.println("❌ No student is logged in.");
                return;
            }

            String stuName = currentStudent.getFullName();
            studentController.loadStudentCourses();
            studentController.sortCachedCourses(sort);
            List<List<String>> courseData = studentController.getFormattedCourseListForDisplayRows();


            printCourseTable(stuName, new ArrayList<>(courseData), sort);

            System.out.println("1) 🔍 Select a course    s) 🔀 Change sort    p) \uD83E\uDD16 Personal feedback    g) 📈 GPA    0) 🚪 Exit");
            System.out.print("👉 Choice: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("0")) return;
            if (choice.equals("g")) {
                showGPA(studentController);
                continue;
            }
            if (choice.equalsIgnoreCase("s")) {
                sort = BaseController.nextCourseSort(sort);

                continue;
            }
            if (choice.equalsIgnoreCase("p")) {
                String report = studentController.getModel()
                        .buildGradeReport(currentStudent.getStuID());

                String prompt = """
                You are an encouraging academic advisor.
                Below is the student's current grade report.
        
                %s
        
                Write a short (4-5 sentence) personalised message:
                  – start with a warm greeting
                  – praise one area of strength
                  – give two concrete suggestions for improvement
                  – end on a motivating note
                """.formatted(report);

                String msg = GPT.chat(prompt);
                System.out.println("\n========== 📬 GPT ADVICE =========\n" + msg +
                        "\n==================================\n");
                continue;
            }
            if (choice.matches("[1-" + courseData.size() + "]")) {
                int index = Integer.parseInt(choice) - 1;
                Course selected = studentController.getCachedCourse(index);
                level_2(studentController, selected);
            } else {
                System.out.println("❌ Invalid choice. Enter again.");
            }
        }
    }

    // StudentUI.java
    private static void printCourseTable(String stuName, List<List<String>> data, StudentController.CourseSort mode) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("No.", "Course Name", "Description", "Status"));

        int idx = 1;
        for (List<String> row : data) {
            String courseName = row.size() > 0 ? row.get(0) : "";
            String courseDesc = row.size() > 1 ? row.get(1) : "";
            String status = row.size() > 2 ? row.get(2) : "🟢 Unknown";
            rows.add(List.of(String.valueOf(idx++), courseName, courseDesc, status));
        }

        String title = String.format("Courses of %s (sorted by %s)", stuName, mode.name().toLowerCase());
        TablePrinter.printDynamicTable(title, rows);
    }


    // StudentUI.java
    private static void showGPA(StudentController ctl) {
        Student cur = ctl.getCurrentStudent();
        if (cur == null) { System.out.println("❌ No student selected."); return; }

        System.out.println("1) All enrolled courses");
        System.out.println("2) Completed courses only");
        System.out.print("👉 Choice: ");
        boolean completedOnly = "2".equals(sc.nextLine().trim());

        LibraryModel m   = ctl.getModel();
        List<String> cids = m.getStudentCourses(cur.getStuID());   // <- add this getter if missing
        if (cids.isEmpty()) { System.out.println("⚠️ No courses."); return; }

        double pctSum  = 0, ptsSum = 0;
        int    counted = 0;

        System.out.println("\n==================================================");
        System.out.printf(" %-12s │ %-9s │ %-5s │ %-6s%n",
                "Course", "Percent", "Pts", "Grade");
        System.out.println("--------------------------------------------------");

        for (String cid : cids) {
            Course c = m.getCourse(cid); // deep copy OK
            if (completedOnly && !c.isCompleted()) continue;

            double pct   = m.getFinalPercentage(cur.getStuID(), cid);
            Grade  grade = Grade.fromScore(pct);
            int    pts   = switch (grade) { case A->4; case B->3; case C->2; case D->1; case F->0; };

            System.out.printf(" %-12s │ %7.2f%% │   %d   │ %s%n",
                    c.getCourseName(), pct, pts, grade);

            pctSum  += pct;
            ptsSum  += pts;
            counted++;
        }
        if (counted == 0) { System.out.println("\n⚠️ Nothing to average."); return; }

        double avgPct = pctSum / counted;
        double avgPts = ptsSum / counted;
        Grade  avgGrade = switch ((int)Math.round(avgPts)) {
            case 4 -> Grade.A; case 3 -> Grade.B; case 2 -> Grade.C;
            case 1 -> Grade.D; default -> Grade.F;
        };

        System.out.println("--------------------------------------------------");
        System.out.printf(" OVERALL      │ %7.2f%% │ %.2f │ %s%n",
                avgPct, avgPts, avgGrade);
        System.out.println("==================================================\n");
    }

    private static void level_2(StudentController studentController, Course course) {
        System.out.println("Current date: " + SYSTEM_DATE);
        System.out.println("➡️ Entered course: " + course.getCourseName());
        System.out.println("📝 Description: " + course.getCourseDescription());

        studentController.loadAssignmentsForCourse(course.getCourseID());
        AssignmentSort sort = AssignmentSort.NONE;
        boolean onlyUnsubmitted = false;

        while (true) {
            switch (sort) {
                case NAME -> studentController.sortCachedAssignmentsByName();
                case ASSIGN_DATE -> studentController.sortCachedAssignmentsByAssignDate();
                case DUE_DATE -> studentController.sortCachedAssignmentsByDueDateAscending();
                case GRADE -> studentController.sortCachedAssignmentsByGradeAscending();
                case NONE -> {} // no sort
            }

            List<List<String>> data = studentController.getFormattedAssignmentTableRowsWithState(onlyUnsubmitted);

            List<List<String>> rows = new ArrayList<>();
            rows.add(List.of("No.", "Assignment", "State", "Assigned Date", "Due Date", "Score", "Grade", "Status"));

            int index = 1;
            for (List<String> row : data) {
                String name = row.get(0);
                String state = row.get(1);
                String assign = row.get(2);
                String due = row.get(3);
                String score = row.get(4);
                String grade = row.get(5);

                LocalDate assignDate = LocalDate.parse(assign);
                LocalDate dueDate = LocalDate.parse(due);
                String status = ProgressBar.fullBar(assignDate, dueDate, SYSTEM_DATE);

                rows.add(List.of(String.valueOf(index++), name, state, assign, due, score, grade, status));
            }

            String filterLabel = onlyUnsubmitted ? "(filter: UNSUBMITTED)" : "";
            double classAvg = studentController.getModel().calculateClassAverage(course.getCourseID());
            System.out.printf("📚 Course: %s (%s)\n", course.getCourseName(), course.getCourseDescription());
            System.out.printf("📊 Class Average (graded only): %.2f%%\n", classAvg);
            TablePrinter.printDynamicTable("Assignments for " + course.getCourseName() + " (sorted by " + sort.name().toLowerCase() + ") " + filterLabel, rows);

            System.out.println("s) 🔀 Change sort    f) 🔎 Toggle filter    0) ⬅️ Back to courses");
            System.out.print("👉 Choice: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("0")) return;
            if (choice.equalsIgnoreCase("s")) {
                sort = nextAssignmentSort(sort);
                continue;
            }
            if (choice.equalsIgnoreCase("f")) {
                onlyUnsubmitted = !onlyUnsubmitted;
                continue;
            }
            if (choice.matches("[1-9][0-9]*")) {
                int selectedIndex = Integer.parseInt(choice) - 1;
                List<Assignment> visible = studentController.getFilteredCachedAssignments(onlyUnsubmitted);
                if (selectedIndex >= 0 && selectedIndex < visible.size()) {
                    Assignment selected = visible.get(selectedIndex);
                    studentController.setCurrentAssignment(selected);
                    level_3(studentController, selected);
                } else {
                    System.out.println("❌ Invalid assignment number.");
                }
            }
        }
    }

    private enum AssignmentSort { NONE, NAME, ASSIGN_DATE, DUE_DATE, GRADE }

    private static AssignmentSort nextAssignmentSort(AssignmentSort current) {
        return switch (current) {
            case NONE -> AssignmentSort.NAME;
            case NAME -> AssignmentSort.ASSIGN_DATE;
            case ASSIGN_DATE -> AssignmentSort.DUE_DATE;
            case DUE_DATE -> AssignmentSort.GRADE;
            case GRADE -> AssignmentSort.NONE;
        };
    }

    private static void level_3(StudentController studentController, Assignment assignment) {
        System.out.println("Current date: " + SYSTEM_DATE);
        System.out.println("📘 Assignment Detail: " + assignment.getAssignmentName());
        System.out.println("🧾 Course: " + assignment.getCourseID());
        System.out.println("🗓️ Assigned: " + assignment.getAssignDate());
        System.out.println("⏰ Due: " + assignment.getDueDate());

        Score score = studentController.getScoreForAssignment(assignment.getAssignmentID());
        Assignment.SubmissionStatus status = assignment.getStatus();

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
        }
        System.out.println("===============================================");

        if (status == Assignment.SubmissionStatus.UNSUBMITTED
                && !SYSTEM_DATE.isBefore(assignment.getAssignDate())
                && !SYSTEM_DATE.isAfter(assignment.getDueDate())) {
            System.out.println("s) 📝 Submit assignment");
        }

        // Show GPT option only if graded
        if (score != null && assignment.getStatus() == Assignment.SubmissionStatus.GRADED) {
            System.out.println("g) 🤖 Get GPT feedback for this assignment");
        }
        System.out.println("⬅️ Press ENTER to return...");
        String input = sc.nextLine().trim();

        if (input.equalsIgnoreCase("s")) {
            if (status != Assignment.SubmissionStatus.UNSUBMITTED) {
                System.out.println("⛔ This assignment has already been submitted.");
            } else if (SYSTEM_DATE.isBefore(assignment.getAssignDate())
                    || SYSTEM_DATE.isAfter(assignment.getDueDate())) {
                System.out.println("⛔ You cannot submit on this date.");
            } else {
                studentController.submitAssignment(assignment.getAssignmentID());
                System.out.println("✅ Assignment submitted successfully.");
            }
            System.out.print("⬅️ Press ENTER to return...");
            sc.nextLine();
            return;
        }

        // Only trigger GPT feedback if available
        if (input.equalsIgnoreCase("g")) {
            if (score == null || assignment.getStatus() != Assignment.SubmissionStatus.GRADED) {
                System.out.println("⛔ Feedback is only available for graded assignments.");
                return;
            }

            Student student = studentController.getCurrentStudent();
            Course course = studentController.getCourseByAssignment(assignment.getAssignmentID());

            if (student == null || course == null) {
                System.out.println("❌ Missing student or course info.");
                return;
            }

            ensureGPT();

            String prompt = String.format(
                    """
                    You are a teaching assistant at the University of Arizona. 
                    Please write a short, polite feedback email (around 4 sentences) to the student %s (%s) regarding their performance on the assignment "%s", part of the course "%s" (%s).
            
                    The student scored %d out of %d. 
                    Provide two clear suggestions for improvement in a kind tone. Sign the email as "TA Team, University of Arizona".
                    """,
                    student.getFullName(), student.getEmail(),
                    assignment.getAssignmentName(),
                    course.getCourseName(), course.getCourseDescription(),
                    score.getEarned(), score.getTotal()
            );

            System.out.println("\n📤 Sending prompt to GPT...");
            String feedback = callGPT(prompt);
            System.out.println("\n========== 📩 GPT Feedback Email =========\n" + feedback + "\n==========================================\n");
            System.out.print("⬅️ Press ENTER to return...");
            sc.nextLine();
        }
    }

    private static void ensureGPT() {
        if (gpt != null) return;
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        gpt = OpenAIOkHttpClient.builder().apiKey(env.get("OPENAI_API_KEY", "")).build();
    }

    private static String callGPT(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addUserMessage(prompt)
                .build();
        ChatCompletion cp = gpt.chat().completions().create(params);
        return cp.choices().get(0).message().content().orElse("No response");
    }

}
