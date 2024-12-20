package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager {
    private static final String TRANSATCION_INITIALIZATION_FAILURE_MSG = "Transaction initialization failed. Cannot start a new transaction when a transaction is already in progress.";
    private static final String TRANSACTION_STARTED_MSG = "Transaction started successfully.";
    private static final String COMMIT_INITIALIZATION_MSG = "Started transaction commit.";
    private static final String COMMIT_FINAL_MSG = "Finished transaction commit.";
    private static final String COMMIT_FAILURE_MSG = "Cannot commit. No transaction found.";
    private static final String ROLLBACK_INITIALIZATION_MSG = "Rollback started.";
    private static final String ROLLBACK_FINAL_MSG = "Finished transaction rollback.";

    private static final String ROLLBACK_FAILURE_MSG = "Cannot rollback. No transaction found.";
    private final List<String> transactionQueries = new ArrayList<>();
    private final DBManager dbManager;
    private List<String> reversedQueries = new ArrayList<>();
    private boolean transactionStarted = false;
    private boolean committing = false;

    public TransactionManager(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<String> getReversedQueries() {
        return reversedQueries;
    }

    public void setReversedQueries(List<String> reversedQueries) {
        this.reversedQueries = reversedQueries;
    }

    public List<String> getReversedQueries(int limit) {
        return reversedQueries.subList(0, limit);
    }

    public boolean startTransaction() throws IOException {
        if (dbManager.getCurrentDatabase() == null) {
            Logger.logEvent("Failed to start transaction as database is not selected");
            System.out.println("Failed to start transaction as database is not selected");
            return false;
        }
        if (transactionStarted) {
            Logger.logEvent(TRANSATCION_INITIALIZATION_FAILURE_MSG);
            System.out.println(TRANSATCION_INITIALIZATION_FAILURE_MSG);
            return false;
        } else {
            transactionStarted = true;
            transactionQueries.clear();
            Logger.logEvent(TRANSACTION_STARTED_MSG);
            System.out.println(TRANSACTION_STARTED_MSG);
            return true;

        }
    }

    public boolean startCommit() {
        if (dbManager.getCurrentDatabase() == null) {
            Logger.logEvent("Failed to commit as database is not selected");
            System.out.println("Failed to commit as database is not selected");
            return false;
        }

        if (!transactionStarted) {
            Logger.logEvent(COMMIT_FAILURE_MSG);
            System.out.println(COMMIT_FAILURE_MSG);
            return false;
        } else {
            committing = true;
            Logger.logEvent(COMMIT_INITIALIZATION_MSG);
            System.out.println(COMMIT_INITIALIZATION_MSG);
            return true;
        }
    }

    public boolean finishCommit() {
        if (!transactionStarted) {
            Logger.logEvent(COMMIT_FAILURE_MSG);
            System.out.println(COMMIT_FAILURE_MSG);
            return false;
        } else {
            committing = false;
            transactionStarted = false;
            transactionQueries.clear();
            reversedQueries.clear();
            Logger.logEvent(COMMIT_FINAL_MSG);
            System.out.println(COMMIT_FINAL_MSG);
            return true;
        }
    }

    public boolean rollback() {
        if (dbManager.getCurrentDatabase() == null) {
            Logger.logEvent("Failed to rollback as database is not selected");
            System.out.println("Failed to rollback as database is not selected");
            return false;
        }
        if (!transactionStarted) {
            Logger.logEvent(ROLLBACK_FAILURE_MSG);
            System.out.println(ROLLBACK_FAILURE_MSG);
            return false;
        } else {
            committing = false;
            transactionStarted = false;
            Logger.logEvent(ROLLBACK_INITIALIZATION_MSG);
            System.out.println(ROLLBACK_INITIALIZATION_MSG);
            return true;
        }
    }

    public boolean finishRollback() {
        if (dbManager.getCurrentDatabase() == null) {
            Logger.logEvent("Failed to rollback as database is not selected");
            System.out.println("Failed to rollback as database is not selected");
            return false;
        }
        if (!transactionStarted) {
            Logger.logEvent(ROLLBACK_FAILURE_MSG);
            System.out.println(ROLLBACK_FAILURE_MSG);
            return false;
        } else {
            committing = false;
            transactionStarted = false;
            transactionQueries.clear();
            reversedQueries.clear();
            Logger.logEvent(ROLLBACK_FINAL_MSG);
            System.out.println(ROLLBACK_FINAL_MSG);
            return true;
        }
    }

    public boolean isTransactionStarted() {
        return transactionStarted;
    }

    public void setTransactionStarted(boolean transactionStarted) {
        this.transactionStarted = transactionStarted;
    }

    public boolean setTransactionQueries(String query) {
        this.transactionQueries.add(query);
        if (query.trim().toLowerCase().startsWith("create table") || query.trim().toLowerCase().startsWith("drop table") || query.trim().toLowerCase().startsWith("select")) {
            return true;
        }
        boolean reverseQueryStatus = this.reverseDMLQuery(query);
        if (!reverseQueryStatus) {
            return false;
        }
        System.out.println("Added " + query + " into the transaction list");
        return false;
    }

    public List<String> getTransactionQueries() {
        return transactionQueries;
    }

    public boolean isCommitting() {
        return committing;
    }

    public void setCommitting(boolean committing) {
        this.committing = committing;
    }

    public boolean reverseDMLQuery(String originalQuery) {
        String reversedQuery = "";

        if (originalQuery.trim().toLowerCase().startsWith("insert")) {
            String tableName = originalQuery.toLowerCase().split(" ")[2].trim();
            String primaryKeyColumn = this.dbManager.getPrimaryColumn(tableName);

            if (!primaryKeyColumn.trim().isEmpty()) {
                // Extract column names
                String columnsString = originalQuery.substring(originalQuery.indexOf("(") + 1, originalQuery.indexOf(")"));
                String[] columns = columnsString.split(", ");
                int primayKeyColumnIndex = 0;
                for (int i = 0; i < columns.length; i++) {
                    if (columns[i].trim().equalsIgnoreCase(primaryKeyColumn.trim())) {
                        primayKeyColumnIndex = i;
                        break;
                    }
                }

                // Extract values
                String valuesString = originalQuery.substring(originalQuery.toLowerCase().indexOf("values (") + 8, originalQuery.lastIndexOf(";") - 1);
                String[] values = valuesString.split(", ");
                String primaryValue = values[primayKeyColumnIndex];

                reversedQuery = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + " = " + primaryValue + ";";
                this.reversedQueries.add(reversedQuery);
                return true;
            }
        } else if (originalQuery.trim().toLowerCase().startsWith("delete")) {
            String tableName = originalQuery.toLowerCase().split(" ")[2].trim();
            String[] tableColumns = this.dbManager.getTableColumns(tableName);

            if (tableColumns.length > 0) {
                String whereClause = originalQuery.substring(originalQuery.toLowerCase().indexOf("where") + 5).trim();
                ArrayList<String> previousValues = new ArrayList<>();
                for (String column : tableColumns) {
                    previousValues.add(getPreviousValue(tableName, column, whereClause));
                }
                if (previousValues.stream().anyMatch(null)) {
                    return false;
                }
                reversedQuery = "INSERT INTO " + tableName + "(" + String.join(",", tableColumns) + ") values (" + String.join(",", previousValues) + ");";
                this.reversedQueries.add(reversedQuery);
                return true;
            }
        } else if (originalQuery.trim().toLowerCase().startsWith("update")) {
            String tableName = originalQuery.substring(originalQuery.toLowerCase().indexOf("update from") + 11, originalQuery.toLowerCase().indexOf("set")).trim();
            String setClause = originalQuery.substring(originalQuery.toLowerCase().indexOf("set") + 3).trim();
            String whereClause = originalQuery.substring(originalQuery.toLowerCase().indexOf("where") + 5).trim();
            reversedQuery = "UPDATE FROM " + tableName + " SET " + reverseSetClause(tableName, setClause, whereClause) + " WHERE " + whereClause + ";";
            this.reversedQueries.add(reversedQuery);
            return true;
        } else if (originalQuery.trim().toLowerCase().startsWith("create table")) {
            return true;
        } else if (originalQuery.trim().toLowerCase().startsWith("drop table")) {
            return true;
        } else {
            return false;
        }
        return false;
    }

    private String reverseSetClause(String tableName, String setClause, String whereClause) {
        String[] assignments = setClause.split(",");
        StringBuilder reversedSetClause = new StringBuilder();

        for (int i = 0; i < assignments.length; i++) {
            String assignment = assignments[i];
            String[] parts = assignment.trim().split("=");
            String columnName = parts[0].trim();
            String previousValue = getPreviousValue(tableName, columnName, whereClause);
            reversedSetClause.append(parts[0].trim()).append(" = ").append(previousValue).append((i == assignments.length - 1) ? "" : ",");
        }

        return reversedSetClause.toString();
    }

    private String getPreviousValue(String tableName, String columnName, String whereClause) {
        // Placeholder for retrieving previous value
        return dbManager.getColumnValue(tableName, columnName, whereClause);
    }
}
