package edu.uob;

import java.io.IOException;
import java.util.ArrayList;

public class InsertCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        if (currentDB == null) {
            return "[ERROR] No database selected. Use 'USE database;' to select a database first.";
        }

        if (tableNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] Table name or values for insertion are missing.";
        }

        String tableName = tableNames.get(0).toLowerCase();
        Table table = tables.get(tableName);

        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        // Process values - remove quotes
        ArrayList<String> processedValues = new ArrayList<>();
        for (String value : values) {
            processedValues.add(removeQuotes(value));
        }

        // Add ID to the beginning of the row
        int id = table.generateNextID();
        processedValues.add(0, String.valueOf(id));

        if (table.addRow(processedValues)) {
            if (saveCurrentDB()) {
                return "[OK] 1 row inserted into '" + tableName + "'.";
            } else {
                return "[ERROR] Failed to save database after insertion.";
            }
        } else {
            return "[ERROR] Failed to insert into '" + tableName + "'. Column count mismatch: expected " +
                    (table.getColumns().size() - 1) + " columns, got " + (processedValues.size() - 1) + " values.";
        }
    }

    private String removeQuotes(String value) {
        if (value == null) return "";
        if ((value.startsWith("'") && value.endsWith("'")) ||
                (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
