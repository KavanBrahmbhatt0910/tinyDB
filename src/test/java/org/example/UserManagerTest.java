package org.example;

import org.example.models.User;
import org.example.utils.UserUtils;
import org.junit.Test;
import org.junit.jupiter.api.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static org.junit.Assert.*;
public class UserManagerTest {

    private Scanner scanner;

    @BeforeEach
    void setUp() {
        // Setup mock data or environment before each test
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
    }

    @Test
    public void testRegisterUserWithValidEmail() throws IOException {
        String userInput = "test@example.com\npassword\nFluffy\nNew York\nInception\n";
        scanner = new Scanner(new ByteArrayInputStream(userInput.getBytes()));

        // Clear existing users for test
        UserUtils.clearUsers("databases/USERS/user.txt");

        UserManager.registerUser(scanner);

        LinkedList<User> users = UserUtils.loadUsers("databases/USERS/user.txt");
        assertEquals(1, users.size());
        assertEquals("test@example.com", users.get(0).getUserID());
    }

    @Test
    public void testRegisterUserWithInvalidEmail() throws IOException {
        String userInput = "invalid-email\nvalid@example.com\npassword\nFluffy\nNew York\nInception\n";
        scanner = new Scanner(new ByteArrayInputStream(userInput.getBytes()));

        // Clear existing users for test
        UserUtils.clearUsers("databases/USERS/user.txt");

        UserManager.registerUser(scanner);

        LinkedList<User> users = UserUtils.loadUsers("databases/USERS/user.txt");
        assertEquals(1, users.size());
        assertEquals("valid@example.com", users.get(0).getUserID());
    }

    @Test
    public void testLoginUserWithCorrectCredentials() throws IOException {
        String registerInput = "login@example.com\npassword\nFluffy\nNew York\nInception\n";
        scanner = new Scanner(new ByteArrayInputStream(registerInput.getBytes()));
        UserManager.registerUser(scanner);

        String loginInput = "login@example.com\npassword\nFluffy\n";
        scanner = new Scanner(new ByteArrayInputStream(loginInput.getBytes()));

        try {
            UserManager.loginUser(scanner);
        } catch (IOException e) {
            fail("IOException was thrown");
        }
    }

    private void assertDoesNotThrow(Object o) {
    }

    @Test
    public void testLoginUserWithIncorrectPassword() throws IOException {
        String registerInput = "login@example.com\npassword\nFluffy\nNew York\nInception\n";
        scanner = new Scanner(new ByteArrayInputStream(registerInput.getBytes()));
        UserUtils.clearUsers("databases/USERS/user.txt");
        UserManager.registerUser(scanner);

        String loginInput = "login@example.com\nwrongpassword";
        scanner = new Scanner(new ByteArrayInputStream(loginInput.getBytes()));

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        UserManager.loginUser(scanner);
        String expectedMessage = "Login failed. Invalid UserID or Password.\n";
        assertTrue(outContent.toString().contains(expectedMessage));
    }

}
