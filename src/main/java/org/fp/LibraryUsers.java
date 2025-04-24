// File: LibraryUsers.java
package org.fp;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryUsers {

    public enum UserType { SUPERADMIN, UNASSIGNED, STUDENT, TEACHER }

    // key: 登录用的 username，value: 对应的用户信息
    private final Map<String, LibraryUser> users = new HashMap<>();

    /**
     * 内部类：存储一个用户所有信息
     */
    private static class LibraryUser {
        String username;
        String encryptedPassword;
        String firstName;
        String lastName;
        String email;
        UserType type;
        String objectID;  // 在 assignRole 时设置

        // no-arg ctor for Gson
        LibraryUser() {}

        LibraryUser(String username,
                    String encryptedPassword,
                    String firstName,
                    String lastName,
                    String email,
                    UserType type) {
            this.username = username;
            this.encryptedPassword = encryptedPassword;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.type = type;
            this.objectID = null;
        }
    }

    public void clearAllUsers() {
        users.clear();
    }

    /** 注册：只保存登录信息和基本资料，不创建实体 */
    public boolean registerUser(String username,
                                String password,
                                String firstName,
                                String lastName,
                                String email,
                                UserType type,
                                VICData vic) {
        if (users.containsKey(username)) return false;
        String encrypted = EncryptVIC.encrypt(password, vic);
        users.put(username,
                new LibraryUser(username, encrypted, firstName, lastName, email, type));
        return true;
    }

    /** 验证登录 */
    public boolean authenticate(String username, String inputPassword, VICData vic) {
        LibraryUser u = users.get(username);
        return u != null && EncryptVIC.encrypt(inputPassword, vic).equals(u.encryptedPassword);
    }

    /** 获取角色 */
    public UserType getUserType(String username) {
        LibraryUser u = users.get(username);
        return u != null ? u.type : null;
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public boolean superAdminExists() {
        return users.values().stream()
                .anyMatch(u -> u.type == UserType.SUPERADMIN);
    }

    /**
     * 超管分配角色时，创建实体并记录 objectID
     */
    public boolean assignRole(String username,
                              UserType newType,
                              LibraryModel model) {
        LibraryUser u = users.get(username);
        if (u == null || u.type == UserType.SUPERADMIN) return false;
        u.type = newType;

        switch (newType) {
            case STUDENT -> {
                Student s = new Student(u.firstName, u.lastName, u.email);
                model.addStudent(s);
                u.objectID = s.getStuID();
            }
            case TEACHER -> {
                Teacher t = new Teacher(u.firstName, u.lastName);
                model.addTeacher(t);
                u.objectID = t.getTeacherID();
            }
            default -> {}
        }
        return true;
    }

    /** 登录时取出真实的 objectID */
    public String getObjectID(String username) {
        LibraryUser u = users.get(username);
        return u == null ? null : u.objectID;
    }

    /** 列出所有用户和角色，用于 AdminUI 展示 */
    public Map<String, UserType> listAllUsers() {
        return users.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().type
                ));
    }

    /** 保存到 JSON 文件 */
    public void saveToJSON(String path) {
        try (Writer writer = new FileWriter(path)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("❌ 保存用户数据失败: " + e.getMessage());
        }
    }

    /** 从 JSON 加载 */
    public void loadFromJSON(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("⚠️ 用户数据文件不存在，跳过加载");
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, LibraryUser>>() {}.getType();
            Map<String, LibraryUser> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                users.clear();
                users.putAll(loaded);
            }
        } catch (IOException e) {
            System.err.println("❌ 加载用户数据失败: " + e.getMessage());
        }
    }
}
