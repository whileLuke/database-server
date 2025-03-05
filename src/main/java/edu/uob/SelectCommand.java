package edu.uob;

public class SelectCommand extends DBCommand {
    @Override
    public String query(DBServer server) {
        if (tableNames.isEmpty()) return "[ERROR] Table name missing in SELECT query.";
        String tableName = tableNames.get(0).toLowerCase() /*+ ".tab"*/;
        Table table = server.tables.get(tableName);
        if (table == null) return "[ERROR] Table '" + tableName + "' does not exist.";
        if (columnNames.contains("*") || columnNames.isEmpty()) {
            return table.selectAllColumns();
        } else {
            return table.selectColumns(columnNames);
        }
    }
}
