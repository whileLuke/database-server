package edu.uob;

import java.io.File;
import java.io.IOException;

public class DropCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        if (!tableNames.isEmpty()) {
            String errorMessage = errorChecker.checkIfDatabaseSelected();
            if (errorMessage != null) return errorMessage;
            String tableName = tableNames.get(0).toLowerCase();
            errorMessage = errorChecker.checkIfTableExists(tableName);
            if (errorMessage != null) return errorMessage;
            File tableFile = new File(server.getStorageFolderPath() + File.separator + currentDB, tableName + ".tab");
            if (tableFile.delete()) {
                tables.remove(tableName);
                saveCurrentDB();
                return "[OK] Dropped table '" + tableName + "'.";
            } else {
                return "[ERROR] Failed to delete the table file for '" + tableName + "'.";
            }
        }
        else if (DBName != null) {
            String errorMessage = errorChecker.checkIfDBNameProvided(DBName);
            if (errorMessage != null) return errorMessage;
            if (server.deleteDatabase(DBName)) {
                return "[OK] Dropped database '" + DBName + "'.";
            } else {
                return "[ERROR] Database '" + DBName + "' does not exist.";
            }
        }
        else {
            return "[ERROR] No table or database specified to drop.";
        }
    }
}
