package edu.uob;

public class UpdateCommand extends DBCommand {
    public String query(DBServer server) {
        if (tableNames.isEmpty() || columnNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] Table name, columns, or values missing in UPDATE query.";
        }
        String tableName = tableNames.get(0);
        Table table = server.tables.get(tableName.toLowerCase());
        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }
        if (table.updateRows(columnNames, values)) {
            return "[OK] Table '" + tableName + "' updated successfully.";
        } else {
            return "[ERROR] Failed to update table '" + tableName + "' (column mismatch or missing?).";
        }
    }

}
