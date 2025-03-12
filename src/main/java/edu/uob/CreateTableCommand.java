package edu.uob;

import java.io.IOException;

public class CreateTableCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        // Validate database is selected
        DBResponse validationResponse = validateDatabaseSelected();
        if (validationResponse != null) return validationResponse;

        // Validate table name is provided
        validationResponse = validateTableNameProvided();
        if (validationResponse != null) return validationResponse;

        String tableName = tableNames.get(0).toLowerCase();

        // Check if the table name is a reserved word
        if (NotAllowedWords.isNotAllowed(tableName)) {
            return DBResponse.error("Cannot use reserved word '" + tableName + "' as a table name.");
        }

        // Check column names as well
        for (String columnName : columnNames) {
            if (NotAllowedWords.isNotAllowed(columnName)) {
                return DBResponse.error("Cannot use reserved word '" + columnName + "' as a column name.");
            }
        }

        Table newTable = new Table(tableName, columnNames);
        tables.put(tableName, newTable);

        if (saveCurrentDB()) {
            return DBResponse.success("Table '" + tableName + "' created.");
        } else {
            return DBResponse.error("Failed to save the table to disk.");
        }
    }
}
