package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryProcessor {
    private final DBManager dbManager;
    private final TransactionManager transactionManager;

    public QueryProcessor(DBManager dbManager) {
        this.dbManager = dbManager;
        this.transactionManager = new TransactionManager(dbManager);
    }

    public boolean processQuery(String query) throws IOException {
        if (!query.endsWith(";")) {
            Logger.logGeneral("Invalid query: Missing ; at EOL");
            System.out.println("Invalid query: Missing ; at EOL");
            return false;
        }
        query = query.trim().toUpperCase();

        if (transactionManager.isTransactionStarted() && !transactionManager.isCommitting()) {
            if (query.startsWith("COMMIT")) {
                transactionManager.startCommit();
                List<String> transactionQueries = transactionManager.getTransactionQueries();
                List<String> reverseTransactionQueries = new ArrayList<>();
                boolean isRollback = false;
                for (int i = 0; i < transactionQueries.size(); i++) {
                    String transactionQuery = transactionQueries.get(i);
                    if (!processQuery(transactionQuery)) {
                        isRollback = true;
                        transactionManager.rollback();
                    }
                    if (isRollback && (i > 0)) {
                        reverseTransactionQueries = transactionManager.getReversedQueries(i);
                        break;
                    }
                }
                for (String rollbackQuery : reverseTransactionQueries) {
                    processQuery(rollbackQuery);
                }
                if (!isRollback) {
                    transactionManager.finishCommit();
                }
                return true;
            } else if (query.startsWith("ROLLBACK")) {
                return transactionManager.rollback();
            } else if (query.startsWith("CREATE TABLE") || query.startsWith("INSERT INTO") || query.startsWith("SELECT FROM") || query.startsWith("SELECT * FROM") || query.startsWith("UPDATE") || query.startsWith("DELETE FROM") || query.startsWith("DROP TABLE")) {
                transactionManager.setTransactionQueries(query);
                return true;
            } else if (query.startsWith("START TRANSACTION")) {
                Logger.logGeneral("A transaction is already in progress.");
                System.out.println("A transaction is already in progress.");
                return false;
            } else {
                Logger.logGeneral("Invalid Query for transaction");
                System.out.println("Invalid Query for transaction.");
                return false;
            }
        }

        query = query.split(";")[0];

        if (query.startsWith("START TRANSACTION")) {
            return transactionManager.startTransaction();
        } else if (query.startsWith("CREATE DATABASE")) {
            return createDatabase(query);
        } else if (query.startsWith("USE DATABASE")) {
            return useDatabase(query);
        } else if (query.startsWith("CREATE TABLE")) {
            return createTable(query);
        } else if (query.startsWith("INSERT INTO")) {
            return insertIntoTable(query);
        }else if(query.startsWith("SELECT (")){
            return selectFromTableLimitedColumns(query);
        }else if (query.startsWith("SELECT * FROM")) {
            return selectFromTable(query);
        } else if (query.startsWith("UPDATE")) {
            return updateTable(query);
        } else if (query.startsWith("DELETE FROM")) {
            return deleteFromTable(query);
        } else if (query.startsWith("DROP TABLE")) {
            return dropTable(query);
        } else {
            Logger.logGeneral("Invalid query.");
            System.out.println("Invalid query.");
            return false;
        }
    }

//    private boolean selectAllFromTable(String query) {
//        String[] tokens = query.split("\\s+");
//        if (tokens.length < 4 || !tokens[2].equalsIgnoreCase("FROM")) {
//            Logger.logGeneral("Invalid SELECT * FROM query syntax.");
//            System.out.println("Invalid SELECT * FROM query syntax.");
//            return false;
//        }
//        String tableName = tokens[3];
//        Logger.logQuery(query);
//        return dbManager.selectAllFromTable(tableName);
//    }

    private boolean createDatabase(String query) {
        String[] db = query.split(" ");
        if (db.length != 3) {
            System.out.println("Invalid query.");
            return false;
        }
        String databaseName = query.split(" ")[2];
        Logger.logQuery(query);
        return dbManager.createDatabase(databaseName);
    }

    private boolean useDatabase(String query) {
        String[] db = query.split(" ");
        if (db.length != 3) {
            System.out.println("Invalid query.");
            return false;
        }
        String databaseName = query.split(" ")[2];
        Logger.logQuery(query);
        return dbManager.useDatabase(databaseName);
    }

    private boolean createTable(String query) {
        // Extract table name and columns from query
        Pattern pattern = Pattern.compile("CREATE TABLE (\\w+) \\((.+)\\)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String columns = matcher.group(2);

            // Handle primary key constraints and construct column definitions
            String primaryKey = null;
            String[] columnDefsArray = columns.split(",");
            for (String columnDef : columnDefsArray) {
                columnDef = columnDef.trim();
                if (columnDef.toUpperCase().contains("PRIMARY KEY")) {
                    primaryKey = columnDef;
                }
            }

            System.out.println("Primary key is : " + primaryKey);
            Logger.logQuery(query);
            return dbManager.createTable(tableName, columns);
        } else {
            Logger.logGeneral("Invalid CREATE TABLE query.");
            System.out.println("Invalid CREATE TABLE query.");
            return false;
        }
    }

//    private boolean insertIntoTable(String query) {
//        // Extract table name and values from query
//        Pattern pattern = Pattern.compile("INSERT\\s+INTO\\s+\\w+\\s+\\((.+)\\)\\s+VALUES\\s+\\((.+)\\)", Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher(query);
//        if (matcher.find()) {
//            String tableName = matcher.group(1);
//            String values = matcher.group(2);
//            Logger.logQuery(query);
//            return dbManager.insertIntoTable(tableName, values);
//        } else {
//            Logger.logGeneral("Invalid INSERT INTO query.");
//            System.out.println("Invalid INSERT INTO query."); return false;
//        }
//    }

    private boolean insertIntoTable(String query) throws IOException {
        // Extract table name, columns, and values from query
        Pattern pattern = Pattern.compile("INSERT INTO (\\w+) \\((.+)\\) VALUES \\((.+)\\)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String columns = matcher.group(2);
            String values = matcher.group(3);
            Logger.logQuery(query);
            return dbManager.insertIntoTable(tableName, columns, values);
        } else {
            matcher = Pattern.compile("INSERT INTO (\\w+) VALUES \\((.+)\\)").matcher(query);
            if (matcher.find()) {
                String tableName = matcher.group(1);
                String values = matcher.group(2);
                Logger.logQuery(query);
                return dbManager.insertFullIntoTable(tableName, values);
            } else {
                Logger.logGeneral("Invalid INSERT INTO query.");
                System.out.println("Invalid INSERT INTO query.");
                return false;
            }
        }
    }


    private boolean selectFromTable(String query) {
        // Extract table name and condition from query
        Pattern pattern = Pattern.compile("SELECT \\* FROM (\\w+)( WHERE (.+))?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String condition = matcher.group(3);

            Logger.logQuery(query);

            // Call selectAllFromTable if no WHERE condition
            if (condition == null) {
                return dbManager.selectAllFromTable(tableName);
            } else {
                return dbManager.selectFromTable(tableName, condition);
            }
        } else {
            Logger.logGeneral("Invalid SELECT query.");
            System.out.println("Invalid SELECT query.");
            return false;
        }
    }


    private boolean selectFromTableLimitedColumns(String query) {
        // Extract columns, table name, and condition from query
        Pattern pattern = Pattern.compile("SELECT (.+) FROM (\\w+)( WHERE (.+))?");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String columns = matcher.group(1).trim();
            String tableName = matcher.group(2).trim();
            String condition = matcher.group(4);

            // Handle selection of all columns
            Logger.logQuery(query);
            List<String> rows = dbManager.selectFromTableReturningListofStrings(tableName, condition);
            return dbManager.extractAndPrintColumns(columns, rows, tableName);
        } else {
            Logger.logGeneral("Invalid SELECT query.");
            System.out.println("Invalid SELECT query.");
            return false;
        }
    }




    //    private boolean updateTable(String query) {
