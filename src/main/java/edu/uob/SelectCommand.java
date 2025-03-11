package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectCommand extends DBCommand {
    private List<String> conditions = new ArrayList<>();

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    @Override
    public String query(DBServer server) throws IOException {
        if (currentDB == null) {
            return "[ERROR] No database selected. Use 'USE database;' to select a database first.";
        }

        if (tableNames.isEmpty()) {
            return "[ERROR] Table name missing in SELECT query.";
        }

        String tableName = tableNames.get(0).toLowerCase();
        Table table = tables.get(tableName);

        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        // Determine which columns to select
        List<String> selectedColumns;
        if (columnNames.contains("*")) {
            selectedColumns = table.getColumns();
        } else {
            selectedColumns = new ArrayList<>(columnNames);
            // Validate columns
            for (String column : selectedColumns) {
                if (!table.getColumns().contains(column)) {
                    return "[ERROR] Column '" + column + "' does not exist in table '" + tableName + "'.";
                }
            }
        }

        // Get rows matching the condition
        List<List<String>> filteredRows;
        TableQuery tableQuery = new TableQuery(table);

        try {
            if (conditions.isEmpty()) {
                // No WHERE clause, select all rows but only the specified columns
                List<Integer> columnIndexes = new ArrayList<>();
                for (String col : selectedColumns) {
                    columnIndexes.add(table.getColumnIndex(col));
                }

                filteredRows = new ArrayList<>();
                extractSelectedData(table, filteredRows, columnIndexes);
            } else {
                filteredRows = tableQuery.selectRowsWithConditions(selectedColumns, conditions);
            }
        } catch (Exception e) {
            return "[ERROR] Failed to process SELECT query: " + e.getMessage();
        }

        if (filteredRows.isEmpty()) {
            return "[OK] No matching rows.";
        }

        return "[OK]\n" + formatRows(selectedColumns, filteredRows);
    }

    private void extractSelectedData(Table table, List<List<String>> filteredRows, List<Integer> columnIndexes) {
        for (List<String> row : table.getRows()) {
            List<String> filteredRow = new ArrayList<>();
            for (int index : columnIndexes) {
                filteredRow.add(row.get(index));
            }
            filteredRows.add(filteredRow);
        }
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

        return builder.toString().trim();
    }
}
