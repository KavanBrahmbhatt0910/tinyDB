package org.example;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class DBManager {
    private String currentDatabase;

    public String getCurrentDatabase() {
        return currentDatabase;
    }

    public void setCurrentDatabase(String currentDatabase) {
        this.currentDatabase = currentDatabase;
    }

    public boolean createDatabase(String databaseName) {
        File dbDir = new File("databases/" + databaseName);
        if (dbDir.exists()) {
            Logger.logGeneral("Database " + databaseName + " already exists.");
            System.out.println("Database " + databaseName + " already exists.");
            return false;
        }

        if (!dbDir.mkdirs()) {
            Logger.logGeneral("Failed to create database " + databaseName + ".");
            System.out.println("Failed to create database " + databaseName + ".");
            return false;
        }

        // Create metadata file for the database
        File metadataFile = new File("databases/" + databaseName + "/metadata.txt");
        try {
            if (metadataFile.createNewFile()) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("databases/metadata_big.txt", true))) {
                    writer.write(databaseName);
                    writer.newLine();
                }
                Logger.logGeneral("Created database: " + databaseName);
                System.out.println("Database " + databaseName + " created successfully.");
                return true;
            } else {
                Logger.logGeneral("Failed to create metadata file for database " + databaseName + ".");
                System.out.println("Failed to create metadata file for database " + databaseName + ".");
                return false;
            }
        } catch (IOException e) {
            Logger.logGeneral("Failed to create metadata file for database " + databaseName + ".");
            System.out.println("Failed to create metadata file for database " + databaseName + ".");
            return false;
        }
    }

    public boolean useDatabase(String databaseName) {
        File dbDir = new File("databases/" + databaseName);
        if (!dbDir.exists()) {
            Logger.logGeneral("Database " + databaseName + " does not exist.");
            System.out.println("Database " + databaseName + " does not exist.");
            return false;
        }

        currentDatabase = databaseName;
        Logger.logGeneral("Switched to database: " + databaseName);
        System.out.println("Using database " + databaseName + ".");
        return true;
    }

    public boolean createTable(String tableName, String columns) {
        if (currentDatabase == null) {
            Logger.logGeneral("No database selected.");
            System.out.println("No database selected.");
            return false;
        }

        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " already exists.");
            System.out.println("Table " + tableName + " already exists.");
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            writer.write(columns);
            writer.newLine();
            Logger.logGeneral("Created table: " + tableName + " in database: " + currentDatabase);

            // Update metadata file for the database
            File metadataFile = new File("databases/" + currentDatabase + "/metadata.txt");
            if (!metadataFile.exists()) {
                metadataFile.createNewFile(); // Create metadata file if it doesn't exist
            }
            try (BufferedWriter metadataWriter = new BufferedWriter(new FileWriter(metadataFile, true))) {
                metadataWriter.write(tableName);
                metadataWriter.newLine();
            }

            System.out.println("Table " + tableName + " created successfully.");
            return true;
        } catch (IOException e) {
            Logger.logGeneral("Failed to create table " + tableName + ".");
            System.out.println("Failed to create table " + tableName + ".");
            return false;
        }
    }

    public boolean insertFullIntoTable(String tableName, String values) {
        if (currentDatabase == null) {
            Logger.logGeneral("No database selected.");
            System.out.println("No database selected.");
            return false;
        }

        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile, true))) {
            writer.write(values);
            writer.newLine();
            Logger.logEvent("Inserted values into table: " + tableName);
            System.out.println("Record inserted into table " + tableName + ".");
            return true;
        } catch (IOException e) {
            Logger.logGeneral("Failed to insert into table " + tableName + ".");
            System.out.println("Failed to insert into table " + tableName + ".");
            return false;
        }
    }


    public boolean insertIntoTable(String tableName, String columns, String values) throws IOException {
        // Check if a database is selected or not
        if (currentDatabase == null) {
            Logger.logGeneral("No database selected.");
            System.out.println("No database selected.");
            return false;
        }

        // Check if table exists or not
        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            // Read the first line to get column names
            String header = reader.readLine();
            if (header == null) {
                Logger.logGeneral("Table " + tableName + " is empty.");
                System.out.println("Table " + tableName + " is empty.");
                return true;
            }

            String[] tableColumns = header.split(",");
            Map<String, String> columnMetadata = new LinkedHashMap<>();
            String primaryKeyColumn = null;

            // Process columns to get metadata and primary key
            for (String column : tableColumns) {
                String[] parts = column.trim().split("\\s+");
                String columnName = parts[0];
                columnMetadata.put(columnName, column);

                if (column.toUpperCase().contains("PRIMARY KEY")) {
                    primaryKeyColumn = columnName;
                }
            }

            // Parse columns and values
            String[] columnArray = columns.split(",");
            String[] valueArray = values.split(",");

            //System.out.println("Column Meta data: "+columnMetadata.toString());
//            for(String c: columnArray)
            //System.out.println(c+", ");
//            for(String v: valueArray)
            //System.out.println(v+", ");

            Map<String, String> finalStringArray = new LinkedHashMap<>();

            for (String column : columnMetadata.keySet()) {
                finalStringArray.put(column.toUpperCase().trim(), null);
            }

            for (int i = 0; i < columnArray.length; i++) {
                finalStringArray.put(columnArray[i].toUpperCase().trim(), valueArray[i].toUpperCase().trim());
            }

            if (primaryKeyColumn != null) {
                if (finalStringArray.get(primaryKeyColumn) != null) {
                    if (isPrimaryKeyDuplicate(tableFile, primaryKeyColumn, finalStringArray.get(primaryKeyColumn))) {
                        System.out.println("Duplicate Primary value violation");
                        return false;
                    }
                } else {
                    System.out.println("Primary key value not entered violation");
                    return false;
                }
            }


            for (String key : columnMetadata.keySet()) {
                //System.out.print("Key: "+key);
                //System.out.println("    Value: "+finalStringArray.get(key));
            }

            StringBuilder finalString = new StringBuilder();
//            for(String value: finalStringArray.values()){
//                finalString.append(value+", ");
//            }

            for (String key : finalStringArray.keySet()) {
                String value = finalStringArray.get(key);
                if (value == null)
                    finalString.append("null, ");
                else finalString.append(value + ", ");
            }

            String finalStrings = finalString.toString().trim();

            if (finalStrings.endsWith(",")) {
                finalStrings = finalStrings.substring(0, finalStrings.length() - 1);
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile, true))) {
                writer.write(finalStrings);
                writer.newLine();
                Logger.logEvent("Inserted values into table: " + tableName);
                System.out.println("Record inserted into table " + tableName + ".");
                return true;
            } catch (IOException e) {
                Logger.logGeneral("Failed to insert into table " + tableName + ".");
                System.out.println("Failed to insert into table " + tableName + ".");
                return false;
            }

        } catch (IOException e) {
            Logger.logGeneral("Failed to read table " + tableName + ".");
            System.out.println("Failed to read table " + tableName + ".");
            return false;
        }
    }

    // Helper method to check for primary key duplicates
    private boolean isPrimaryKeyDuplicate(File tableFile, String primaryKeyColumn, String value) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String line;
            String header = reader.readLine(); // Skip header
            String[] tableColumns = header.split(",");

