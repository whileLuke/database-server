package edu.uob;

public class SelectCommand extends DBCommand {
    public String query(DBServer server) {
        if (tableNames.isEmpty()) {
            return "[ERROR] Table name missing in SELECT query.";
        }
        String tableName = tableNames.get(0);
        Table table = server.tables.get(tableName.toLowerCase());
        if (table == null) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }
        if (columnNames.contains("*")) {
            return table.selectAllColumns();
        } else {
            return table.selectColumns(columnNames);
        }
    }
}
