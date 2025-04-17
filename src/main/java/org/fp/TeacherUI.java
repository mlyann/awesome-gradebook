package org.fp;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import io.github.cdimascio.dotenv.Dotenv;

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
    private static final Scanner SC = new Scanner(System.in);
    private static final LibraryModel MODEL = new LibraryModel();
    private static final TablePrinter PRINTER = new TablePrinter();

    // GPT client initialised lazily
    private static OpenAIClient GPT;

    /* =============================================================
     *  Entry point – simple username prompt for now
     * ============================================================= */
    public static void main(String[] args) {
        MODEL.state(); // demo data
        while (true) {
            clear();
            System.out.print("Enter teacher username (0 to quit): ");
            String user = SC.nextLine().trim();
            if (user.equals("0")) quit();
            if (!MODEL.isValidTeacher(user)) {            // TODO add to model
                pause("❌  Unknown teacher.");
                continue;
            }
            teacherDashboard(user);
        }
    }

    /* =============================================================
     *  DASHBOARD – list courses taught by this instructor
     * ============================================================= */
    private static void teacherDashboard(String teacherUser) {
        while (true) {
            clear();
            List<Course> courses = MODEL.getCoursesTaughtBy(teacherUser); // TODO add to model
            if (courses.isEmpty()) {
                pause("You are not assigned to any courses.");
                return;
            }
            List<List<String>> rows = new ArrayList<>();
            rows.add(List.of("No.", "Course", "Description"));
            int i = 1;
            for (Course c : courses) rows.add(List.of(String.valueOf(i++), c.getCourseName(), c.getCourseDescription()));
            TablePrinter.printDynamicTable("Your Courses", rows);

            System.out.println("0) 🔙  Log out    |    number) open course");
            System.out.print("👉  Choice: ");
            String in = SC.nextLine().trim();
            if (in.equals("0")) return;
            if (in.matches("[1-" + courses.size() + "]")) {
                int pos = Integer.parseInt(in) - 1;
                courseMenu(teacherUser, courses.get(pos));
            }
        }
    }

    /* =============================================================
     *  COURSE MENU
     * ============================================================= */
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

    /* =============================================================
     *  ROSTER VIEW & SORTING
     * ============================================================= */
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

    /* =============================================================
     *  ASSIGNMENT MANAGEMENT
     * ============================================================= */
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

    /* =============================================================
     *  STUDENT MANAGEMENT
     * ============================================================= */
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

    /* =============================================================
     *  GRADE ENTRY
     * ============================================================= */
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

    /* =============================================================
     *  ANALYTICS & FINAL GRADES
     * ============================================================= */
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
        List<String> ungraded = MODEL.getUngradedAssignments(cid); // TODO helper
        if (ungraded.isEmpty()) { pause("All assignments graded."); return; }
        System.out.println("Ungraded assignments: " + String.join(", ", ungraded));
        pause("");
    }

    private static void assignFinalGrades(String cid) {
        MODEL.assignFinalLetterGrades(cid); // TODO helper
        pause("✅ Final grades assigned.");
    }

    /* =============================================================
     *  GPT TOOLS
     * ============================================================= */
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

    /* =============================================================
     *  Utility helpers
     * ============================================================= */
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
}
