package org.fp;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.*;

/**
* REQUIRED FUNCTIONS NOT CHECKED YET.
 */
public class StudentUI {

    /* =============================================================
     *  Runtime state
     * ============================================================= */
    private static final Scanner sc = new Scanner(System.in);
    private static String stuID;
    private static LibraryModel model;

    // GPT client – created lazily
    private static OpenAIClient gpt;

    /* =============================================================
     *  Entry
     * ============================================================= */
    public static void main(String[] args) {
        model = new LibraryModel();
        model.state(); // demo seed
        stuID = "20250001";//idk what is this
        level_1(CourseSort.NONE);
    }

    /* =============================================================
     *  LEVEL‑1  – list courses with dynamic sort & GPA option
     * ============================================================= */
    private enum CourseSort { NONE, NAME, STATUS }

    private static void level_1(CourseSort sort) {
        while (true) {
            String stuName = model.getStuName(stuID);
            ArrayList<ArrayList<String>> result = model.getCoursesString(stuID); // [name, desc, id]
            if (result == null || result.isEmpty()) {
                System.out.println("No courses found.");
                return;
            }
            sortCourses(result, sort);
            printCourseTable(stuName, result, sort);

            System.out.println("1) 🔍 Select a course    s) 🔀 Change sort    2) 📈 GPA    0) 🚪 Exit");
            System.out.print("👉 Choice: ");
            String choice = sc.nextLine().trim();
            if (choice.equals("0")) return;
            if (choice.equals("2")) {
                showGPA();
                continue;
            }
            if (choice.equalsIgnoreCase("s")) {
                sort = next(sort);
                continue; // refresh table
            }
            if (choice.matches("[1-" + result.size() + "]")) {
                int courseIndex = Integer.parseInt(choice) - 1;
                level_2(courseIndex, result);
            } else {
                System.out.println("❌ Invalid choice. Enter again.");
            }
        }
    }

    private static CourseSort next(CourseSort s) {
        return switch (s) {
            case NONE -> CourseSort.NAME;
            case NAME -> CourseSort.STATUS;
            case STATUS -> CourseSort.NONE;
        };
    }

    private static void sortCourses(List<ArrayList<String>> list, CourseSort mode) {
        switch (mode) {
            case NAME -> list.sort(Comparator.comparing(a -> a.get(0)));
            case STATUS -> list.sort((a, b) -> {
                boolean aCompl = model.isCourseCompleted(a.get(2));
                boolean bCompl = model.isCourseCompleted(b.get(2));
                if (aCompl != bCompl) return aCompl ? 1 : -1; // current first
                return a.get(0).compareTo(b.get(0));
            });
            case NONE -> {}
        }
    }

    private static void level_2(int courseIndex, ArrayList<ArrayList<String>> courses) {
        String courseID = courses.get(courseIndex).get(2); // third column assumed id
        while (true) {
            System.out.println("\n📘 Course: " + courses.get(courseIndex).get(0));
            System.out.println("1) 📊 View my current average & class average");
            System.out.println("2) 👥 View everyone's grade sheet");
            System.out.println("3) 📝 View my assignment grades");
            System.out.println("4) 🤖 Get GPT feedback for this course");
            System.out.println("0) 🔙 Back to course list");
            System.out.print("👉 Enter choice: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1" -> viewMyCurrentGrade(courseID);
                case "2" -> viewAllGrades(courseID);
                case "3" -> viewMyAssignmentGrades(courseID);
                case "4" -> gptFeedbackCourse(courseID);
                case "0" -> {
                    return; // back to level_1
                }
                default -> System.out.println("❌ Invalid choice. Try again.");
            }
        }
    }

    /* ----------------  analytics  ---------------- */
    private static void viewMyCurrentGrade(String courseID) {
        String grade = model.getStudentCourseGrade(stuID, courseID);
        double classAvg = model.getClassAverage(courseID);
        System.out.printf("📈 Your current grade: %s | Class average: %.2f %%\n", grade, classAvg);
    }

    private static void showGPA() {
        double gpa = model.calculateGPA(stuID);
        System.out.printf("\n🎓 Your cumulative GPA (completed courses): %.2f\n\n", gpa);
    }

    /* ----------------  GPT System Prompts  ---------------- */
    private static void gptFeedbackCourse(String courseID) {
        ensureGPT();
        String courseName = model.getCourseTitle(courseID);
        String grade = model.getStudentCourseGrade(stuID, courseID);
        String prompt = "As a TA, give the student concise advice (4 bullets) to improve in " + courseName +
                ". Current grade: " + grade + ".";
        String reply = callGPT(prompt);
        System.out.println("\n================ GPT Feedback ================\n" + reply + "\n=============================================\n");
    }

    private static String callGPT(String prompt) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4O_MINI)
                .addUserMessage(prompt)
                .build();
        ChatCompletion cp = gpt.chat().completions().create(params);
        return cp.choices().get(0).message().content().orElse("No response");
    }

    private static void ensureGPT() {
        if (gpt != null) return;
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        gpt = OpenAIOkHttpClient.builder().apiKey(env.get("OPENAI_API_KEY", "")).build();
    }

    /* =============================================================
     *  Mccann Table Printer
     * ============================================================= */
    private static void viewAllGrades(String courseID) {
        // unchanged – relies on existing model helpers and TablePrinter
        String courseTitle = model.getCourseTitle(courseID);
        List<String> identifiers = model.getAssignmentIdentifiers(courseID);
        List<String> weights = model.getAssignmentWeights(courseID);
        List<String> maxPoints = model.getAssignmentMaxPoints(courseID);
        List<String> submissionDates = model.getAssignmentSubmissions(courseID);
        List<String> resubmits = model.getAssignmentResubmitDates(courseID);
        ArrayList<ArrayList<String>> studentGrades = model.getGradeRows(courseID);

        List<List<String>> rows = new ArrayList<>();
        rows.add(prependRow("       Weight", weights));
        rows.add(prependRow("       Points", maxPoints));
        rows.add(prependRow("    Submitted", submissionDates));
        rows.add(prependRow("Resubmit Date", resubmits));
        rows.add(List.of("###SEPARATOR###"));
        rows.add(prependRow("   Identifier", identifiers));
        rows.add(List.of("###SEPARATOR###"));
        for (ArrayList<String> row : studentGrades) {
            ArrayList<String> r = new ArrayList<>(row);
            r.set(0, "   " + r.get(0));
            rows.add(r);
        }
        TablePrinter.printDynamicTable("Full Grade Sheet: " + courseTitle, rows);
    }

    private static void viewMyAssignmentGrades(String courseID) {
        ArrayList<ArrayList<String>> assgGrades = model.getStudentAssignmentGrades(stuID, courseID);
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Assignment", "Grade"));
        rows.addAll(assgGrades);
        TablePrinter.printDynamicTable("Your Assignment Grades", rows);
    }

    private static List<String> prependRow(String label, List<String> items) {
        List<String> row = new ArrayList<>();
        row.add(label);
        row.addAll(items);
        return row;
    }

    private static void printCourseTable(String stuName, ArrayList<ArrayList<String>> data, CourseSort mode) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("No.", "Course Name", "Description"));
        int idx = 1;
        for (ArrayList<String> r : data) rows.add(List.of(String.valueOf(idx++), r.get(0), r.get(1)));
        TablePrinter.printDynamicTable("Courses of " + stuName + " (sorted by " + mode.name().toLowerCase() + ")", rows);
    }
}
