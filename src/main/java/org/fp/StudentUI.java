package org.fp;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StudentUI {
    private static final Scanner sc = new Scanner(System.in);
    private static String stuID;
    private static LibraryModel model;
    private static TablePrinter printer;

    public static void main(String[] args) {
        StudentUI.model = new LibraryModel();
        model.state();
        stuID = "20250001";
        level_1();
    }

    /**
     *  Design for UI:
     *      |-  Level 1: List of Courses
     *          |-  Level 2: List of Assignments
     *              |-  Level 3: Grade
     */

    private static void level_1() {
        String stuName = model.getStuName(stuID);
        ArrayList<ArrayList<String>> result = model.getCoursesString(stuID);
        int length = result.size();
        printCourse(stuName, result);
        System.out.println("1) 🔍 Select the number of the course");
        System.out.println("0) 🎧 Return to the main menu");
        System.out.print("👉 Enter choice: ");
        while (true) {
            String choice = sc.nextLine();
            if (choice.equals("0")) {
                System.out.println("🎧 Returning to the main menu...");
                break;
            } else if (choice.matches("[1-" + length + "]")) {
                int courseIndex = Integer.parseInt(choice) - 1;
                level_2(courseIndex);
                break;
            } else {
                System.out.println("❌ Invalid choice. Please try again.");
            }
        }

    }


    private static void level_2(int courseIndex) {
        ArrayList<ArrayList<String>> courses = model.getCoursesString(stuID);
        String courseID = courses.get(courseIndex).get(2);  // 假设第3列是courseID

        System.out.println("\n📘 Course: " + courses.get(courseIndex).get(0));
        System.out.println("1) 📊 View my current grade");
        System.out.println("2) 👥 View all students' grades");
        System.out.println("3) 📝 View my assignment grades");
        System.out.println("0) 🔙 Back to course list");
        System.out.print("👉 Enter choice: ");

        while (true) {
            String choice = sc.nextLine();
            switch (choice) {
                case "1":
                    viewMyCurrentGrade(courseID);
                    break;
                case "2":
                    viewAllGrades(courseID);
                    break;
                case "3":
                    viewMyAssignmentGrades(courseID);
                    break;
                case "0":
                    System.out.println("🔙 Back to course list...");
                    level_1();
                    return;
                default:
                    System.out.println("❌ Invalid choice. Please try again.");
            }
            System.out.print("\n👉 Enter another choice (or 0 to go back): ");
        }
    }

    private static void viewMyCurrentGrade(String courseID) {
        String grade = model.getStudentCourseGrade(stuID, courseID);
        System.out.println("📈 Your current grade for the course is: " + grade);
    }

    private static void viewAllGrades(String courseID) {
        String courseTitle = model.getCourseTitle(courseID); // e.g. "CSc 460, Database Design (Spring,2025)"
        List<String> identifiers = model.getAssignmentIdentifiers(courseID); // e.g. ["LDAYS", "PROG1", ..., "EXAM1"]
        List<String> weights = model.getAssignmentWeights(courseID);         // e.g. ["0", "6", ..., "20"]
        List<String> maxPoints = model.getAssignmentMaxPoints(courseID);     // e.g. ["5", "100", ..., "90"]
        List<String> submissionDates = model.getAssignmentSubmissions(courseID);
        List<String> resubmits = model.getAssignmentResubmitDates(courseID);
        ArrayList<ArrayList<String>> studentGrades = model.getGradeRows(courseID); // each: [stuID, g1, g2, ..., avg]

        List<List<String>> rows = new ArrayList<>();

        // Metadata rows
        rows.add(prependRow("       Weight", weights));
        rows.add(prependRow("       Points", maxPoints));
        rows.add(prependRow("    Submitted", submissionDates));
        rows.add(prependRow("Resubmit Date", resubmits));
        rows.add(List.of("###SEPARATOR###")); // separator for metadata & headers

        rows.add(prependRow("   Identifier", identifiers));
        rows.add(List.of("###SEPARATOR###"));

        // Student grade rows
        for (ArrayList<String> row : studentGrades) {
            List<String> formattedRow = new ArrayList<>(row);
            formattedRow.set(0, String.format("   %s", row.get(0))); // format ID
            rows.add(formattedRow);
        }

        printer.printDynamicTable("Full Grade Sheet: " + courseTitle, rows);
    }

    private static List<String> prependRow(String label, List<String> items) {
        List<String> newRow = new ArrayList<>();
        newRow.add(label);
        newRow.addAll(items);
        return newRow;
    }


    private static void viewMyAssignmentGrades(String courseID) {
        ArrayList<ArrayList<String>> assgGrades = model.getStudentAssignmentGrades(stuID, courseID);
        List<String> header = List.of("Assignment", "Grade"); // implement here yourself @Jerry
        List<List<String>> rows = new ArrayList<>();
        rows.add(header);
        for (ArrayList<String> row : assgGrades) {
            rows.add(row);
        }
        printer.printDynamicTable("Your Assignment Grades", rows);
    }

    private static void printCourse(String stuName, ArrayList<ArrayList<String>> result ) {
        List<String> header = new ArrayList<>();
        header.add("No.");
        header.add("Course Name");
        header.add("Course Description");
        List<List<String>> tableRows = new ArrayList<>();
        tableRows.add(header);
        int index = 1;
        for (List<String> row : result) {
            List<String> newRow = new ArrayList<>();
            newRow.add(String.valueOf(index++));
            newRow.add(row.get(0));
            newRow.add(row.get(1));
            tableRows.add(newRow);
        }
        printer.printDynamicTable("Courses of " + stuName, tableRows);
    }

    private static void level_2(String stuName) {}






}
