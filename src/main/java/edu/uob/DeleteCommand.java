package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeleteCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.validateDatabaseSelected();
        if (errorMessage != null) return errorMessage;
        //error = errorChecker.validateTableNameProvided(tableNames);
        //if (error != null) return error;
        String tableName = tableNames.get(0).toLowerCase();
        errorMessage = errorChecker.validateTableExists(tableName);
        if (errorMessage != null) return errorMessage;
        DBTable table = getTable(tableName);
        if (conditions.isEmpty()) return "[ERROR] DELETE commands require a WHERE condition.";
        List<List<String>> rows = table.getRows();
        List<String> columns = table.getColumns();
        List<String> tokens = tokenizeConditions(conditions);
        ConditionParser parser = new ConditionParser(tokens);
        ConditionNode conditionTree = parser.parse();
        int initialRowCount = rows.size();
        Iterator<List<String>> iterator = rows.iterator();
        while (iterator.hasNext()) {
            List<String> row = iterator.next();
            boolean matches = conditionTree.evaluate(row, columns);
            if (matches) iterator.remove();
        }
        int deletedRows = initialRowCount - rows.size();
        if (deletedRows > 0) {
            if (saveCurrentDB()) return "[OK] " + deletedRows + " row(s) deleted.";
            else return "[ERROR] Could not save the database after deletion.";
        } else return "[OK] No rows deleted from the database."; //Maybe print column names here.
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
