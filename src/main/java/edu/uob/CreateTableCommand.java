package edu.uob;

import java.io.IOException;

public class CreateTableCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        if (currentDB == null) {
            return "[ERROR] No database selected. Use 'USE database;' to select a database first.";
        }

        if (tableNames.isEmpty()) {
            return "[ERROR] No table name specified.";
        }

        String tableName = tableNames.get(0).toLowerCase();

        // Check if table already exists
        /*if (tables.containsKey(tableName)) {
            return "[ERROR] Table '" + tableName + "' already exists.";
        }*/

        // Create table with specified columns
        Table newTable = new Table(tableName, columnNames);
        tables.put(tableName, newTable);

        if (saveCurrentDB()) {
            return "[OK] Table '" + tableName + "' created.";
        } else {
            return "[ERROR] Failed to save the table to disk.";
        }
    }
}
