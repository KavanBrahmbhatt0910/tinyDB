package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SQLExporter {

    private String currentDatabase;

    private String databaseDirectory = "databases/";

    public boolean exportDatabaseToSQL(String databaseName) {

        currentDatabase = databaseName;
        // Check if a database is selected
        if (currentDatabase == null) {
            Logger.logGeneral("Database " + databaseName + " is not selected.");
            System.out.println("Database " + databaseName + " is not selected.");
            return false;
        }

        // Prepare the SQL dump file
        File sqlDumpFile = new File("databases/" + databaseName + "/dump.sql");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sqlDumpFile))) {
            // Export each table
            List<String> tableNames = getAllTablesInDatabase(databaseName);
            for (String tableName : tableNames) {
                exportTableToSQL(writer, tableName);
            }

            Logger.logGeneral("Database " + databaseName + " exported to SQL successfully.");
            System.out.println("Database " + databaseName + " exported to SQL successfully.");
            return false;
        } catch (IOException e) {
            Logger.logGeneral("Failed to export database " + databaseName + " to SQL.");
            System.out.println("Failed to export database " + databaseName + " to SQL.");
            return false;
        }
    }

    private void exportTableToSQL(BufferedWriter writer, String tableName) throws IOException {
        // Get table structure and data
        String tableStructure = getTableStructure(tableName);
        List<String> tableData = getTableData(tableName);

        // Write table creation statement
        writer.write("CREATE TABLE " + tableName + " (" + tableStructure + ");");
        writer.newLine();

        // Write insert statements
        for (String data : tableData) {
            writer.write("INSERT INTO " + tableName + " VALUES (" + data + ");");
            writer.newLine();
        }
    }

    private String getTableStructure(String tableName) {
        File tableFile = new File(databaseDirectory + "/" + currentDatabase + "/" + tableName + ".txt");

        StringBuilder structure = new StringBuilder();

        if (!tableFile.exists()) {
            System.err.println("Table file not found for table: " + tableName);
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String firstLine = reader.readLine();
            if (firstLine != null) {
                structure.append(firstLine.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading table file for table: " + tableName);
            e.printStackTrace();
        }

        return structure.toString();
    }

    private List<String> getTableData(String tableName) {
        File tableFile = new File(databaseDirectory + "/" + currentDatabase + "/" + tableName + ".txt");
        List<String> data = new ArrayList<>();

        if (!tableFile.exists()) {
            System.err.println("Table file not found for table: " + tableName);
            return data;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            // Skip the first line (structure)
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                data.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error reading table data for table: " + tableName);
            e.printStackTrace();
        }

        return data;
    }

    private List<String> getAllTablesInDatabase(String databaseName) {
        List<String> tables = new ArrayList<>();
        File metadataFile = new File(databaseDirectory + databaseName + "/metadata.txt");

        if (!metadataFile.exists()) {
            System.err.println("Metadata file not found for database: " + databaseName);
            return tables;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(metadataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tables.add(line.trim()); // Assuming each line contains a table name
            }
        } catch (IOException e) {
            System.err.println("Error reading metadata file for database: " + databaseName);
            e.printStackTrace();
        }

        return tables;
    }

}
