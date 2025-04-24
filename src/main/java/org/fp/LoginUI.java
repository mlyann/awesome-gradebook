// File: LoginUI.java
package org.fp;

import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Scanner;

public class LoginUI {
    private static final Scanner sc = new Scanner(System.in);

    // 使用一个顶层 DataStore 包含 users 和 model
    private static DataStore ds = new DataStore();
    private static final Path STORE_PATH = Paths.get("data/datastore.json");

    public static void main(String[] args) {
        // 1) 确保 data 目录存在
        try {
            Files.createDirectories(STORE_PATH.getParent());
        } catch (IOException e) {
            System.err.println("❌ 无法创建 data 目录: " + e.getMessage());
            return;
        }

        // 2) 加载或初始化 DataStore
        ds = loadDataStore();  // 确保 ds 不为 null
        ds.model.initializeIDGen();

        // 3) 初始化 VICData
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        VICData vic = new VICData(
                env.get("VIC_KEY",    "12345"),
                env.get("VIC_DATE",   "250314"),
                env.get("VIC_PASS",   "HELLOWORLD"),
                env.get("VIC_PATTERN","AB CD EFGH"),
                ""
        );

        // 4) 主循环
        boolean running = true;
        while (running) {
            System.out.println("1) 注册  2) 登录  3) 退出");
            System.out.print("👉 Choice: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> doRegister(vic);
                case "2" -> doLogin(vic);
                case "3" -> {
                    saveDataStore();
                    running = false;
                }
                default  -> System.out.println("❌ 无效选项");
            }
        }
    }

    private static void doRegister(VICData vic) {
        System.out.print("用户名：");
        String u = sc.nextLine().trim();
        if (ds.users.userExists(u)) {
            System.out.println("⚠️ 用户已存在");
            return;
        }
        System.out.print("密码：");
        String p = sc.nextLine().trim();

        System.out.print("名 (First Name)：");
        String fn = sc.nextLine().trim();
        System.out.print("姓 (Last Name)：");
        String ln = sc.nextLine().trim();
        System.out.print("邮箱 (Email)：");
        String em = sc.nextLine().trim();
        if (!em.contains("@")) {
            System.out.println("❌ 无效邮箱");
            return;
        }

        // 首位注册成 SUPERADMIN，否则 UNASSIGNED
        LibraryUsers.UserType type =
                ds.users.superAdminExists()
                        ? LibraryUsers.UserType.UNASSIGNED
                        : LibraryUsers.UserType.SUPERADMIN;

        if (ds.users.registerUser(u, p, fn, ln, em, type, vic)) {
            System.out.println("✅ 注册成功，角色：" + type);
            saveDataStore();
            if (type == LibraryUsers.UserType.SUPERADMIN) {
                AdminUI.start(ds.users, vic, ds.model);
            }
        } else {
            System.out.println("❌ 注册失败");
        }
    }

    private static void doLogin(VICData vic) {
        System.out.print("用户名：");
        String username = sc.nextLine().trim();
        System.out.print("密码：");
        String password = sc.nextLine().trim();

        if (!ds.users.authenticate(username, password, vic)) {
            System.out.println("密码错误或不存在该账户");
            return;
        }

        LibraryUsers.UserType role = ds.users.getUserType(username);
        String objectID = ds.users.getObjectID(username);

        System.out.println("✅ 登录成功");

        if (role == LibraryUsers.UserType.UNASSIGNED) {
            System.out.println("❌ 该账号尚未被超级管理员分配角色，请联系管理员。");
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

    /** 加载或创建 DataStore */
    private static DataStore loadDataStore() {
        if (Files.exists(STORE_PATH)) {
            try (Reader r = new FileReader(STORE_PATH.toFile())) {
                Gson gson = createGson();
                DataStore loaded = gson.fromJson(r, DataStore.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException e) {
                System.err.println("⚠️ 加载 datastore.json 失败，使用新实例: " + e.getMessage());
            }
        }
        // 文件不存在或解析失败，返回一个全新的
        return new DataStore();
    }

    /** 保存当前 DataStore 到 JSON */
    private static void saveDataStore() {
        try (Writer w = new FileWriter(STORE_PATH.toFile())) {
            Gson gson = createGson();
            gson.toJson(ds, w);
        } catch (IOException e) {
            System.err.println("❌ 保存 datastore.json 失败: " + e.getMessage());
        }
    }

    /** 构造支持 LocalDate 的 Gson */
    private static Gson createGson() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        // LocalDate 序列化/反序列化
        builder.registerTypeAdapter(LocalDate.class,
                (JsonSerializer<LocalDate>) (src, typeOfSrc, ctx) -> new JsonPrimitive(src.toString()));
        builder.registerTypeAdapter(LocalDate.class,
                (JsonDeserializer<LocalDate>) (json, type, ctx) -> LocalDate.parse(json.getAsString()));
        return builder.create();
    }


}
