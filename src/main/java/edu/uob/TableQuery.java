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
                                        List<String> conditions, DBServer server) throws Exception {
        System.out.println("🔍 [DEBUG] ColumnsToUpdate: " + columnsToUpdate);
        System.out.println("🔍 [DEBUG] NewValues: " + newValues);
        System.out.println("🔍 [DEBUG] Conditions: " + conditions);

        if (columnsToUpdate.size() != newValues.size()) {
            System.out.println("[ERROR] Mismatched column and value count!");
            return 0;
        }

        List<Integer> updateIndexes = new ArrayList<>();
        for (String columnName : columnsToUpdate) {
            int index = table.getColumnIndex(columnName);
            if (index == -1) {
                System.out.println("[ERROR] Column '" + columnName + "' not found!");
                return 0;
            }
            updateIndexes.add(index);
        }

        System.out.println("[DEBUG] Table BEFORE update: " + table.getRows());

        int updateCount = 0;
        for (List<String> row : table.getRows()) {
            boolean matches = evaluator.isRowMatchConditions(row, conditions, table.getColumns());
            System.out.println("[DEBUG] Checking row: " + row + " -> Matches Condition: " + matches);

            if (matches) {
                for (int i = 0; i < updateIndexes.size(); i++) {
                    System.out.println("[DEBUG] Updating row from: " + row);
                    row.set(updateIndexes.get(i), newValues.get(i));
                    System.out.println("[DEBUG] Updated row to: " + row);
                }
                updateCount++;
            }
        }

        System.out.println("[DEBUG] Table AFTER update: " + table.getRows());

        if (updateCount > 0) {
            boolean saved = server.saveCurrentDB();
            if (!saved) {
                System.out.println("[ERROR] Failed to save table!");
                return 0;
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
            System.out.println("[DEBUG] One or both join columns do not exist.");
            return new ArrayList<>();
        }

        List<List<String>> result = new ArrayList<>();
        System.out.println("[DEBUG] thisColumnIndex: " + thisColumnIndex + ", otherColumnIndex: " + otherColumnIndex);

        List<String> newColumns = new ArrayList<>(table.getColumns());
        for (String col : otherTable.getColumns()) {
            if (!col.equals(otherColumn)) {  // Avoid adding duplicate join column
                newColumns.add(otherTable.getName() + "." + col);  // Prefix to avoid conflict
            }
        }

        for (List<String> row1 : table.getRows()) {
            String value1 = row1.get(thisColumnIndex);
            if (value1 != null) value1 = value1.trim().toLowerCase();

            for (List<String> row2 : otherTable.getRows()) {
                String value2 = row2.get(otherColumnIndex);
                if (value2 != null) value2 = value2.trim().toLowerCase();
                System.out.println("[DEBUG] Comparing: '" + value1 + "' with '" + value2 + "'");
                if (value1 != null && value1.equals(value2)) {  // Ensure values match
                    List<String> combinedRow = new ArrayList<>(row1);
                    for (int i = 0; i < row2.size(); i++) {
                        if (i != otherColumnIndex) {  // Skip duplicate join column
                            combinedRow.add(row2.get(i));
                        }
                    }
                    result.add(combinedRow);
                    System.out.println("[DEBUG] Match found! Added row: " + combinedRow);
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