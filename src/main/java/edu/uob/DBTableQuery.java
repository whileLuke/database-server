package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class DBTableQuery {
    private final DBTable table;
    private final InputTokeniser inputTokeniser;
    private String errorMessage;

    public DBTableQuery(DBTable table) {
        this.table = table;
        this.inputTokeniser = new InputTokeniser();
    }

    public String getErrorMessage() { return errorMessage; }

    public List<List<String>> selectRowsWithConditions(List<String> selectedColumns, List<String> conditions) {
        List<String> tokens = inputTokeniser.tokeniseConditions(conditions);
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
        return findMatchingRows(columnIndexes, conditionTree);
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

    private List<List<String>> findMatchingRows(List<Integer> columnIndexes, ConditionNode conditionTree) {
        List<List<String>> matchingRows = new ArrayList<>();
        List<String> columns = table.getColumns();

        for (List<String> row : table.getRows()) {
            if (conditionTree == null || conditionTree.evaluateCondition(row, columns)) {
                List<String> matchingRow = new ArrayList<>();
                for (int index : columnIndexes) matchingRow.add(row.get(index));
                matchingRows.add(matchingRow);
            }
        }
        return matchingRows;
    }

    private List<List<String>> createJoinedTable(DBTable secondTable, int firstColumnIndex,
                                                 int secondColumnIndex, String secondTableColumn) {
        List<List<String>> joinedRows = new ArrayList<>();
        List<String> secondTableColumns = secondTable.getColumns();

        for (List<String> firstTableRows : table.getRows()) {
            String joinValue = firstTableRows.get(firstColumnIndex);

            for (List<String> secondTableRows : secondTable.getRows()) {

                if (joinValue.equals(secondTableRows.get(secondColumnIndex))) {
                    List<String> joinedRow = new ArrayList<>(firstTableRows);

                    for (int i = 0; i < secondTableRows.size(); i++) {
                        if (!secondTableColumns.get(i).equals(secondTableColumn)) {
                            joinedRow.add(secondTableRows.get(i));
                        }
                    }
                    joinedRows.add(joinedRow);
                }
            }
        }
        return joinedRows;
    }
}
