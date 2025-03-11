package edu.uob;

import java.io.File;
import java.io.IOException;

public class DropCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        // Handle DROP TABLE
        if (!tableNames.isEmpty()) {
            if (currentDB == null) {
                return "[ERROR] No database selected. Use 'USE database;' to select a database first.";
            }

            String tableName = tableNames.get(0).toLowerCase();

            if (!tables.containsKey(tableName)) {
                return "[ERROR] Table '" + tableName + "' does not exist in the current database.";
            }

            // Remove table file
            File tableFile = new File(server.storageFolderPath + File.separator + currentDB, tableName + ".tab");
            if (tableFile.delete()) {
                tables.remove(tableName);
                saveCurrentDB();
                return "[OK] Dropped table '" + tableName + "'.";
            } else {
                return "[ERROR] Failed to delete the table file for '" + tableName + "'.";
            }
        }
        // Handle DROP DATABASE
        else if (DBName != null) {
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
