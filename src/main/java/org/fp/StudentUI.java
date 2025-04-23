package org.fp;

import com.openai.client.OpenAIClient;

import java.time.LocalDate;
import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;
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
    private static StudentController StudentController;
    private static final LocalDate SYSTEM_DATE = LocalDate.of(2025, 4, 15);
    private static String stuID;
    // GPT client – created lazily
    private static OpenAIClient gpt;

    /* =============================================================
     *  Entry
     * ============================================================= */

    public static void main(String[] args) {
        LibraryModel model = new LibraryModel();
        try {
            model.loadStudentsFromDirectory(Paths.get("src/main/DataBase/Students"));
        } catch (IOException e) {
            System.err.println("❌ 无法加载学生列表：" + e.getMessage());
            return;
        }
        model.stateStudent();

        StudentController controller = new StudentController(model);
        String firstId = model.getFirstStudentID();
        controller.setCurrentStudent(firstId);

        level_1(controller, sc);
    }


    /* =============================================================
     *  LEVEL‑1  – list courses with dynamic sort & GPA option
     * ============================================================= */


    private static void level_1(StudentController studentController, Scanner sc) {
        StudentController.CourseSort sort = BaseController.CourseSort.NONE;


        while (true) {
            Student currentStudent = studentController.getCurrentStudent();
            if (currentStudent == null) {
                System.out.println("❌ No student is logged in.");
                return;
            }

            String stuName = currentStudent.getFullName();
            studentController.loadStudentCourses();                     // 加载数据
            studentController.sortCachedCourses(sort);                  // 排序
            List<List<String>> courseData = studentController.getFormattedCourseListForDisplayRows();

            if (courseData.isEmpty()) {
                System.out.println("❌ No courses found.");
                return;
            }

            printCourseTable(stuName, new ArrayList<>(courseData), sort);

            System.out.println("1) 🔍 Select a course    s) 🔀 Change sort    g) 📈 GPA    0) 🚪 Exit");
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
            if (choice.matches("[1-" + courseData.size() + "]")) {
                int index = Integer.parseInt(choice) - 1;
                Course selected = studentController.getCachedCourse(index);
                level_2(studentController, selected); // 进入下一级
            } else {
                System.out.println("❌ Invalid choice. Enter again.");
            }
        }
    }

    private static void printCourseTable(String stuName, List<List<String>> data, StudentController.CourseSort mode) {
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("No.", "Course Name", "Description"));

        int idx = 1;
        for (List<String> row : data) {
            String courseName = row.size() > 0 ? row.get(0) : "";
            String courseDesc = row.size() > 1 ? row.get(1) : "";
            rows.add(List.of(String.valueOf(idx++), courseName, courseDesc));
        }

        String title = String.format("Courses of %s (sorted by %s)", stuName, mode.name().toLowerCase());
        TablePrinter.printDynamicTable(title, rows);
    }



    private static void showGPA(StudentController studentController) {
        Student current = studentController.getCurrentStudent();
        if (current == null) {
            System.out.println("❌ No student selected.");
            return;
        }
        double gpa = studentController.getModel().calculateGPA(current.getStuID());
        System.out.printf("\n🎓 Your cumulative GPA: %.2f\n\n", gpa);
    }


    private static void level_2(StudentController studentController, Course course) {
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

        // Show GPT option only if graded
        if (score != null && assignment.getStatus() == Assignment.SubmissionStatus.GRADED) {
            System.out.println("g) 🤖 Get GPT feedback for this assignment");
        }
        System.out.println("⬅️ Press ENTER to return...");
        String input = sc.nextLine().trim();

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





}




/**


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
 **/
    /* ----------------  analytics  ---------------- */

/**
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
/**
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

/**
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

 **/