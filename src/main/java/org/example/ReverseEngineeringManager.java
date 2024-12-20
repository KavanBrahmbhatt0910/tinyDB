package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ReverseEngineeringManager {
    public void generateERD(String databaseName)  {
        databaseName = databaseName.toUpperCase().trim();
        Logger.logGeneral("Generating ERD for database: " + databaseName);
        if (databaseExists(databaseName)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("databases/"+databaseName+"/erd.txt", true))){
                List<String> lines = Files.readAllLines(Paths.get("databases/"+databaseName+"/metadata.txt"));
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                    WriteTableInfo(writer,line,databaseName);
                    writer.write("--------------------------------------------------");
                    writer.newLine();
                }
                System.out.println( "ERD generated successfully");
            } catch (IOException e) {
                Logger.logGeneral("Error generating ERD: " + e.getMessage());
                System.err.println("Error generating ERD: " + e.getMessage());
            }
            Logger.logGeneral( "ERD generated successfully");
        } else {
            Logger.logGeneral("Error generating ERD: Database does not exist");
            System.out.println("Error generating ERD: Database does not exist");
        }

    }

    public void WriteTableInfo(BufferedWriter writer, String line, String databaseName) {
        try (BufferedReader reader = new BufferedReader(new FileReader("databases/"+databaseName + "/" + line+".txt"))){
            String tableLine = reader.readLine();
            String[] columns = tableLine.split(",");
            for (String column : columns) {
                writer.write(column);
                writer.newLine();
            }
        } catch (IOException e) {
            Logger.logGeneral("Error writing table info: " + e.getMessage());
            System.err.println("Error writing table info: " + e.getMessage());
        }
    }

    public boolean databaseExists(String databaseName) {
        try {
            List<String> lines = Files.readAllLines(Paths.get("databases/metadata_big.txt"));
            for (String line : lines) {
                if (line.contains(databaseName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            Logger.logGeneral("Error checking database existence: " + e.getMessage());
            System.err.println("Error checking database existence: " + e.getMessage());
        }
        return false;
    }
}
