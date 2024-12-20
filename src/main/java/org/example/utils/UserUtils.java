package org.example.utils;

import org.example.Logger;
import org.example.models.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UserUtils {
    public static void saveUser(User user, String dataFile) {
        try {
            Logger.logGeneral("Saving user: " + user.toString());
            // Append the user data to the data file
            String userData = user.toString() + System.lineSeparator();
            Files.write(Paths.get(dataFile), userData.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            Logger.logGeneral( "Error saving user: " + e.getMessage());
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    public static LinkedList<User> loadUsers(String dataFile) {
        Logger.logGeneral("Loading users from file: " + dataFile);
        LinkedList<User>users = new LinkedList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(dataFile));
            for (String line : lines) {
                users.add(User.fromString(line));
            }
        } catch (IOException e) {
            Logger.logGeneral( "Error loading users: " + e.getMessage());
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    public static void clearUsers(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
