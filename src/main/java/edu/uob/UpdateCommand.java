package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateCommand extends DBCommand {
    private List<String> conditions = new ArrayList<>();

    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    @Override
    public String query(DBServer server) throws IOException, Exception {
        //loadTables(currentDB);
        if (tableNames.isEmpty() || columnNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] Invalid UPDATE command format.";
        }
        String tableName = tableNames.get(0).toLowerCase() /*+ ".tab"*/;
        Table table = tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }
        if (columnNames.size() != values.size()) {
            return "[ERROR] The number of columns do not match the number of values.";
        }
        TableQuery tableQuery = new TableQuery(table);
        int updatedRows = tableQuery.updateRowsWithConditions(columnNames, values, conditions, server);
        if (updatedRows > 0) {
            if (saveCurrentDB()) {
                System.out.println("[DEBUG] saved " + server.saveCurrentDB());
                loadTables(currentDB);
                return "[OK] " + updatedRows + " row(s) updated.";
            } else {
                return "[ERROR] Failed to save updated table to disk.";
            }
        } else {
            return "[ERROR] No rows matched the update condition.";
        }
        /*if (table.updateRows(columnNames, values)) {
            if (saveCurrentDB()) {
                return "[OK] Table '" + tableName + "' updated.";
            } else {
                return "[ERROR] Failed to save updated table to disk.";
            }
        } else {
            return "[ERROR] Failed to update table '" + tableName + "' (column mismatch or missing?).";
        }*/
    }
}
