package edu.uob;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandErrorChecker {
    private final String currentDB;
    private final Map<String, DBTable> tables;
    private final List<String> reservedWords = Arrays.asList(
            "USE", "CREATE", "DATABASE", "TABLE", "DROP",
            "ALTER", "INSERT", "INTO", "VALUES", "DELETE",
            "FROM", "UPDATE", "SET", "SELECT", "JOIN", "ON",
            "WHERE", "AND", "OR", "NULL", "TRUE", "FALSE"
    );

    public CommandErrorChecker(String currentDB, Map<String, DBTable> tables) {
        this.currentDB = currentDB;
        this.tables = tables;
    }

    public boolean isReservedWord(String word) { return reservedWords.contains(word.toUpperCase()); }

    public String checkIfReservedWord(String word) {
        if (isReservedWord(word)) return "[ERROR] You cannot use reserved word '" + word + "' .";
        return null;
    }

    public String checkIfDBSelected() {
        if (currentDB == null) return "[ERROR] No database selected. Type 'USE [DBName];' to select a database.";
        return null;
    }

    public String checkIfTableExists(String tableName) {
        if (tableName == null || tableName.isEmpty()) return "[ERROR] You have not specified a table name.";

        DBTable table = tables.get(tableName.toLowerCase());
        if (table == null) return "[ERROR] The table '" + tableName + "' does not exist.";
        return null;
    }

    public String checkIfColumnExists(DBTable table, String columnName) {
        if (columnName == null || columnName.isEmpty()) return "[ERROR] You have not specified a column name.";

        if (!table.getColumnsLowerCase().contains(columnName)) {
            return "[ERROR] Column '" + columnName + "' does not exist in table '" + table.getName() + "'.";
        }
        return null;
    }

    public String checkForDuplicateColumns(List<String> columnNames) {
        if (columnNames == null || columnNames.isEmpty()) return null;

        for (int i = 0; i < columnNames.size(); i++) {
            String currentColumn = columnNames.get(i).toLowerCase();
            for (int j = i + 1; j < columnNames.size(); j++) {
                if (currentColumn.equals(columnNames.get(j).toLowerCase())) {
                    return "[ERROR] Cannot create a table with duplicate column names: " + columnNames.get(i) + ".";
                }
            }
        }
        return null;
    }

    public String checkIfIDColumn(String columnName) {
        if (columnName.equalsIgnoreCase("id")) return "[ERROR] You cannot change the ID column.";
        return null;
    }

    public String checkIfValuesEmpty(List<String> values) {
        if (values == null || values.isEmpty()) return "[ERROR] You have not specified any values for the command.";
        return null;
    }

    public String checkIfTableNameProvided(List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) return "[ERROR] You have not specified a table name.";
        return null;
    }

    public String checkIfDBNameProvided(String DBName) {
        if (DBName == null || DBName.isEmpty()) return "[ERROR] You have not specified a database name.";
        return null;
    }

    public String checkTableFunctionality(List<String> tableNames) {
        String error = checkIfDBSelected();
        if (error != null) return error;

        error = checkIfTableNameProvided(tableNames);
        if (error != null) return error;

        String tableName = tableNames.get(0).toLowerCase();
        return checkIfTableExists(tableName);
    }

    public String checkUpdateCommand(List<String> tableNames, List<String> columnNames,
                                     List<String> values, List<String> conditions) {
        String error = checkTableFunctionality(tableNames);
        if (error != null) return error;

        if (columnNames.isEmpty() || values.isEmpty()) {
            return "[ERROR] UPDATE needs at least one column name and at least one value.";
        }

        if (columnNames.size() != values.size()) {
            return "[ERROR] You have input a different number of column names (" +
                    columnNames.size() + ") to values (" + values.size() +").";
        }

        if (conditions.isEmpty()) {
            return "[ERROR] UPDATE commands need a WHERE condition.";
        }
        return null;
    }

    public String checkDeleteCommand(List<String> tableNames, List<String> conditions) {
        String error = checkTableFunctionality(tableNames);
        if (error != null) return error;

        if (conditions.isEmpty()) return "[ERROR] DELETE commands require a WHERE condition.";
        return null;
    }

    public String checkAlterCommand(List<String> tableNames, List<String> columnNames, String commandType) {
        String error = checkIfDBSelected();
        if (error != null) return error;
        if (tableNames.isEmpty() || columnNames.isEmpty() || commandType == null) {
            return "[ERROR] Incorrect ALTER command - either no table name, column names or alteration type provided.";
        }

        String tableName = tableNames.get(0).toLowerCase();
        error = checkIfTableExists(tableName);
        if (error != null) return error;

        String columnName = columnNames.get(0);
        error = checkIfIDColumn(columnName);
        if (error != null) return error;

        if (isReservedWord(columnName)) return "[ERROR] '" + columnName + "' not allowed as a column name";
        if (!commandType.equalsIgnoreCase("ADD") && !commandType.equalsIgnoreCase("DROP")) {
            return "[ERROR] Your ALTER command must specify either 'ADD' or 'DROP'";
        }
        return null;
    }

    public String checkCreateTableCommand(List<String> tableNames, List<String> columnNames) {
        String error = checkIfDBSelected();
        if (error != null) return error;

        error = checkIfTableNameProvided(tableNames);
        if (error != null) return error;

        String tableName = tableNames.get(0).toLowerCase();
        error = checkIfReservedWord(tableName);
        if (error != null) return error;

        error = checkForDuplicateColumns(columnNames);
        if (error != null) return error;

        for (String columnName : columnNames) {
            error = checkIfReservedWord(columnName);
            if (error != null) return error;
        }
        return null;
    }

    public String checkJoinCommand(List<String> tableNames, List<String> columnNames) {
        String error = checkIfDBSelected();
        if (error != null) return error;

        if (tableNames.size() != 2) return "[ERROR] The JOIN command needs two table names.";

        String table1Name = tableNames.get(0).toLowerCase();
        String table2Name = tableNames.get(1).toLowerCase();

        error = checkIfTableExists(table1Name);
        if (error != null) return error;

        error = checkIfTableExists(table2Name);
        if (error != null) return error;

        if (columnNames.size() != 2) return "[ERROR] The JOIN command needs two column names";
        return null;
    }
}