//        // Extract table name, set clause, and condition from query
//        Pattern pattern = Pattern.compile("UPDATE (\\w+) SET (.+) WHERE (.+)");
//        Matcher matcher = pattern.matcher(query);
//        if (matcher.find()) {
//            String tableName = matcher.group(1);
//            String setClause = matcher.group(2);
//            String condition = matcher.group(3);
//            return dbManager.updateTable(tableName, setClause, condition);
//        } else {
//            System.out.println("Invalid UPDATE query."); return false;
//        }
//    }
    private boolean updateTable(String query) {
        String[] tokens = query.split("\\s+");
        if (tokens.length < 6 || !tokens[2].equalsIgnoreCase("SET")) {
            Logger.logGeneral("Invalid UPDATE query syntax.");
            System.out.println("Invalid UPDATE query syntax.");
            return false;
        }

        // Extract parts from the query
        int setIndex = query.toUpperCase().indexOf(" SET ");
        int whereIndex = query.toUpperCase().indexOf(" WHERE ");
        String tableName = tokens[1];
        String setClause = query.substring(setIndex + 5, whereIndex).trim();
        String condition = query.substring(whereIndex + 7).trim().replace(";", "");
        Logger.logQuery(query);
        return dbManager.updateTable(tableName, setClause, condition);
    }

    private boolean deleteFromTable(String query) {
        // Extract table name and condition from query
        Pattern pattern = Pattern.compile("DELETE FROM (\\w+) WHERE (.+)");
        Matcher matcher = pattern.matcher(query);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String condition = matcher.group(2);
            Logger.logQuery(query);
            return dbManager.deleteFromTable(tableName, condition);
        } else {
            Logger.logGeneral("Invalid DELETE FROM query.");
            System.out.println("Invalid DELETE FROM query.");
            return false;
        }
    }

    private boolean dropTable(String query) {
        String[] db = query.split(" ");
        if (db.length != 3) {
            System.out.println("Invalid query.");
            return false;
        }
        String tableName = query.split(" ")[2];
        Logger.logQuery(query);
        return dbManager.dropTable(tableName);
    }
}
