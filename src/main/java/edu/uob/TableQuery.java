// TableQuery class for selection/filtering operations
package edu.uob;

import java.util.*;

public class TableQuery {
    private final Table table;

    public TableQuery(Table table) {
        this.table = table;
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

    public List<List<String>> selectRowsWithConditions(List<String> selectedColumns, List<String> conditions) {
        System.out.println("[DEBUG] selectRowsWithConditions called with columns: " + selectedColumns + ", conditions: " + conditions);

        List<List<String>> allRows = table.getRows();
        List<String> tableColumns = table.getColumns();

        ConditionParser parser = new ConditionParser(tokeniseConditions(conditions));
        ConditionNode conditionTree = parser.parse();

        if (conditionTree == null) {
            System.out.println("[ERROR] Failed to parse conditions");
            return new ArrayList<>();
        }

        List<List<String>> matchingRows = new ArrayList<>();
        for (List<String> row : allRows) {
            if (conditionTree.evaluate(row, tableColumns)) {
                System.out.println("[DEBUG] Found matching row: " + row);

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