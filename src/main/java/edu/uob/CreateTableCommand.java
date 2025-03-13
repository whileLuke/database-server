package edu.uob;

import java.io.IOException;

public class CreateTableCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkIfDatabaseSelected();
        if (errorMessage != null) return errorMessage;

        errorMessage = errorChecker.checkIfTableNameProvided(tableNames);
        if (errorMessage != null) return errorMessage;

        String tableName = tableNames.get(0).toLowerCase();
        errorMessage = errorChecker.checkIfReservedWord(tableName);
        if (errorMessage != null) return errorMessage;

        if (tables.containsKey(tableName)) return "[ERROR] Table '" + tableName + "' already exists.";

        errorMessage = errorChecker.checkForDuplicateColumns(columnNames);
        if (errorMessage != null) return errorMessage;

        //errorMessage = errorChecker.checkIfTableExists(tableName);
        //if (errorMessage != null) return "[ERROR] Failed to create table '" + tableName + "'. Check if it already exists.";
        for (String columnName : columnNames) {
            errorMessage = errorChecker.checkIfReservedWord(columnName);
            if (errorMessage != null) return errorMessage;
        }
        DBTable newTable = new DBTable(tableName, columnNames);
        tables.put(tableName, newTable);
        if (saveCurrentDB()) return "[OK] You have successfully created table '" + tableName + "'.";
        else return "[ERROR] Failed to save the table.";
    }
}
