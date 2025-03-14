package edu.uob;

import java.io.IOException;
import java.util.List;

public class UpdateCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkUpdateCommand(tableNames, columnNames, values, conditions);
        if (errorMessage != null) return errorMessage;

        String tableName = tableNames.get(0).toLowerCase();
        DBTable table = getTable(tableName);
        errorMessage = checkColumnNames(table);
        if (errorMessage != null) return errorMessage;

        int updatedRowCount = updateMatchingRows(table);
        return formatResult(updatedRowCount);
    }

    private String checkColumnNames(DBTable table) {
        for (String columnName : columnNames) {
            String errorMessage = errorChecker.checkIfColumnExists(table, columnName);
            if (errorMessage != null) return errorMessage;

            errorMessage = errorChecker.checkIfIDColumn(columnName);
            if (errorMessage != null) return errorMessage;
        }
        return null;
    }

    private int updateMatchingRows(DBTable table) {
        List<List<String>> rows = table.getRows();
        List<String> columns = table.getColumnsLowerCase();
        InputTokeniser tokeniser = new InputTokeniser();
        List<String> tokens = tokeniser.tokeniseConditions(conditions);

        ConditionParser parser = new ConditionParser(tokens);
        ConditionNode conditionTree = parser.parseConditions();

        if (parser.getErrorMessage() != null) return -1;
        return applyRowUpdates(rows, columns, conditionTree);
    }

    private int applyRowUpdates(List<List<String>> rows, List<String> columns, ConditionNode conditionTree) {
        int updatedRowCount = 0;
        for (List<String> row : rows) {
            if (conditionTree.evaluateCondition(row, columns)) {
                updatedRowCount++;
                updateRow(row, columns);
            }
        }
        return updatedRowCount;
    }

    private void updateRow(List<String> row, List<String> columns) {
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            String value = values.get(i);

            int columnIndex = columns.indexOf(columnName);
            if (columnIndex >= 0) row.set(columnIndex, removeQuotes(value));
        }
    }

    private String formatResult(int updatedRowCount) throws IOException {
        if (updatedRowCount < 0) return "[ERROR] There was an error in your conditions.";
        else {
            if(saveCurrentDB()) return "[OK] " + updatedRowCount + " rows were updated.";
            else return "[ERROR] Unable to save the database after the update.";
        }
    }
}
