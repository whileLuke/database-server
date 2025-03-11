package edu.uob;

import java.io.IOException;
import java.util.Objects;

public class AlterCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        //loadTables(currentDB);
        if (currentDB == null) return "[ERROR] No database selected. Use 'USE database;' to select a database first.";
        if (tableNames.isEmpty() || columnNames.isEmpty() || commandType == null) {
            return "[ERROR] Invalid ALTER TABLE command format.";
        }
        String tableName = tableNames.get(0).toLowerCase() /*+ ".tab"*/; // Only one table name is allowed
        String columnName = columnNames.get(0);
        if (Objects.equals(columnName, "id")) return "[ERROR] Cannot alter the ID column.";
        Table table = tables.get(tableName);
        if (table == null) return "[ERROR] Table '" + tableName + "' does not exist in the current database.";
        boolean success;
        if (commandType.equalsIgnoreCase("ADD")) {
            success = table.addColumn(columnName);
            if (!success) return "[ERROR] Column '" + columnName + "' already exists in table '" + tableName + "'.";
        }
        else if (commandType.equalsIgnoreCase("DROP")) {
            success = table.dropColumn(columnName);
            if (!success) return "[ERROR] Column '" + columnName + "' does not exist in table '" + tableName + "'.";
        }
        else return "[ERROR] Unknown ALTER operation: " + commandType + ". Please use either ADD or DROP.";
        if (saveCurrentDB()) return "[OK] Table '" + tableName + "' altered successfully.";
        else return "[ERROR] Failed to save altered table to the disk.";
    }
}
