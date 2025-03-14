package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeleteCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkDeleteCommand(tableNames, conditions);
        if (errorMessage != null) return errorMessage;

        String tableName = tableNames.get(0).toLowerCase();
        DBTable table = getTable(tableName);

        List<List<String>> rows = table.getRows();
        List<String> columns = table.getColumnsLowerCase();
        List<String> tokens = tokeniseConditions(conditions);
        ConditionParser parser = new ConditionParser(tokens);
        ConditionNode conditionTree = parser.parseConditions();
        if (parser.getErrorMessage() != null) return parser.getErrorMessage();

        int initialRowCount = rows.size();
        Iterator<List<String>> iterator = rows.iterator();

        while (iterator.hasNext()) {
            List<String> row = iterator.next();
            boolean matches = conditionTree.evaluateCondition(row, columns);
            if (matches) iterator.remove();
        }
        int deletedRows = initialRowCount - rows.size();
        if (deletedRows > 0) {
            if (saveCurrentDB()) return "[OK] " + deletedRows + " row(s) deleted.";
            else return "[ERROR] Could not save the database after deletion.";
        } else return "[OK] No rows deleted from the database."; //Maybe print column names here.
    }

    private List<String> tokeniseConditions(List<String> conditions) {
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
