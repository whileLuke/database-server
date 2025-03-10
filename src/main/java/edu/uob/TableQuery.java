// TableQuery class for selection/filtering operations
package edu.uob;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TableQuery {
    private final Table table;
    private final ConditionEvaluator evaluator;

    public TableQuery(Table table) {
        this.table = table;
        this.evaluator = new ConditionEvaluator();
    }

    public String selectAllColumns() {
        return TableFormatter.formatRows(table.getColumns(), table.getRows());
    }

    public String selectAllColumns(List<String> conditions) throws Exception {
        if (conditions == null || conditions.isEmpty()) {
            return selectAllColumns();
        }

        List<List<String>> selectedRows = new ArrayList<>();
        for (List<String> row : table.getRows()) {
            if (evaluator.isRowMatchConditions(row, conditions, table.getColumns())) {
                selectedRows.add(row);
            }
        }

        return TableFormatter.formatRows(table.getColumns(), selectedRows);
    }

    public String selectColumns(List<String> selectedColumns) {
        List<Integer> columnIndexes = getColumnIndexes(selectedColumns);
        if (columnIndexes.isEmpty()) {
            return "[ERROR] One or more columns do not exist.";
        }

        List<String> selectedColumnNames = new ArrayList<>();
        for (int index : columnIndexes) {
            selectedColumnNames.add(table.getColumns().get(index));
        }

        List<List<String>> selectedRows = new ArrayList<>();
        SelectCommand.extractSelectedData(table, selectedRows, columnIndexes);

        return TableFormatter.formatRows(selectedColumnNames, selectedRows);
    }

    public String selectColumns(List<String> selectedColumns, List<String> conditions) throws Exception {
        List<Integer> columnIndexes = getColumnIndexes(selectedColumns);
        if (columnIndexes.isEmpty()) {
            return "[ERROR] Columns '" + String.join(", ", selectedColumns) + "' do not exist.";
        }

        List<String> selectedColumnNames = new ArrayList<>();
        for (int index : columnIndexes) {
            selectedColumnNames.add(table.getColumns().get(index));
        }

        List<List<String>> selectedRows = new ArrayList<>();
        for (List<String> row : table.getRows()) {
            if (evaluator.isRowMatchConditions(row, conditions, table.getColumns())) {
                List<String> selectedRowValues = new ArrayList<>();
                for (int index : columnIndexes) {
                    selectedRowValues.add(row.get(index));
                }
                selectedRows.add(selectedRowValues);
            }
        }

        return "[OK] " + TableFormatter.formatRows(selectedColumnNames, selectedRows);
    }

    public int updateRows(List<String> columnNamesToUpdate, List<String> newValues) {
        if (columnNamesToUpdate.size() != newValues.size()) {
            return 0;
        }

        List<Integer> updateIndexes = new ArrayList<>();
        for (String columnName : columnNamesToUpdate) {
            int index = table.getColumnIndex(columnName);
            if (index == -1) {
                return 0;
            }
            updateIndexes.add(index);
        }

        int updateCount = 0;
        for (List<String> row : table.getRows()) {
            for (int i = 0; i < updateIndexes.size(); i++) {
                row.set(updateIndexes.get(i), newValues.get(i));
            }
            updateCount++;
        }

        return updateCount;
    }

    public int updateRowsWithConditions(List<String> columnsToUpdate, List<String> newValues,
                                        List<String> conditions) throws Exception {
        if (columnsToUpdate.size() != newValues.size()) {
            return 0;
        }

        List<Integer> updateIndexes = new ArrayList<>();
        for (String columnName : columnsToUpdate) {
            int index = table.getColumnIndex(columnName);
            if (index == -1) {
                return 0;
            }
            updateIndexes.add(index);
        }

        int updateCount = 0;
        for (List<String> row : table.getRows()) {
            if (evaluator.isRowMatchConditions(row, conditions, table.getColumns())) {
                for (int i = 0; i < updateIndexes.size(); i++) {
                    row.set(updateIndexes.get(i), newValues.get(i));
                }
                updateCount++;
            }
        }

        return updateCount;
    }

    public int deleteRowsWithConditions(List<String> conditions) throws Exception {
        int deleteCount = 0;
        List<List<String>> rowsToKeep = new ArrayList<>();

        for (List<String> row : table.getRows()) {
            if (!evaluator.isRowMatchConditions(row, conditions, table.getColumns())) {
                rowsToKeep.add(row);
            } else {
                deleteCount++;
            }
        }

        // Clear and rebuild the rows list
        table.getRows().clear();
        table.getRows().addAll(rowsToKeep);

        return deleteCount;
    }

    public List<List<String>> joinWith(Table otherTable, String thisColumn, String otherColumn) {
        int thisColumnIndex = table.getColumnIndex(thisColumn);
        int otherColumnIndex = otherTable.getColumnIndex(otherColumn);

        if (thisColumnIndex == -1 || otherColumnIndex == -1) {
            return new ArrayList<>();
        }

        List<List<String>> result = new ArrayList<>();
        for (List<String> row1 : table.getRows()) {
            for (List<String> row2 : otherTable.getRows()) {
                if (row1.get(thisColumnIndex).equals(row2.get(otherColumnIndex))) {
                    List<String> combinedRow = new ArrayList<>(row1);
                    combinedRow.addAll(row2);
                    result.add(combinedRow);
                }
            }
        }

        return result;
    }

    private List<Integer> getColumnIndexes(List<String> columnNames) {
        List<Integer> columnIndexes = new ArrayList<>();

        for (String columnName : columnNames) {
            int index = table.getColumnIndex(columnName);
            if (index == -1) {
                return new ArrayList<>();  // Return empty list if any column doesn't exist
            }
            columnIndexes.add(index);
        }

        return columnIndexes;
    }

    public List<List<String>> selectRowsWithConditions(List<String> selectedColumns, List<String> conditions) throws Exception {
        List<Integer> columnIndexes = getColumnIndexes(selectedColumns);
        if (columnIndexes.isEmpty()) {
            return new ArrayList<>();  // Return empty list if any column doesn't exist
        }

        List<List<String>> selectedRows = new ArrayList<>();
        for (List<String> row : table.getRows()) {
            if (evaluator.isRowMatchConditions(row, conditions, table.getColumns())) {
                List<String> selectedRowValues = new ArrayList<>();
                for (int index : columnIndexes) {
                    selectedRowValues.add(row.get(index));
                }
                selectedRows.add(selectedRowValues);
            }
        }

        return selectedRows;
    }
}