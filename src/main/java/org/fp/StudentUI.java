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
        String grade = model.getStudentCourseGrade(stuID, courseID);  // 假设你有这个方法
        System.out.println("📈 Your current grade for the course is: " + grade);
    }

    private static void viewAllGrades(String courseID) {
        ArrayList<ArrayList<String>> grades = model.getAllGrades(courseID);  // 假设每项：[stuName, grade]
        List<String> header = List.of("Student Name", "Grade");
        List<List<String>> rows = new ArrayList<>();
        rows.add(header);
        for (ArrayList<String> row : grades) {
            rows.add(row);
        }
        printer.printDynamicTable("All Students' Grades for Course", rows);
    }

    private static void viewMyAssignmentGrades(String courseID) {
        ArrayList<ArrayList<String>> assgGrades = model.getStudentAssignmentGrades(stuID, courseID);
        List<String> header = List.of("Assignment", "Grade");
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
