package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeleteCommand extends DBCommand {
    private List<String> conditions = new ArrayList<>();

    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    @Override
    public String query(DBServer server) throws IOException {
        //loadTables(currentDB);
        if (tableNames.isEmpty()) return "[ERROR] No table specified for deletion.";

        String tableName = tableNames.get(0).toLowerCase();
        Table table = tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }
        if (conditions.isEmpty()) {
            return "[ERROR] DELETE command requires a WHERE condition.";
        }

        // Delete rows matching the conditions
        try {
            // Table does the actual deletion
            TableQuery tableQuery = new TableQuery(table);
            int rowsDeleted = tableQuery.deleteRowsWithConditions(conditions);
            if (rowsDeleted > 0) {
                if (server.saveCurrentDB()) {
                    return "[OK] " + rowsDeleted + " row(s) deleted.";
                } else {
                    return "[ERROR] Failed to save updated table to disk.";
                }
            } else {
                return "[ERROR] No rows matched the delete condition.";
            }
        } catch (Exception e) {
            return "[ERROR] Failed to process delete conditions: " + e.getMessage();
        }
    }
}
