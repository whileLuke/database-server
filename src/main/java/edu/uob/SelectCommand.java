package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String error = errorChecker.validateDatabaseSelected();
        if (error != null) return error;
        error = errorChecker.validateTableNameProvided(tableNames);
        if (error != null) return error;
        String tableName = tableNames.get(0).toLowerCase();
        error = errorChecker.validateTableExists(tableName);
        if (error != null) return error;
        DBTable table = getTable(tableName);
        List<String> selectedColumns;
        if (columnNames.contains("*")) selectedColumns = table.getColumns();
        else {
            selectedColumns = new ArrayList<>(columnNames);
            for (String column : selectedColumns) {
                error = errorChecker.validateColumnExists(table, column);
                if (error != null) return error;
            }
        }
        List<List<String>> filteredRows;
        TableQuery tableQuery = new TableQuery(table);
        if (conditions.isEmpty()) {
            List<Integer> columnIndexes = new ArrayList<>();
            for (String col : selectedColumns) columnIndexes.add(table.getColumnIndex(col));
            filteredRows = new ArrayList<>();
            extractSelectedData(table, filteredRows, columnIndexes);
        } else filteredRows = tableQuery.selectRowsWithConditions(selectedColumns, conditions);
        String formattedRows = formatRows(selectedColumns, filteredRows);
        return "[OK]\n" + formattedRows;
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
