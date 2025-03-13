package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String error = errorChecker.validateDatabaseSelected();
        if (error != null) return error;

        error = errorChecker.validateTableNameProvided(tableNames);
        if (error != null) return error;

        String tableName = tableNames.get(0).toLowerCase();

        error = errorChecker.validateTableExists(tableName);
        if (error != null) return error;

        DBTable table = getTable(tableName);

        if (columnNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] UPDATE requires at least one column and value pair.";
        }

        if (columnNames.size() != values.size()) {
            return "[ERROR] Column and value counts do not match.";
        }

        // Validate columns exist and are not ID
        for (String columnName : columnNames) {
            error = errorChecker.validateColumnExists(table, columnName);
            if (error != null) return error;

            error = errorChecker.validateNotIdColumn(columnName);
            if (error != null) return error;
        }

        try {
            List<List<String>> rows = table.getRows();
            List<String> columns = table.getColumns();

            // If no conditions, return error
            if (conditions.isEmpty()) {
                return "[ERROR] UPDATE command requires a WHERE condition.";
            }

            List<String> tokens = tokenizeConditions(conditions);
            ConditionParser parser = new ConditionParser(tokens);
            ConditionNode conditionTree = parser.parse();

            int updatedRowCount = 0;

            for (List<String> row : rows) {
                boolean matches = conditionTree.evaluate(row, columns);

                if (matches) {
                    updatedRowCount++;
                    // Update the matching row
                    updateRow(row, columns, columnNames, values);
                }
            }

            if (updatedRowCount > 0) {
                if (saveCurrentDB()) {
                    return "[OK] " + updatedRowCount + " row(s) updated.";
                } else {
                    return "[ERROR] Failed to save database after update.";
                }
            } else {
                return "[ERROR] No rows matched the update condition.";
            }
        } catch (Exception e) {
            return "[ERROR] Failed to process update operation: " + e.getMessage();
        }
    }

    private void updateRow(List<String> row, List<String> columns, List<String> updateColumns, List<String> updateValues) {
        for (int i = 0; i < updateColumns.size(); i++) {
            String columnName = updateColumns.get(i);
            String value = updateValues.get(i);
            int columnIndex = columns.indexOf(columnName);
            if (columnIndex >= 0) {
                row.set(columnIndex, removeQuotes(value));
            }
        }
    }

    private List<String> tokenizeConditions(List<String> conditions) {
        List<String> tokens = new ArrayList<>();
        for (String condition : conditions) {
            String[] parts = condition.split("\\s+");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    tokens.add(part);
                }
            }
        }
        return tokens;
    }
}
