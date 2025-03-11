package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UpdateCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        DBResponse validationResponse;
        if ((validationResponse = validateDatabaseSelected()) != null) return validationResponse;
        if ((validationResponse = validateTableNameProvided()) != null) return validationResponse;
        if ((validationResponse = CommandValidator.validateValuesNotEmpty(values))!= null) return validationResponse;

        String tableName = tableNames.get(0).toLowerCase();
        if ((validationResponse = validateTableExists(tableName)) != null) return validationResponse;

        Table table = getTable(tableName);
        for (String columnName : columnNames) {
            if ((validationResponse = CommandValidator.validateColumnExists(table, columnName)) != null) return validationResponse;
            if ((validationResponse = CommandValidator.validateNotIdColumn(columnName)) != null) return validationResponse;
        }

        List<String> processedValues = processValues(values);
        if (conditions.isEmpty()) return DBResponse.error("UPDATE command needs a WHERE condition.");

        try {
            List<String> tokens = tokenizeConditions(conditions);
            ConditionParser parser = new ConditionParser(tokens);
            ConditionNode conditionTree = parser.parse();

            List<List<String>> rows = table.getRows();
            List<String> tableColumns = table.getColumns();
            int updatedRowCount = 0;

            for (List<String> row : rows) {
                if (conditionTree.evaluate(row, tableColumns)) {
                    for (int i = 0; i < columnNames.size(); i++) {
                        String columnName = columnNames.get(i);
                        String newValue = processedValues.get(i);
                        int columnIndex = table.getColumnIndex(columnName);

                        if (columnIndex >= 0) {
                            row.set(columnIndex, newValue);
                            updatedRowCount++;
                        }
                    }
                }
            }

            if (updatedRowCount > 0) {
                if (saveCurrentDB()) return DBResponse.success(updatedRowCount + " row(s) updated.");
                else return DBResponse.error("Failed to save database after update.");
            } else return DBResponse.error("No rows matched the update condition.");
        } catch (Exception e) {
            return DBResponse.error("Failed to process update operation: " + e.getMessage());
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
