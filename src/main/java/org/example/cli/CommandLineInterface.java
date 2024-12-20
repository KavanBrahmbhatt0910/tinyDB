package org.example.cli;

import org.example.*;

import java.io.IOException;
import java.util.Scanner;

public class CommandLineInterface {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DBManager dbManager = new DBManager();
    private static final QueryProcessor queryProcessor = new QueryProcessor(dbManager);
    private static final SQLExporter sqlExporter = new SQLExporter();

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to TinyDB Command Line Interface");
        Logger.logGeneral("Application started");
        while (true) {
            System.out.print("tinydb> ");
            System.out.println("\nWelcome to the DBMS Builder");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice (1-3): ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            switch (choice) {
                case 1:
                    UserManager.registerUser(scanner);
                    break;
                case 2:
                    UserManager.loginUser(scanner);
                    break;
                case 3:
                    System.out.println("Exiting...");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
//            String input = scanner.nextLine();
//            if (input.equalsIgnoreCase("exit")) {
//                break;
//            }
//            String result = queryProcessor.processQuery(input);
//            System.out.println(result);
        }
    }

    private static void handleExportCommand() {
        System.out.println("Enter the name of the database you want to export:");
        String dbName = scanner.nextLine();
        boolean exportResult = sqlExporter.exportDatabaseToSQL(dbName);
        if(exportResult) {
            System.out.println("Export successful!");
        }
    }
}

