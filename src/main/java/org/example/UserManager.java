package org.example;

import org.example.models.User;
import org.example.utils.HashUtils;
import org.example.utils.UserUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class UserManager {
    public static void registerUser(Scanner scanner) throws IOException {

        System.out.println("\n--- User Registration ---");
        // Email validation since user ID is email
        String userID;
        while (true) {
            System.out.print("Enter UserID (email): ");
            userID = scanner.nextLine();
            Logger.logGeneral("registering user with id: " + userID);
            if (isValidEmail(userID)) {
                break;
            } else {
                Logger.logGeneral("Invalid email format tried for user id: " + userID);
                System.out.println("Invalid email format. Please try again.");
            }
        }

        // Check if the userID already exists
        LinkedList<User> users = UserUtils.loadUsers("databases/USERS/user.txt");
        for (User user : users) {
            if (user.getUserID().equals(userID)) {
                Logger.logGeneral("User ID already exists for user id: " + userID);
                System.out.println("UserID already exists. Please try again.");
                return;
            }
        }

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        String hashedPassword = HashUtils.hashMD5(password);

        List<String> securityQuestions = new ArrayList<>();
        List<String> securityAnswers = new ArrayList<>();

        securityQuestions.add("What is your pet's name?");
        securityQuestions.add("What city were you born in?");
        securityQuestions.add("What is your favorite movie?");
        for (String question : securityQuestions) {
            System.out.print(question + ": ");
            String answer = scanner.nextLine();
            securityAnswers.add(answer);
        }

        User newUser = new User(userID, hashedPassword, securityQuestions, securityAnswers);
        UserUtils.saveUser(newUser, "databases/USERS/user.txt");
        Logger.logGeneral("User registered successfully with user id: " + userID);
        System.out.println("Registration successful.\n");
    }

    public static void loginUser(Scanner scanner) throws IOException {
        try {
            Logger.logGeneral("User login");
            System.out.println("\n--- User Login ---");
            System.out.print("Enter UserID: ");
            String userID = scanner.nextLine();
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();
            String hashedPassword = HashUtils.hashMD5(password);

            LinkedList<User> users = UserUtils.loadUsers("databases/USERS/user.txt");

            User currentUser = null;

            for (User user : users) {
                if (user.getUserID().equals(userID) && user.getPassword().equals(hashedPassword)) {
                    currentUser = user;
                    break;
                }
            }

            if (currentUser != null) {
                if (verifySecurityQuestion(currentUser, scanner)) {
                    Logger.logGeneral("User login successful with user id: " + userID);
                    System.out.println("Login successful.\n");
                    userMenu(currentUser, scanner);
                } else {
                    Logger.logGeneral("User login failed with user id: " + userID);
                    System.out.println("Login failed. Incorrect answer to security question.\n");
                }
            } else {
                Logger.logGeneral("User login failed with user id: " + userID);
                System.out.println("Login failed. Invalid UserID or Password.\n");
            }
        } catch (Exception e) {
            Logger.logGeneral("User login failed with exception: " + e.getMessage());
            System.out.println("Login failed. Invalid UserID or Password.\n");
        }
    }

    private static boolean verifySecurityQuestions(User user, Scanner scanner) {
        Logger.logGeneral("Verifying security questions for user id: " + user.getUserID());
        List<String> securityQuestions = user.getSecurityQuestions();
        List<String> securityAnswers = user.getSecurityAnswers();

        for (int i = 0; i < securityQuestions.size(); i++) {
            System.out.print(securityQuestions.get(i) + ": ");
            String answer = scanner.nextLine();
            if (!answer.equals(securityAnswers.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidEmail(String email) {
        return Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", email);
    }

    private static void userMenu(User user, Scanner scanner) throws IOException {
        while (true) {
            System.out.println("User Menu");
            System.out.println("1. Write Queries");
            System.out.println("2. Export Data and Structure");
            System.out.println("3. ERD");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            String userChoice = scanner.nextLine().trim();
            if (!userChoice.isEmpty()) {
                switch (userChoice) {
                    case "1":
                        // Write query functionality
                        Logger.logGeneral("User selected write queries");
                        QueryProcessor queryProcessor = new QueryProcessor(new DBManager());
                        while (true) {
                            System.out.print("Enter your query: ");
                            String query = scanner.nextLine();
                            if(query.trim().equalsIgnoreCase("exit;")) {
                                break;
                            }
                            queryProcessor.processQuery(query);
                        }
                        break;
                    case "2":
                        // Data structure export functionality
                        Logger.logGeneral("User selected export data and structure");
                        System.out.println("Export Data and Structure");
                        SQLExporter sqlExporter = new SQLExporter();
                        System.out.print("Enter database name: ");
                        String databaseName = scanner.next();
                        sqlExporter.exportDatabaseToSQL(databaseName);
                        break;
                    case "3":
                        ReverseEngineeringManager reverseEngineeringManager = new ReverseEngineeringManager();
                        // ERD reverse engineer functionality
                        Logger.logGeneral("User selected ERD");
                        System.out.println("Reverse engineer");
                        System.out.print("Enter database name: ");
                        String databasename = scanner.next();
                        reverseEngineeringManager.generateERD(databasename);
                        break;
                    case "4":
                        System.out.println("Logging out.\n");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }

    public static boolean verifySecurityQuestion(User user, Scanner scanner) {
        Logger.logGeneral("Verifying security question for user id: " + user.getUserID());
        List<String> securityQuestions = user.getSecurityQuestions();
        List<String> securityAnswers = user.getSecurityAnswers();

        // Randomly select one of the security questions
        Random random = new Random();
        int questionIndex = random.nextInt(securityQuestions.size());

        System.out.print(securityQuestions.get(questionIndex) + ": ");
        String answer = scanner.nextLine();
        return answer.equals(securityAnswers.get(questionIndex));
    }

}
