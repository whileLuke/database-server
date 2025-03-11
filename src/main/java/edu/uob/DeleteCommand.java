package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeleteCommand extends DBCommand {
    private List<String> conditions = new ArrayList<>();

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    @Override
    public String query(DBServer server) throws IOException {
        if (currentDB == null) {
            return "[ERROR] No database selected. Use 'USE database;' to select a database first.";
        }

        if (tableNames.isEmpty()) {
            return "[ERROR] No table specified for deletion.";
        }

        String tableName = tableNames.get(0).toLowerCase();
        Table table = tables.get(tableName);

        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        if (conditions.isEmpty()) {
            return "[ERROR] DELETE command requires a WHERE condition.";
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
                    return "[OK] " + deletedRows + " row(s) deleted.";
                } else {
                    return "[ERROR] Failed to save database after deletion.";
                }
            } else {
                return "[ERROR] No rows matched the delete condition.";
            }
        } catch (Exception e) {
            return "[ERROR] Failed to process delete operation: " + e.getMessage();
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
