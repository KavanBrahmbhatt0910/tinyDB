package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReverseEngineeringManagerTest {

    private ReverseEngineeringManager manager;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        manager = new ReverseEngineeringManager();

        // Set up the directory structure
        Files.createDirectories(tempDir.resolve("databases"));
        System.setProperty("user.dir", tempDir.toString());
    }

    @Test
    void testGenerateERD_DatabaseExists() throws IOException {
        String dbName = "testDB";
        setupTestDatabase(dbName);

        manager.generateERD(dbName);

        Path erdFile = tempDir.resolve("databases").resolve(dbName).resolve("erd.txt");
        assertTrue(Files.exists(erdFile));
        List<String> erdContent = Files.readAllLines(erdFile);
        assertTrue(erdContent.contains("table1"));
        assertTrue(erdContent.contains("column1,column2"));
        assertTrue(erdContent.contains("table2"));
        assertTrue(erdContent.contains("column3,column4"));
    }

    @Test
    void testGenerateERD_DatabaseDoesNotExist() {
        String dbName = "nonExistentDB";
        manager.generateERD(dbName);
        // Since we can't easily check the log, we'll just ensure no exception is thrown
    }

    @Test
    void testDatabaseExists_True() throws IOException {
        String dbName = "existingDB";
        setupTestDatabase(dbName);

        assertTrue(manager.databaseExists(dbName));
    }

    @Test
    void testDatabaseExists_False() {
        String dbName = "nonExistentDB";
        assertFalse(manager.databaseExists(dbName));
    }

    private void setupTestDatabase(String dbName) throws IOException {
        Path dbDir = tempDir.resolve("databases").resolve(dbName);
        Files.createDirectories(dbDir);

        // Create metadata file
        Files.write(dbDir.resolve("metadata.txt"), Arrays.asList("table1", "table2"));

        // Create table files
        Files.write(dbDir.resolve("table1.txt"), List.of("column1,column2"));
        Files.write(dbDir.resolve("table2.txt"), List.of("column3,column4"));

        // Create metadata_big.txt
        Files.write(tempDir.resolve("databases").resolve("metadata_big.txt"), List.of(dbName));
    }
}