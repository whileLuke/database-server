// TableQuery class for selection/filtering operations
package edu.uob;

import java.util.*;

public class TableQuery {
    private final DBTable table;

    public TableQuery(DBTable table) {
        this.table = table;
    }

    public List<List<String>> joinWith(DBTable otherTable, String thisColumn, String otherColumn) {
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

    public List<List<String>> selectRowsWhere(String columnName, String operator, String value) {
        int columnIndex = table.getColumnIndex(columnName);
        if (columnIndex == -1) return new ArrayList<>();

        List<List<String>> result = new ArrayList<>();
        for (List<String> row : table.getRows()) {
            String cellValue = row.get(columnIndex);
            if (evaluateCondition(cellValue, operator, value)) {
                result.add(new ArrayList<>(row));
            }
        }
        return result;
    }

    private boolean evaluateCondition(String left, String operator, String right) {
        return switch (operator) {
            case "=" -> left.equals(right);
            case ">" -> left.compareTo(right) > 0;
            case "<" -> left.compareTo(right) < 0;
            case ">=" -> left.compareTo(right) >= 0;
            case "<=" -> left.compareTo(right) <= 0;
            case "!=" -> !left.equals(right);
            default -> false;
        };
    }
}