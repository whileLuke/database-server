package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InsertCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        // Validate database is selected
        DBResponse validationResponse = validateDatabaseSelected();
        if (validationResponse != null) return validationResponse;

        // Validate table name and values
        validationResponse = validateTableNameProvided();
        if (validationResponse != null) return validationResponse;

        validationResponse = CommandValidator.validateValuesNotEmpty(values);
        if (validationResponse != null) return validationResponse;

        String tableName = tableNames.get(0).toLowerCase();

        // Validate table exists
        validationResponse = validateTableExists(tableName);
        if (validationResponse != null) return validationResponse;

        Table table = getTable(tableName);

        // Process values - remove quotes
        List<String> processedValues = processValues(values);

        // Add ID to the beginning of the row
        int id = table.generateNextID();
        processedValues.add(0, String.valueOf(id));

        if (table.addRow(processedValues)) {
            if (saveCurrentDB()) {
                return DBResponse.success("1 row inserted into '" + tableName + "'.");
            } else {
                return DBResponse.error("Failed to save database after insertion.");
            }
        } else {
            return DBResponse.error("Failed to insert into '" + tableName + "'. Column count mismatch: expected " +
                    (table.getColumns().size() - 1) + " columns, got " + (processedValues.size() - 1) + " values.");
        }
    }
}
