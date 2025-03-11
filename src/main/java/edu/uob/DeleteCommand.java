package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeleteCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        // Validate database is selected
        DBResponse validationResponse = validateDatabaseSelected();
        if (validationResponse != null) return validationResponse;

        // Validate table name is provided
        validationResponse = validateTableNameProvided();
        if (validationResponse != null) return validationResponse;

        String tableName = tableNames.get(0).toLowerCase();

        // Validate table exists
        validationResponse = validateTableExists(tableName);
        if (validationResponse != null) return validationResponse;

        Table table = getTable(tableName);

        if (conditions.isEmpty()) {
            return DBResponse.error("DELETE command requires a WHERE condition.");
        }

        try {
            List<List<String>> rows = table.getRows();
            List<String> columns = table.getColumns();

            // Create condition parser with tokenized conditions
            List<String> tokens = tokenizeConditions(conditions);
            ConditionParser parser = new ConditionParser(tokens);
            ConditionNode conditionTree = parser.parse();

            int initialRowCount = rows.size();
            Iterator<List<String>> iterator = rows.iterator();

            // Delete rows that match the condition
            while (iterator.hasNext()) {
                List<String> row = iterator.next();
                boolean matches = conditionTree.evaluate(row, columns);

                if (matches) {
                    iterator.remove();
                }
            }

            int deletedRows = initialRowCount - rows.size();

            if (deletedRows > 0) {
                if (saveCurrentDB()) {
                    return DBResponse.success(deletedRows + " row(s) deleted.");
                } else {
                    return DBResponse.error("Failed to save database after deletion.");
                }
            } else {
                return DBResponse.error("No rows matched the delete condition.");
            }
        } catch (Exception e) {
            return DBResponse.error("Failed to process delete operation: " + e.getMessage());
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
