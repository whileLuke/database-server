package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String error = validateUpdateCommand();
        if (error != null) return error;
        String tableName = tableNames.get(0).toLowerCase();
        DBTable table = getTable(tableName);
        error = validateColumns(table);
        if (error != null) return error;
        int updatedRowCount = updateMatchingRows(table);
        if (updatedRowCount > 0) {
            if (saveCurrentDB()) return "[OK] " + updatedRowCount + " row(s) updated.";
            else return "[ERROR] Failed to save database after update.";
        } else return "[ERROR] No rows matched the update condition.";
    }

    private String validateUpdateCommand() {
        String error = errorChecker.validateDatabaseSelected();
        if (error != null) return error;
        error = errorChecker.validateTableNameProvided(tableNames);
        if (error != null) return error;
        String tableName = tableNames.get(0).toLowerCase();
        error = errorChecker.validateTableExists(tableName);
        if (error != null) return error;
        if (columnNames.isEmpty() || values.isEmpty()) { //Should this be using more of my error checking class?
            return "[ERROR] UPDATE needs at least one column name and at least one value.";
        }
        if (columnNames.size() != values.size()) {
            return "[ERROR] You have input different number of column names (" +
                    columnNames.size() + ") to values (" + values.size() +".";
        }
        if (conditions.isEmpty()) return "[ERROR] UPDATE commands need a WHERE condition.";
        return null;
    }

    private String validateColumns(DBTable table) {
        for (String columnName : columnNames) {
            String error = errorChecker.validateColumnExists(table, columnName);
            if (error != null) return error;
            error = errorChecker.validateNotIDColumn(columnName);
            if (error != null) return error;
        }
        return null;
    }

    private int updateMatchingRows(DBTable table) {
        List<List<String>> rows = table.getRows();
        List<String> columns = table.getColumns();
        List<String> tokens = tokeniseConditions(conditions);
        ConditionParser parser = new ConditionParser(tokens);
        ConditionNode conditionTree = parser.parse();
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

    private List<String> tokeniseConditions(List<String> conditions) {
        List<String> tokens = new ArrayList<>();
        for (String condition : conditions) {
            InputTokeniser tokeniser = new InputTokeniser(); //Perhaps I could do this part with a tokeniser.
            for (String part : condition.split("\\s+")) {
                if (!part.isEmpty()) {
                    tokens.add(part);
                }
            }
        }
        return tokens;
    }
}