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

        // Check if table already exists
        /*if (tables.containsKey(tableName)) {
            return DBResponse.error("Table '" + tableName + "' already exists.");
        }*/

        // Create table with specified columns
        Table newTable = new Table(tableName, columnNames);
        tables.put(tableName, newTable);

        if (saveCurrentDB()) {
            return DBResponse.success("Table '" + tableName + "' created.");
        } else {
            return DBResponse.error("Failed to save the table to disk.");
        }
    }
}
