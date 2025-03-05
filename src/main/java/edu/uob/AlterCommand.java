package edu.uob;

public class AlterCommand extends DBCommand {
    public String query(DBServer server) {
        if (tableNames.isEmpty() || columnNames.isEmpty() || commandType == null) {
            return "[ERROR] Table name, column name, or operation missing.";
        }
        String tableName = tableNames.get(0); // Only one table name is allowed
        String columnName = columnNames.get(0); // Only one column name is allowed
        Table table = server.tables.get(tableName.toLowerCase());
        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }
        if (commandType.equals("ADD")) {
            if (table.addColumn(columnName)) {
                return "[OK] Column '" + columnName + "' added to table '" + tableName + "'.";
            } else {
                return "[ERROR] Could not add column '" + columnName + "' (already exists?).";
            }
        } else if (commandType.equals("DROP")) {
            if (table.dropColumn(columnName)) {
                return "[OK] Column '" + columnName + "' dropped from table '" + tableName + "'.";
            } else {
                return "[ERROR] Could not drop column '" + columnName + "' (does not exist?).";
            }
        }
        return "[ERROR] Unknown ALTER operation.";
    }
}
