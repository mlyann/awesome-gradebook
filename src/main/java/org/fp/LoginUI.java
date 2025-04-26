package org.fp;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import io.github.cdimascio.dotenv.Dotenv;

public class LoginUI {
    private static final Scanner sc = new Scanner(System.in);

    // Singleton DataStore
    private static DataStore ds = new DataStore();
    private static final Path STORE_PATH = Paths.get("data/datastore.json");

    public static void main(String[] args) {
        // Ensure the data directory exists
        try {
            Files.createDirectories(STORE_PATH.getParent());
        } catch (IOException e) {
            System.err.println("❌ Failed to create data directory: " + e.getMessage());
            return;
        }

        // Ensure the data directory exists
        ds = loadDataStore();  // load or create a new DataStore
        ds.model.initializeIDGen();

        // Load environment variables
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        VICData vic = new VICData(
                env.get("VIC_KEY","12345"),
                env.get("VIC_DATE","250314"),
                env.get("VIC_PASS","HELLOWORLD"),
                env.get("VIC_PATTERN","AB CD EFGH"),
                ""
        );

        // Main loop
        boolean running = true;
        while (running) {
            System.out.println("1) Register  2) Login  3) Exit");
            System.out.print("👉 Choice: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> doRegister(vic);
                case "2" -> doLogin(vic);
                case "3" -> {
                    saveDataStore();
                    running = false;
                }
                default  -> System.out.println("❌ Invalid option, please enter '1', '2', or '3'");
            }
        }
    }

    private static void doRegister(VICData vic) {
        System.out.print("Username: ");
        String u = sc.nextLine().trim();
        if (ds.users.userExists(u)) {
            System.out.println("⚠️ User already exists");
            return;
        }
        System.out.print("Password: ");
        String p = sc.nextLine().trim();

        System.out.print("First Name：");
        String fn = sc.nextLine().trim();
        System.out.print("Last Name：");
        String ln = sc.nextLine().trim();
        System.out.print("Email：");
        String em = sc.nextLine().trim();
        if (!em.contains("@")) {
            System.out.println("❌ Invalid email");
            return;
        }

        // Check if the user is a super admin
        LibraryUsers.UserType type =
                ds.users.superAdminExists()
                        ? LibraryUsers.UserType.UNASSIGNED
                        : LibraryUsers.UserType.SUPERADMIN;

        if (ds.users.registerUser(u, p, fn, ln, em, type, vic)) {
            System.out.println("✅ register success：" + type);
            saveDataStore();
            if (type == LibraryUsers.UserType.SUPERADMIN) {
                AdminUI.start(ds.users, vic, ds.model);
            }
        } else {
            System.out.println("❌ Registration failed");
        }
    }

    private static void doLogin(VICData vic) {
        System.out.print("Username：");
        String username = sc.nextLine().trim();
        System.out.print("Password：");
        String password = sc.nextLine().trim();

        if (!ds.users.authenticate(username, password, vic)) {
            System.out.println("❌ Invalid username or password");
            return;
        }

        LibraryUsers.UserType role = ds.users.getUserType(username);
        String objectID = ds.users.getObjectID(username);

        System.out.println("✅ Login success：" + role);

        if (role == LibraryUsers.UserType.UNASSIGNED) {
            System.out.println("❌ This account has not been assigned a role by a super admin, please contact the administrator.");
            return;
        }

        switch (role) {
            case SUPERADMIN ->
                    AdminUI.start(ds.users, vic, ds.model);
            case TEACHER   ->
                    TeacherUI.start(ds.model, objectID);
            case STUDENT   ->
                    StudentUI.start(ds.model, objectID);
            default       -> { }
        }
    }

    /** load or create DataStore */
    private static DataStore loadDataStore() {
        if (Files.exists(STORE_PATH)) {
            try (Reader r = new FileReader(STORE_PATH.toFile())) {
                Gson gson = createGson();
                DataStore loaded = gson.fromJson(r, DataStore.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException e) {
                System.err.println("⚠️ Loading datastore.json failed, using new instance: " + e.getMessage());
            }
        }
        // File does not exist or parsing failed, returning a new one
        return new DataStore();
    }

    /** Save the current DataStore to JSON */
    private static void saveDataStore() {
        try (Writer w = new FileWriter(STORE_PATH.toFile())) {
            Gson gson = createGson();
            gson.toJson(ds, w);
        } catch (IOException e) {
            System.err.println("❌ Saving datastore.json failed: " + e.getMessage());
        }
    }

    /** Construct Gson that supports LocalDate */
    private static Gson createGson() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        // LocalDate serialization/deserialization
        builder.registerTypeAdapter(LocalDate.class,
                (JsonSerializer<LocalDate>) (src, typeOfSrc, ctx) -> new JsonPrimitive(src.toString()));
        builder.registerTypeAdapter(LocalDate.class,
                (JsonDeserializer<LocalDate>) (json, type, ctx) -> LocalDate.parse(json.getAsString()));
        return builder.create();
    }


}
