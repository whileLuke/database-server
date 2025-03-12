package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        DBResponse validationResponse = validateDatabaseSelected();
        if (validationResponse != null) return validationResponse;

        validationResponse = validateTableNameProvided();
        if (validationResponse != null) return validationResponse;

        String tableName = tableNames.get(0).toLowerCase();

        validationResponse = validateTableExists(tableName);
        if (validationResponse != null) return validationResponse;

        DBTable table = getTable(tableName);

        List<String> selectedColumns;
        if (columnNames.contains("*")) selectedColumns = table.getColumns();
        else {
            selectedColumns = new ArrayList<>(columnNames);
            for (String column : selectedColumns) {
                if (!table.getColumns().contains(column)) {
                    return DBResponse.error("Column '" + column + "' does not exist in table '" + tableName + "'.");
                }
            }
        }

        List<List<String>> filteredRows;
        TableQuery tableQuery = new TableQuery(table);

        try {
            if (conditions.isEmpty()) {
                List<Integer> columnIndexes = new ArrayList<>();
                for (String col : selectedColumns) columnIndexes.add(table.getColumnIndex(col));

                filteredRows = new ArrayList<>();
                extractSelectedData(table, filteredRows, columnIndexes);
            } else filteredRows = tableQuery.selectRowsWithConditions(selectedColumns, conditions);
        } catch (Exception e) {
            return DBResponse.error("Failed to process SELECT query: " + e.getMessage());
        }

        String formattedRows = formatRows(selectedColumns, filteredRows);
        if (filteredRows.isEmpty()) return DBResponse.success(formattedRows);

        return DBResponse.success("Query executed successfully.", formattedRows);
    }

    private void extractSelectedData(DBTable table, List<List<String>> filteredRows, List<Integer> columnIndexes) {
        for (List<String> row : table.getRows()) {
            List<String> filteredRow = new ArrayList<>();
            for (int index : columnIndexes) filteredRow.add(row.get(index));
            filteredRows.add(filteredRow);
        }
    }

    private String formatRows(List<String> columns, List<List<String>> rows) {
        StringBuilder formattedRows = new StringBuilder();

        formattedRows.append(String.join("\t", columns));
        formattedRows.append("\n");

        for (List<String> row : rows) {
            formattedRows.append(String.join("\t", row));
            formattedRows.append("\n");
        }

        return formattedRows.toString().trim();
    }
}
