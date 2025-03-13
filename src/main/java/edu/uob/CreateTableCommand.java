package edu.uob;

import java.io.IOException;

public class CreateTableCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.validateDatabaseSelected();
        if (errorMessage != null) return errorMessage;
        errorMessage = errorChecker.validateTableNameProvided(tableNames);
        if (errorMessage != null) return errorMessage;
        String tableName = tableNames.get(0).toLowerCase();
        if (ReservedWords.isNotAllowed(tableName)) {
            return "[ERROR] Cannot use reserved word '" + tableName + "' as a table name.";
        }
        for (String columnName : columnNames) {
            if (ReservedWords.isNotAllowed(columnName)) {
                return "[ERROR] Cannot use reserved word '" + columnName + "' as a column name.";
            }
        }
        DBTable newTable = new DBTable(tableName, columnNames);
        tables.put(tableName, newTable);
        if (saveCurrentDB()) return "[OK] Successfully created table '" + tableName + ".";
        else return "[ERROR] Failed to save the table.";
    }
}
