package edu.uob;

import java.io.IOException;

public class AlterCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String error = errorChecker.validateDatabaseSelected();
        if (error != null) return error;
        if (tableNames.isEmpty() || columnNames.isEmpty() || commandType == null){
            return "[ERROR] Incorrect ALTER command - either no table name, column names or alteration type provided.";
        }
        String tableName = tableNames.get(0).toLowerCase();
        error = errorChecker.validateTableExists(tableName);
        if (error != null) return error;
        String columnName = columnNames.get(0);
        error = errorChecker.validateNotIdColumn(columnName);
        if (error != null) return error;
        if (errorChecker.isReservedWord(columnName)) return "[ERROR] '" + columnName + "' not allowed as a column name";
        DBTable table = getTable(tableName);
        if (commandType.equalsIgnoreCase("ADD")) {
            if (!table.addColumn(columnName)) return "[ERROR] The column '" + columnName + "' already exists.";
        }
        else if (commandType.equalsIgnoreCase("DROP")) {
            if (!table.dropColumn(columnName)) return "[ERROR] The column - '" + columnName + "' does not exist.";
        }
        else return ("[ERROR] Your ALTER command must specify either 'ADD' or 'DROP'");
        if (saveCurrentDB()) return "[OK] You have successfully altered the table " + tableName + ".";
        else return "[ERROR] The altered table could not be saved to your file system";
    }
}
