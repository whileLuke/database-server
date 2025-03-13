package edu.uob;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandErrorChecker {
    private final String currentDB;
    private final Map<String, DBTable> tables;
    private final List<String> reserved_words = Arrays.asList(
            "USE", "CREATE", "DATABASE", "TABLE", "DROP",
            "ALTER", "INSERT", "INTO", "VALUES", "DELETE",
            "FROM", "UPDATE", "SET", "SELECT", "JOIN", "ON",
            "WHERE", "AND", "OR", "NULL", "TRUE", "FALSE"
    );

    public CommandErrorChecker(String currentDB, Map<String, DBTable> tables) {
        this.currentDB = currentDB;
        this.tables = tables;
    }

    public boolean isReservedWord(String word) { return reserved_words.contains(word.toUpperCase()); }

    public String CheckIfReservedWord(String word) {
        if (isReservedWord(word)) return "[ERROR] You cannot use reserved word '" + word + "' .";
        return null;
    }

    public String validateDatabaseSelected() {
        if (currentDB == null) return "[ERROR] No database selected. Type 'USE [DBName];' to select a database.";
        return null;
    }

    public String validateTableExists(String tableName) {
        if (tableName == null || tableName.isEmpty()) return "[ERROR] You have not specified a table name.";
        DBTable table = tables.get(tableName.toLowerCase());
        if (table == null) return "[ERROR] The table '" + tableName + "' does not exist in the current database.";
        return null;
    }

    public String validateColumnExists(DBTable table, String columnName) {
        if (columnName == null || columnName.isEmpty()) return "[ERROR] You have not specified a column name.";
        if (!table.getColumns().contains(columnName)) {
            return "[ERROR] Column '" + columnName + "' does not exist in table '" + table.getName() + "'.";
        }
        return null;
    }

    public String validateNotIDColumn(String columnName) {
        if (columnName.equalsIgnoreCase("id")) return "[ERROR] You cannot change the ID column.";
        return null;
    }

    public String validateValuesNotEmpty(List<String> values) {
        if (values == null || values.isEmpty()) return "[ERROR] You have not specified any values for the command.";
        return null;
    }

    public String validateTableNameProvided(List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) return "[ERROR] You have not specified a table name.";
        return null;
    }

    public String validateDatabaseNameProvided(String DBName) {
        if (DBName == null || DBName.isEmpty()) return "[ERROR] You have not specified a database name.";
        return null;
    }
}