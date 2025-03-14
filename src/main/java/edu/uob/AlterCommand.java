package edu.uob;

import java.io.IOException;

public class AlterCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String error = errorChecker.checkAlterCommand(tableNames, columnNames, commandType);
        if (error != null) return error;

        String tableName = tableNames.get(0).toLowerCase();
        String columnName = columnNames.get(0);

        DBTable table = getTable(tableName);
        if (commandType.equalsIgnoreCase("ADD")) {
            if (!table.addColumn(columnName)) return "[ERROR] The column '" + columnName + "' already exists.";
        }
        else if (commandType.equalsIgnoreCase("DROP")) {
            if (!table.dropColumn(columnName)) return "[ERROR] The column - '" + columnName + "' does not exist.";
        }

        if (saveCurrentDB()) return "[OK] You have successfully altered the table " + tableName + ".";
        else return "[ERROR] The altered table could not be saved to your file system";
    }
}
