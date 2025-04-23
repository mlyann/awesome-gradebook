
/**
package org.fp;

import io.github.cdimascio.dotenv.Dotenv;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class LoginUI {
    private static final Scanner sc = new Scanner(System.in);
    private static LibraryUsers libraryUsers;

    public static void main(String[] args) {
        libraryUsers = new LibraryUsers();

        // 加载 JSON 用户数据
        libraryUsers.loadFromJSON("data/users.json");

        // 创建 VICData（用于加密）
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        VICData vic = new VICData(
                env.get("VIC_KEY", "12345"),
                env.get("VIC_DATE", "250314"),
                env.get("VIC_PASS", "HELLOWORLD"),
                env.get("VIC_PATTERN", "AB CD EFGH"),
                ""
        );

        boolean running = true;
        while (running) {
            System.out.println("====================================");
            System.out.println("📚 Welcome to the Gradebook System!");
            System.out.println("1) Register");
            System.out.println("2) Login");
            System.out.println("3) Exit");
            System.out.print("👉 Choice: ");

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> register(vic);
                case "2" -> login(vic);
                case "3" -> {
                    libraryUsers.saveToJSON("data/users.json");
                    running = false;
                    System.out.println("👋 Goodbye!");
                }
                default -> System.out.println("❌ Invalid choice.");
            }
        }
    }

    private static void register(VICData vic) {
        System.out.print("👤 Username: ");
        String userID = sc.nextLine().trim();

        if (libraryUsers.userExists(userID)) {
            System.out.println("⚠️ User already exists.");
            return;
        }

        System.out.print("🔒 Password: ");
        String password = sc.nextLine().trim();

        System.out.print("🎓 Role (student/teacher): ");
        String role = sc.nextLine().trim().toLowerCase();

        boolean ok = switch (role) {
            case "student" -> libraryUsers.registerStudent(userID, password, vic);
            case "teacher" -> libraryUsers.registerTeacher(userID, password, vic);
            default -> false;
        };

        if (ok) System.out.println("✅ Registered successfully.");
        else System.out.println("❌ Failed to register.");
    }

    private static void login(VICData vic) {
        System.out.print("👤 Username: ");
        String userID = sc.nextLine().trim();

        System.out.print("🔒 Password: ");
        String password = sc.nextLine().trim();

        LibraryModel model = libraryUsers.authenticate(userID, password, vic);
        if (model == null) {
            System.out.println("🚫 Login failed.");
            return;
        }

        System.out.println("✅ Login successful.");
        if (model.isTeacher()) {
            TeacherUI.start(model);  // TeacherUI 接收的是 LibraryModel
        } else {
            StudentUI.start(model);  // StudentUI 接收的是 LibraryModel
        }

    }
}
 **/