// File: AdminUI.java
package org.fp;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminUI {
    private static final Scanner sc = new Scanner(System.in);

    public static void start(LibraryUsers users, VICData vic, LibraryModel model) {
        while (true) {
            System.out.println("──── SuperAdmin Interface ────");
            // 列出所有用户
            for (Map.Entry<String, LibraryUsers.UserType> e : users.listAllUsers().entrySet()) {
                System.out.printf("  %s → %s%n", e.getKey(), e.getValue());
            }
            System.out.println("m) Roster Manage    b) Bulk Register Students    c) Clear All Users    0) Back");
            System.out.print("👉 Choice: ");
            String choice = sc.nextLine().trim().toLowerCase();

            switch (choice) {
                case "0" -> {
                    return;
                }
                case "m" -> rosterManage(users, model);
                case "b" -> bulkRegisterStudents(users, vic, model);
                case "c" -> {
                    clearAllUsersUI(users, vic);
                    return;
                }
                default  -> System.out.println("❌ Invalid option, please enter 'm', 'b', 'c', or '0'");
            }
        }
    }

    private static void rosterManage(LibraryUsers users, LibraryModel model) {
        while (true) {
            System.out.println("──── Roster Management ────");
            System.out.print("Enter username to manage (or 0 to back): ");
            String username = sc.nextLine().trim();
            if (username.equals("0")) return;

            if (!users.userExists(username)) {
                System.out.println("❌ User not found");
                continue;
            }

            LibraryUsers.UserType newType = null;
            while (true) {
                System.out.println("s) Student    t) Teacher    0) Back");
                System.out.print("👉 Select role: ");
                String r = sc.nextLine().trim().toLowerCase();
                if (r.equals("0")) {
                    break;
                } else if (r.equals("s")) {
                    newType = LibraryUsers.UserType.STUDENT;
                    break;
                } else if (r.equals("t")) {
                    newType = LibraryUsers.UserType.TEACHER;
                    break;
                } else {
                    System.out.println("❌ Invalid input, please enter 's', 't', or '0'");
                }
            }

            if (newType == null) continue;  // back to roster menu

            if (users.assignRole(username, newType, model)) {
                System.out.printf("✅ Assigned %s → %s%n", username, newType);
                users.saveToJSON("data/users.json");
            } else {
                System.out.println("❌ Assignment failed");
            }
        }
    }

    private static void bulkRegisterStudents(LibraryUsers users, VICData vic, LibraryModel model) {
        System.out.print("Enter initial password for all new students: ");
        String initPassword = sc.nextLine().trim();
        if (initPassword.isEmpty()) {
            System.out.println("❌ Password cannot be empty");
            return;
        }

        System.out.println("Place your StudentList#.csv files under src/main/DataBase/Students/ and press ENTER to continue...");
        sc.nextLine();

        Path dir = Paths.get("src/main/DataBase/Students");
        if (!Files.isDirectory(dir)) {
            System.out.println("❌ Directory not found: " + dir);
            return;
        }

        List<Path> csvFiles = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "StudentList*.csv")) {
            for (Path p : ds) {
                csvFiles.add(p);
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading directory: " + e.getMessage());
            return;
        }

        if (csvFiles.isEmpty()) {
            System.out.println("❌ No CSV files found matching StudentList*.csv");
            return;
        }

        int totalNew = 0, totalDup = 0, totalInvalid = 0;

        for (Path file : csvFiles) {
            int newCount = 0, dupCount = 0, invalidCount = 0;
            System.out.println("Processing file: " + file.getFileName());
            try (BufferedReader reader = Files.newBufferedReader(file)) {
                String header = reader.readLine();
                if (header == null) {
                    System.out.println("❌ File is empty: " + file.getFileName());
                    continue;
                }
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    if (parts.length < 3) {
                        invalidCount++;
                        continue;
                    }
                    String first = parts[0].trim();
                    String last  = parts[1].trim();
                    String email = parts[2].trim();
                    if (first.isEmpty() || last.isEmpty() || !email.contains("@")) {
                        invalidCount++;
                        continue;
                    }
                    if (users.userExists(email)) {
                        dupCount++;
                        continue;
                    }
                    // register as UNASSIGNED then assign to STUDENT
                    boolean reg = users.registerUser(email, initPassword, first, last, email,
                            LibraryUsers.UserType.UNASSIGNED, vic);
                    if (!reg) {
                        dupCount++;
                        continue;
                    }
                    boolean assigned = users.assignRole(email, LibraryUsers.UserType.STUDENT, model);
                    if (!assigned) {
                        System.out.println("❌ Failed to assign STUDENT for: " + email);
                        continue;
                    }
                    newCount++;
                }

                System.out.printf("→ %s: %d new, %d duplicates, %d invalid%n",
                        file.getFileName(), newCount, dupCount, invalidCount);
                totalNew     += newCount;
                totalDup     += dupCount;
                totalInvalid += invalidCount;

            } catch (IOException e) {
                System.out.println("❌ Error processing file " +
                        file.getFileName() + ": " + e.getMessage());
            }
        }

        System.out.println("── Bulk registration summary ──");
        System.out.printf("Total new: %d, duplicates skipped: %d, invalid lines: %d%n",
                totalNew, totalDup, totalInvalid);
        users.saveToJSON("data/users.json");
    }

    private static void clearAllUsersUI(LibraryUsers users, VICData vic) {
        // 1) 找到当前超级管理员的用户名
        String superadmin = users.listAllUsers().entrySet().stream()
                .filter(e -> e.getValue() == LibraryUsers.UserType.SUPERADMIN)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (superadmin == null) {
            System.out.println("❌ No SUPERADMIN account found; cannot clear users.");
            return;
        }

        // 2) 重输密码验证
        System.out.print("Re-enter SUPERADMIN password: ");
        String pwd = sc.nextLine().trim();
        if (!users.authenticate(superadmin, pwd, vic)) {
            System.out.println("❌ Password incorrect. Aborting.");
            return;
        }

        // 3) 二次确认
        System.out.print("Type 'DELETE' to confirm clearing all user data: ");
        String confirm = sc.nextLine().trim();
        if (!"DELETE".equals(confirm)) {
            System.out.println("❌ Operation cancelled.");
            return;
        }

        // 4) 执行清空
        users.clearAllUsers();
        users.saveToJSON("data/users.json");
        System.out.println("✅ All user data cleared. Returning to main menu.");
    }
}
