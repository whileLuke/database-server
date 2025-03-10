// TableQuery class for selection/filtering operations
package edu.uob;

import java.util.*;

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

        Map<String, List<String>> otherTableMap = new HashMap<>();
        for (List<String> row : otherTable.getRows()) {
            String key = row.get(otherColumnIndex).trim().toLowerCase();
            otherTableMap.put(key, row);
        }
        List<List<String>> result = new ArrayList<>();

        /*List<String> newColumns = new ArrayList<>(table.getColumns());
        for (String col : otherTable.getColumns()) {
            if (!col.equals(otherColumn)) {  // Avoid adding duplicate join column
                newColumns.add(otherTable.getName() + "." + col);  // Prefix to avoid conflict
            }
        }*/

        for (List<String> row1 : table.getRows()) {
            String key = row1.get(thisColumnIndex).trim().toLowerCase();
            if (otherTableMap.containsKey(key)) {
                List<String> row2 = otherTableMap.get(key);
                List<String> combinedRow = new ArrayList<>(row1);
                for (int i = 0; i < row2.size(); i++) {
                    if (i != otherColumnIndex) {
                        combinedRow.add(row2.get(i));
                    }
                }
                result.add(combinedRow);
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

    public List<List<String>> selectRowsWithConditions(List<String> selectedColumns, List<String> conditions) {
        System.out.println("[DEBUG] selectRowsWithConditions called with columns: " + selectedColumns + ", conditions: " + conditions);

        // Get all rows from the table
        List<List<String>> allRows = table.getRows();
        List<String> tableColumns = table.getColumns();

        // Parse and build the condition tree
        ConditionParser parser = new ConditionParser(tokeniseConditions(conditions));
        ConditionNode conditionTree = parser.parse();

        if (conditionTree == null) {
            System.out.println("[ERROR] Failed to parse conditions");
            return new ArrayList<>();
        }

        // Filter rows based on the condition
        List<List<String>> matchingRows = new ArrayList<>();
        for (List<String> row : allRows) {
            if (conditionTree.evaluate(row, tableColumns)) {
                System.out.println("[DEBUG] Found matching row: " + row);

                // Extract only the selected columns
                List<String> selectedValues = new ArrayList<>();
                for (String colName : selectedColumns) {
                    int colIndex = tableColumns.indexOf(colName);
                    if (colIndex != -1) {
                        selectedValues.add(row.get(colIndex));
                    }
                }
                matchingRows.add(selectedValues);
            }
        }

        System.out.println("[DEBUG] Matching rows found: " + matchingRows.size());
        return matchingRows;
    }

    private List<String> tokeniseConditions(List<String> conditions) {
        List<String> tokens = new ArrayList<>();

        for (String condition : conditions) {
            String[] parts = condition.split("\\s+");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    tokens.add(part);
                }
            }
        }

        System.out.println("[DEBUG] Tokenized conditions: " + tokens);
        return tokens;
    }

    /*public List<List<String>> selectRowsWithConditions(List<String> selectedColumns, List<String> conditions) {
        List<List<String>> resultRows = new ArrayList<>();
        List<List<String>> allRows = table.getRows();
        List<String> tableColumns = table.getColumns();

        System.out.println("[DEBUG] selectRowsWithConditions called with columns: " + selectedColumns + ", conditions: " + conditions);

        if (conditions.isEmpty()) {
            System.out.println("[DEBUG] No conditions given, returning all rows.");
            return allRows;
        }

        ConditionParser parser = new ConditionParser(conditions);
        ConditionNode rootCondition = parser.parse();

        if (rootCondition == null) {
            System.out.println("[ERROR] Condition parsing failed! No valid condition tree was built.");
            return new ArrayList<>();
        }

        // 🔍 Iterate through rows and evaluate conditions
        for (List<String> row : allRows) {
            System.out.println("[DEBUG] Checking row: " + row);

            boolean matches = rootCondition.evaluate(row, tableColumns);
            System.out.println("[DEBUG] Condition evaluation result for row " + row + " -> " + matches);

            if (matches) {
                System.out.println("[DEBUG] Row matches conditions! Adding: " + row);
                resultRows.add(row);
            } else {
                System.out.println("[DEBUG] Row does NOT match conditions.");
            }
        }

        System.out.println("[DEBUG] Matching rows found: " + resultRows.size());
        return resultRows;
    }*/
}