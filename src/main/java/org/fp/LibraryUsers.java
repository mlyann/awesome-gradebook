// File: LibraryUsers.java
package org.fp;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class LibraryUsers {

    public enum UserType { SUPERADMIN, UNASSIGNED, STUDENT, TEACHER }

    // key: username
    private final Map<String, LibraryUser> users = new HashMap<>();

    /**
     * instance of LibraryUser
     */
    private static class LibraryUser {
        String username;
        String encryptedPassword;
        String firstName;
        String lastName;
        String email;
        UserType type;
        String objectID;

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

    /** 
     * Registers a user by saving login information and basic details without creating an entity 
     */
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

    /** 
     * Authenticates a user by checking the username and password
     */
    public boolean authenticate(String username, String inputPassword, VICData vic) {
        LibraryUser u = users.get(username);
        return u != null && EncryptVIC.encrypt(inputPassword, vic).equals(u.encryptedPassword);
    }

    /** 
     * Retrieves the user role based on the username 
     */
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
     * Assigns a role to a user and creates an entity if necessary, recording the objectID
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

    /** 
     * Retrieves the objectID of a user during login 
     */
    public String getObjectID(String username) {
        LibraryUser u = users.get(username);
        return u == null ? null : u.objectID;
    }

    /** 
     * Lists all users and their roles for AdminUI display 
     */
    public Map<String, UserType> listAllUsers() {
        return users.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().type
                ));
    }

    /** 
     * Saves user data to a JSON file 
     * @param path the file path where user data will be saved
     */
    public void saveToJSON(String path) {
        try (Writer writer = new FileWriter(path)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("❌ Failed to save user info: " + e.getMessage());
        }
    }

    /** 
     * Loads user data from a JSON file 
     * @param path the file path from which user data will be loaded
     */
    public void loadFromJSON(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("⚠️ User data file not found, stop loading.");
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
            System.err.println("❌ Failed to load user data: " + e.getMessage());
        }
    }
}
