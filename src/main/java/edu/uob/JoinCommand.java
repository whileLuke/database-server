package edu.uob;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class JoinCommand extends DBCommand {
    @Override
    public String query(DBServer server) throws IOException {
        //loadTables(currentDB);
        if (tableNames.size() != 2) return "[ERROR] JOIN needs two table names.";

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
        // Columns to join on (assume they are populated in tableColumns)
        String column1 = columnNames.get(0);
        String column2 = columnNames.get(1);

        if (!table1.getColumns().contains(column1) || !table2.getColumns().contains(column2)) {
            return "[ERROR] Specified columns for JOIN not found in tables.";
        }
        TableQuery tableQuery = new TableQuery(table1);
        List<List<String>> joinResult = tableQuery.joinWith(table2, column1, column2);

        // Format and return the result
        List<String> combinedColumnNames = new ArrayList<>(table1.getColumns());
        boolean table1HasId = table1.hasColumn("id");
        boolean table2HasId = table2.hasColumn("id");

        // Add table2 columns, renaming "id" and excluding join columns
        for (String col : table2.getColumns()) {
            if (!col.equals(column2) && !col.equals(column1)) {
                if (table1HasId && table2HasId && col.equals("id")) {
                    combinedColumnNames.add(table2Name + ".id");  // Rename second 'id' column
                } else {
                    combinedColumnNames.add(col);
                }
            }
        }
        //GET JOINRESULT WORKING. PUT THE VALUES THAT AREIN BOTH.
        return formatResult(combinedColumnNames, joinResult);
    }

    private String formatResult(List<String> columnNames, List<List<String>> rows) {
        StringBuilder result = new StringBuilder();
        //Doubles up the OKs
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
