package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateCommand extends DBCommand {
    private List<String> conditions = new ArrayList<>();

    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    @Override
    public String query(DBServer server) throws IOException {
        if (tableNames.isEmpty() || columnNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] Invalid UPDATE command format.";
        }

        String tableName = tableNames.get(0).toLowerCase();
        Table table = tables.get(tableName);

        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        if (columnNames.size() != values.size()) {
            return "[ERROR] The number of columns do not match the number of values.";
        }

        try {
            // Process values - remove quotes
            List<String> processedValues = new ArrayList<>();
            for (String value : values) {
                processedValues.add(removeQuotes(value));
            }

            int updatedRows = 0;
            List<List<String>> rows = table.getRows();
            List<String> columns = table.getColumns();

            // Debug to verify conditions aren't empty
            System.out.println("[DEBUG] Conditions in UpdateCommand: " + conditions);

            if (!conditions.isEmpty()) {
                // Create condition parser with tokenized conditions
                List<String> tokens = tokenizeConditions(conditions);
                System.out.println("[DEBUG] Tokenized conditions: " + tokens);

                ConditionParser parser = new ConditionParser(tokens);
                ConditionNode conditionTree = parser.parse();

                // Update rows that match the condition
                for (List<String> row : rows) {
                    boolean matches = conditionTree.evaluate(row, columns);
                    System.out.println("[DEBUG] Row: " + row + " matches condition: " + matches);

                    if (matches) {
                        updateRow(row, columns, columnNames, processedValues);
                        updatedRows++;
                    }
                }
            } else {
                // Update all rows if no condition specified
                for (List<String> row : rows) {
                    updateRow(row, columns, columnNames, processedValues);
                    updatedRows++;
                }
            }

            if (updatedRows > 0) {
                saveCurrentDB();
                return "[OK] " + updatedRows + " row(s) updated.";
            } else {
                return "[ERROR] No rows matched the update condition.";
            }
        } catch (Exception e) {
            e.printStackTrace(); // Add this to get full stack trace
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
            // Split condition while preserving operators
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
