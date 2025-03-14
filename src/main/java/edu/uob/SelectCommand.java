package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkTableFunctionality(tableNames);
        if (errorMessage != null) return errorMessage;

        String tableName = tableNames.get(0).toLowerCase();
        DBTable table = getTable(tableName);
        List<String> selectedColumns;

        if (columnNames.contains("*")) selectedColumns = table.getColumns();
        else {
            selectedColumns = new ArrayList<>(columnNames);
            for (String column : selectedColumns) {
                errorMessage = errorChecker.checkIfColumnExists(table, column);
                if (errorMessage != null) return errorMessage;
            }
        }
        List<String> originalCaseColumns = new ArrayList<>();

        for (String col : selectedColumns) originalCaseColumns.add(table.getStoredColumnName(col));
        List<List<String>> matchingRows;
        DBTableQuery tableQuery = new DBTableQuery(table);

        if (conditions.isEmpty()) {
            List<Integer> columnIndexes = new ArrayList<>();
            for (String column : selectedColumns) columnIndexes.add(table.getColumnIndex(column));
            matchingRows = new ArrayList<>();
            getSelectedData(table, matchingRows, columnIndexes);
        } else {
            try {
                matchingRows = tableQuery.selectRowsWithConditions(originalCaseColumns, conditions);
                if (tableQuery.getErrorMessage() != null) return tableQuery.getErrorMessage();
            } catch (Exception e) { return "[ERROR] Your conditions were not formatted correctly."; }
        }
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
