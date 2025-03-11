package edu.uob;

import java.io.File;
import java.io.IOException;

public class DropCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        // Handle DROP TABLE
        if (!tableNames.isEmpty()) {
            // Validate database is selected
            DBResponse validationResponse = validateDatabaseSelected();
            if (validationResponse != null) return validationResponse;

            String tableName = tableNames.get(0).toLowerCase();

            // Validate table exists
            validationResponse = validateTableExists(tableName);
            if (validationResponse != null) return validationResponse;

            // Remove table file
            File tableFile = new File(server.getStorageFolderPath() + File.separator + currentDB, tableName + ".tab");
            if (tableFile.delete()) {
                tables.remove(tableName);
                saveCurrentDB();
                return DBResponse.success("Dropped table '" + tableName + "'.");
            } else {
                return DBResponse.error("Failed to delete the table file for '" + tableName + "'.");
            }
        }
        // Handle DROP DATABASE
        else if (DBName != null) {
            // Validate database name
            DBResponse validationResponse = CommandValidator.validateDatabaseNameProvided(DBName);
            if (validationResponse != null) return validationResponse;

            if (server.deleteDatabase(DBName)) {
                return DBResponse.success("Dropped database '" + DBName + "'.");
            } else {
                return DBResponse.error("Database '" + DBName + "' does not exist.");
            }
        }
        else {
            return DBResponse.error("No table or database specified to drop.");
        }
    }
}