//            for(String s: tableColumns)
            //System.out.println("::::    "+s);

            int primaryKeyIndex = getIndexOf(tableColumns, primaryKeyColumn);

            //System.out.println("primaryKeyIndex : "+primaryKeyIndex);

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (primaryKeyIndex != -1 && values[primaryKeyIndex].trim().equals(value.trim())) {
                    //System.out.println("Value is similar for : "+ values[primaryKeyIndex]);
                    return true;
                }
            }
        }
        //System.out.println("Returning false");
        return false;
    }

    // Helper method to get index of a column name
    private int getIndexOf(String[] columnArray, String columnName) {
        for (int i = 0; i < columnArray.length; i++) {
            //System.out.println("checking :::"+columnArray[i].split("\\s")[0]+" and "+columnName);
            if (columnArray[i].contains(columnName)) {
                return i;
            }
        }
        return -1;
    }


    public boolean selectFromTable(String tableName, String condition) {
        if (currentDatabase == null) {
            Logger.logGeneral("No database selected.");
            System.out.println("No database selected.");
            return false;
        }

        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String header = reader.readLine();
            if (header == null) {
                Logger.logGeneral("Empty table " + tableName + ".");
                System.out.println("Empty table " + tableName + ".");
                return false;
            }

            List<String> records = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.toUpperCase(); // Convert line to upper case

                boolean matchesCondition = true;
                if (condition != null && !condition.trim().isEmpty()) {
                    String[] conditionParts = condition.split("=");
                    if (conditionParts.length == 2) {
                        String columnName = conditionParts[0].trim().toUpperCase(); // Convert column name to upper case
                        String expectedValue = conditionParts[1].trim().toUpperCase(); // Convert expected value to upper case

                        // Extract columns from header
                        String[] headers = header.split("\\s*,\\s*");
                        Map<String, Integer> columnIndexes = new HashMap<>();
                        for (int i = 0; i < headers.length; i++) {
                            String headerColumnName = headers[i].trim().split("\\s+")[0];
                            columnIndexes.put(headerColumnName.toUpperCase(), i); // Store column name in upper case
                        }

                        Integer columnIndex = columnIndexes.get(columnName);
                        if (columnIndex != null && columnIndex < line.split("\\s*,\\s*").length) {
                            String actualValue = line.split("\\s*,\\s*")[columnIndex].trim();
                            if (!actualValue.equals(expectedValue)) {
                                matchesCondition = false;
                            }
                        } else {
                            // If the column does not exist, the condition cannot be matched
                            matchesCondition = false;
                        }
                    } else {
                        Logger.logGeneral("Invalid condition format.");
                        System.out.println("Invalid condition format.");
                        return false;
                    }
                }

                if (matchesCondition) {
                    records.add(line);
                }
            }

            if (records.isEmpty()) {
                Logger.logGeneral("No records found.");
                System.out.println("No records found.");
                return true;
            } else {
                Logger.logQuery("Selected from table: " + tableName);
                System.out.println(String.join("\n", records));
                return true;
            }
        } catch (IOException e) {
            Logger.logGeneral("Failed to read from table " + tableName + ".");
            System.out.println("Failed to read from table " + tableName + ".");
            return false;
        }
    }


    public List<String> selectFromTableReturningListofStrings(String tableName, String condition) {
        if (currentDatabase == null) {
            Logger.logGeneral("No database selected.");
            System.out.println("No database selected.");
            return null;
        }

        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String header = reader.readLine();
            if (header == null) {
                Logger.logGeneral("Empty table " + tableName + ".");
                return null;
            }

            // Convert header to upper case and extract column names
            header = header.toUpperCase();
            String[] headers = header.split("\\s*,\\s*");
            Map<String, Integer> columnIndexes = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String columnName = headers[i].trim().split("\\s+")[0];
                columnIndexes.put(columnName, i);
            }

            List<String> records = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.toUpperCase(); // Convert line to upper case

                boolean matchesCondition = true;
                if (condition != null && !condition.trim().isEmpty()) {
                    String[] conditionParts = condition.split("=");
                    if (conditionParts.length == 2) {
                        String columnName = conditionParts[0].trim().toUpperCase(); // Convert column name to upper case
                        String expectedValue = conditionParts[1].trim().toUpperCase(); // Convert expected value to upper case

                        Integer columnIndex = columnIndexes.get(columnName);
                        if (columnIndex != null && columnIndex < line.split("\\s*,\\s*").length) {
                            String actualValue = line.split("\\s*,\\s*")[columnIndex].trim();
                            if (!actualValue.equals(expectedValue)) {
                                matchesCondition = false;
                            }
                        } else {
                            // If the column does not exist, the condition cannot be matched
                            matchesCondition = false;
                        }
                    } else {
                        Logger.logGeneral("Invalid condition format.");
                        System.out.println("Invalid condition format.");
                        return null;
                    }
                }

                if (matchesCondition) {
                    records.add(line);
                }
            }

            if (records.isEmpty()) {
                Logger.logGeneral("No records found.");
                System.out.println("No records found.");
                return null;
            } else {
                Logger.logQuery("Selected from table: " + tableName);
                return records;
            }
        } catch (IOException e) {
            Logger.logGeneral("Failed to read from table " + tableName + ".");
            System.out.println("Failed to read from table " + tableName + ".");
            return null;
        }
    }


    //    select (EMPLOYEEID,LASTNAME) from employees where employeeid=1;
