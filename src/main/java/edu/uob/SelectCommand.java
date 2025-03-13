package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String error = errorChecker.checkIfDatabaseSelected();
        if (error != null) return error;

        error = errorChecker.checkIfTableNameProvided(tableNames);
        if (error != null) return error;

        String tableName = tableNames.get(0).toLowerCase();
        error = errorChecker.checkIfTableExists(tableName);
        if (error != null) return error;

        DBTable table = getTable(tableName);
        List<String> selectedColumns;
        //String matchedRows = formatRows(originalCaseColumns, matchingRows);
        if (columnNames.contains("*")) selectedColumns = table.getColumns();
        else {
            selectedColumns = new ArrayList<>(columnNames);
            for (String column : selectedColumns) {
                error = errorChecker.checkIfColumnExists(table, column);
                if (error != null) return error;
            }
        }
        List<String> originalCaseColumns = new ArrayList<>();
        for (String col : selectedColumns) originalCaseColumns.add(table.getStoredColumnName(col));
        List<List<String>> matchingRows;
        TableQuery tableQuery = new TableQuery(table);

        if (conditions.isEmpty()) {
            List<Integer> columnIndexes = new ArrayList<>();
            for (String column : selectedColumns) columnIndexes.add(table.getColumnIndex(column));
            matchingRows = new ArrayList<>();
            getSelectedData(table, matchingRows, columnIndexes);
        } else matchingRows = tableQuery.selectRowsWithConditions(originalCaseColumns, conditions);
        String matchedRows = formatRows(originalCaseColumns, matchingRows);
        return "[OK]\n" + matchedRows;
    }

    private void getSelectedData(DBTable table, List<List<String>> matchingRows, List<Integer> columnIndexes) {
        for (List<String> row : table.getRows()) {
            List<String> matchingRow = new ArrayList<>();
            for (int index : columnIndexes) matchingRow.add(row.get(index));
            matchingRows.add(matchingRow);
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
