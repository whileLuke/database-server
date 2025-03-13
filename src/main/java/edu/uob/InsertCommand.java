package edu.uob;

import java.io.IOException;
import java.util.List;

public class InsertCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkIfDatabaseSelected();
        if (errorMessage != null) return errorMessage;
        errorMessage = errorChecker.checkIfTableNameProvided(tableNames);
        if (errorMessage != null) return errorMessage;
        errorMessage = errorChecker.checkIfValuesEmpty(values);
        if (errorMessage != null) return errorMessage;
        String tableName = tableNames.get(0).toLowerCase();
        errorMessage = errorChecker.checkIfTableExists(tableName);
        if (errorMessage != null) return errorMessage;
        DBTable table = getTable(tableName);
        List<String> processedValues = processValues(values);
        int id = table.generateNextID();
        processedValues.add(0, String.valueOf(id));
        if (table.addRow(processedValues)) {
            if (saveCurrentDB()) {
                return "[OK] 1 row inserted into '" + tableName + "'.";
            } else {
                return "[ERROR] Failed to save database after insertion.";
            }
        } else {
            return "[ERROR] Failed to insert into '" + tableName + "'. Column count mismatch: expected " +
                    (table.getColumns().size() - 1) + " columns, got " + (processedValues.size() - 1) + " values.";
        }
    }
}
