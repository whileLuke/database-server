package edu.uob;

import java.io.File;
import java.io.IOException;

public class DropCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        if (!tableNames.isEmpty()) {
            String tableName = tableNames.get(0).toLowerCase() /*+ ".tab"*/;
            System.out.println("Table name is " + tableName);
            System.out.println("Server.tables is" + tables);
            //check the current db for tables instead.
            //This is not working properly
            File tableFile = new File(server.storageFolderPath + File.separator + currentDB,tableName + FILE_EXTENSION);
            System.out.println("Attempting to delete table: " + tableName);
            System.out.println("Expected table file path: " + tableFile.getAbsolutePath());
            System.out.println("File exists: " + tableFile.exists());

            if (!tableFile.exists()) {
                return "[ERROR] Table '" + tableName + "' does not exist in the current database.";
            }
            if (tableFile.delete()) {
                tables.remove(tableName); // Remove the table from in-memory representation
                server.saveCurrentDB(); // Save database state to persist changes
                return "[OK] Dropped table '" + tableName + "'.";
            } else {
                return "[ERROR] Failed to delete the table file for '" + tableName + "'.";
            }

        } /*else {
            return "[ERROR] Table does not exist.";*/
        else if (DBName != null) {
            if (server.deleteDatabase(DBName)) {
                return "[OK] Dropped database '" + DBName + "'.";
            } else { return "[ERROR] Database '" + DBName + "' does not exist."; }
        }
        else return "[ERROR] No table or database specified to drop.";
    }
}
