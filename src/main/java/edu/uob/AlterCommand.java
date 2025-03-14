package edu.uob;

import java.io.IOException;

public class AlterCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkAlterCommand(tableNames, columnNames, commandType);
        if (errorMessage != null) return errorMessage;

        String tableName = tableNames.get(0).toLowerCase();
        String columnName = columnNames.get(0);
        DBTable table = getTable(tableName);

        errorMessage = executeAlterCommand(table, columnName);
        if (errorMessage != null) return errorMessage;

        if (saveCurrentDB()) return "[OK] You have successfully altered the table " + tableName + ".";
        else return "[ERROR] The altered table could not be saved to your file system";
    }

    private String executeAlterCommand(DBTable table, String columnName) {
        if (commandType.equalsIgnoreCase("ADD")) {
            if (!table.addColumn(columnName)) return "[ERROR] The column '" + columnName + "' already exists.";
        }
        else if (commandType.equalsIgnoreCase("DROP")) {
            if (!table.dropColumn(columnName)) return "[ERROR] The column - '" + columnName + "' does not exist.";
        }
        return null;
    }
}
