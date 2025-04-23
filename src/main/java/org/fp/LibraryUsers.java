
/**
package org.fp;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class LibraryUsers {
    private final Map<String, UserRecord> users = new HashMap<>();

    // 注册用户时定义身份
    public enum UserType { STUDENT, TEACHER }

    public boolean registerUser(String username, String password, boolean isTeacher, VICData vic) {
        if (passwordMap.containsKey(username)) return false;

        String encrypted = EncryptVIC.encrypt(password, vic);
        passwordMap.put(username, encrypted);
        isTeacherMap.put(username, isTeacher);

        if (isTeacher) {
            Teacher t = new Teacher(username, "T_" + username);
            model.addTeacher(t);
        } else {
            Student s = new Student(username, "S_" + username, "student@" + username + ".com");
            model.addStudent(s);
        }
        return true;
    }

    public boolean authenticate(String userID, String inputPassword, VICData vicData) {
        UserRecord user = users.get(userID);
        if (user == null) return false;
        return EncryptVIC.encrypt(inputPassword, vicData).equals(user.encryptedPassword);
    }

    public UserType getUserType(String userID) {
        UserRecord record = users.get(userID);
        return (record != null) ? record.type : null;
    }

    public boolean userExists(String userID) {
        return users.containsKey(userID);
    }

    // 保存到 JSON 文件
    public void saveToJSON(String filePath) {
        try (Writer writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.out.println("❌ 保存用户数据失败: " + e.getMessage());
        }
    }

    // 从 JSON 加载
    public void loadFromJSON(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("⚠️ 用户数据文件不存在，跳过加载");
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, UserRecord>>(){}.getType();
            Map<String, UserRecord> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                users.clear();
                users.putAll(loaded);
            }
        } catch (IOException e) {
            System.out.println("❌ 加载用户数据失败: " + e.getMessage());
        }
    }

    // 用于封装用户记录
    public static class UserRecord {
        private String userID;
        private String encryptedPassword;
        private UserType type;

        public UserRecord(String userID, String encryptedPassword, UserType type) {
            this.userID = userID;
            this.encryptedPassword = encryptedPassword;
            this.type = type;
        }

        public String getEncryptedPassword() { return encryptedPassword; }

        public UserType getType() { return type; }
    }
}
**/
