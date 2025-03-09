package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class SelectCommand extends DBCommand {
    private List<String> conditions = new ArrayList<>();

    public void setCondition(String condition) { this.conditions.add(condition); }

    @Override
    public String query(DBServer server) throws Exception {
        //loadTables(currentDB);
        if (currentDB == null) return "[ERROR] No database selected. Use 'USE database;' to select a database first.";

        if (tableNames.isEmpty()) return "[ERROR] Table name missing in SELECT query.";
        String tableName = tableNames.get(0).toLowerCase() /*+ ".tab"*/;
        Table table = tables.get(tableName);
        if (table == null) return "[ERROR] Table '" + tableName + "' does not exist.";
        List<String> selectedColumns;
        if (columnNames.contains("*")) {
            selectedColumns = table.getColumns();
        } else {
            selectedColumns = new ArrayList<>(columnNames);
            // Validate that all specified columns exist in the table
            for (String column : selectedColumns) {
                if (!table.getColumns().contains(column)) {
                    return "[ERROR] Column '" + column + "' does not exist in table '" + tableName + "'.";
                }
            }
        }

        // Validate the WHERE conditions if provided
        if (!conditions.isEmpty() && !isValidCondition(conditions, table.getColumns())) {
            return "[ERROR] Invalid or missing condition in WHERE clause.";
        }

        // Perform the selection
        List<List<String>> filteredRows;
        if (conditions.isEmpty()) {
            filteredRows = table.selectRowsWithoutConditions(selectedColumns); // No filtering if no conditions
        } else {
            filteredRows = table.selectRowsWithConditions(selectedColumns, conditions); // Apply filtering
        }

        // Format the result
        if (filteredRows.isEmpty()) {
            return "[OK]"; // No rows match, return only [OK]
        }
        return "[OK]\n" + formatRows(selectedColumns, filteredRows);


    }

    private boolean isValidCondition(List<String> conditions, List<String> columns) {
        for (String condition : conditions) {
            // Example condition: columnName == "value"
            String comparatorRegex = "==|!=|>|<|>=|<=|LIKE";
            String regex = "^([a-zA-Z0-9_]+)\\s*(" + comparatorRegex + ")\\s*(\".*\"|'.*'|\\d+(\\.\\d+)?|TRUE|FALSE|NULL)$";
            if (!condition.matches(regex)) {
                return false; // Invalid format
            }

            // Split the condition (e.g., name == "anna")
            String[] parts = condition.split("\\s*(==|!=|>|<|>=|<=|LIKE)\\s*");
            if (parts.length != 2) return false;

            String columnName = parts[0].trim();
            if (!columns.contains(columnName)) {
                return false; // Column in condition does not exist
            }

            String value = parts[1].trim();
            if (!isProperlyQuoted(value) && !isLiteralValue(value)) {
                return false; // Value must be quoted or valid literal
            }
        }
        return true;
    }

    private boolean isProperlyQuoted(String value) {
        return (value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"));
    }

    private boolean isLiteralValue(String value) {
        String literalRegex = "^-?\\d+(\\.\\d+)?$|^(TRUE|FALSE|NULL)$";
        return value.matches(literalRegex);
    }

    private String formatRows(List<String> columns, List<List<String>> rows) {
        StringBuilder builder = new StringBuilder();

        // Add the column headers
        builder.append(String.join("\t", columns));
        builder.append("\n");

        // Add the data rows
        for (List<String> row : rows) {
            builder.append(String.join("\t", row));
            builder.append("\n");
        }

        return builder.toString().trim(); // Remove the trailing newline
    }
}
