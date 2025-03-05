package edu.uob;

public class AlterCommand extends DBCommand {
    @Override
    public String query(DBServer server) {
        if (tableNames.isEmpty() || columnNames.isEmpty() || commandType == null) {
            return "[ERROR] Invalid ALTER TABLE command format.";
        }
        String tableName = tableNames.get(0).toLowerCase() /*+ ".tab"*/; // Only one table name is allowed
        String columnName = columnNames.get(0); // Only one column name is allowed
        Table table = server.tables.get(tableName);
        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }
        boolean success;
        if ("ADD".equals(commandType)) {
            success = table.addColumn(columnName);
            if (!success) {
                return "[ERROR] Column '" + columnName + "' already exists in table '" + tableName + "'.";
            }
        } else if ("DROP".equals(commandType)) {
            success = table.dropColumn(columnName);
            if (!success) {
                return "[ERROR] Column '" + columnName + "' does not exist in table '" + tableName + "'.";
            }
        } else {
            return "[ERROR] Unknown ALTER operation: " + commandType;
        }
        if (server.saveCurrentDB()) {
            return "[OK] Table '" + tableName + "' altered.";
        } else {
            return "[ERROR] Failed to save altered table to disk.";
        }


        /*if (commandType.equals("ADD")) {
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
        return "[ERROR] Unknown ALTER operation.";*/
    }
}
