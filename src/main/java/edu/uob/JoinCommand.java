package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoinCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String error = errorChecker.validateDatabaseSelected();
        if (error != null) return error;
        if (tableNames.size() != 2) return "[ERROR] The JOIN command needs two table names.";
        String table1Name = tableNames.get(0).toLowerCase();
        String table2Name = tableNames.get(1).toLowerCase();
        error = errorChecker.validateTableExists(table1Name);
        if (error != null) return error;
        error = errorChecker.validateTableExists(table2Name);
        if (error != null) return error;
        DBTable table1 = getTable(table1Name);
        DBTable table2 = getTable(table2Name);
        if (columnNames.size() != 2) return "[ERROR] The JOIN command needs two column names";
        String column1 = columnNames.get(0);
        String column2 = columnNames.get(1);
        error = errorChecker.validateColumnExists(table1, column1);
        if (error != null) return error;
        error = errorChecker.validateColumnExists(table2, column2);
        if (error != null) return error;
        TableQuery tableQuery = new TableQuery(table1);
        List<List<String>> joinResult = tableQuery.joinWith(table2, column1, column2);
        List<String> combinedColumnNames = new ArrayList<>();
        for (String col : table1.getColumns()) combinedColumnNames.add(table1Name + "." + col);
        for (String col : table2.getColumns()) {
            if (!col.equals(column2)) combinedColumnNames.add(table2Name + "." + col);
        }
        String formattedResult = formatResult(combinedColumnNames, joinResult);
        return "[OK] Tables joined successfully.\n" + formattedResult;
    }

    private String formatResult(List<String> columnNames, List<List<String>> rows) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");
        if (rows.isEmpty()) {
            result.append("No matching rows");
        } else {
            for (List<String> row : rows) {
                result.append(String.join("\t", row)).append("\n");
            }
        }
        return result.toString();
    }
}
