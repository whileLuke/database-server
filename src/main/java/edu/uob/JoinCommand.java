package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JoinCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkJoinCommand(tableNames, columnNames);
        if (errorMessage != null) return errorMessage;

        String table1Name = tableNames.get(0).toLowerCase();
        String table2Name = tableNames.get(1).toLowerCase();
        DBTable table1 = getTable(table1Name);
        DBTable table2 = getTable(table2Name);
        String column1 = columnNames.get(0).toLowerCase();
        String column2 = columnNames.get(1).toLowerCase();

        DBTableQuery tableQuery = new DBTableQuery(table1);
        List<List<String>> joinResult = tableQuery.joinWith(table2, column1, column2);
        List<String> combinedColumnNames = getCombinedColumnNames(table1, table2, table1Name, table2Name, column2);

        String formattedResult = formatResult(combinedColumnNames, joinResult);
        return "[OK] Tables joined successfully.\n" + formattedResult;
    }

    private List<String> getCombinedColumnNames(DBTable table1, DBTable table2, String table1Name,
                                                  String table2Name, String column2) {
        List<String> combinedColumnNames = new ArrayList<>();
        for (String column : table1.getColumns()) combinedColumnNames.add(table1Name + "." + column);
        for (String column : table2.getColumns()) {
            if (!column.equals(column2)) combinedColumnNames.add(table2Name + "." + column);
        }
        return combinedColumnNames;
    }

    private String formatResult(List<String> columnNames, List<List<String>> rows) {
        StringBuilder result = new StringBuilder();
        result.append(String.join("\t", columnNames)).append("\n");

        for (List<String> row : rows) result.append(String.join("\t", row)).append("\n");
        return result.toString();
    }
}
