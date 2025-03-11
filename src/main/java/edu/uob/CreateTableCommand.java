package edu.uob;

import java.io.IOException;

public class CreateTableCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        System.out.println("Create table test");
        if (tableNames.isEmpty()) return "[ERROR] Table name not specified.";
        if (tableNames.size() != 1) return "[ERROR] Incorrect number of table names selected.";
        if (currentDB == null) return "[ERROR] No database selected. Use 'USE database;' first.";
        String tableName = tableNames.get(0).toLowerCase();
        System.out.println("Create table test 2");

        if (!columnNames.contains("id")) {
            columnNames.add(0, "id");
        }
        Table newTable = new Table(tableName, columnNames);
        tables.put(tableName, newTable);
        if (saveCurrentDB()) {
            return "[OK] Created table '" + tableName + "'.";
        } else {
            tables.remove(tableName);
            return "[ERROR] Failed to save table '" + tableName + "' to disk.";
        }
    }
}
