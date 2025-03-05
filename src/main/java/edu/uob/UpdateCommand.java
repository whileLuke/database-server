package edu.uob;

public class UpdateCommand extends DBCommand {
    @Override
    public String query(DBServer server) {
        if (tableNames.isEmpty() || columnNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] Invalid UPDATE command format.";
        }
        String tableName = tableNames.get(0).toLowerCase() /*+ ".tab"*/;
        Table table = server.tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }
        if (columnNames.size() != values.size()) {
            return "[ERROR] The number of columns do not match the number of values.";
        }
        if (table.updateRows(columnNames, values)) {
            if (server.saveCurrentDB()) {
                return "[OK] Table '" + tableName + "' updated.";
            } else {
                return "[ERROR] Failed to save updated table to disk.";
            }
        } else {
            return "[ERROR] Failed to update table '" + tableName + "' (column mismatch or missing?).";
        }
    }

}