//    use database testdb;
    public boolean extractAndPrintColumns(String columns, List<String> rows, String tableName) {
        // Extract column names from SELECT statement
        // Remove parentheses and split by commas
        String cleanColumns = columns.replaceAll("[()]", "");
        String[] columnNames = cleanColumns.split("\\s*,\\s*");

        // Print column names
        System.out.println(String.join("\t", columnNames));

        // Get the table header
        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            // Read header
            String header = reader.readLine();
            if (header == null) {
                Logger.logGeneral("Empty table " + tableName + ".");
                return false;
            }

            // Extract column names from the header
            String[] headers = header.split("\\s*,\\s*");
            Map<String, Integer> columnIndexes = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                // Extract the first word as column name
                String columnName = headers[i].trim().split("\\s+")[0];
                columnIndexes.put(columnName, i);
            }

            // Print rows with only the selected columns
            for (String row : rows) {
                String[] values = row.split("\\s*,\\s*");

                List<String> selectedValues = new ArrayList<>();
                for (String columnName : columnNames) {
                    // Trim any leading or trailing whitespace from the column name
                    columnName = columnName.trim();
                    Integer index = columnIndexes.get(columnName);

                    if (index != null && index < values.length) {
                        selectedValues.add(values[index].trim());
                    } else {
                        // Handle missing column case
                        selectedValues.add("NULL");
                    }
                }
                // Print the selected values for the current row
                System.out.println(String.join("\t", selectedValues));
            }
        } catch (IOException e) {
            Logger.logGeneral("Failed to read table file " + tableName + ".");
            return false;
        }

        return true;
    }







    public boolean updateTable(String tableName, String setClause, String condition) {
        if (currentDatabase == null) {
            Logger.logGeneral("No database selected.");
            System.out.println("No database selected.");
            return false;
        }

        //System.out.println("Current Database: " + currentDatabase);

        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
            return false;
        }

        try {
            // Read all lines from the table file
            List<String> lines = Files.readAllLines(tableFile.toPath());

            if (lines.isEmpty() || lines.size() < 2) {
                Logger.logGeneral("Table " + tableName + " is empty.");
                System.out.println("Table " + tableName + " is empty.");
            }

            // Extract header and data rows
            String header = lines.get(0);
            List<String> dataRows = lines.subList(1, lines.size());

            //System.out.println("Data Rows before update:");
            //System.out.println(dataRows);

            // Parse setClause and condition
            String[] setParts = setClause.trim().split("=");
            String setColumn = setParts[0].trim();
            String setValue = setParts[1].trim();

            // Find index of columns
            String[] headers = header.split(",");
            int setColumnIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                String columnName = headers[i].trim().split(" ")[0]; // Extract the column name
                if (columnName.equalsIgnoreCase(setColumn)) {
                    setColumnIndex = i;
                    break;
                }
            }

            if (setColumnIndex == -1) {
                Logger.logGeneral("Column " + setColumn + " not found in table " + tableName + ".");
                System.out.println("Column " + setColumn + " not found in table " + tableName + ".");
                return false;
            }

            // Update data rows based on condition
            boolean updated = false;
            for (int i = 0; i < dataRows.size(); i++) {
                String dataRow = dataRows.get(i);
                String[] columns = dataRow.split(",");

                // Check condition
                if (evaluateCondition(columns, headers, condition)) {
                    // Update column value
                    columns[setColumnIndex] = setValue;
                    dataRows.set(i, String.join(",", columns));
                    updated = true;
                }
            }

            //System.out.println("Data Rows after update:");
            //System.out.println(dataRows);

            if (updated) {
                // Write updated lines back to the table file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
                    writer.write(header);
                    writer.newLine();
                    for (String row : dataRows) {
                        writer.write(row);
                        writer.newLine();
                    }
                    Logger.logGeneral("Table " + tableName + " updated successfully.");
                    System.out.println("Table " + tableName + " updated successfully.");
                    return true;
                }
            } else {
                Logger.logGeneral("No records updated in table " + tableName + ".");
                System.out.println("No records updated in table " + tableName + ".");
                return false;
            }

        } catch (IOException e) {
            Logger.logGeneral("Failed to update table " + tableName + ".");
            System.out.println("Failed to update table " + tableName + ".");
            return false;
        }
    }

    private boolean evaluateCondition(String[] columns, String[] headers, String condition) {
        String[] orConditions = condition.split(" OR ");
        for (String orCondition : orConditions) {
            if (evaluateSingleCondition(columns, headers, orCondition.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean evaluateSingleCondition(String[] columns, String[] headers, String condition) {
        boolean negate = condition.startsWith("NOT ");
        if (negate) {
            condition = condition.substring(4).trim();
        }

        String[] parts = condition.split("=");
        if (parts.length != 2) {
            //System.out.println("Invalid condition format: " + condition);
            return false;
        }

        String column = parts[0].trim();
        String value = parts[1].trim().replaceAll("'", ""); // Remove quotes from value if present

        int columnIndex = -1;
        for (int i = 0; i < headers.length; i++) {
            String columnName = headers[i].trim().split(" ")[0]; // Extract the column name
            if (columnName.equalsIgnoreCase(column)) {
                columnIndex = i;
                break;
            }
        }

        if (columnIndex == -1) {
            //System.out.println("Column '" + column + "' not found in headers.");
            return false;
        }

        //System.out.println("Condition: " + condition);
        //System.out.println("Column to check: " + headers[columnIndex].trim());
        //System.out.println("Expected value: " + value);

        //System.out.println("Columns array:");
        for (String col : columns) {
            //System.out.println("   " + col);
        }

        boolean result = columns[columnIndex].trim().replace("'", "").equalsIgnoreCase(value);
        //System.out.println("Actual value: " + columns[columnIndex].trim().replace("'", ""));
        //System.out.println("Value looking for : "+value);
        //System.out.println("Comparison result: " + result);

        return negate ? !result : result;
    }

    public boolean deleteFromTable(String tableName, String condition) {
        if (currentDatabase == null) {
            System.out.println("No database selected.");
            return false;
        }

        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            System.out.println("Table " + tableName + " does not exist.");
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String header = reader.readLine();
            List<String> records = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.contains(condition.split("=")[1].trim())) {
                    records.add(line);
                }
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
                writer.write(header);
                writer.newLine();
                for (String record : records) {
                    writer.write(record);
                    writer.newLine();
                }
                //logger.logEvent("Deleted from table: " + tableName);
                System.out.println("Record(s) deleted from table " + tableName + ".");
                return true;
            }
        } catch (IOException e) {
            System.out.println("Failed to delete from table " + tableName + ".");
            return false;
        }
    }

    public boolean dropTable(String tableName) {
        if (currentDatabase == null) {
            Logger.logGeneral("No database selected.");
            System.out.println("No database selected.");
            return false;
        }

        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
            return false;
        }

        if (tableFile.delete()) {
            Logger.logGeneral("Dropped table: " + tableName);

            File metadataFile = new File("databases/" + currentDatabase + "/metadata.txt");
            if (metadataFile.exists()) {
                try {
                    List<String> lines = Files.readAllLines(metadataFile.toPath());
                    lines.remove(tableName); // Remove the table name from metadata
                    Files.write(metadataFile.toPath(), lines);
                } catch (IOException e) {
                    Logger.logGeneral("Failed to update metadata file for database " + currentDatabase + ".");
                    System.out.println("Failed to update metadata file for database " + currentDatabase + ".");
                    return false;
                }
            }

            System.out.println("Table " + tableName + " dropped successfully.");
            return true;
        } else {
            Logger.logGeneral("Failed to drop table " + tableName + ".");
            System.out.println("Failed to drop table " + tableName + ".");
            return false;
        }
    }

    public boolean selectAllFromTable(String tableName) {
        if (currentDatabase == null) {
            Logger.logGeneral("No database selected.");
            System.out.println("No database selected.");
            return false;
        }

        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
            return false;
        }

        try {
            List<String> lines = readTableData(tableFile);
            if (lines.isEmpty() || lines.size() < 2) {
                Logger.logGeneral("Table " + tableName + " is empty.");
                System.out.println("Table " + tableName + " is empty.");
                return true;
            }

            StringBuilder result = new StringBuilder();
            for (String line : lines) {
                result.append(line).append("\n");
            }

            System.out.println(result.toString().trim());
            return true;
        } catch (IOException e) {
            Logger.logGeneral("Failed to read table " + tableName + ".");
            System.out.println("Failed to read table " + tableName + ".");
            return false;
        }
    }

    private List<String> readTableData(File tableFile) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public String getColumnValue(String tableName, String columnName, String whereClause) {
        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String header = reader.readLine();
            int whereIndex = 0;
            int columnIndex = 0;
            String whereColumn = whereClause.split("=")[0].trim();
            String whereValue = whereClause.substring(0, whereClause.length() - 1).split("=")[1].trim();
            String[] columns = header.split(",");
            for (int i = 0; i < columns.length; i++) {
                String column = columns[i].trim().split(" ")[0].trim();
                if (column.equalsIgnoreCase(columnName.trim())) {
                    columnIndex = i;
                }

                if (column.equals(whereColumn.trim())) {
                    whereIndex = i;
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(",")[whereIndex].trim().contains(whereValue.trim())) {
                    return line.split(",")[columnIndex];
                }
            }
            return null;
        } catch (IOException e) {
            Logger.logGeneral("Failed to read from table " + tableName + ".");
            System.out.println("Failed to read from table " + tableName + ".");
            return null;
        }
    }

    public String getPrimaryColumn(String tableName) {
        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String header = reader.readLine();
            String primaryKeyColumn = "";
            if (header != null) {
                String[] columns = header.split(", ");
                for (String column : columns) {
                    if (column.contains("PRIMARY KEY")) {
                        primaryKeyColumn = column.trim().split(" ")[0];
                        break;
                    }
                }
            }
            return primaryKeyColumn;
        } catch (IOException e) {
            Logger.logGeneral("Failed to read from table " + tableName + ".");
            System.out.println("Failed to read from table " + tableName + ".");
            return null;
        }
    }

    public String[] getTableColumns(String tableName) {
        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        ArrayList<String> columnNames = new ArrayList<>();
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String header = reader.readLine();
            if (header != null) {
                String[] columns = header.split(", ");
                for (String column : columns) {
                    columnNames.add(column.trim().split(" ")[0]);
                }
            }
            return columnNames.toArray(new String[0]);
        } catch (IOException e) {
            Logger.logGeneral("Failed to read from table " + tableName + ".");
            System.out.println("Failed to read from table " + tableName + ".");
            return null;
        }
    }

    public String[] getColumnValues(String tableName) {
        File tableFile = new File("databases/" + currentDatabase + "/" + tableName + ".txt");
        ArrayList<String> columnNames = new ArrayList<>();
        if (!tableFile.exists()) {
            Logger.logGeneral("Table " + tableName + " does not exist.");
            System.out.println("Table " + tableName + " does not exist.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
            String header = reader.readLine();
            if (header != null) {
                String[] columns = header.split(", ");
                for (String column : columns) {
                    columnNames.add(column.split(" ")[0]);
                }
            }
            return columnNames.toArray(new String[0]);
        } catch (IOException e) {
            Logger.logGeneral("Failed to read from table " + tableName + ".");
            System.out.println("Failed to read from table " + tableName + ".");
            return null;
        }
    }
}
