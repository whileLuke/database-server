package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoinCommand extends DBCommand {
    @Override
    public DBResponse query() throws IOException {
        // Validate database is selected
        DBResponse validationResponse = validateDatabaseSelected();
        if (validationResponse != null) return validationResponse;

        if (tableNames.size() != 2) {
            return DBResponse.error("JOIN needs two table names.");
        }

        String table1Name = tableNames.get(0).toLowerCase();
        String table2Name = tableNames.get(1).toLowerCase();

        // Validate tables exist
        validationResponse = validateTableExists(table1Name);
        if (validationResponse != null) return validationResponse;

        validationResponse = validateTableExists(table2Name);
        if (validationResponse != null) return validationResponse;

        Table table1 = getTable(table1Name);
        Table table2 = getTable(table2Name);

        if (columnNames.size() != 2) {
            return DBResponse.error("JOIN requires exactly two column names.");
        }

        String column1 = columnNames.get(0);
        String column2 = columnNames.get(1);

        // Validate columns exist in their respective tables
        validationResponse = CommandValidator.validateColumnExists(table1, column1);
        if (validationResponse != null) return validationResponse;

        validationResponse = CommandValidator.validateColumnExists(table2, column2);
        if (validationResponse != null) return validationResponse;

        TableQuery tableQuery = new TableQuery(table1);
        List<List<String>> joinResult = tableQuery.joinWith(table2, column1, column2);

        // Format combined column names with table prefixes
        List<String> combinedColumnNames = new ArrayList<>();

        for (String col : table1.getColumns()) {
            combinedColumnNames.add(table1Name + "." + col);
        }

        for (String col : table2.getColumns()) {
            if (!col.equals(column2)) {
                combinedColumnNames.add(table2Name + "." + col);
            }
        }

        String formattedResult = formatResult(combinedColumnNames, joinResult);
        return DBResponse.success("Tables joined successfully.", formattedResult);
    }

    private String formatResult(List<String> columnNames, List<List<String>> rows) {
        StringBuilder result = new StringBuilder();

        result.append(String.join("\t", columnNames)).append("\n");

        if (rows.isEmpty()) {
            result.append("[No matching rows to display]");
        } else {
            for (List<String> row : rows) {
                result.append(String.join("\t", row)).append("\n");
            }
        }

        return result.toString();
    }
}
