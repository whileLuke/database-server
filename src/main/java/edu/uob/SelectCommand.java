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

        List<String> selectedColumns = getSelectedColumns(table);
        if (selectedColumns == null) return "[ERROR] At least one of those columns does not exist in the table.";

        List<String> originalCaseColumns = getOriginalCaseColumns(table, selectedColumns);
        List<List<String>> matchingRows = getMatchingRows(table, selectedColumns, originalCaseColumns);
        if (matchingRows == null) return "[ERROR] Your conditions were not formatted correctly.";

        String matchedRows = formatRows(originalCaseColumns, matchingRows);
        return "[OK]\n" + matchedRows;
    }

    private List<String> getSelectedColumns(DBTable table) {
        List<String> selectedColumns;

        if (columnNames.contains("*")) selectedColumns = table.getColumns();
        else {
            selectedColumns = new ArrayList<>(columnNames);
            for (String column : selectedColumns) {
                String errorMessage = errorChecker.checkIfColumnExists(table, column);
                if (errorMessage != null) return null;
            }
        }
        return selectedColumns;
    }

    private List<String> getOriginalCaseColumns(DBTable table, List<String> selectedColumns) {
        List<String> originalCaseColumns = new ArrayList<>();
        for (String column : selectedColumns) {
            originalCaseColumns.add(table.getStoredColumnName(column));
        }
        return originalCaseColumns;
    }

    private List<List<String>> getMatchingRows(DBTable table, List<String> selectedColumns,
                                               List<String> originalCaseColumns) {
        DBTableQuery tableQuery = new DBTableQuery(table);

        if (conditions.isEmpty()) return getAllRows(table, selectedColumns);
        else {
            try {
                List<List<String>> rows = tableQuery.selectRowsWithConditions(
                        originalCaseColumns, conditions);

                if (tableQuery.getErrorMessage() != null) return null;
                return rows;
            } catch (Exception ignored) { return null; }
        }
    }

    private List<List<String>> getAllRows(DBTable table, List<String> selectedColumns) {
        List<Integer> columnIndexes = new ArrayList<>();
        for (String column : selectedColumns) columnIndexes.add(table.getColumnIndex(column));

        List<List<String>> matchingRows = new ArrayList<>();
        getSelectedData(table, matchingRows, columnIndexes);
        return matchingRows;
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
