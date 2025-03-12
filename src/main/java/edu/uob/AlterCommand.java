package edu.uob;

import java.io.IOException;

public class AlterCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        if (currentDB == null) return "[ERROR] No database selected. Type 'USE [DBName];' to select a database.";
        if (tableNames.isEmpty() || columnNames.isEmpty() || commandType == null){
            return "[ERROR] Incorrect ALTER command - either no table name, column names or alteration type provided.";
        }
        String tableName = tableNames.get(0).toLowerCase();
        if (tables.get(tableName) == null) return "ERROR: Table '" + tableName + "' does not exist.";
        String columnName = columnNames.get(0);
        if ( columnName.equals("id") ) return "[ERROR] You cannot alter the ID column.";
        if (NotAllowedWords.isNotAllowed(columnName)) return "[ERROR] '" + columnName + "' is not allowed as a column name";
        DBTable table = getTable(tableName);
        if (commandType.equalsIgnoreCase("ADD")) {
            if (!table.addColumn(columnName)) return "[ERROR] The column '" + columnName + "' already exists.";
        }
        else if (commandType.equalsIgnoreCase("DROP")) {
            if (!table.dropColumn(columnName)) return "[ERROR] The column - '" + columnName + "' does not exist.";
        }
        else return ("[ERROR] Your ALTER command must specify either 'ADD' or 'DROP'");
        if (saveCurrentDB()) return "[OK] You have successfully altered the table" + tableName + ".";
        else return "[ERROR] The altered table could not be saved to your file system";
    }
}
