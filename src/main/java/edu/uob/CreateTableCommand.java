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
        errorMessage = errorChecker.CheckIfReservedWord(tableName);
        if (errorMessage != null) return errorMessage;
        for (String columnName : columnNames) {
            errorMessage = errorChecker.CheckIfReservedWord(columnName);
            if (errorMessage != null) return errorMessage;
        }
        DBTable newTable = new DBTable(tableName, columnNames);
        tables.put(tableName, newTable);
        if (saveCurrentDB()) return "[OK] Successfully created table '" + tableName + ".";
        else return "[ERROR] Failed to save the table.";
    }
}
