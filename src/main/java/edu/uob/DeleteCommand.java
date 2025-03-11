package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class DeleteCommand extends DBCommand {
    private List<String> conditions = new ArrayList<>();

    public void setConditions(List<String> conditions) { this.conditions = conditions; }

    @Override
    public String query(DBServer server) throws IOException {
        if (tableNames.isEmpty()) {
            return "[ERROR] No table specified for deletion.";
        }

        String tableName = tableNames.get(0).toLowerCase();
        Table table = tables.get(tableName);

        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        // Debug the conditions list to see what's happening
        System.out.println("[DEBUG] Delete command conditions: " + conditions);

        // Fix: Check if conditions list is actually empty, not just appears empty
        if (conditions.isEmpty() || (conditions.size() == 1 && conditions.get(0).trim().isEmpty())) {
            return "[ERROR] DELETE command requires a WHERE condition.";
        }

        try {
            List<List<String>> rows = table.getRows();
            List<String> columns = table.getColumns();

            // Create condition parser with tokenized conditions
            List<String> tokens = tokenizeConditions(conditions);
            System.out.println("[DEBUG] Tokenized conditions: " + tokens);

            ConditionParser parser = new ConditionParser(tokens);
            ConditionNode conditionTree = parser.parse();

            int initialRowCount = rows.size();
            Iterator<List<String>> iterator = rows.iterator();

            // Delete rows that match the condition
            while (iterator.hasNext()) {
                List<String> row = iterator.next();
                boolean matches = conditionTree.evaluate(row, columns);
                System.out.println("[DEBUG] Row: " + row + " matches condition: " + matches);

                if (matches) {
                    iterator.remove();
                }
            }

            int deletedRows = initialRowCount - rows.size();

            if (deletedRows > 0) {
                saveCurrentDB();
                return "[OK] " + deletedRows + " row(s) deleted.";
            } else {
                return "[ERROR] No rows matched the delete condition.";
            }
        } catch (Exception e) {
            e.printStackTrace(); // Add stack trace for debugging
            return "[ERROR] Failed to process delete operation: " + e.getMessage();
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
}
