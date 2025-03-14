package edu.uob;

import java.io.IOException;

public class CreateTableCommand extends DBCommand {
    @Override
    public String query() throws IOException {
        String errorMessage = errorChecker.checkCreateTableCommand(tableNames, columnNames);
        if (errorMessage != null) return errorMessage;

        String tableName = tableNames.get(0).toLowerCase();
        if (tables.containsKey(tableName)) return "[ERROR] Table '" + tableName + "' already exists.";

        DBTable newTable = new DBTable(tableName, columnNames);
        tables.put(tableName, newTable);
        if (saveCurrentDB()) return "[OK] You have successfully created table '" + tableName + "'.";
        else return "[ERROR] Failed to save the table.";
    }
}
