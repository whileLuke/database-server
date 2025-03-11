package edu.uob;

import java.util.List;
import java.util.Map;

public class CommandValidator {

    public static DBResponse validateDatabaseSelected(String currentDB) {
        if (currentDB == null) return DBResponse.error("No database selected. Use 'USE database;' to select a database first.");
        return null;
    }

    public static DBResponse validateTableExists(Map<String, Table> tables, String tableName) {
        if (tableName == null || tableName.isEmpty()) return DBResponse.error("Table name is missing.");

        Table table = tables.get(tableName.toLowerCase());
        if (table == null) return DBResponse.error("Table '" + tableName + "' does not exist in the current database.");
        return null;
    }

    public static DBResponse validateColumnExists(Table table, String columnName) {
        if (columnName == null || columnName.isEmpty()) return DBResponse.error("Column name is missing.");

        if (!table.getColumns().contains(columnName)) return DBResponse.error("Column '" + columnName + "' does not exist in table '" + table.getName() + "'.");
        return null;
    }

    public static DBResponse validateNotIdColumn(String columnName) {
        if ("id".equalsIgnoreCase(columnName)) return DBResponse.error("Cannot modify the ID column.");
        return null;
    }

    public static DBResponse validateValuesNotEmpty(List<String> values) {
        if (values == null || values.isEmpty()) return DBResponse.error("No values provided for operation.");
        return null;
    }

    public static DBResponse validateTableNameProvided(List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) return DBResponse.error("No table name specified.");
        return null;
    }

    public static DBResponse validateDatabaseNameProvided(String dbName) {
        if (dbName == null || dbName.isEmpty()) return DBResponse.error("No database name specified.");
        return null;
    }
}
