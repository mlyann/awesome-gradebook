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
