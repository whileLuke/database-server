package edu.uob;

import java.io.IOException;
import java.util.List;

public class InsertCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkInsertCommand(tableNames, values);
        if (errorMessage != null) return errorMessage;

        String tableName = tableNames.get(0).toLowerCase();
        DBTable table = getTable(tableName);
        List<String> processedValues = processValues(values);
        int id = table.generateNextID();
        processedValues.add(0, String.valueOf(id));

        if (table.addRow(processedValues)) {
            if (saveCurrentDB()) return "[OK] 1 row inserted into '" + tableName + "'.";
            else return "[ERROR] Failed to save database after insertion.";
        } else return "[ERROR] Failed to insert into '" + tableName + "'.";
    }
}
