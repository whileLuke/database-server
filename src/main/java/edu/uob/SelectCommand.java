package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        // Validate database is selected
        DBResponse validationResponse = validateDatabaseSelected();
        if (validationResponse != null) return validationResponse;

        // Validate table name is provided
        validationResponse = validateTableNameProvided();
        if (validationResponse != null) return validationResponse;

        String tableName = tableNames.get(0).toLowerCase();

        // Validate table exists
        validationResponse = validateTableExists(tableName);
        if (validationResponse != null) return validationResponse;

        Table table = getTable(tableName);

        // Determine which columns to select
        List<String> selectedColumns;
        if (columnNames.contains("*")) {
            selectedColumns = table.getColumns();
        } else {
            selectedColumns = new ArrayList<>(columnNames);
            // Validate columns
            for (String column : selectedColumns) {
                if (!table.getColumns().contains(column)) {
                    return DBResponse.error("Column '" + column + "' does not exist in table '" + tableName + "'.");
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
            return DBResponse.error("Failed to process SELECT query: " + e.getMessage());
        }

        String formattedRows = formatRows(selectedColumns, filteredRows);
        if (filteredRows.isEmpty()) {
            return DBResponse.success("No matching rows.");
        }

        return DBResponse.success("Query executed successfully.", formattedRows);
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
