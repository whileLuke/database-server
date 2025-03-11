package edu.uob;

import java.io.IOException;

public class AlterCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        DBResponse validationResponse = validateDatabaseSelected();
        if (validationResponse != null) return validationResponse;

        if (tableNames.isEmpty() || columnNames.isEmpty() || commandType == null) return DBResponse.error("Invalid ALTER TABLE command format.");

        String tableName = tableNames.get(0).toLowerCase();
        validationResponse = validateTableExists(tableName);
        if (validationResponse != null) return validationResponse;

        String columnName = columnNames.get(0);
        validationResponse = CommandValidator.validateNotIdColumn(columnName);
        if (validationResponse != null) return validationResponse;

        Table table = getTable(tableName);
        boolean success;

        if (commandType.equalsIgnoreCase("ADD")) {
            success = table.addColumn(columnName);
            if (!success) return DBResponse.error("Column '" + columnName + "' already exists in table '" + tableName + "'.");
        }
        else if (commandType.equalsIgnoreCase("DROP")) {
            success = table.dropColumn(columnName);
            if (!success) return DBResponse.error("Column '" + columnName + "' does not exist in table '" + tableName + "'.");
        }
        else return DBResponse.error("Unknown ALTER operation: " + commandType + ". Please use either ADD or DROP.");

        if (saveCurrentDB()) return DBResponse.success("Table '" + tableName + "' altered successfully.");
        else return DBResponse.error("Failed to save altered table to the disk.");
    }
}
