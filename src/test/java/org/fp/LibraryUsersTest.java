package org.fp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LibraryUsersTest {

    // Helper VICData that works end-to-end
    private VICData makeVic() {
        // numeric step3 and step5, and anagram covering A–D for message "ABCD"
        return new VICData(
                "12345",
                "210101",
                "JKLMNOPQRS",
                "ABCD EFG H",
                null
        );
    }

    @Test
    void testRegisterAuthenticateAndUserExists() {
        LibraryUsers lu = new LibraryUsers();
        VICData vic = makeVic();

        // register returns true first, false on duplicate
        boolean first = lu.registerUser(
                "user1", "ABCD", "First", "Last", "u1@ex.com",
                LibraryUsers.UserType.UNASSIGNED, vic
        );
        assertTrue(first);
        boolean dup = lu.registerUser(
                "user1", "ABCD", "First", "Last", "u1@ex.com",
                LibraryUsers.UserType.UNASSIGNED, vic
        );
        assertFalse(dup);

        // existence and default type
        assertTrue(lu.userExists("user1"));
        assertEquals(LibraryUsers.UserType.UNASSIGNED, lu.getUserType("user1"));

        // unknown user → type should be null
        assertNull(lu.getUserType("no_such_user"));

        // authentication works with the same VICData and password
        assertTrue(lu.authenticate("user1", "ABCD", vic));
        // wrong password fails
        assertFalse(lu.authenticate("user1", "WXYZ", vic));
        // unknown user fails
        assertFalse(lu.authenticate("nope", "ABCD", vic));
    }

    @Test
    void testAssignRoleStudentAndTeacher() {
        LibraryUsers lu = new LibraryUsers();
        LibraryModel model = new LibraryModel();
        VICData vic = makeVic();

        lu.registerUser("u", "ABCD", "F", "L", "u@e.com",
                LibraryUsers.UserType.UNASSIGNED, vic);

        // assign STUDENT
        boolean okStud = lu.assignRole("u", LibraryUsers.UserType.STUDENT, model);
        assertTrue(okStud);
        assertEquals(LibraryUsers.UserType.STUDENT, lu.getUserType("u"));
        String stuID = lu.getObjectID("u");
        assertNotNull(stuID);
        assertNotNull(model.getStudent(stuID));

        // assign TEACHER
        boolean okTeach = lu.assignRole("u", LibraryUsers.UserType.TEACHER, model);
        assertTrue(okTeach);
        assertEquals(LibraryUsers.UserType.TEACHER, lu.getUserType("u"));
        String teachID = lu.getObjectID("u");
        assertNotNull(teachID);
        assertNotNull(model.getTeacher(teachID));

        // cannot re-assign SUPERADMIN or unknown
        lu.clearAllUsers();
        assertFalse(lu.assignRole("nope", LibraryUsers.UserType.STUDENT, model));
    }

    @Test
    void testSuperAdminExistsAndGetObjectIDBranches() {
        LibraryUsers lu = new LibraryUsers();
        VICData vic = makeVic();
        assertFalse(lu.superAdminExists());
        assertNull(lu.getObjectID("no_such_user"));
        assertTrue(lu.registerUser(
                "user", "PW", "F", "L", "u@e.com",
                LibraryUsers.UserType.STUDENT, vic
        ));
        assertFalse(lu.superAdminExists());
        // user has no objectID until you assignRole
        assertNull(lu.getObjectID("user"));

        assertTrue(lu.registerUser(
                "sa", "PASS", "S", "A", "sa@ex.com",
                LibraryUsers.UserType.SUPERADMIN, vic
        ));
        // now superAdminExists() → true
        assertTrue(lu.superAdminExists());

        assertNull(lu.getObjectID("sa"));

        Map<String, LibraryUsers.UserType> all = lu.listAllUsers();
        assertEquals(2, all.size());
        assertEquals(LibraryUsers.UserType.STUDENT, all.get("user"));
        assertEquals(LibraryUsers.UserType.SUPERADMIN, all.get("sa"));

        LibraryModel model = new LibraryModel();
        assertFalse(lu.assignRole("sa", LibraryUsers.UserType.TEACHER, model));

        assertTrue(lu.assignRole("user", LibraryUsers.UserType.TEACHER, model));
        String obj = lu.getObjectID("user");
        assertNotNull(obj);
        assertNotNull(model.getTeacher(obj));
    }


    @Test
    void testSaveAndLoadJSON(@TempDir Path tmp) {
        LibraryUsers lu1 = new LibraryUsers();
        VICData vic = makeVic();

        lu1.registerUser("u1", "ABCD", "F1", "L1", "u1@ex.com",
                LibraryUsers.UserType.STUDENT, vic);
        lu1.registerUser("u2", "EFGH", "F2", "L2", "u2@ex.com",
                LibraryUsers.UserType.TEACHER, vic);

        Path file = tmp.resolve("users.json");
        lu1.saveToJSON(file.toString());

        LibraryUsers lu2 = new LibraryUsers();
        lu2.loadFromJSON(file.toString());

        // loaded users exist and types preserved
        assertTrue(lu2.userExists("u1"));
        assertTrue(lu2.userExists("u2"));
        assertEquals(LibraryUsers.UserType.STUDENT, lu2.getUserType("u1"));
        assertEquals(LibraryUsers.UserType.TEACHER, lu2.getUserType("u2"));

        // authentication still works after load
        assertTrue(lu2.authenticate("u1", "ABCD", vic));
        assertTrue(lu2.authenticate("u2", "EFGH", vic));
    }

    @Test
    void testSaveToJSON_CatchesIOException(@TempDir Path tmp) throws IOException {
        LibraryUsers lu = new LibraryUsers();
        // create a directory instead of a file
        Path dir = tmp.resolve("outputDir");
        Files.createDirectory(dir);
        assertDoesNotThrow(() -> lu.saveToJSON(dir.toString()));
    }

    @Test
    void testLoadFromJSON_CatchesIOException(@TempDir Path tmp) throws IOException {
        LibraryUsers lu = new LibraryUsers();

        Path dir = tmp.resolve("userDir");
        Files.createDirectory(dir);
        assertTrue(Files.isDirectory(dir));

        assertDoesNotThrow(() -> lu.loadFromJSON(dir.toString()));

        assertFalse(lu.userExists("anything"));
    }

    @Test
    void testLoadFromJSON_FileNotFoundBranch(@TempDir Path tmp) {
        LibraryUsers lu = new LibraryUsers();
        Path missing = tmp.resolve("nonexistent.json");
        assertFalse(missing.toFile().exists());
        assertDoesNotThrow(() -> lu.loadFromJSON(missing.toString()));
        assertFalse(lu.userExists("anyone"));
    }
}
