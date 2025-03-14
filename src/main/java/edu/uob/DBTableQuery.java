// TableQuery class for selection/filtering operations
package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class DBTableQuery {
    private final DBTable table;
    private String errorMessage;

    public DBTableQuery(DBTable table) { this.table = table; }

    public String getErrorMessage() { return errorMessage; }

    public List<List<String>> selectRowsWithConditions(List<String> selectedColumns, List<String> conditions) {
        List<String> tokens = tokeniseConditions(conditions);
        ConditionParser parser = new ConditionParser(tokens);
        ConditionNode conditionTree = parser.parseConditions();

        if (parser.getErrorMessage() != null){
            this.errorMessage = parser.getErrorMessage();
            return new ArrayList<>();
        }

        if (conditionTree == null) {
            this.errorMessage = "[ERROR] Your conditions were not formatted correctly.";
            return new ArrayList<>();
        }

        List<Integer> columnIndexes = getColumnIndexes(selectedColumns);
        return filterRows(columnIndexes, conditionTree);
    }

    public List<List<String>> joinWith(DBTable secondTable, String firstTableColumn, String secondTableColumn) {
        int firstColumnIndex = table.getColumnIndex(firstTableColumn);
        int secondColumnIndex = secondTable.getColumnIndex(secondTableColumn);
        if (firstColumnIndex < 0 || secondColumnIndex < 0) return new ArrayList<>();
        return createJoinedTable(secondTable, firstColumnIndex, secondColumnIndex, secondTableColumn);
    }

    private List<Integer> getColumnIndexes(List<String> selectedColumns) {
        List<Integer> indexes = new ArrayList<>();
        for (String column : selectedColumns) indexes.add(table.getColumnIndex(column));
        return indexes;
    }

    private List<List<String>> filterRows(List<Integer> columnIndexes, ConditionNode conditionTree) {
        List<List<String>> filteredRows = new ArrayList<>();
        List<String> columns = table.getColumns();
        for (List<String> row : table.getRows()) {
            if (conditionTree == null || conditionTree.evaluateCondition(row, columns)) {
                List<String> filteredRow = new ArrayList<>();
                for (int index : columnIndexes) filteredRow.add(row.get(index));
                filteredRows.add(filteredRow);
            }
        }
        return filteredRows; //Could potentially rename this variable. And a few others in this file. rowsthatapply or something
    }

    private List<List<String>> createJoinedTable(DBTable secondTable, int firstColumnIndex, int secondColumnIndex, String secondTableColumn) {
        List<List<String>> joinedRows = new ArrayList<>();
        List<String> secondTableColumns = secondTable.getColumns();

        for (List<String> firstTableRows : table.getRows()) {
            String joinValue = firstTableRows.get(firstColumnIndex);
            for (List<String> secondTableRows : secondTable.getRows()) {
                if (joinValue.equals(secondTableRows.get(secondColumnIndex))) {
                    List<String> joinedRow = new ArrayList<>(firstTableRows);
                    for (int i = 0; i < secondTableRows.size(); i++) {
                        if (!secondTableColumns.get(i).equals(secondTableColumn)) joinedRow.add(secondTableRows.get(i));
                    }
                    joinedRows.add(joinedRow);
                }
            }
        }
        return joinedRows;
    }

    private List<String> tokeniseConditions(List<String> conditions) {
        List<String> tokens = new ArrayList<>();
        for (String condition : conditions) {
            for (String part : condition.split("\\s+")) {
                if (!part.isEmpty()) {
                    tokens.add(part);
                }
            }
        }
        return tokens;
    }
}
