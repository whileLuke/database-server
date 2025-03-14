package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DeleteCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        this.inputTokeniser = new InputTokeniser();

        String errorMessage = errorChecker.checkDeleteCommand(tableNames, conditions);
        if (errorMessage != null) return errorMessage;

        String tableName = tableNames.get(0).toLowerCase();
        DBTable table = getTable(tableName);

        ConditionNode conditionTree = parseConditions();
        if (conditionTree == null) return "[ERROR] Your condition was formatted incorrectly.";

        int deletedRowCount = deleteMatchingRows(table, conditionTree);
        return formatResult(deletedRowCount);
    }

    private ConditionNode parseConditions() {
        List<String> tokens = inputTokeniser.tokeniseConditions(conditions);
        ConditionParser parser = new ConditionParser(tokens);
        ConditionNode conditionTree = parser.parseConditions();

        if (parser.getErrorMessage() != null) return null;
        return conditionTree;
    }

    private int deleteMatchingRows(DBTable table, ConditionNode conditionTree) {
        List<List<String>> rows = table.getRows();
        List<String> columns = table.getColumnsLowerCase();
        int initialRowCount = rows.size();
        List<List<String>> rowsToDelete = new ArrayList<>();

        for (List<String> row : rows) {
            boolean matches = conditionTree.evaluateCondition(row, columns);
            if (matches) rowsToDelete.add(row);
        }
        rows.removeAll(rowsToDelete);
        return initialRowCount - rows.size();
    }

    private String formatResult(int deletedRowCount) throws IOException {
        if (saveCurrentDB()) return "[OK] " + deletedRowCount + " rows deleted.";
        else return "[ERROR] Could not save the database after deletion.";
    }
}
