package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoinCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        if (currentDB == null) {
            return "[ERROR] No database selected. Use 'USE database;' to select a database first.";
        }

        if (tableNames.size() != 2) {
            return "[ERROR] JOIN needs two table names.";
        }

        String table1Name = tableNames.get(0).toLowerCase();
        String table2Name = tableNames.get(1).toLowerCase();

        Table table1 = tables.get(table1Name);
        Table table2 = tables.get(table2Name);

        if (table1 == null || table2 == null) {
            return "[ERROR] One or both tables do not exist.";
        }

        if (columnNames.size() != 2) {
            return "[ERROR] JOIN requires exactly two column names.";
        }

        String column1 = columnNames.get(0);
        String column2 = columnNames.get(1);

        if (!table1.getColumns().contains(column1) || !table2.getColumns().contains(column2)) {
            return "[ERROR] Specified columns for JOIN not found in tables.";
        }

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

        return formatResult(combinedColumnNames, joinResult);
    }

    private String formatResult(List<String> columnNames, List<List<String>> rows) {
        StringBuilder result = new StringBuilder();

        result.append("[OK]\n");
        result.append(String.join("\t", columnNames)).append("\n");

        if (rows.isEmpty()) {
            result.append("[DEBUG] No matching rows to display.\n");
        }

        for (List<String> row : rows) {
            result.append(String.join("\t", row)).append("\n");
        }

        return result.toString();
    }
}
