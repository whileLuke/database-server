package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectCommand extends DBCommand {
    private List<String> conditions = new ArrayList<>();

    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    @Override
    public String query(DBServer server) throws IOException {
        //loadTables(currentDB);
        System.out.println("[DEBUG] Query called with conditions: " + conditions);
        if (currentDB == null) return "[ERROR] No database selected. Use 'USE database;' to select a database first.";
        System.out.println("Tokens: " + tokens);
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

        List<List<String>> filteredRows;
        TableQuery tableQuery = new TableQuery(table);
        try {
            if (conditions.isEmpty()) {
                // For selection without conditions, we need to manually extract the data
                List<Integer> columnIndexes = new ArrayList<>();
                for (String col : selectedColumns) {
                    columnIndexes.add(table.getColumnIndex(col));
                }

                // Extract the data for the selected columns from all rows
                filteredRows = new ArrayList<>();
                extractSelectedData(table, filteredRows, columnIndexes);
            } else {
                System.out.println("[DEBUG] Conditions passed to selectRowsWithConditions:" + conditions);
                filteredRows = tableQuery.selectRowsWithConditions(selectedColumns, conditions);
            }
        } catch (Exception e) {
            filteredRows = new ArrayList<>();
        }

        if (filteredRows.isEmpty()) {
            return "[OK] No matching rows";
        }
        return "[OK]\n" + formatRows(selectedColumns, filteredRows);


    }

    static void extractSelectedData(Table table, List<List<String>> filteredRows, List<Integer> columnIndexes) {
        for (List<String> row : table.getRows()) {
            List<String> filteredRow = new ArrayList<>();
            for (int index : columnIndexes) {
                filteredRow.add(row.get(index));
            }
            filteredRows.add(filteredRow);
        }
    }

    private boolean isValidCondition(List<String> conditions, List<String> columns) {
        for (String condition : conditions) {
            String[] parts = splitCondition(condition); // Split condition manually
            if (parts == null || parts.length != 3) {
                return false; // Condition doesn't follow the format "column operator value"
            }

            String columnName = parts[0].trim();
            String operator = parts[1].trim();
            String value = parts[2].trim();

            // Check if the column exists in the table
            if (!columns.contains(columnName)) {
                return false;
            }

            // Check for valid operator
            if (!isValidOperator(operator)) {
                return false;
            }

            // Check if the value is properly quoted or is a valid literal
            if (!isProperlyQuoted(value) && !isLiteralValue(value)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidOperator(String operator) {
        return operator.equals("==") || operator.equals("!=") || operator.equals(">") ||
                operator.equals(">=") || operator.equals("<") || operator.equals("<=") ||
                operator.equals("LIKE");
    }

    private String[] splitCondition(String condition) {
        String[] operators = {"==", "!=", ">=", "<=", ">", "<", "LIKE"};

        for (String operator : operators) {
            int index = condition.indexOf(operator);
            if (index != -1) {
                String column = condition.substring(0, index);
                String value = condition.substring(index + operator.length());
                return new String[]{column, operator, value};
            }
        }
        return null; // No valid operator found
    }


    private boolean isProperlyQuoted(String value) {
        // Check for single or double quotes
        return (value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"));
    }

    private boolean isLiteralValue(String value) {
        if ("NULL".equals(value) || "TRUE".equals(value) || "FALSE".equals(value)) return true;
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String formatRows(List<String> columns, List<List<String>> rows) {
        if (rows.isEmpty()) return "[OK] No rows matched.";
        StringBuilder builder = new StringBuilder();

        // Add the column headers
        builder.append(String.join("\t", columns));
        builder.append("\n");

        // Add the data rows
        for (List<String> row : rows) {
            builder.append(String.join("\t", row));
            builder.append("\n");
        }
        System.out.println("[DEBUG] Formatted rows: \n" + builder.toString());
        return builder.toString().trim(); // Remove the trailing newline
    }
}
