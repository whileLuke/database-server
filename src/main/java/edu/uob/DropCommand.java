package edu.uob;

import java.io.File;

public class DropCommand extends DBCommand {
    public String query(DBServer server) {
        if (!tableNames.isEmpty()) {
            String tableName = tableNames.get(0);
            if (server.tables.containsKey(tableName.toLowerCase())) {
                server.tables.remove(tableName.toLowerCase());
                File tableFile = new File(storageFolderPath + File.separator + currentDB, tableName.toLowerCase() + ".tab");
                if (tableFile.exists()) {
                    tableFile.delete(); // Remove table file from disk
                }
                server.saveCurrentDB();
                return "[OK] Dropped table '" + tableName + "'.";
            } else {
                return "[ERROR] Table '" + tableName + "' does not exist.";
            }
        } else if (DBName != null) {
            if (server.deleteDatabase(DBName)) {
                return "[OK] Dropped database '" + DBName + "'.";
            } else {
                return "[ERROR] Database '" + DBName + "' does not exist.";
            }
        }
        return "[ERROR] No table or database specified to drop.";
    }

}
