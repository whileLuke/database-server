package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateCommand extends DBCommand {
    private List<String> conditions = new ArrayList<>();

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    @Override
    public String query(DBServer server) throws IOException {
        if (currentDB == null) {
            return "[ERROR] No database selected. Use 'USE database;' to select a database first.";
        }

        if (tableNames.isEmpty() || columnNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] Invalid UPDATE command format.";
        }

        String tableName = tableNames.get(0).toLowerCase();
        Table table = tables.get(tableName);

        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        if (columnNames.size() != values.size()) {
            return "[ERROR] The number of columns does not match the number of values.";
        }

        try {
            // Process values - remove quotes
            List<String> processedValues = new ArrayList<>();
            for (String value : values) {
                processedValues.add(removeQuotes(value));
            }

            // Validate columns
            for (String column : columnNames) {
                if (!table.getColumns().contains(column)) {
                    return "[ERROR] Column '" + column + "' does not exist in table '" + tableName + "'.";
                }
                if (column.equals("id")) {
                    return "[ERROR] Cannot update the ID column.";
                }
            }

            int updatedRows = 0;
            List<List<String>> rows = table.getRows();
            List<String> columns = table.getColumns();

            if (conditions.isEmpty()) {
                // Update all rows if no condition specified
                for (List<String> row : rows) {
                    updateRow(row, columns, columnNames, processedValues);
                    updatedRows++;
                }
            } else {
                // Create condition parser with tokenized conditions
                List<String> tokens = tokenizeConditions(conditions);
                ConditionParser parser = new ConditionParser(tokens);
                ConditionNode conditionTree = parser.parse();

                // Update rows that match the condition
                for (List<String> row : rows) {
                    boolean matches = conditionTree.evaluate(row, columns);
                    if (matches) {
                        updateRow(row, columns, columnNames, processedValues);
                        updatedRows++;
                    }
                }
            }

            if (updatedRows > 0) {
                if (saveCurrentDB()) {
                    return "[OK] " + updatedRows + " row(s) updated.";
                } else {
                    return "[ERROR] Failed to save database after update.";
                }
            } else {
                return "[ERROR] No rows matched the update condition.";
            }
        } catch (Exception e) {
            return "[ERROR] Failed to process update: " + e.getMessage();
        }
    }

    private void updateRow(List<String> row, List<String> tableColumns,
                           List<String> updateColumns, List<String> updateValues) {
        for (int i = 0; i < updateColumns.size(); i++) {
            String columnName = updateColumns.get(i);
            int columnIndex = tableColumns.indexOf(columnName);

            if (columnIndex >= 0 && columnIndex < row.size()) {
                row.set(columnIndex, updateValues.get(i));
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

    private String removeQuotes(String value) {
        if (value == null) return "";
        if ((value.startsWith("'") && value.endsWith("'")) ||
                (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
