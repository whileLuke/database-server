package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkUpdateCommand(tableNames, columnNames, values, conditions);
        if (errorMessage != null) return errorMessage;

        String tableName = tableNames.get(0).toLowerCase();
        DBTable table = getTable(tableName);
        errorMessage = validateColumns(table);
        if (errorMessage != null) return errorMessage;

        List<List<String>> rows = table.getRows();
        List<String> columns = table.getColumnsLowerCase();
        List<String> tokens = tokeniseConditions(conditions);
        ConditionParser parser = new ConditionParser(tokens);
        ConditionNode conditionTree = parser.parseConditions();
        if (parser.getErrorMessage() != null) return parser.getErrorMessage();

        int updatedRowCount = 0;
        for (List<String> row : rows) {
            if (conditionTree.evaluateCondition(row, columns)) {
                updatedRowCount++;
                updateRow(row, columns);
            }
        }

        if (updatedRowCount > 0) {
            if (saveCurrentDB()) return "[OK] " + updatedRowCount + " row(s) updated.";
            else return "[ERROR] Failed to save database after update.";
        } else return "[ERROR] No rows matched the update condition.";
    }

    private String validateColumns(DBTable table) {
        for (String columnName : columnNames) {
            String error = errorChecker.checkIfColumnExists(table, columnName);
            if (error != null) return error;

            error = errorChecker.checkIfIDColumn(columnName);
            if (error != null) return error;
        }
        return null;
    }

    private void updateRow(List<String> row, List<String> columns) {
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            String value = values.get(i);
            int columnIndex = columns.indexOf(columnName);
            if (columnIndex >= 0) row.set(columnIndex, removeQuotes(value));
        }
    }

    private List<String> tokeniseConditions(List<String> conditions) {
        List<String> tokens = new ArrayList<>();
        for (String condition : conditions) {
            for (String conditionPart : condition.split("\\s+")) {
                if (!conditionPart.isEmpty()) tokens.add(conditionPart);
            }
        }
        return tokens;
    }
}